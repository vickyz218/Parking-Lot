package com.example.parkinglot;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ParkingLotTest {

    @Test
    public void testParkingAndRemoving() {
        List<List<SpotType>> config = List.of(
                List.of(SpotType.REGULAR, SpotType.REGULAR, SpotType.COMPACT),
                List.of(SpotType.REGULAR, SpotType.COMPACT)
        );
        ParkingLot lot = new ParkingLot(config);

        Vehicle car = new Vehicle("C1", VehicleType.CAR);
        Vehicle van = new Vehicle("V1", VehicleType.VAN);
        Vehicle moto = new Vehicle("M1", VehicleType.MOTORCYCLE);

        
        assertFalse(lot.parkVehicle(van).isEmpty());
        assertFalse(lot.parkVehicle(car).isEmpty());
        assertFalse(lot.parkVehicle(moto).isEmpty());

        Map<String, Object> status = lot.getStatus();
        assertEquals(5, status.get("totalSpots"));
        assertTrue((int)status.get("availableSpots") <= 5);
        assertTrue(status.containsKey("perRowSummary"));
        assertTrue(status.containsKey("occupiedByVansVehiclesCount"));
        assertTrue(((Integer) status.get("occupiedByVansVehiclesCount")) >= 1); // 因为我们停入了一辆厢式车

        assertTrue(lot.removeVehicle("C1"));
        assertFalse(lot.removeVehicle("NotExist"));
    }

    @Test
    public void testDeterministicAllocationRowMajor() {
        // 1 row 4 spots: REG, REG, REG, REG
        List<List<SpotType>> config = List.of(
                List.of(SpotType.REGULAR, SpotType.REGULAR, SpotType.REGULAR, SpotType.REGULAR)
        );
        ParkingLot lot = new ParkingLot(config);

        // Van should take spots R1-1 and R1-2
        Vehicle van = new Vehicle("V1", VehicleType.VAN);
        List<ParkingSpot> allocated = lot.parkVehicle(van);
        assertEquals(2, allocated.size());
        assertEquals("R1-1", allocated.get(0).getId());
        assertEquals("R1-2", allocated.get(1).getId());

        // Car should take first remaining REG which is R1-3
        Vehicle car = new Vehicle("C1", VehicleType.CAR);
        List<ParkingSpot> carSpot = lot.parkVehicle(car);
        assertEquals(1, carSpot.size());
        assertEquals("R1-3", carSpot.get(0).getId());
    }

    @Test
    public void testReparkReturnsExistingAllocation() {
        List<List<SpotType>> config = List.of(
                List.of(SpotType.REGULAR, SpotType.REGULAR)
        );
        ParkingLot lot = new ParkingLot(config);
        Vehicle moto = new Vehicle("M1", VehicleType.MOTORCYCLE);
        List<ParkingSpot> first = lot.parkVehicle(moto);
        List<ParkingSpot> second = lot.parkVehicle(moto);
        assertSame(first.get(0), second.get(0));
    }

    @Test
    public void testVanRequiresTwoContiguousRegularSameRow() {
        // Two rows: Row1 [REG, COMPACT], Row2 [REG, REG]
        List<List<SpotType>> config = List.of(
                List.of(SpotType.REGULAR, SpotType.COMPACT),
                List.of(SpotType.REGULAR, SpotType.REGULAR)
        );
        ParkingLot lot = new ParkingLot(config);

        // Block Row2-R1 so only Row2-R2 remains; no contiguous pair left in any row
        lot.parkVehicle(new Vehicle("C1", VehicleType.CAR)); // occupies R1-1
        lot.parkVehicle(new Vehicle("C2", VehicleType.CAR)); // occupies R2-1

        // Now a van should fail because Row1 doesn't have 2 REG contiguous and Row2 has only one REG left
        List<ParkingSpot> result = lot.parkVehicle(new Vehicle("V1", VehicleType.VAN));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testStatusFlagsAndCounts() {
        List<List<SpotType>> config = List.of(
                List.of(SpotType.REGULAR),
                List.of(SpotType.COMPACT)
        );
        ParkingLot lot = new ParkingLot(config);

        // Initially one REG and one COMPACT available
        Map<String, Object> s0 = lot.getStatus();
        assertFalse((boolean) s0.get("isFull"));
        assertTrue((boolean) s0.get("isEmpty"));
        assertFalse((boolean) s0.get("isRegularFull"));
        assertFalse((boolean) s0.get("isCompactFull"));

        // Park a car (REG only)
        lot.parkVehicle(new Vehicle("C1", VehicleType.CAR));
        Map<String, Object> s1 = lot.getStatus();
        assertEquals(0, s1.get("regularAvailable")); // after car parked, REG available should be 0
        assertFalse((boolean) s1.get("isFull"));

        // Park a motorcycle to the only remaining spot (COMPACT)
        lot.parkVehicle(new Vehicle("M1", VehicleType.MOTORCYCLE));
        Map<String, Object> s2 = lot.getStatus();
        assertTrue((boolean) s2.get("isFull"));
        assertFalse((boolean) s2.get("isEmpty"));
        assertTrue((boolean) s2.get("isRegularFull"));
        assertTrue((boolean) s2.get("isCompactFull"));
    }
}
