package com.example.transactionservice.kafka.service;

import com.example.common.model.AuthenticationResponse;
import com.example.common.model.AuthenticationState;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AuthResponseListenerTest {

    private AuthResponseHandler responseHandler;
    private AuthResponseListener responseListener;

    @BeforeEach
    void setUp() {
        responseHandler = mock(AuthResponseHandler.class);
        responseListener = new AuthResponseListener(responseHandler);
    }

    @Test
    void listen_shouldCallResponseHandlerSuccessfully() {
        String requestId = "req-123";
        AuthenticationState authenticationState = new AuthenticationState();
        authenticationState.setAuthenticationResult(Boolean.TRUE);
        authenticationState.setDriverId("driver-123");
        AuthenticationResponse response = new AuthenticationResponse();
        response.setRequestId(requestId);
        response.setType("AuthResponse");
        response.setPayload(authenticationState);

        ConsumerRecord<String, AuthenticationResponse> record = new ConsumerRecord<>(
                "auth-responses", 0, 0L, requestId, response
        );
        responseListener.listen(record);
        verify(responseHandler, times(1)).complete(requestId, response);
    }

    @Test
    void listen_shouldThrowRuntimeExceptionIfHandlerFails() {
        String requestId = "req-456";
        AuthenticationState authenticationState = new AuthenticationState();
        AuthenticationResponse response = new AuthenticationResponse();
        authenticationState.setAuthenticationResult(Boolean.TRUE);
        authenticationState.setDriverId("driver-123");
        response.setRequestId(requestId);
        response.setType("AuthResponse");
        response.setPayload(authenticationState);

        ConsumerRecord<String, AuthenticationResponse> record = new ConsumerRecord<>(
                "auth-responses", 0, 0L, requestId, response
        );
        doThrow(new RuntimeException("Handler failure")).when(responseHandler).complete(requestId, response);
        assertThrows(RuntimeException.class, () -> responseListener.listen(record));
        verify(responseHandler, times(1)).complete(requestId, response);
    }
}