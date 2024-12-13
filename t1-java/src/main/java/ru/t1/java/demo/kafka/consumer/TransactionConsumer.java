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
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionProcessingService;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionConsumer {
    private final TransactionProcessingService transactionProcessingService;

    @KafkaListener(groupId = "${t1.kafka.consumer.transaction-id}",
            topics = "${t1.kafka.topic.transactions}",
            containerFactory = "transactionKafkaListenerContainerFactory")
    public void listener(@Payload TransactionDto transactionDto, Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Transaction consumer: new message from {} topic", topic);

        try {
            transactionProcessingService.processTransaction(transactionDto);
        } catch (AccountNotOpenException e) {
            log.warn("Transaction consumer: an account is not in OPEN status. Transaction: {}", transactionDto.toString(), e);
        } catch (Throwable throwable) {
            log.error("Transaction consumer: Error while saving new transaction: {}", transactionDto.toString(), throwable);
        } finally {
            ack.acknowledge();
        }

        log.info("Transaction consumer: message processed");
    }
}
