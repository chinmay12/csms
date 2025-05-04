package com.example.transactionservice.kafka.service;

import com.example.common.model.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseListener {

    private final AuthResponseHandler responseHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(AuthResponseListener.class);

    public AuthResponseListener(AuthResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @KafkaListener(topics = "auth-responses", groupId = "transaction-service")
    public void listen(ConsumerRecord<String, AuthenticationResponse> record) {
        String requestId = record.key();
        AuthenticationResponse authenticationResponse = record.value();
        try {
            responseHandler.complete(requestId, authenticationResponse);
        } catch (Exception e) {
            logger.error("Failed to parse AuthenticationResponse for requestId: {} with error {}", requestId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
