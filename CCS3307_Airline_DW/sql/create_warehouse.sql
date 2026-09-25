-- CCS3307 Data Warehousing
-- Group 20
-- Airline Flight Booking Data Warehouse

CREATE TABLE Dim_Passenger (
    passenger_sk INTEGER PRIMARY KEY AUTOINCREMENT,
    passenger_id TEXT NOT NULL,
    passenger_name TEXT NOT NULL,
    gender TEXT,
    nationality TEXT,
    loyalty_tier TEXT,
    effective_start_date TEXT NOT NULL,
    effective_end_date TEXT,
    is_current INTEGER NOT NULL
);

CREATE TABLE Dim_Airport (
    airport_sk INTEGER PRIMARY KEY AUTOINCREMENT,
    airport_code TEXT NOT NULL,
    airport_name TEXT NOT NULL,
    city TEXT,
    country TEXT
);

CREATE TABLE Dim_Flight (
    flight_sk INTEGER PRIMARY KEY AUTOINCREMENT,
    flight_id TEXT NOT NULL,
    flight_number TEXT NOT NULL,
    aircraft_type TEXT,
    distance_km REAL
);

CREATE TABLE Dim_Date (
    date_sk INTEGER PRIMARY KEY,
    full_date TEXT NOT NULL UNIQUE,
    day INTEGER NOT NULL,
    month INTEGER NOT NULL,
    month_name TEXT NOT NULL,
    quarter INTEGER NOT NULL,
    year INTEGER NOT NULL
);

CREATE TABLE Fact_Flight_Booking (
    booking_sk INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id TEXT NOT NULL UNIQUE,

    passenger_sk INTEGER NOT NULL,
    flight_sk INTEGER NOT NULL,
    departure_airport_sk INTEGER NOT NULL,
    arrival_airport_sk INTEGER NOT NULL,
    booking_date_sk INTEGER NOT NULL,
    flight_date_sk INTEGER NOT NULL,

    ticket_fare REAL NOT NULL,
    taxes_and_fees REAL DEFAULT 0,
    baggage_weight_kg REAL DEFAULT 0,
    distance_km REAL DEFAULT 0,
    total_amount REAL NOT NULL,

    FOREIGN KEY (passenger_sk)
        REFERENCES Dim_Passenger(passenger_sk),

    FOREIGN KEY (flight_sk)
        REFERENCES Dim_Flight(flight_sk),

    FOREIGN KEY (departure_airport_sk)
        REFERENCES Dim_Airport(airport_sk),

    FOREIGN KEY (arrival_airport_sk)
        REFERENCES Dim_Airport(airport_sk),

    FOREIGN KEY (booking_date_sk)
        REFERENCES Dim_Date(date_sk),

    FOREIGN KEY (flight_date_sk)
        REFERENCES Dim_Date(date_sk)
);

CREATE TABLE stg_passenger (
    passenger_id TEXT,
    passenger_name TEXT,
    gender TEXT,
    nationality TEXT,
    loyalty_tier TEXT
);

CREATE TABLE stg_airport (
    airport_code TEXT,
    airport_name TEXT,
    city TEXT,
    country TEXT
);

CREATE TABLE stg_flight (
    flight_id TEXT,
    flight_number TEXT,
    departure_airport TEXT,
    arrival_airport TEXT,
    flight_date TEXT,
    aircraft_type TEXT,
    distance_km REAL
);

CREATE TABLE stg_booking (
    booking_id TEXT,
    passenger_id TEXT,
    flight_id TEXT,
    booking_date TEXT,
    ticket_fare REAL,
    taxes_and_fees REAL,
    baggage_weight_kg REAL
);