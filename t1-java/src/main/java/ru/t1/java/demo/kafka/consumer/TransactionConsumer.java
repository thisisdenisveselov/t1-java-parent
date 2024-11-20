package ru.t1.java.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.exception.AccountNotOpenException;
import ru.t1.java.demo.kafka.message.TransactionAcceptMessage;
import ru.t1.java.demo.kafka.producer.TransactionAcceptProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionConsumer {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final TransactionAcceptProducer transactionAcceptProducer;
    private final ClientService clientService;

    @KafkaListener(groupId = "${t1.kafka.consumer.transaction-id}",
            topics = "${t1.kafka.topic.transactions}",
            containerFactory = "transactionKafkaListenerContainerFactory")
    public void listener(@Payload TransactionDto transactionDto, Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Transaction consumer: new message from {} topic", topic);
        Transaction transaction = TransactionMapper.toEntity(transactionDto);

        try {
            Account account = accountService.getAccount(transaction.getAccount().getAccountId());

            transaction = transactionService.createTransaction(transaction);

            Client client = clientService.getClient(account.getOwner().getClientId());

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
        } catch (AccountNotOpenException e) {
            log.warn("Transaction consumer: an account is not in OPEN status. Transaction: {}", transaction.toString(), e);
        } catch (Throwable throwable) {
            log.error("Transaction consumer: Error while saving new transaction: {}", transaction.toString(), throwable);
        } finally {
            ack.acknowledge();
        }

        log.info("Transaction consumer: message processed");
    }
}
