package com.example.authenticationservice.kafka.service;


import com.example.common.model.AuthenticationResponse;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationResponseProducer {

    private static final String TOPIC = "auth-responses";

    private final KafkaTemplate<String, AuthenticationResponse> kafkaTemplate;

    public AuthenticationResponseProducer(KafkaTemplate<String, AuthenticationResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAuthResponse(AuthenticationResponse response) {
        if(response == null)
            throw new IllegalArgumentException("Response cannot be null");
        kafkaTemplate.send(TOPIC, response.getRequestId(), response);
    }
}
