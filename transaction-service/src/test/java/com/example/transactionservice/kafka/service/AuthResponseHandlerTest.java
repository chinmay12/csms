package com.example.transactionservice.kafka.service;

import com.example.common.model.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthResponseHandlerTest {

    private AuthResponseHandler authResponseHandler;

    @BeforeEach
    void setUp() {
        authResponseHandler = new AuthResponseHandler();
    }

    @Test
    void createFuture_shouldReturnCompletableFuture() {
        String requestId = "req-1";

        CompletableFuture<AuthenticationResponse> future = authResponseHandler.createFuture(requestId);

        assertNotNull(future);
        assertFalse(future.isDone());
    }

    @Test
    void complete_shouldCompleteFutureIfPresent() throws Exception {
        String requestId = "req-2";
        AuthenticationResponse response = new AuthenticationResponse();
        response.setRequestId(requestId);

        CompletableFuture<AuthenticationResponse> future = authResponseHandler.createFuture(requestId);
        authResponseHandler.complete(requestId, response);

        AuthenticationResponse result = future.get(1, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(requestId, result.getRequestId());
    }

    @Test
    void complete_shouldDoNothingIfNoFuturePresent() {
        String unknownRequestId = "unknown";
        AuthenticationResponse response = new AuthenticationResponse();
        authResponseHandler.complete(unknownRequestId, response);
    }
}