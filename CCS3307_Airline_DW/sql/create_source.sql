-- CCS3307 Data Warehousing
-- Group 20
-- Airline Flight Booking System
-- Source / OLTP Database

CREATE TABLE Passenger (
    passenger_id TEXT PRIMARY KEY,
    passenger_name TEXT NOT NULL,
    gender TEXT,
    nationality TEXT,
    loyalty_tier TEXT
);

CREATE TABLE Airport (
    airport_code TEXT PRIMARY KEY,
    airport_name TEXT NOT NULL,
    city TEXT,
    country TEXT
);

CREATE TABLE Flight (
    flight_id TEXT PRIMARY KEY,
    flight_number TEXT NOT NULL,
    departure_airport TEXT NOT NULL,
    arrival_airport TEXT NOT NULL,
    flight_date TEXT NOT NULL,
    aircraft_type TEXT,
    distance_km REAL,

    FOREIGN KEY (departure_airport)
        REFERENCES Airport(airport_code),

    FOREIGN KEY (arrival_airport)
        REFERENCES Airport(airport_code)
);

CREATE TABLE Booking (
    booking_id TEXT PRIMARY KEY,
    passenger_id TEXT NOT NULL,
    flight_id TEXT NOT NULL,
    booking_date TEXT NOT NULL,
    ticket_fare REAL NOT NULL,
    taxes_and_fees REAL DEFAULT 0,
    baggage_weight_kg REAL DEFAULT 0,

    FOREIGN KEY (passenger_id)
        REFERENCES Passenger(passenger_id),

    FOREIGN KEY (flight_id)
        REFERENCES Flight(flight_id)
);