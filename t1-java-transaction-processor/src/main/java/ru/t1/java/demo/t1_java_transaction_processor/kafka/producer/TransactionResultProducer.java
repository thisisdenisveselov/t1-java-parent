package ru.t1.java.demo.t1_java_transaction_processor.kafka.producer;

import ru.t1.java.demo.t1_java_transaction_processor.kafka.message.TransactionResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionResultProducer {

    private final KafkaTemplate<String, TransactionResultMessage> template;

    public TransactionResultProducer(@Qualifier("transactionResult")KafkaTemplate<String, TransactionResultMessage> template) {
        this.template = template;
    }

    public void send(TransactionResultMessage value) {
        try {
            template.sendDefault(value).get();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            template.flush();
        }
    }
}
