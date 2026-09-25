-- CCS3307 Data Warehousing
-- Group 20
-- Airline Flight Booking System
-- Initial Source Data - Run 1

-- PASSENGERS
INSERT INTO Passenger VALUES
('P001', 'Kamal Perera', 'Male', 'Sri Lankan', 'Silver'),
('P002', 'Nimal Silva', 'Male', 'Sri Lankan', 'Gold'),
('P003', 'Fathima Azeez', 'Female', 'Sri Lankan', 'Silver'),
('P004', 'Arun Kumar', 'Male', 'Indian', 'Gold'),
('P005', 'Sarah Ahmed', 'Female', 'Maldivian', 'Bronze'),
('P006', 'John Smith', 'Male', 'British', 'Silver'),
('P007', 'Kavindi Fernando', 'Female', 'Sri Lankan', 'Bronze'),
('P008', 'Mohamed Irfan', 'Male', 'Sri Lankan', 'Gold'),
('P009', 'Priya Sharma', 'Female', 'Indian', 'Silver'),
('P010', 'Anne Williams', 'Female', 'British', 'Bronze');

-- AIRPORTS
INSERT INTO Airport VALUES
('CMB', 'Bandaranaike International Airport', 'Colombo', 'Sri Lanka'),
('JAF', 'Jaffna International Airport', 'Jaffna', 'Sri Lanka'),
('DEL', 'Indira Gandhi International Airport', 'Delhi', 'India'),
('MAA', 'Chennai International Airport', 'Chennai', 'India'),
('MLE', 'Velana International Airport', 'Male', 'Maldives'),
('DXB', 'Dubai International Airport', 'Dubai', 'UAE');

-- FLIGHTS
INSERT INTO Flight VALUES
('F001', 'SC101', 'CMB', 'JAF', '2026-07-10', 'ATR 72', 320),
('F002', 'SC102', 'CMB', 'MAA', '2026-07-12', 'A320', 650),
('F003', 'SC103', 'CMB', 'DEL', '2026-07-15', 'A320', 2400),
('F004', 'SC104', 'CMB', 'MLE', '2026-07-18', 'A320', 780),
('F005', 'SC105', 'CMB', 'DXB', '2026-07-20', 'A330', 3300),
('F006', 'SC106', 'MAA', 'CMB', '2026-07-22', 'A320', 650),
('F007', 'SC107', 'DEL', 'CMB', '2026-07-25', 'A320', 2400),
('F008', 'SC108', 'MLE', 'CMB', '2026-07-28', 'A320', 780);

-- BOOKINGS
INSERT INTO Booking VALUES
('B001', 'P001', 'F001', '2026-07-01', 15000, 2500, 10),
('B002', 'P002', 'F002', '2026-07-02', 45000, 6500, 20),
('B003', 'P003', 'F003', '2026-07-03', 75000, 9500, 15),
('B004', 'P004', 'F004', '2026-07-04', 55000, 7000, 20),
('B005', 'P005', 'F005', '2026-07-05', 120000, 15000, 25),
('B006', 'P006', 'F002', '2026-07-06', 47000, 6500, 15),
('B007', 'P007', 'F003', '2026-07-07', 78000, 9500, 20),
('B008', 'P008', 'F005', '2026-07-08', 125000, 15000, 30),
('B009', 'P009', 'F006', '2026-07-09', 44000, 6500, 10),
('B010', 'P010', 'F007', '2026-07-10', 76000, 9500, 15),
('B011', 'P001', 'F004', '2026-07-11', 53000, 7000, 20),
('B012', 'P002', 'F005', '2026-07-12', 118000, 15000, 25),
('B013', 'P003', 'F008', '2026-07-13', 52000, 7000, 15),
('B014', 'P004', 'F003', '2026-07-14', 74000, 9500, 20),
('B015', 'P005', 'F002', '2026-07-15', 46000, 6500, 10),
('B016', 'P006', 'F004', '2026-07-16', 54000, 7000, 15),
('B017', 'P007', 'F005', '2026-07-17', 121000, 15000, 25),
('B018', 'P008', 'F006', '2026-07-18', 45000, 6500, 20),
('B019', 'P009', 'F007', '2026-07-19', 77000, 9500, 15),
('B020', 'P010', 'F008', '2026-07-20', 51000, 7000, 10);