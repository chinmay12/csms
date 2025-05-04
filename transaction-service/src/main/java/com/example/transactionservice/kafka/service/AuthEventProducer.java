package com.example.transactionservice.kafka.service;


import com.example.common.model.AuthenticationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthEventProducer {
    private final KafkaTemplate<String, AuthenticationRequest> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String TOPIC = "auth-requests";

    public AuthEventProducer(KafkaTemplate<String, AuthenticationRequest> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendAuthEvent(AuthenticationRequest authRequest) {
        try {
            //String json = objectMapper.writeValueAsString(authRequest);
            kafkaTemplate.send(TOPIC, authRequest.getRequestId(), authRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send authentication request to Kafka", e);
        }
    }
}
