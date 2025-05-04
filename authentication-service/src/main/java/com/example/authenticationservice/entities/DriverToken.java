package com.example.authenticationservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "driver_tokens")
public class DriverToken {

    @Id
    private String driverId;
    private String token;

    public DriverToken() {
    }

    public DriverToken(String driverId, String token) {
        this.driverId = driverId;
        this.token = token;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
