package com.example.authenticationservice.kafka.service;

import com.example.authenticationservice.encryption.DecryptionService;
import com.example.authenticationservice.service.DriverTokenService;
import com.example.common.model.AuthenticationRequest;
import com.example.common.model.AuthenticationResponse;
import com.example.common.model.AuthenticationState;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class AuthEventsListener {

    private DriverTokenService driverTokenService;

    private AuthenticationResponseProducer authenticationResponseProducer;

    private DecryptionService tokenDecryptionService;

    private static final Logger logger = LoggerFactory.getLogger(AuthEventsListener.class);

    public AuthEventsListener(DriverTokenService driverTokenService, AuthenticationResponseProducer authenticationResponseProducer, DecryptionService tokenDecryptionService) {
        this.driverTokenService = driverTokenService;
        this.authenticationResponseProducer = authenticationResponseProducer;
        this.tokenDecryptionService = tokenDecryptionService;
    }

    @KafkaListener(topics = "auth-requests", groupId = "authentication-service")
    public void listen(ConsumerRecord<String, AuthenticationRequest> record) {
        String key = record.key();
        AuthenticationRequest request = record.value();
        MDC.put("requestId", key);
        String requestId = MDC.get("requestId");
        String driverId = request.getPayload().getDriverId();
        try {
            MDC.put("requestId", key);
            logger.info("Received authentication request with Request ID: {}", requestId);
            String token = tokenDecryptionService.decrypt(request.getPayload().getToken());
            boolean authenticationStatus = driverTokenService.validateDriverToken(driverId, token);
            AuthenticationState authenticationState = AuthenticationState.builder().driverId(driverId).authenticationResult(authenticationStatus).build();
            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().requestId(key).payload(authenticationState).build();
            authenticationResponseProducer.sendAuthResponse(authenticationResponse);
            logger.info("Authentication status for  request with Request ID: {} sent to kafka", requestId);
            MDC.remove("requestId");
        } catch (Exception e) {
            logger.error("Error occurred while validating the token for Request ID: {}", record.key(), e);
            AuthenticationState authenticationState = AuthenticationState.builder().driverId(driverId).authenticationResult(Boolean.FALSE).build();
            AuthenticationResponse authenticationResponse = AuthenticationResponse.builder().requestId(key).payload(authenticationState).build();
            authenticationResponseProducer.sendAuthResponse(authenticationResponse);
        }
    }
}
