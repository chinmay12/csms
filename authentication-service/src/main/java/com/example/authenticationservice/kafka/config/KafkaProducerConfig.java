package com.example.authenticationservice.kafka.config;

import com.example.common.model.AuthenticationResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, AuthenticationResponse> kafkaTemplate(ProducerFactory<String, AuthenticationResponse> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
