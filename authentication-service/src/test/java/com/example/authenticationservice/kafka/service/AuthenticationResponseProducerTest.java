package com.example.authenticationservice.kafka.service;

import com.example.common.model.AuthenticationResponse;
import com.example.common.model.AuthenticationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

class AuthenticationResponseProducerTest {

    @Mock
    private KafkaTemplate<String, AuthenticationResponse> kafkaTemplate;

    @InjectMocks
    private AuthenticationResponseProducer authenticationResponseProducer;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSendAuthenticationResponseToKafka() {

        String requestId = "test-request-id";
        AuthenticationState authenticationState = new AuthenticationState();
        authenticationState.setAuthenticationResult(Boolean.TRUE);
        AuthenticationResponse response = AuthenticationResponse.builder()
                .requestId(requestId)
                .payload(authenticationState)
                .build();


        authenticationResponseProducer.sendAuthResponse(response);

        ArgumentCaptor<AuthenticationResponse> captor = ArgumentCaptor.forClass(AuthenticationResponse.class);
        verify(kafkaTemplate).send("auth-responses", requestId, response);
    }

    @Test
    void shouldNotSendNullAuthenticationResponse() {

        assertThrows(IllegalArgumentException.class, () -> authenticationResponseProducer.sendAuthResponse(null));
    }

    @Test
    void shouldSendCorrectRequestIdInKafkaMessage() {

        String requestId = "test-request-id-2";
        AuthenticationState authenticationState = new AuthenticationState();
        authenticationState.setAuthenticationResult(Boolean.TRUE);
        AuthenticationResponse response = AuthenticationResponse.builder()
                .requestId(requestId)
                .payload(authenticationState)
                .build();

        authenticationResponseProducer.sendAuthResponse(response);


        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("auth-responses"), stringCaptor.capture(), eq(response));
        assertEquals(requestId, stringCaptor.getValue());
    }
}