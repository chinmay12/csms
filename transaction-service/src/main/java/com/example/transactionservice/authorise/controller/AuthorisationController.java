package com.example.transactionservice.authorise.controller;

import com.example.common.model.AuthenticationRequest;
import com.example.common.model.AuthenticationResponse;
import com.example.common.model.AuthenticationToken;
import com.example.transactionservice.authorise.model.dto.AuthorizationResponse;
import com.example.transactionservice.authorise.model.dto.AuthorizationRequest;
import com.example.transactionservice.authorise.model.dto.DriverIdentifier;
import com.example.transactionservice.kafka.service.AuthEventProducer;
import com.example.transactionservice.kafka.service.AuthResponseHandler;
import com.example.transactionservice.service.DriverFinderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/transaction")
public class AuthorisationController {

    private final AuthEventProducer producer;
    private final DriverFinderService driverFinderService;
    private final AuthResponseHandler responseHandler;

    private final String ACCEPTED = "Accepted";
    private final String REJECTED = "Rejected";

    private static final Logger logger = LoggerFactory.getLogger(AuthorisationController.class);

    public AuthorisationController(AuthEventProducer producer,
                                   DriverFinderService driverFinderService,
                                   AuthResponseHandler responseHandler) {
        this.producer = producer;
        this.driverFinderService = driverFinderService;
        this.responseHandler = responseHandler;
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorize(@RequestBody AuthorizationRequest request) {
        String stationUUID = request.getStationUUID();
        DriverIdentifier driverDetails = request.getDriverIdentifier();
        String driverIdentifier = driverDetails.getId();

        if (stationUUID == null || driverIdentifier == null ||driverIdentifier.length() < 20 || driverIdentifier.length() > 80) {
            return ResponseEntity.status(400).body(AuthorizationResponse.builder().authorisationStatus("Invalid").build());
        }

        if (!driverFinderService.findDriver(driverIdentifier)) {
            return ResponseEntity.status(404).body(AuthorizationResponse.builder().authorisationStatus("Unknown").build());
        }
        try {
            String requestId = MDC.get("requestId");
            logger.info("Processing authorization request with Request ID: {}", requestId);
            String token = driverDetails.getToken();
            AuthenticationToken authenticationToken = AuthenticationToken.builder().driverId(driverIdentifier).token(token).build();
            AuthenticationRequest authenticationRequest = AuthenticationRequest.builder().requestId(requestId).payload(authenticationToken).build();
            producer.sendAuthEvent(authenticationRequest);
            logger.info("Added authentication request for request with Request ID : {} to kafka", requestId);
            CompletableFuture<AuthenticationResponse> future = responseHandler.createFuture(requestId);
            AuthenticationResponse response = future.get(20, TimeUnit.SECONDS);
            logger.info("Received authentication response from authentication service for request with Request ID : {}", requestId);
            String authStatus = response.getPayload().getAuthenticationResult().equals(Boolean.TRUE) ? ACCEPTED : REJECTED;
            return ResponseEntity.ok().body(AuthorizationResponse.builder().authorisationStatus(authStatus).build());
        } catch (TimeoutException e) {
            return ResponseEntity.status(504).body(AuthorizationResponse.builder().authorisationStatus(REJECTED).build());
        } catch (Exception e) {
            logger.error("Error occurred while processing authorization request", e);
            return ResponseEntity.status(500).body(AuthorizationResponse.builder().authorisationStatus(REJECTED).build());
        }
    }

}
