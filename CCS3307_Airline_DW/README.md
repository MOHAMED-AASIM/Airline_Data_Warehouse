\# CCS3307 Data Warehousing Project



\## Airline Flight Booking Data Warehouse



\*\*Module:\*\* CCS3307 - Data Warehousing  

\*\*Domain:\*\* Airline / Travel  

\*\*Business Process:\*\* Flight Ticket Booking  

\*\*Group:\*\* 20  



\### Group Members



\- Premnath - CIT-24-01-0241

\- Afrith - CIT-24-01-0297

\- Aasim - CIT-24-01-0298

\- Himas - CIT-24-01-0302



\## Project Overview



This project implements a Data Warehouse for an Airline Flight Booking System.



The selected business process is \*\*Flight Ticket Booking\*\*. The solution extracts data from an operational SQLite database, loads it into staging tables, transforms the data, and loads it into a dimensional Data Warehouse for analytical reporting.



The Data Warehouse contains one fact table and four dimensions. It also implements \*\*Slowly Changing Dimension (SCD) Type 2\*\* for passenger data to preserve historical changes in loyalty tier.



The ETL pipeline is implemented using \*\*Python and SQLite\*\* and supports incremental loading without creating duplicate booking records.



\## Project Structure



CCS3307\_Airline\_DW/

├── db/

│   ├── airline\_source.db

│   └── airline\_warehouse.db

├── sql/

│   ├── create\_source.sql

│   ├── insert\_source\_data.sql

│   ├── create\_warehouse.sql

│   ├── run2\_source\_changes.sql

│   └── analytical\_queries.sql

├── etl/

│   └── etl\_pipeline.py

├── diagrams/

│   ├── star\_schema.drawio

│   ├── star\_schema.png

│   ├── etl\_architecture.drawio

│   ├── etl\_architecture.png

│   ├── source\_oltp\_schema.drawio

│   ├── source\_oltp\_schema.png

│   ├── scd\_type2\_history.drawio

│   └── scd\_type2\_history.png

├── screenshots/

└── README.md



\## Running the Project



\### Requirements



\- Python 3.9 or later
\- Visual Studio Code
\- Python extension for VS Code (recommended)
\- SQLite through Python's built-in `sqlite3` module

Maven, Java, and JavaFX are not required for this project.



\### Step 1 - Check Python



Open the VS Code terminal and run:



```powershell

python --version

```

The command should display Python 3.9 or later.

\### Step 2 - Open the project folder

Open `CCS3307_Airline_DW` in VS Code. Run all commands below from this project folder.

\### Step 3 - Initialise the databases

To create clean databases from the SQL scripts, run:

```powershell
Remove-Item db/airline_source.db, db/airline_warehouse.db -ErrorAction SilentlyContinue

python -c "import sqlite3; db=sqlite3.connect('db/airline_source.db'); db.executescript(open('sql/create_source.sql').read()); db.executescript(open('sql/insert_source_data.sql').read()); db.commit(); db.close()"

python -c "import sqlite3; db=sqlite3.connect('db/airline_warehouse.db'); db.executescript(open('sql/create_warehouse.sql').read()); db.commit(); db.close()"
```

\### Step 4 - Run the initial ETL load

```powershell
python etl/etl_pipeline.py
```

The initial load should process 10 passengers, 6 airports, 8 flights, and 20 bookings.

\### Step 5 - Run the incremental load demonstration

```powershell
python -c "import sqlite3; db=sqlite3.connect('db/airline_source.db'); db.executescript(open('sql/run2_source_changes.sql').read()); db.commit(); db.close()"
python etl/etl_pipeline.py
```

Run 2 changes passenger `P001` from Silver to Gold, adds passenger `P011`, and adds booking `B021`. After Run 2, the fact table should contain 21 records and `P001` should have two SCD Type 2 records.

\### Step 6 - Run analytical queries

Open `db/airline_warehouse.db` in a SQLite client such as the VS Code SQLite extension or DB Browser for SQLite, then execute `sql/analytical_queries.sql`.

\## JavaFX Dashboard

The optional desktop application is provided in `javafx-app`. It is a Maven JavaFX application that reads the existing `db/airline_warehouse.db` file and displays warehouse metrics and analytical reports.

\### Requirements

\- JDK 17 or later
\- Maven 3.9 or later
\- Visual Studio Code with the Extension Pack for Java (recommended)

JavaFX is downloaded automatically by Maven. No separate JavaFX installation is required.

Check the tools from the VS Code terminal:

```powershell
java -version
javac -version
mvn -version
```

\### Build and run the dashboard

Run the Python ETL first so that the warehouse database exists, then run:

```powershell
Set-Location javafx-app
mvn clean javafx:run
```

The dashboard opens with booking, passenger, flight, and airport totals, followed by tabs for revenue by flight, loyalty tier, destination, date, and route.



The ETL pipeline performs the following process:



1\. Connects to the source and Data Warehouse databases.

2\. Extracts Passenger, Airport, Flight, and Booking data.

3\. Loads the extracted data into staging tables.

4\. Loads the dimension tables.

5\. Applies SCD Type 2 processing to Dim\_Passenger.

6\. Performs surrogate key lookups.

7\. Loads new booking records into Fact\_Flight\_Booking.

8\. Prevents duplicate fact records during repeated ETL runs.



\## ETL Run Demonstration



\### Run 1 - Initial Load



The first ETL run loads the initial source data:



\- 10 passengers

\- 6 airports

\- 8 flights

\- 20 bookings



The data is extracted from the source database, loaded into staging tables, and then loaded into the dimension and fact tables.



\### Run 2 - Incremental Load and SCD Type 2



For the second ETL run, the source data is changed to demonstrate incremental loading and historical tracking:



\- Passenger P001 loyalty tier changes from Silver to Gold.

\- New passenger P011 is added.

\- New booking B021 is added.



After Run 2:



\- Fact\_Flight\_Booking contains 21 booking records.

\- Dim\_Passenger contains 12 records for 11 passengers.

\- P001 has two dimension versions.

\- The old Silver record is marked as historical with is\_current = 0.

\- The new Gold record is marked as current with is\_current = 1.

\- Re-running the ETL does not create duplicate booking or passenger-version records.



\## Analytical Queries



The Data Warehouse supports analytical reporting for business decision-making.



The following analytical queries are included:



1\. Total Revenue by Flight

2\. Total Revenue by Passenger Loyalty Tier

3\. Total Revenue by Destination

4\. Daily Booking Revenue

5\. Total Revenue by Route



These queries demonstrate the use of fact and dimension tables for flight, passenger, airport, route, and time-based analysis.



\## Conclusion



This project demonstrates the complete implementation of an Airline Flight Booking Data Warehouse using Python and SQLite.



The solution includes an operational source database, staging layer, dimensional model, ETL pipeline, surrogate keys, SCD Type 2 historical tracking, incremental loading, and analytical SQL queries.



The Data Warehouse enables historical and business-oriented analysis of airline bookings while maintaining data consistency and preventing duplicate fact records during repeated ETL runs.

