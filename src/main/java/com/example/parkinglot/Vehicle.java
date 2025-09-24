package com.example.parkinglot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
    private String id;
    private VehicleType type;
}
