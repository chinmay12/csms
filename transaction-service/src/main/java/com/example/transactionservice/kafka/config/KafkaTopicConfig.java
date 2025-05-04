package com.example.transactionservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic authRequestsTopic() {
        return new NewTopic("auth-requests", 10, (short) 1);
    }

    @Bean
    public NewTopic authResponsesTopic() {
        return new NewTopic("auth-responses", 10, (short) 1);
    }
}