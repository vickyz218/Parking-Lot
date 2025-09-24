package com.example.parkinglot;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class ParkingSpot {
    private final String id;
    private final SpotType type;
    private Vehicle currentVehicle;

    public ParkingSpot(String id, SpotType type) {
        this.id = id;
        this.type = type;
        this.currentVehicle = null;
    }

    public boolean isAvailable() {
        return currentVehicle == null;
    }
}
