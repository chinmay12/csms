package com.example.transactionservice.kafka.service;

import com.example.common.model.AuthenticationRequest;
import com.example.common.model.AuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AuthEventProducerTest {

    private KafkaTemplate<String, AuthenticationRequest> kafkaTemplate;
    private ObjectMapper objectMapper;
    private AuthEventProducer authEventProducer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = new ObjectMapper();
        authEventProducer = new AuthEventProducer(kafkaTemplate, objectMapper);
    }

    @Test
    void sendAuthEvent_shouldSendMessageToKafka() {
        AuthenticationRequest request = new AuthenticationRequest();
        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setDriverId("d134");
        authenticationToken.setToken("some-token");
        request.setRequestId("abc123");
        request.setType("AuthenticationRequest");
        request.setPayload(authenticationToken);
        authEventProducer.sendAuthEvent(request);
        verify(kafkaTemplate, times(1)).send("auth-requests", "abc123", request);
    }

    @Test
    void sendAuthEvent_shouldThrowRuntimeExceptionOnError() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setRequestId("abc123");
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString(), any());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authEventProducer.sendAuthEvent(request);
        });
        assertTrue(exception.getMessage().contains("Failed to send authentication request"));
    }
}