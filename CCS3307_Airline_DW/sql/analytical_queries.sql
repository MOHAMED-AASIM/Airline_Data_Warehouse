-- Query 1: Total Revenue by Flight

SELECT
    f.flight_number,
    COUNT(fb.booking_id) AS total_bookings,
    SUM(fb.total_amount) AS total_revenue
FROM Fact_Flight_Booking fb
JOIN Dim_Flight f
    ON fb.flight_sk = f.flight_sk
GROUP BY
    f.flight_number
ORDER BY
    total_revenue DESC;

-- Query 2: Total Revenue by Passenger Loyalty Tier

SELECT
    p.loyalty_tier,
    COUNT(fb.booking_id) AS total_bookings,
    SUM(fb.total_amount) AS total_revenue
FROM Fact_Flight_Booking fb
JOIN Dim_Passenger p
    ON fb.passenger_sk = p.passenger_sk
GROUP BY
    p.loyalty_tier
ORDER BY
    total_revenue DESC;

-- Query 3: Total Revenue by Destination

SELECT
    a.city AS destination_city,
    a.country AS destination_country,
    COUNT(fb.booking_id) AS total_bookings,
    SUM(fb.total_amount) AS total_revenue
FROM Fact_Flight_Booking fb
JOIN Dim_Airport a
    ON fb.arrival_airport_sk = a.airport_sk
GROUP BY
    a.city,
    a.country
ORDER BY
    total_revenue DESC;

-- Query 4: Daily Booking Revenue

SELECT
    d.full_date,
    d.day,
    d.month_name,
    d.year,
    COUNT(fb.booking_id) AS total_bookings,
    SUM(fb.total_amount) AS total_revenue
FROM Fact_Flight_Booking fb
JOIN Dim_Date d
    ON fb.booking_date_sk = d.date_sk
GROUP BY
    d.full_date,
    d.day,
    d.month_name,
    d.year
ORDER BY
    d.full_date;

-- Query 5: Revenue by Route

SELECT
    dep.city AS departure_city,
    arr.city AS arrival_city,
    COUNT(fb.booking_id) AS total_bookings,
    SUM(fb.total_amount) AS total_revenue
FROM Fact_Flight_Booking fb
JOIN Dim_Airport dep
    ON fb.departure_airport_sk = dep.airport_sk
JOIN Dim_Airport arr
    ON fb.arrival_airport_sk = arr.airport_sk
GROUP BY
    dep.city,
    arr.city
ORDER BY
    total_revenue DESC;