package com.example.parkinglot;

import lombok.Getter;

import java.util.*;

@Getter
public class ParkingLot {
    private final List<List<ParkingSpot>> rows;
    private final Map<String, List<ParkingSpot>> vehicleAllocations;

    public ParkingLot(List<List<SpotType>> configuration) {
        rows = new ArrayList<>();
        vehicleAllocations = new HashMap<>();
        int rowNum = 1;
        for (List<SpotType> rowConfig : configuration) {
            List<ParkingSpot> row = new ArrayList<>();
            int spotNum = 1;
            for (SpotType type : rowConfig) {
                row.add(new ParkingSpot("R" + rowNum + "-" + spotNum, type));
                spotNum++;
            }
            rows.add(row);
            rowNum++;
        }
    }

    public synchronized List<ParkingSpot> parkVehicle(Vehicle vehicle) {
        if (vehicleAllocations.containsKey(vehicle.getId())) {
            return vehicleAllocations.get(vehicle.getId());
        }

        switch (vehicle.getType()) {
            case MOTORCYCLE -> {
                for (List<ParkingSpot> row : rows) {
                    for (ParkingSpot spot : row) {
                        if (spot.isAvailable()) {
                            spot.setCurrentVehicle(vehicle);
                            vehicleAllocations.put(vehicle.getId(), List.of(spot));
                            return List.of(spot);
                        }
                    }
                }
            }
            case CAR -> {
                for (List<ParkingSpot> row : rows) {
                    for (ParkingSpot spot : row) {
                        if (spot.isAvailable() && spot.getType() == SpotType.REGULAR) {
                            spot.setCurrentVehicle(vehicle);
                            vehicleAllocations.put(vehicle.getId(), List.of(spot));
                            return List.of(spot);
                        }
                    }
                }
            }
            case VAN -> {
                for (List<ParkingSpot> row : rows) {
                    for (int i = 0; i < row.size() - 1; i++) {
                        ParkingSpot s1 = row.get(i);
                        ParkingSpot s2 = row.get(i + 1);
                        if (s1.isAvailable() && s2.isAvailable() &&
                                s1.getType() == SpotType.REGULAR &&
                                s2.getType() == SpotType.REGULAR) {
                            s1.setCurrentVehicle(vehicle);
                            s2.setCurrentVehicle(vehicle);
                            vehicleAllocations.put(vehicle.getId(), List.of(s1, s2));
                            return List.of(s1, s2);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public synchronized boolean removeVehicle(String vehicleId) {
        List<ParkingSpot> spots = vehicleAllocations.remove(vehicleId);
        if (spots == null) return false;
        for (ParkingSpot spot : spots) {
            spot.setCurrentVehicle(null);
        }
        return true;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        int total = 0, available = 0, compact = 0, compactAvail = 0, regular = 0, regularAvail = 0;
        int vanOccupiedSpots = 0;
        Set<String> vanVehicleIds = new HashSet<>();

        List<Map<String, Object>> perRow = new ArrayList<>();

        int rowIndex = 1;
        for (List<ParkingSpot> row : rows) {
            int rowTotal = row.size();
            int rowAvailable = 0;
            int rowCompactAvail = 0;
            int rowRegularAvail = 0;
            int rowVanOccupiedSpots = 0;

            for (ParkingSpot spot : row) {
                total++;
                if (spot.getType() == SpotType.COMPACT) compact++;
                else regular++;
                if (spot.isAvailable()) {
                    available++;
                    rowAvailable++;
                    if (spot.getType() == SpotType.COMPACT) {
                        compactAvail++;
                        rowCompactAvail++;
                    } else {
                        regularAvail++;
                        rowRegularAvail++;
                    }
                } else if (spot.getCurrentVehicle().getType() == VehicleType.VAN) {
                    vanOccupiedSpots++;
                    rowVanOccupiedSpots++;
                    vanVehicleIds.add(spot.getCurrentVehicle().getId());
                }
            }

            Map<String, Object> rowSummary = new LinkedHashMap<>();
            rowSummary.put("row", rowIndex);
            rowSummary.put("totalSpots", rowTotal);
            rowSummary.put("availableSpots", rowAvailable);
            rowSummary.put("compactAvailable", rowCompactAvail);
            rowSummary.put("regularAvailable", rowRegularAvail);
            rowSummary.put("occupiedSpotsByVans", rowVanOccupiedSpots);
            perRow.add(rowSummary);
            rowIndex++;
        }

        status.put("totalSpots", total);
        status.put("availableSpots", available);
        status.put("compactSpots", compact);
        status.put("compactAvailable", compactAvail);
        status.put("regularSpots", regular);
        status.put("regularAvailable", regularAvail);
        status.put("isFull", available == 0);
        status.put("isEmpty", available == total);
        status.put("isCompactFull", compactAvail == 0 && compact > 0);
        status.put("isRegularFull", regularAvail == 0 && regular > 0);
        status.put("occupiedSpotsByVans", vanOccupiedSpots);
        status.put("occupiedByVansVehiclesCount", vanVehicleIds.size());
        status.put("perRowSummary", perRow);
        return status;
    }
}
