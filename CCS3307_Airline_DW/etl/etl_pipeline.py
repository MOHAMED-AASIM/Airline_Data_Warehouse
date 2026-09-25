import sqlite3

# Database file paths
SOURCE_DB = "db/airline_source.db"
WAREHOUSE_DB = "db/airline_warehouse.db"

# Connect to Source Database
source_conn = sqlite3.connect(SOURCE_DB)
source_cursor = source_conn.cursor()

# Connect to Data Warehouse
warehouse_conn = sqlite3.connect(WAREHOUSE_DB)
warehouse_cursor = warehouse_conn.cursor()

print("Connected to Source Database successfully")
print("Connected to Data Warehouse successfully")

# -----------------------------
# Extract Passenger Data
# -----------------------------

passengers = source_cursor.execute("""
    SELECT passenger_id,
           passenger_name,
           gender,
           nationality,
           loyalty_tier
    FROM Passenger
""").fetchall()

# Clear old staging data
warehouse_cursor.execute("DELETE FROM stg_passenger")

# Load passenger data into staging
warehouse_cursor.executemany("""
    INSERT INTO stg_passenger
    (passenger_id, passenger_name, gender, nationality, loyalty_tier)
    VALUES (?, ?, ?, ?, ?)
""", passengers)

warehouse_conn.commit()

print(f"{len(passengers)} passenger records loaded into staging")

# -----------------------------
# Extract Airport Data
# -----------------------------

airports = source_cursor.execute("""
    SELECT airport_code,
           airport_name,
           city,
           country
    FROM Airport
""").fetchall()

# Clear old staging data
warehouse_cursor.execute("DELETE FROM stg_airport")

# Load airport data into staging
warehouse_cursor.executemany("""
    INSERT INTO stg_airport
    (airport_code, airport_name, city, country)
    VALUES (?, ?, ?, ?)
""", airports)

warehouse_conn.commit()

print(f"{len(airports)} airport records loaded into staging")

# -----------------------------
# Extract Flight Data
# -----------------------------

flights = source_cursor.execute("""
    SELECT flight_id,
           flight_number,
           departure_airport,
           arrival_airport,
           flight_date,
           aircraft_type,
           distance_km
    FROM Flight
""").fetchall()

# Clear old staging data
warehouse_cursor.execute("DELETE FROM stg_flight")

# Load flight data into staging
warehouse_cursor.executemany("""
    INSERT INTO stg_flight
    (flight_id, flight_number, departure_airport,
     arrival_airport, flight_date, aircraft_type, distance_km)
    VALUES (?, ?, ?, ?, ?, ?, ?)
""", flights)

warehouse_conn.commit()

print(f"{len(flights)} flight records loaded into staging")

# -----------------------------
# Extract Booking Data
# -----------------------------

bookings = source_cursor.execute("""
    SELECT booking_id,
           passenger_id,
           flight_id,
           booking_date,
           ticket_fare,
           taxes_and_fees,
           baggage_weight_kg
    FROM Booking
""").fetchall()

# Clear old staging data
warehouse_cursor.execute("DELETE FROM stg_booking")

# Load booking data into staging
warehouse_cursor.executemany("""
    INSERT INTO stg_booking
    (booking_id, passenger_id, flight_id, booking_date,
     ticket_fare, taxes_and_fees, baggage_weight_kg)
    VALUES (?, ?, ?, ?, ?, ?, ?)
""", bookings)

warehouse_conn.commit()

print(f"{len(bookings)} booking records loaded into staging")

# -----------------------------
# Load Dim_Passenger - SCD Type 2
# -----------------------------

staging_passengers = warehouse_cursor.execute("""
    SELECT passenger_id,
           passenger_name,
           gender,
           nationality,
           loyalty_tier
    FROM stg_passenger
""").fetchall()

for passenger in staging_passengers:

    passenger_id = passenger[0]
    passenger_name = passenger[1]
    gender = passenger[2]
    nationality = passenger[3]
    loyalty_tier = passenger[4]

    # Find current passenger record
    current_record = warehouse_cursor.execute("""
        SELECT passenger_sk,
               passenger_name,
               gender,
               nationality,
               loyalty_tier
        FROM Dim_Passenger
        WHERE passenger_id = ?
          AND is_current = 1
    """, (passenger_id,)).fetchone()

    # New passenger
    if current_record is None:

        warehouse_cursor.execute("""
            INSERT INTO Dim_Passenger
            (passenger_id, passenger_name, gender, nationality,
             loyalty_tier, effective_start_date,
             effective_end_date, is_current)
            VALUES (?, ?, ?, ?, ?, DATE('now'), NULL, 1)
        """, (
            passenger_id,
            passenger_name,
            gender,
            nationality,
            loyalty_tier
        ))

    # Existing passenger - check for changes
    else:

        old_name = current_record[1]
        old_gender = current_record[2]
        old_nationality = current_record[3]
        old_loyalty_tier = current_record[4]

        if (
            passenger_name != old_name
            or gender != old_gender
            or nationality != old_nationality
            or loyalty_tier != old_loyalty_tier
        ):

            # Close old record
            warehouse_cursor.execute("""
                UPDATE Dim_Passenger
                SET effective_end_date = DATE('now'),
                    is_current = 0
                WHERE passenger_sk = ?
            """, (current_record[0],))

            # Insert new historical version
            warehouse_cursor.execute("""
                INSERT INTO Dim_Passenger
                (passenger_id, passenger_name, gender, nationality,
                 loyalty_tier, effective_start_date,
                 effective_end_date, is_current)
                VALUES (?, ?, ?, ?, ?, DATE('now'), NULL, 1)
            """, (
                passenger_id,
                passenger_name,
                gender,
                nationality,
                loyalty_tier
            ))

warehouse_conn.commit()

print("Dim_Passenger loaded successfully")

# -----------------------------
# Load Dim_Airport
# -----------------------------

staging_airports = warehouse_cursor.execute("""
    SELECT airport_code,
           airport_name,
           city,
           country
    FROM stg_airport
""").fetchall()

for airport in staging_airports:

    airport_code = airport[0]
    airport_name = airport[1]
    city = airport[2]
    country = airport[3]

    existing_airport = warehouse_cursor.execute("""
        SELECT airport_sk
        FROM Dim_Airport
        WHERE airport_code = ?
    """, (airport_code,)).fetchone()

    if existing_airport is None:

        warehouse_cursor.execute("""
            INSERT INTO Dim_Airport
            (airport_code, airport_name, city, country)
            VALUES (?, ?, ?, ?)
        """, (
            airport_code,
            airport_name,
            city,
            country
        ))

warehouse_conn.commit()

print("Dim_Airport loaded successfully")

# -----------------------------
# Load Dim_Flight
# -----------------------------

staging_flights = warehouse_cursor.execute("""
    SELECT flight_id,
           flight_number,
           aircraft_type,
           distance_km
    FROM stg_flight
""").fetchall()

for flight in staging_flights:

    flight_id = flight[0]
    flight_number = flight[1]
    aircraft_type = flight[2]
    distance_km = flight[3]

    existing_flight = warehouse_cursor.execute("""
        SELECT flight_sk
        FROM Dim_Flight
        WHERE flight_id = ?
    """, (flight_id,)).fetchone()

    if existing_flight is None:

        warehouse_cursor.execute("""
            INSERT INTO Dim_Flight
            (flight_id, flight_number, aircraft_type, distance_km)
            VALUES (?, ?, ?, ?)
        """, (
            flight_id,
            flight_number,
            aircraft_type,
            distance_km
        ))

warehouse_conn.commit()

print("Dim_Flight loaded successfully")

# -----------------------------
# Load Dim_Date
# -----------------------------

from datetime import datetime

# Get booking dates
booking_dates = warehouse_cursor.execute("""
    SELECT DISTINCT booking_date
    FROM stg_booking
""").fetchall()

# Get flight dates
flight_dates = warehouse_cursor.execute("""
    SELECT DISTINCT flight_date
    FROM stg_flight
""").fetchall()

# Combine both date lists
all_dates = set()

for row in booking_dates:
    all_dates.add(row[0])

for row in flight_dates:
    all_dates.add(row[0])

# Insert dates into Dim_Date
for date_text in sorted(all_dates):

    date_value = datetime.strptime(date_text, "%Y-%m-%d")

    date_sk = int(date_value.strftime("%Y%m%d"))
    day = date_value.day
    month = date_value.month
    month_name = date_value.strftime("%B")
    quarter = ((month - 1) // 3) + 1
    year = date_value.year

    warehouse_cursor.execute("""
        INSERT OR IGNORE INTO Dim_Date
        (date_sk, full_date, day, month, month_name, quarter, year)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, (
        date_sk,
        date_text,
        day,
        month,
        month_name,
        quarter,
        year
    ))

warehouse_conn.commit()

print("Dim_Date loaded successfully")

# -----------------------------
# Load Fact_Flight_Booking
# -----------------------------

staging_bookings = warehouse_cursor.execute("""
    SELECT booking_id,
           passenger_id,
           flight_id,
           booking_date,
           ticket_fare,
           taxes_and_fees,
           baggage_weight_kg
    FROM stg_booking
""").fetchall()

for booking in staging_bookings:

    booking_id = booking[0]
    passenger_id = booking[1]
    flight_id = booking[2]
    booking_date = booking[3]
    ticket_fare = booking[4]
    taxes_and_fees = booking[5]
    baggage_weight_kg = booking[6]

    # Avoid duplicate fact records
    existing_booking = warehouse_cursor.execute("""
        SELECT booking_sk
        FROM Fact_Flight_Booking
        WHERE booking_id = ?
    """, (booking_id,)).fetchone()

    if existing_booking is None:

        # Passenger surrogate key
        passenger_sk = warehouse_cursor.execute("""
            SELECT passenger_sk
            FROM Dim_Passenger
            WHERE passenger_id = ?
              AND is_current = 1
        """, (passenger_id,)).fetchone()[0]

        # Flight information
        flight_info = warehouse_cursor.execute("""
            SELECT departure_airport,
                   arrival_airport,
                   flight_date,
                   distance_km
            FROM stg_flight
            WHERE flight_id = ?
        """, (flight_id,)).fetchone()

        departure_code = flight_info[0]
        arrival_code = flight_info[1]
        flight_date = flight_info[2]
        distance_km = flight_info[3]

        # Flight surrogate key
        flight_sk = warehouse_cursor.execute("""
            SELECT flight_sk
            FROM Dim_Flight
            WHERE flight_id = ?
        """, (flight_id,)).fetchone()[0]

        # Departure airport surrogate key
        departure_airport_sk = warehouse_cursor.execute("""
            SELECT airport_sk
            FROM Dim_Airport
            WHERE airport_code = ?
        """, (departure_code,)).fetchone()[0]

        # Arrival airport surrogate key
        arrival_airport_sk = warehouse_cursor.execute("""
            SELECT airport_sk
            FROM Dim_Airport
            WHERE airport_code = ?
        """, (arrival_code,)).fetchone()[0]

        # Date surrogate keys
        booking_date_sk = int(
            datetime.strptime(booking_date, "%Y-%m-%d").strftime("%Y%m%d")
        )

        flight_date_sk = int(
            datetime.strptime(flight_date, "%Y-%m-%d").strftime("%Y%m%d")
        )

        # Generated measure
        total_amount = ticket_fare + taxes_and_fees

        warehouse_cursor.execute("""
            INSERT INTO Fact_Flight_Booking
            (booking_id,
             passenger_sk,
             flight_sk,
             departure_airport_sk,
             arrival_airport_sk,
             booking_date_sk,
             flight_date_sk,
             ticket_fare,
             taxes_and_fees,
             baggage_weight_kg,
             distance_km,
             total_amount)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            booking_id,
            passenger_sk,
            flight_sk,
            departure_airport_sk,
            arrival_airport_sk,
            booking_date_sk,
            flight_date_sk,
            ticket_fare,
            taxes_and_fees,
            baggage_weight_kg,
            distance_km,
            total_amount
        ))

warehouse_conn.commit()

print("Fact_Flight_Booking loaded successfully")

source_conn.close()
warehouse_conn.close()

print("ETL pipeline completed successfully")