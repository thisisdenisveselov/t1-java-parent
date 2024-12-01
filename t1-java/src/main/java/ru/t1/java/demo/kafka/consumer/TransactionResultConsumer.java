package ru.t1.java.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.exception.IllegalOperationTypeException;
import ru.t1.java.demo.kafka.message.TransactionResultMessage;
import ru.t1.java.demo.service.TransactionResultService;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionResultConsumer {

    private final TransactionResultService transactionResultService;

    @KafkaListener(groupId = "${t1.kafka.consumer.transaction-result-id}",
            topics = "${t1.kafka.topic.transaction_result}",
            containerFactory = "transactionResultKafkaListenerContainerFactory")
    public void listener(@Payload TransactionResultMessage transactionResultMessage, Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("TransactionResult consumer: new message from {} topic", topic);

        try {
            transactionResultService.processTransactionResult(transactionResultMessage);
        } catch (IllegalOperationTypeException illegalOperationTypeException) {
            log.error("TransactionResult consumer: unknown operation type: {}", transactionResultMessage.getOperationType(), illegalOperationTypeException);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("TransactionResult consumer: unknown translation status: {}", transactionResultMessage.getStatus(), illegalArgumentException);
        } catch (Throwable throwable) {
            log.error("TransactionResult consumer: Error while processing new message: {}", transactionResultMessage.toString(), throwable);
        } finally {
            ack.acknowledge();
        }

        log.info("TransactionResult consumer: message processed");
    }
}
