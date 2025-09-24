# Parking Lot – Object-Oriented Design (Java)

## Overview
This project is an object-oriented parking lot system written in Java. It models rows of parking spots, applies vehicle-specific parking rules, and provides status queries. The solution uses a simple in-memory domain model with JUnit tests. A small Spring Boot entry point is included to make it easy to run, though the problem statement does not require an API layer.

## Features
- Deterministic, row-major parking allocation.
- Parking rules:
  - Motorcycles can park in any spot (COMPACT or REGULAR).
  - Cars can park only in REGULAR spots.
  - Vans require two contiguous REGULAR spots in the same row.
- Re-parking the same vehicle returns the same allocation.
- Safe removal of vehicles by ID (no crash when ID not found).
- Rich status query:
  - Totals and available counts (overall and by type).
  - Full/empty flags (overall and per type).
  - Count of spots occupied by vans and number of van vehicles.
  - Optional per-row summary.

## Project Structure
```
./
├─ pom.xml
├─ README.md
├─ src/
│  ├─ main/java/com/example/parkinglot/
│  │  ├─ ParkingLotApplication.java
│  │  ├─ ParkingLot.java
│  │  ├─ ParkingSpot.java
│  │  ├─ SpotType.java
│  │  ├─ Vehicle.java
│  │  └─ VehicleType.java
│  └─ test/java/com/example/parkinglot/
│     └─ ParkingLotTest.java
```

## Requirements
- Java 17+
- Maven 3.9+

## Build
```
mvn -q -DskipTests=true clean package
```

## Run (Optional)
This project includes a Spring Boot main class to bootstrap a JVM process. There is no REST API included by default because the problem statement does not require one. You can still run the application:
```
mvn spring-boot:run
```

## Run Tests
```
mvn test
```


## Design Choices
- Deterministic allocation: always scans rows from top to bottom, left to right.
- Domain model:
  - `ParkingLot` manages rows, spot creation, allocation map, and status.
  - `ParkingSpot` encapsulates ID, type, and current vehicle.
  - `Vehicle` + `VehicleType` and `SpotType` define the domain vocabulary.
- Van allocation:checks for two contiguous REGULAR spots in one pass.
- Thread-safety: parkVehicle and removeVehicle are synchronized to avoid race conditions.

## Assumptions
- Spot IDs follow `R{row}-{index}` with 1-based indexing (e.g., `R1-1`).
- Configurations are valid and non-empty.
- Re-parking with the same ID returns the existing allocation.
- If no suitable spot is found, an empty list is returned.
- getStatus() is not synchronized; small inconsistencies are acceptable in a single-process use case.

## Possible Improvements
- Input validation for `Vehicle` objects.  
- Stronger concurrency control on `getStatus()`.  
- Smarter allocation strategy (e.g., prefer `COMPACT` for motorcycles).  
- REST API layer with controllers and DTOs.  
- Persistence for stateful/multi-process usage.  
- Metrics/logging for monitoring usage.  

## Alternatives Considered
- **Allocation strategies**: stuck with greedy row-major for predictability, though heuristics could reduce fragmentation.  
- **Van modeling**: kept a simple list of spots per vehicle rather than a separate object.  
- **Spot mutability**: kept `currentVehicle` mutable for simplicity; immutable modeling would improve traceability at the cost of complexity.  