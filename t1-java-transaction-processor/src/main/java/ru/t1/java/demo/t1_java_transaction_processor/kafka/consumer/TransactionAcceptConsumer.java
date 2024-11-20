package ru.t1.java.demo.t1_java_transaction_processor.kafka.consumer;

import ru.t1.java.demo.t1_java_transaction_processor.kafka.message.TransactionAcceptMessage;
import ru.t1.java.demo.t1_java_transaction_processor.kafka.message.TransactionResultMessage;
import ru.t1.java.demo.t1_java_transaction_processor.kafka.producer.TransactionResultProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.t1.java.demo.t1_java_transaction_processor.model.enums.OperationType;
import ru.t1.java.demo.t1_java_transaction_processor.model.enums.TransactionStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionAcceptConsumer {

    private final TransactionResultProducer transactionResultProducer;

    @Value("${t1-transaction-processor.kafka.transactions.max-count}")
    private int maxCount;

    @Value("${t1-transaction-processor.kafka.transactions.time-window}")
    private int timeWindow;

    private final Map<String, List<TransactionAcceptMessage>> transactionCache = new HashMap<>();

    @KafkaListener(groupId = "${t1-transaction-processor.kafka.consumer.group-id}",
            topics = "${t1-transaction-processor.kafka.topic.transaction_accept}",
            containerFactory = "transactionAcceptKafkaListenerContainerFactory")
    public void listener(@Payload TransactionAcceptMessage transaction, Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("TransactionAccept consumer: new message from {} topic", topic);

        String key = transaction.getClientId() + "_" + transaction.getAccountId();
        Instant currentTransactionTimestamp = transaction.getTimestamp();

        transactionCache.putIfAbsent(key, new ArrayList<>());
        List<TransactionAcceptMessage> transactions = transactionCache.get(key);

        //Remove if transaction is out of the window(expired)
        transactions.removeIf(t -> t.getTimestamp().isBefore(currentTransactionTimestamp.minusMillis(timeWindow)));

        transactions.add(transaction);
        try {
            if (transactions.size() > maxCount) {
                for (TransactionAcceptMessage t : transactions) {
                    TransactionResultMessage transactionResultMessage = TransactionResultMessage.builder()
                            .status(TransactionStatus.BLOCKED)
                            .accountId(t.getAccountId())
                            .transactionId(t.getTransactionId())
                            .operationType(t.getOperationType())
                            .transactionAmount(t.getTransactionAmount())
                            .build();

                    transactionResultProducer.send(transactionResultMessage);
                }
                transactions.clear();
            } else {
                TransactionStatus status;
                if (transaction.getOperationType() == OperationType.OUTGOING
                        && transaction.getAccountBalance().subtract(transaction.getTransactionAmount()).compareTo(BigDecimal.ZERO) < 0)
                    status = TransactionStatus.REJECTED;
                else
                    status = TransactionStatus.ACCEPTED;

                TransactionResultMessage transactionResultMessage = TransactionResultMessage.builder()
                        .status(status)
                        .accountId(transaction.getAccountId())
                        .transactionId(transaction.getTransactionId())
                        .operationType(transaction.getOperationType())
                        .transactionAmount(transaction.getTransactionAmount())
                        .build();

                transactionResultProducer.send(transactionResultMessage);
            }
        } catch (Throwable throwable) {
            log.error("TransactionAccept consumer: Error while processing new message: {}", transaction, throwable);
        } finally {
            ack.acknowledge();
        }

        log.info("TransactionAccept consumer: message processed");
    }
}
