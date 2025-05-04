package com.example.transactionservice.service;

import com.example.transactionservice.entities.Drivers;
import com.example.transactionservice.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DriverFinderServiceTest {

    private DriverRepository driverRepository;
    private DriverFinderService driverFinderService;

    @BeforeEach
    void setUp() {
        driverRepository = mock(DriverRepository.class);
        driverFinderService = new DriverFinderService(driverRepository);
    }

    @Test
    void findDriver_shouldReturnTrue_whenDriverExists() {
        String driverId = "driver123";
        Drivers driver = new Drivers();
        driver.setDriverId(driverId);
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));

        boolean result = driverFinderService.findDriver(driverId);

        assertTrue(result);
        verify(driverRepository).findById(driverId);
    }

    @Test
    void findDriver_shouldReturnFalse_whenDriverDoesNotExist() {
        String driverId = "driver456";
        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        boolean result = driverFinderService.findDriver(driverId);

        assertFalse(result);
        verify(driverRepository).findById(driverId);
    }
}