package ru.t1.java.demo.t1_java_transaction_processor.config;

import ru.t1.java.demo.t1_java_transaction_processor.kafka.message.TransactionResultMessage;
import ru.t1.java.demo.t1_java_transaction_processor.kafka.producer.TransactionResultProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
public class KafkaProducerConfig<T> {

    @Value("${t1-transaction-processor.kafka.bootstrap.server}")
    private String servers;
    @Value("${t1-transaction-processor.kafka.topic.transaction_result}")
    private String transactionResultTopic;

    @Bean("transactionResult")
    @Primary
    public KafkaTemplate<String, T> kafkaTransactionResultTemplate(@Qualifier("producerTransactionResultFactory") ProducerFactory<String, T> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1-transaction-processor.kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public TransactionResultProducer producerTransactionResult(@Qualifier("transactionResult") KafkaTemplate<String, TransactionResultMessage> template) {
        template.setDefaultTopic(transactionResultTopic);
        return new TransactionResultProducer(template);
    }

    @Bean("producerTransactionResultFactory")
    public ProducerFactory<String, T> producerTransactionResultFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

}