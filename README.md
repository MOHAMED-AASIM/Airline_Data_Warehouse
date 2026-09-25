# ✈️ Airline Flight Booking Data Warehouse

> **CCS3307 – Data Warehousing | Group 20**

A complete **Airline Flight Booking Data Warehouse and Business Intelligence Dashboard** built with **Python, SQLite, SQL, Maven and JavaFX**. The project demonstrates ETL processing, staging, dimensional modelling, SCD Type 2, incremental loading, analytical SQL queries and an interactive dashboard for airline booking insights.

### 🔗 GitHub Repository
`https://github.com/MOHAMED-AASIM/Airline_Data_Warehouse`

### 🎥 Demo Video
**Watch the short project demo:**  
`PASTE-YOUR-SHORT-DEMO-LINK-HERE`

> Replace the placeholder with your actual short YouTube/Google Drive demo link, for example `https://youtu.be/XXXXXXXXXXX`.

---

## 📌 Short Project Description

**Airline Flight Booking Data Warehouse** is a Python/SQLite-based data warehouse project with a JavaFX dashboard. It extracts airline booking data from an operational database, transforms and loads it through an ETL pipeline into a star-schema warehouse, tracks passenger loyalty history using SCD Type 2, and provides analytical reports for revenue, flights, destinations, routes and loyalty tiers.

---

## 👥 Group Members

| Name | Student ID |
|---|---|
| Premnath | CIT-24-01-0241 |
| Afrith | CIT-24-01-0297 |
| Aasim | CIT-24-01-0298 |
| Himas | CIT-24-01-0302 |

---

# 1. Project Overview

The selected business process is **Flight Ticket Booking**.

The complete workflow is:

```text
Operational SQLite Database
          ↓
       Staging
          ↓
      ETL Process
          ↓
Dimensional Data Warehouse
          ↓
 Analytical SQL Queries
          ↓
 JavaFX BI Dashboard
```

The system supports:

- Airline booking data management
- ETL processing
- Staging tables
- Star-schema dimensional modelling
- Surrogate keys
- Incremental loading
- SCD Type 2 passenger history
- Duplicate prevention
- Revenue analysis
- Flight analysis
- Route and destination analysis
- Loyalty-tier analysis
- Interactive JavaFX dashboard

---

# 2. Project Architecture

```text
┌─────────────────────────────┐
│   SOURCE / OLTP DATABASE    │
│      airline_source.db      │
└──────────────┬──────────────┘
               │ Extract
               ▼
┌─────────────────────────────┐
│          STAGING            │
│ Passenger / Airport / Flight│
│ Booking staging tables      │
└──────────────┬──────────────┘
               │ Transform + Load
               ▼
┌─────────────────────────────┐
│      DATA WAREHOUSE         │
│     airline_warehouse.db    │
│                             │
│ FACT_Flight_Booking         │
│ DIM_Passenger               │
│ DIM_Flight                  │
│ DIM_Airport                 │
│ DIM_Date                    │
└──────────────┬──────────────┘
               │
               │ SQL Analytics
               ▼
┌─────────────────────────────┐
│       JAVA FX DASHBOARD     │
│ KPI + Revenue + Reports     │
└─────────────────────────────┘
```

---

# 3. Project Structure

```text
CCS3307_Airline_DW/
│
├── db/
│   ├── airline_source.db
│   └── airline_warehouse.db
│
├── sql/
│   ├── create_source.sql
│   ├── insert_source_data.sql
│   ├── create_warehouse.sql
│   ├── run2_source_changes.sql
│   └── analytical_queries.sql
│
├── etl/
│   └── etl_pipeline.py
│
├── javafx-app/
│   ├── pom.xml
│   └── src/main/java/com/ccs3307/airline/
│       └── AirlineDashboard.java
│
├── airline-booking-javafx/
│   ├── pom.xml
│   ├── README.md
│   └── src/main/java/com/ccs3307/airline/
│       └── App.java
│
├── diagrams/
├── screenshots/
├── report/
└── README.md
```

---

# 4. Requirements

### Python / ETL

- Python 3.9+
- Visual Studio Code
- Python extension
- SQLite through Python's built-in `sqlite3`

### JavaFX Dashboard

- JDK 17+
- Apache Maven 3.9+
- Visual Studio Code
- Extension Pack for Java
- Internet access for the first Maven build

Check:

```powershell
python --version
java -version
javac -version
mvn -version
```

---

# 5. Step-by-Step Setup in VS Code

## Step 1 – Clone the Repository

```powershell
git clone https://github.com/MOHAMED-AASIM/Airline_Data_Warehouse.git
cd Airline_Data_Warehouse
code .
```

Open the `CCS3307_Airline_DW` folder in VS Code.

---

## Step 2 – Check the Project

```powershell
cd CCS3307_Airline_DW
dir
```

You should see folders such as:

```text
db
sql
etl
javafx-app
airline-booking-javafx
diagrams
screenshots
report
```

---

# 6. Initialise the Databases

Run:

```powershell
Remove-Item db/airline_source.db, db/airline_warehouse.db -ErrorAction SilentlyContinue
```

Create the source database:

```powershell
python -c "import sqlite3; db=sqlite3.connect('db/airline_source.db'); db.executescript(open('sql/create_source.sql').read()); db.executescript(open('sql/insert_source_data.sql').read()); db.commit(); db.close()"
```

Create the warehouse:

```powershell
python -c "import sqlite3; db=sqlite3.connect('db/airline_warehouse.db'); db.executescript(open('sql/create_warehouse.sql').read()); db.commit(); db.close()"
```

---

# 7. Run the Initial ETL Load

```powershell
python etl/etl_pipeline.py
```

Expected initial data:

- 10 passengers
- 6 airports
- 8 flights
- 20 bookings

The ETL process:

1. Extracts source data.
2. Loads staging data.
3. Transforms data.
4. Loads dimensions.
5. Generates surrogate keys.
6. Applies SCD Type 2.
7. Loads fact bookings.
8. Prevents duplicate records.

---

# 8. Demonstrate Incremental Loading

Run:

```powershell
python -c "import sqlite3; db=sqlite3.connect('db/airline_source.db'); db.executescript(open('sql/run2_source_changes.sql').read()); db.commit(); db.close()"
```

Then:

```powershell
python etl/etl_pipeline.py
```

Run 2 demonstrates:

- `P001`: Silver → Gold
- New passenger `P011`
- New booking `B021`

Expected result:

- Fact table contains 21 booking records.
- `P001` has historical and current dimension versions.
- Old Silver record: `is_current = 0`
- New Gold record: `is_current = 1`
- Re-running ETL does not create duplicates.

---

# 9. SCD Type 2

```text
P001
 │
 ├── Silver
 │     is_current = 0
 │     historical record
 │
 └── Gold
       is_current = 1
       current record
```

This preserves passenger loyalty history instead of overwriting previous values.

---

# 10. Run Analytical SQL Queries

Open:

```text
db/airline_warehouse.db
```

Use a VS Code SQLite extension or DB Browser for SQLite.

Execute:

```text
sql/analytical_queries.sql
```

Included analyses:

1. Total Revenue by Flight
2. Total Revenue by Passenger Loyalty Tier
3. Total Revenue by Destination
4. Daily Booking Revenue
5. Total Revenue by Route

---

# 11. Run the JavaFX Dashboard

First run the Python ETL so that the warehouse database exists.

Then:

```powershell
Set-Location javafx-app
mvn clean javafx:run
```

The **Airline Warehouse Dashboard** should open.

---

# 12. Dashboard Features

### KPI Cards

- Total Bookings
- Total Passengers
- Total Flights
- Total Airports

### Analytical Reports

- Revenue by Flight
- Revenue by Loyalty Tier
- Revenue by Destination
- Daily Booking Revenue
- Revenue by Route

### Booking Entry

- Booking ID
- Passenger
- Flight
- Booking Date
- Ticket Fare
- Taxes & Fees
- Baggage Weight

### Controls

- Refresh Data
- Analytical report selection
- Live warehouse data

---

# 13. Planned Dashboard Enhancements

The dashboard can be extended with:

- Total Revenue KPI
- Average Ticket Fare
- Average Baggage Weight
- Total Taxes and Fees
- Revenue charts
- Daily booking trend chart
- Passenger analytics
- Flight performance analytics
- Date filters
- Destination filters
- Flight filters
- Loyalty-tier filters
- Search Booking
- Edit Booking
- Delete Booking
- Booking status
- Payment method
- Seat class
- CSV/Excel export

---

# 14. OLAP Operations

### Slice

Example:

```text
Business Class bookings only
```

### Dice

Example:

```text
Business Class
+ Dubai
+ September
```

### Drill-down

```text
Year
 ↓
Month
 ↓
Day
```

### Roll-up

```text
Day
 ↓
Month
 ↓
Year
```

---

# 15. Star Schema

```text
                 DIM_PASSENGER
                       │
                       │
DIM_DATE ───── FACT_FLIGHT_BOOKING ───── DIM_FLIGHT
                       │
                       │
                  DIM_AIRPORT
```

### Fact Table

`FACT_FLIGHT_BOOKING`

Measures include:

- Ticket fare
- Taxes and fees
- Baggage weight
- Total revenue

### Dimensions

- `DIM_PASSENGER`
- `DIM_FLIGHT`
- `DIM_AIRPORT`
- `DIM_DATE`

---

# 16. Testing

Test the following:

- Valid booking insertion
- Duplicate Booking ID
- Missing required fields
- Invalid numeric values
- Negative values
- Passenger/flight validation
- Incremental ETL
- SCD Type 2 history
- Duplicate prevention
- Analytical query accuracy
- Dashboard refresh
- Filtering
- Export

---

# 17. Demo Video

🎥 **[Watch the Airline Data Warehouse Demo](https://drive.google.com/file/d/1Y83yb6oovMWwlWt0Z7xKDWC9SZppbL_K/view?usp=drive_link)**

Recommended demo order:

```text
Project structure
      ↓
Database setup
      ↓
ETL Run 1
      ↓
SCD Type 2 / ETL Run 2
      ↓
Analytical SQL
      ↓
JavaFX Dashboard
      ↓
KPI Cards
      ↓
Revenue Reports
      ↓
Add Booking
      ↓
Refresh Data
```

**Important:** Replace `https://drive.google.com/file/d/1Y83yb6oovMWwlWt0Z7xKDWC9SZppbL_K/view?usp=drive_link` with your real short video URL before pushing to GitHub.

---

# 18. Technologies

| Technology | Purpose |
|---|---|
| Python | ETL pipeline |
| SQLite | Source and warehouse |
| SQL | Database and analytics |
| Java | Dashboard |
| JavaFX | Desktop BI interface |
| Maven | Java build/dependencies |
| Draw.io | Architecture diagrams |
| VS Code | Development |
| Git/GitHub | Version control |

---

# 19. Git Commands

For future changes:

```powershell
git status
git add .
git commit -m "Update Airline Data Warehouse Dashboard"
git push
```

First push:

```powershell
git branch -M main
git remote add origin https://github.com/MOHAMED-AASIM/Airline_Data_Warehouse.git
git push -u origin main
```

If `origin` already exists:

```powershell
git remote set-url origin https://github.com/MOHAMED-AASIM/Airline_Data_Warehouse.git
git push -u origin main
```

---

# 20. Conclusion

The **Airline Flight Booking Data Warehouse** demonstrates a complete data warehousing workflow:

**Source Database → Staging → ETL → Star Schema → SCD Type 2 → Incremental Loading → Analytical SQL → JavaFX Dashboard**

It provides historical and business-oriented analysis of airline bookings, revenue, passengers, flights, destinations, routes and loyalty information while maintaining data consistency and preventing duplicate warehouse records.

---

## 🔗 Repository

**GitHub:**  
https://github.com/MOHAMED-AASIM/Airline_Data_Warehouse

**Demo:**  
`PASTE-YOUR-SHORT-DEMO-LINK-HERE`
