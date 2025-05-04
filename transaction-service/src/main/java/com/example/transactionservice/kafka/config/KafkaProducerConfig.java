package com.example.transactionservice.kafka.config;


import com.example.common.model.AuthenticationRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public KafkaTemplate<String, AuthenticationRequest> kafkaTemplate(ProducerFactory<String, AuthenticationRequest> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
