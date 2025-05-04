package com.example.transactionservice.service;


import com.example.transactionservice.repository.DriverRepository;
import org.springframework.stereotype.Service;

@Service
public class DriverFinderService {

    private final DriverRepository driverRepository;

    public DriverFinderService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public boolean findDriver(String driverId){
        return driverRepository.findById(driverId).isPresent();
    }
}
