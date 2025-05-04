package com.example.authenticationservice.kafka.service;

import com.example.authenticationservice.encryption.DecryptionService;
import com.example.authenticationservice.service.DriverTokenService;
import com.example.common.model.AuthenticationRequest;
import com.example.common.model.AuthenticationToken;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.common.model.AuthenticationResponse;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthEventsListenerTest {

    private DriverTokenService driverTokenService;
    private AuthenticationResponseProducer authenticationResponseProducer;
    private DecryptionService decryptionService;
    private AuthEventsListener authEventsListener;

    @BeforeEach
    void setUp() {
        driverTokenService = mock(DriverTokenService.class);
        authenticationResponseProducer = mock(AuthenticationResponseProducer.class);
        decryptionService = mock(DecryptionService.class);

        authEventsListener = new AuthEventsListener(driverTokenService, authenticationResponseProducer, decryptionService);
    }

    @Test
    void shouldProcessValidRequestSuccessfully() throws Exception {
        String driverId = "driver123";
        String requestId = "req-001";
        String encryptedToken = "encrypted";
        String decryptedToken = "token123";

        AuthenticationRequest request = AuthenticationRequest.builder()
                .requestId(requestId)
                .payload(AuthenticationToken.builder()
                        .driverId(driverId)
                        .token(encryptedToken)
                        .build())
                .build();

        ConsumerRecord<String, AuthenticationRequest> record = new ConsumerRecord<>("auth-requests", 0, 0, requestId, request);

        when(decryptionService.decrypt(encryptedToken)).thenReturn(decryptedToken);
        when(driverTokenService.validateDriverToken(driverId, decryptedToken)).thenReturn(true);

        authEventsListener.listen(record);

        ArgumentCaptor<AuthenticationResponse> responseCaptor = ArgumentCaptor.forClass(AuthenticationResponse.class);
        verify(authenticationResponseProducer).sendAuthResponse(responseCaptor.capture());

        AuthenticationResponse sentResponse = responseCaptor.getValue();
        assertEquals(requestId, sentResponse.getRequestId());
        assertTrue(sentResponse.getPayload().getAuthenticationResult());
        assertEquals(driverId, sentResponse.getPayload().getDriverId());
    }


    @Test
    void shouldSendFalseAuthenticationWhenTokenIsInvalid() throws Exception {
        String requestId = "test-request-id";
        String driverId = "driver-123";
        String encryptedToken = "encrypted-token";
        String decryptedToken = "decrypted-token";

        AuthenticationRequest request = AuthenticationRequest.builder()
                .requestId(requestId)
                .payload(AuthenticationToken.builder()
                        .driverId(driverId)
                        .token(encryptedToken)
                        .build())
                .build();

        ConsumerRecord<String, AuthenticationRequest> record = new ConsumerRecord<>(
                "auth-requests", 0, 0, requestId, request
        );

        when(decryptionService.decrypt(encryptedToken)).thenReturn(decryptedToken);
        when(driverTokenService.validateDriverToken(driverId, decryptedToken)).thenReturn(false);
        authEventsListener.listen(record);
        ArgumentCaptor<AuthenticationResponse> captor = ArgumentCaptor.forClass(AuthenticationResponse.class);
        verify(authenticationResponseProducer).sendAuthResponse(captor.capture());

        AuthenticationResponse response = captor.getValue();
        assertEquals(requestId, response.getRequestId());
        assertFalse(response.getPayload().getAuthenticationResult());
        assertEquals(driverId, response.getPayload().getDriverId());
    }


    @Test
    void shouldHandleExceptionAndSendFalseAuthentication() throws Exception {
        String driverId = "driver123";
        String requestId = "req-002";
        String encryptedToken = "invalid-encrypted";

        AuthenticationRequest request = AuthenticationRequest.builder()
                .requestId(requestId)
                .payload(AuthenticationToken.builder()
                        .driverId(driverId)
                        .token(encryptedToken)
                        .build())
                .build();

        ConsumerRecord<String, AuthenticationRequest> record = new ConsumerRecord<>("auth-requests", 0, 0, requestId, request);

        when(decryptionService.decrypt(encryptedToken)).thenThrow(new RuntimeException("Decryption failed"));

        authEventsListener.listen(record);

        ArgumentCaptor<AuthenticationResponse> responseCaptor = ArgumentCaptor.forClass(AuthenticationResponse.class);
        verify(authenticationResponseProducer).sendAuthResponse(responseCaptor.capture());

        AuthenticationResponse sentResponse = responseCaptor.getValue();
        assertEquals(requestId, sentResponse.getRequestId());
        assertFalse(sentResponse.getPayload().getAuthenticationResult());
        assertEquals(driverId, sentResponse.getPayload().getDriverId());
    }
}