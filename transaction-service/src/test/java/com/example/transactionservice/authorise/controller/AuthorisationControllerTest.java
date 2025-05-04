package com.example.transactionservice.authorise.controller;

import com.example.common.model.AuthenticationResponse;
import com.example.common.model.AuthenticationState;
import com.example.transactionservice.authorise.model.dto.AuthorizationRequest;
import com.example.transactionservice.authorise.model.dto.DriverIdentifier;
import com.example.transactionservice.kafka.service.AuthEventProducer;
import com.example.transactionservice.kafka.service.AuthResponseHandler;
import com.example.transactionservice.service.DriverFinderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthorisationControllerTest {

    @Mock
    private AuthEventProducer authEventProducer;

    @Mock
    private DriverFinderService driverFinderService;

    @Mock
    private AuthResponseHandler authResponseHandler;

    @InjectMocks
    private AuthorisationController authorisationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authorisationController).build();
    }

    @Test
    void shouldReturnAcceptedWhenAuthenticationIsTrue() throws Exception {
        DriverIdentifier driverIdentifier = new DriverIdentifier();
        driverIdentifier.setId("driver123456789012345");
        driverIdentifier.setToken("token1234");
        AuthorizationRequest request = AuthorizationRequest.builder()
                .stationUUID("station123")
                .driverIdentifier(driverIdentifier)
                .build();

        AuthenticationState state = AuthenticationState.builder()
                .driverId("driver123456789012345")
                .authenticationResult(true)
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .requestId("test-request-id")
                .payload(state)
                .build();

        when(driverFinderService.findDriver(any())).thenReturn(true);
        when(authResponseHandler.createFuture(any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorisationStatus", is("Accepted")));
    }

    @Test
    void shouldReturnRejectedWhenAuthenticationIsFalse() throws Exception {
        DriverIdentifier driverIdentifier = new DriverIdentifier();
        driverIdentifier.setId("driver123456789012345");
        driverIdentifier.setToken("token1234");
        AuthorizationRequest request = AuthorizationRequest.builder()
                .stationUUID("station123")
                .driverIdentifier(driverIdentifier)
                .build();

        AuthenticationState state = AuthenticationState.builder()
                .driverId("driver123456789012345")
                .authenticationResult(false)
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .requestId("test-request-id")
                .payload(state)
                .build();

        when(driverFinderService.findDriver(any())).thenReturn(true);
        when(authResponseHandler.createFuture(any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.authorisationStatus", is("Rejected")));
    }

    @Test
    void shouldReturnInvalidWhenDriverIdentifierIsInvalid() throws Exception {
        DriverIdentifier driverIdentifier = new DriverIdentifier();
        driverIdentifier.setId("short");
        driverIdentifier.setToken("token1234");
        AuthorizationRequest request = AuthorizationRequest.builder()
                .stationUUID("station123")
                .driverIdentifier(driverIdentifier)
                .build();

        mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.authorisationStatus", is("Invalid")));
    }

    @Test
    void shouldReturnUnknownWhenDriverNotFound() throws Exception {
        DriverIdentifier driverIdentifier = new DriverIdentifier();
        driverIdentifier.setId("driver12345678901234554");
        driverIdentifier.setToken("token1234");
        AuthorizationRequest request = AuthorizationRequest.builder()
                .stationUUID("station123")
                .driverIdentifier(driverIdentifier)
                .build();

        when(driverFinderService.findDriver(any())).thenReturn(false);

        mockMvc.perform(post("/transaction/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token123")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.authorisationStatus", is("Unknown")));
    }
}