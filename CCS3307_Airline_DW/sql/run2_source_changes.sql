-- CCS3307 Data Warehousing
-- Group 20
-- Airline Flight Booking System
-- Source Changes for ETL Run 2

-- 1. Changed existing passenger for SCD Type 2 demonstration
UPDATE Passenger
SET loyalty_tier = 'Gold'
WHERE passenger_id = 'P001';

-- 2. New passenger
INSERT INTO Passenger
(passenger_id, passenger_name, gender, nationality, loyalty_tier)
VALUES
('P011', 'Ravi Raj', 'Male', 'Sri Lankan', 'Bronze');

-- 3. New booking for incremental load demonstration
INSERT INTO Booking
(booking_id, passenger_id, flight_id, booking_date,
 ticket_fare, taxes_and_fees, baggage_weight_kg)
VALUES
('B021', 'P011', 'F005', '2026-07-21',
 123000, 15000, 20);