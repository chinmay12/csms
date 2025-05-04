package com.example.authenticationservice.service;


import com.example.authenticationservice.repository.DriverTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class DriverTokenService {

    private DriverTokenRepository driverTokenRepository;

    public DriverTokenService(DriverTokenRepository driverTokenRepository) {
        this.driverTokenRepository = driverTokenRepository;
    }

    public boolean validateDriverToken(String driverId, String token) {
        return driverTokenRepository.existsByDriverIdAndToken(driverId, token);
    }
}
