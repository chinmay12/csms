package com.example.transactionservice.repository;

import com.example.transactionservice.entities.Drivers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Drivers, String> {
    // Custom query methods, if needed
}