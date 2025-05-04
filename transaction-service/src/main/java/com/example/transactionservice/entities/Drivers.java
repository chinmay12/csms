package com.example.transactionservice.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Setter
@Getter
public class Drivers {

        @Id
        private String driverId;

        private String driverName;
}
