package com.example.transactionservice.kafka.service;

import com.example.common.model.AuthenticationResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthResponseHandler {

    private final ConcurrentMap<String, CompletableFuture<AuthenticationResponse>> pendingResponses = new ConcurrentHashMap<>();

    public CompletableFuture<AuthenticationResponse> createFuture(String requestId) {
        CompletableFuture<AuthenticationResponse> future = new CompletableFuture<>();
        pendingResponses.put(requestId, future);
        return future;
    }

    public void complete(String requestId, AuthenticationResponse response) {
        CompletableFuture<AuthenticationResponse> future = pendingResponses.remove(requestId);
        if (future != null) {
            future.complete(response);
        }
    }
}

