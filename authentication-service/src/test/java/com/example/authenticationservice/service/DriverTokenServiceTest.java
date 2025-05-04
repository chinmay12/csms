package com.example.authenticationservice.service;

import com.example.authenticationservice.repository.DriverTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DriverTokenServiceTest {

    @Mock
    private DriverTokenRepository driverTokenRepository;

    @InjectMocks
    private DriverTokenService driverTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        String driverId = "driver123";
        String token = "validToken";

        when(driverTokenRepository.existsByDriverIdAndToken(driverId, token)).thenReturn(true);

        boolean result = driverTokenService.validateDriverToken(driverId, token);

        assertTrue(result);
        verify(driverTokenRepository, times(1)).existsByDriverIdAndToken(driverId, token);
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {
        String driverId = "driver123";
        String token = "invalidToken";

        when(driverTokenRepository.existsByDriverIdAndToken(driverId, token)).thenReturn(false);

        boolean result = driverTokenService.validateDriverToken(driverId, token);

        assertFalse(result);
        verify(driverTokenRepository, times(1)).existsByDriverIdAndToken(driverId, token);
    }
}