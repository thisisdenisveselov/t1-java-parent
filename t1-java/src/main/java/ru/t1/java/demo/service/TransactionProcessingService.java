package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import ru.t1.java.demo.util.TransactionMapper;
import ru.t1.java.demo.web.CheckBlacklistWebClient;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionProcessingService {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final TransactionAcceptProducer transactionAcceptProducer;
    private final ClientService clientService;
    private final CheckBlacklistWebClient checkBlacklistWebClient;
    private final Map<UUID, List<Transaction>> transactionCache = new HashMap<>();

    @Value("${t1.kafka.transactions.rejected-limit}")
    private int rejectedTransactionsLimit;

    public void processTransaction(TransactionDto transactionDto) {
        Transaction transaction = TransactionMapper.toEntity(transactionDto);

        try {
            Account account = accountService.getAccount(transaction.getAccount().getAccountId());
            Client client = clientService.getClient(account.getOwner().getClientId());

            transaction = transactionService.createTransaction(transaction);

            if (client.getBlocked() == null) { //if the client's status is unknown
                handleUnknownClientStatus(client, account, transaction);
            } else if (transactionService.checkRejected(account.getAccountId())) { //if we know client status and account has transactions in REJECTED status
                handleRejectedTransactions(client, account, transaction);
            } else {
                sendTransactionAcceptMessage(client, account, transaction);
            }
        } catch (AccountNotOpenException e) {
            log.warn("Transaction processing: account not in OPEN status. Transaction: {}", transaction, e);
        }
    }

    private void handleUnknownClientStatus(Client client, Account account, Transaction transaction) {
        Boolean clientIsBlocked = checkBlacklistWebClient.check(client.getClientId(), account.getAccountId()).getBlocked();

        Client updatedClient = Client.builder().blocked(clientIsBlocked).build();
        clientService.updateClient(client.getClientId(), updatedClient);

        if (clientIsBlocked) {
            accountService.changeAccountStatus(account.getAccountId(), AccountStatus.BLOCKED);
            accountService.alterAccountBalance(transaction, account);
            transactionService.changeTransactionStatus(transaction.getTransactionId(), TransactionStatus.REJECTED);
        } else {
            sendTransactionAcceptMessage(client, account, transaction);
        }
    }

    private void handleRejectedTransactions(Client client, Account account, Transaction transaction) {
        transactionCache.putIfAbsent(account.getAccountId(), new ArrayList<>());
        List<Transaction> transactions = transactionCache.get(account.getAccountId());
        transactions.add(transaction);

        if (transactions.size() > rejectedTransactionsLimit) {
            accountService.changeAccountStatus(account.getAccountId(), AccountStatus.ARRESTED);
            for (Transaction t : transactions) {
                log.info("Transaction processing: rejecting transaction: {}", t.getId());
                accountService.alterAccountBalance(t, account);
                transactionService.changeTransactionStatus(t.getTransactionId(), TransactionStatus.REJECTED);
            }
            transactionCache.remove(account.getAccountId());
        } else {
            sendTransactionAcceptMessage(client, account, transaction);
        }
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
