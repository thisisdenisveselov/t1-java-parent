package ru.t1.java.demo.config;

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
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.t1.java.demo.kafka.producer.ClientProducer;
import ru.t1.java.demo.kafka.producer.DataSourceErrorLogProducer;
import ru.t1.java.demo.kafka.producer.MetricsProducer;
import ru.t1.java.demo.kafka.producer.TransactionAcceptProducer;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.model.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.kafka.message.TransactionAcceptMessage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
public class KafkaProducerConfig<T> {

    @Value("${t1.kafka.bootstrap.server}")
    private String servers;
    @Value("${t1.kafka.topic.client_id_registered}")
    private String clientTopic;
    @Value("${t1.kafka.topic.metrics}")
    private String metricsTopic;
    @Value("${t1.kafka.topic.transaction_accept}")
    private String transactionAcceptTopic;

    // Client config
    @Bean("client")
    @Primary
    public KafkaTemplate<String, T> kafkaClientTemplate(@Qualifier("producerClientFactory") ProducerFactory<String, T> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public ClientProducer producerClient(@Qualifier("client") KafkaTemplate<String, ClientDto> template) {
        template.setDefaultTopic(clientTopic);
        return new ClientProducer(template);
    }

    @Bean("producerClientFactory")
    public ProducerFactory<String, T> producerClientFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    // DataSourceErrorLog config
    @Bean("dataSourceErrorLog")
    public KafkaTemplate<String, DataSourceErrorLogDto> kafkaDataSourceTemplate(@Qualifier("producerDataSourceFactory") ProducerFactory<String, DataSourceErrorLogDto> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public DataSourceErrorLogProducer producerDataSource(@Qualifier("dataSourceErrorLog") KafkaTemplate<String, DataSourceErrorLogDto> template) {
        template.setDefaultTopic(metricsTopic);
        return new DataSourceErrorLogProducer(template);
    }

    @Bean("producerDataSourceFactory")
    public ProducerFactory<String, T> producerDataSourceFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    // Metrics config
    @Bean("metrics")
    public KafkaTemplate<String, String> kafkaMetricsTemplate(@Qualifier("producerMetricsFactory") ProducerFactory<String, String> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public MetricsProducer producerMetrics(@Qualifier("metrics") KafkaTemplate<String, String> template) {
        template.setDefaultTopic(metricsTopic);
        return new MetricsProducer(template);
    }

    @Bean("producerMetricsFactory")
    public ProducerFactory<String, T> producerMetricsFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    // TransactionAccept config
    @Bean("transactionAccept")
    public KafkaTemplate<String, TransactionAcceptMessage> kafkaTransactionAcceptTemplate(@Qualifier("producerTransactionAcceptFactory") ProducerFactory<String, TransactionAcceptMessage> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public TransactionAcceptProducer producerTransactionAccept(@Qualifier("transactionAccept") KafkaTemplate<String, TransactionAcceptMessage> template) {
        template.setDefaultTopic(transactionAcceptTopic);
        return new TransactionAcceptProducer(template);
    }

    @Bean("producerTransactionAcceptFactory")
    public ProducerFactory<String, T> producerTransactionAcceptFactory() {
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