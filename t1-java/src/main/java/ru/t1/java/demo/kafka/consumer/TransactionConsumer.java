package ru.t1.java.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.exception.AccountNotOpenException;
import ru.t1.java.demo.exception.IllegalOperationTypeException;
import ru.t1.java.demo.kafka.message.TransactionAcceptMessage;
import ru.t1.java.demo.kafka.producer.TransactionAcceptProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;
import ru.t1.java.demo.web.CheckBlacklistWebClient;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionConsumer {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final TransactionAcceptProducer transactionAcceptProducer;
    private final ClientService clientService;
    private final CheckBlacklistWebClient checkBlacklistWebClient;
    private final Map<UUID, List<Transaction>> transactionCache = new HashMap<>();

    @Value("${t1.kafka.transactions.rejected-limit}")
    private int rejectedTransactionsLimit;

    @KafkaListener(groupId = "${t1.kafka.consumer.transaction-id}",
            topics = "${t1.kafka.topic.transactions}",
            containerFactory = "transactionKafkaListenerContainerFactory")
    public void listener(@Payload TransactionDto transactionDto, Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Transaction consumer: new message from {} topic", topic);
        Transaction transaction = TransactionMapper.toEntity(transactionDto);

        try {
            Account account = accountService.getAccount(transaction.getAccount().getAccountId());
            Client client = clientService.getClient(account.getOwner().getClientId());

            transaction = transactionService.createTransaction(transaction);

            if (client.getBlocked() == null) { //if the client's status is unknown
                //getting client status
                Boolean clientIsBlocked = checkBlacklistWebClient.check(client.getClientId(), account.getAccountId()).getBlocked();

                //updating client status
                Client updatedClient = Client.builder().blocked(clientIsBlocked).build();
                clientService.updateClient(client.getClientId(), updatedClient);

                if (clientIsBlocked) {
                    changeAccountStatus(account.getAccountId(), AccountStatus.BLOCKED);
                    alterAccountBalance(transaction, account);
                    changeTransactionStatus(transaction.getTransactionId(), TransactionStatus.REJECTED);
                    return;
                }

            } else if(transactionService.checkRejected(account.getAccountId())) { //if we know client status and account has transactions in REJECTED status

                transactionCache.putIfAbsent(account.getAccountId(), new ArrayList<>());
                List<Transaction> transactions = transactionCache.get(account.getAccountId());
                transactions.add(transaction);

                if (transactions.size() > rejectedTransactionsLimit) {
                    changeAccountStatus(account.getAccountId(), AccountStatus.ARRESTED);
                    for (Transaction t : transactions) {
                        log.info("Transaction consumer: rejecting transaction: {}", t.getId());
                        alterAccountBalance(t, account);
                        changeTransactionStatus(t.getTransactionId(), TransactionStatus.REJECTED);
                    }
                    transactionCache.remove(account.getAccountId());
                    return;
                }
            }

            //sending message to topic t1_demo_transaction_accept
            sendTransactionAcceptMessage(client, account, transaction);
        } catch (AccountNotOpenException e) {
            log.warn("Transaction consumer: an account is not in OPEN status. Transaction: {}", transaction.toString(), e);
        } catch (Throwable throwable) {
            log.error("Transaction consumer: Error while saving new transaction: {}", transaction.toString(), throwable);
        } finally {
            ack.acknowledge();
        }

        log.info("Transaction consumer: message processed");
    }

    private void changeTransactionStatus(UUID transactionId, TransactionStatus newStatus) {
        Transaction updatedTransaction = Transaction.builder().status(newStatus).build();
        transactionService.updateTransaction(transactionId, updatedTransaction);
    }

    private void changeAccountStatus(UUID accountId, AccountStatus newStatus) {
        Account updatedAccount = Account.builder().status(newStatus).build();
        accountService.updateAccount(accountId, updatedAccount);
    }

    private void alterAccountBalance(Transaction transaction, Account account) {
        if (transaction.getOperationType() == OperationType.INCOMING)
            accountService.decreaseBalance(account.getAccountId(), transaction.getAmount());
        else if (transaction.getOperationType() == OperationType.OUTGOING)
            accountService.increaseBalance(account.getAccountId(), transaction.getAmount());
        else
            throw new IllegalOperationTypeException("Unknown operation type: " + transaction.getOperationType());
    }

    private void sendTransactionAcceptMessage(Client client, Account account, Transaction transaction) {
        TransactionAcceptMessage transactionAcceptMessage = TransactionAcceptMessage.builder()
                .clientId(client.getClientId())
                .accountId(account.getAccountId())
                .transactionId(transaction.getTransactionId())
                .timestamp(transaction.getTimestamp())
                .transactionAmount(transaction.getAmount())
                .operationType(transaction.getOperationType())
                .accountBalance(account.getBalance())
                .build();

        transactionAcceptProducer.send(transactionAcceptMessage);
    }
}
