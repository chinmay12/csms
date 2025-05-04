package com.example.authenticationservice.repository;

import com.example.authenticationservice.entities.DriverToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverTokenRepository extends JpaRepository<DriverToken, String> {
    boolean existsByDriverIdAndToken(String driverId, String token);
}
