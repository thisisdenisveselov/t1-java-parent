package ru.t1.java.demo.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MetricsProducer {

    private final KafkaTemplate<String, String> template;

    public MetricsProducer(@Qualifier("metrics")KafkaTemplate<String, String> template) {
        this.template = template;
    }

    public void send(String value, String headerValue) {
        try {
            List<Header> headers = new ArrayList<>();
            headers.add(new RecordHeader("error-type", headerValue.getBytes(StandardCharsets.UTF_8)));

            ProducerRecord<String, String> record = new ProducerRecord<>(template.getDefaultTopic(), null, UUID.randomUUID().toString(), value, headers);
            template.send(record).get();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            template.flush();
        }
    }
}
