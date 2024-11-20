package ru.t1.java.demo.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.message.TransactionAcceptMessage;

@Slf4j
@Component
public class TransactionAcceptProducer {

    private final KafkaTemplate<String, TransactionAcceptMessage> template;

    public TransactionAcceptProducer(@Qualifier("transactionAccept")KafkaTemplate<String, TransactionAcceptMessage> template) {
        this.template = template;
    }

    public void send(TransactionAcceptMessage value) {
        try {
            //List<Header> headers = new ArrayList<>();
            //headers.add(new RecordHeader("error-type", headerValue.getBytes(StandardCharsets.UTF_8)));

          //  ProducerRecord<String, DataSourceErrorLogDto> record = new ProducerRecord<>(template.getDefaultTopic(), null, UUID.randomUUID().toString(), value, headers);
            //template.send(record).get();
            template.sendDefault(value).get();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            template.flush();
        }
    }
}
