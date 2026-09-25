package com.ccs3307.airline;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AirlineDashboard extends Application {
    private static final String DATABASE_URL = "jdbc:sqlite:../db/airline_warehouse.db";

    private final List<Report> reports = List.of(
        new Report("Revenue by Flight", """
            SELECT f.flight_number AS flight, COUNT(fb.booking_id) AS bookings,
                   ROUND(SUM(fb.total_amount), 2) AS revenue
            FROM Fact_Flight_Booking fb JOIN Dim_Flight f ON fb.flight_sk = f.flight_sk
            GROUP BY f.flight_number ORDER BY revenue DESC
            """),
        new Report("Revenue by Loyalty Tier", """
            SELECT p.loyalty_tier AS loyalty_tier, COUNT(fb.booking_id) AS bookings,
                   ROUND(SUM(fb.total_amount), 2) AS revenue
            FROM Fact_Flight_Booking fb JOIN Dim_Passenger p ON fb.passenger_sk = p.passenger_sk
            GROUP BY p.loyalty_tier ORDER BY revenue DESC
            """),
        new Report("Revenue by Destination", """
            SELECT a.city AS destination, a.country, COUNT(fb.booking_id) AS bookings,
                   ROUND(SUM(fb.total_amount), 2) AS revenue
            FROM Fact_Flight_Booking fb JOIN Dim_Airport a ON fb.arrival_airport_sk = a.airport_sk
            GROUP BY a.city, a.country ORDER BY revenue DESC
            """),
        new Report("Daily Booking Revenue", """
            SELECT d.full_date, COUNT(fb.booking_id) AS bookings,
                   ROUND(SUM(fb.total_amount), 2) AS revenue
            FROM Fact_Flight_Booking fb JOIN Dim_Date d ON fb.booking_date_sk = d.date_sk
            GROUP BY d.full_date ORDER BY d.full_date
            """),
        new Report("Revenue by Route", """
            SELECT dep.city || ' -> ' || arr.city AS route, COUNT(fb.booking_id) AS bookings,
                   ROUND(SUM(fb.total_amount), 2) AS revenue
            FROM Fact_Flight_Booking fb
            JOIN Dim_Airport dep ON fb.departure_airport_sk = dep.airport_sk
            JOIN Dim_Airport arr ON fb.arrival_airport_sk = arr.airport_sk
            GROUP BY dep.city, arr.city ORDER BY revenue DESC
            """)
    );

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #f5f7fb;");

        Label eyebrow = new Label("CCS3307 / AIRLINE DATA WAREHOUSE");
        eyebrow.setStyle("-fx-text-fill: #2f6fed; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label title = new Label("Flight booking intelligence");
        title.setStyle("-fx-text-fill: #172033; -fx-font-size: 28px; -fx-font-weight: bold;");
        Label subtitle = new Label("Live analytical views from the SQLite warehouse");
        subtitle.setStyle("-fx-text-fill: #657189; -fx-font-size: 14px;");

        VBox heading = new VBox(7, eyebrow, title, subtitle);
        root.setTop(heading);

        root.setCenter(buildContent(stage));

        Scene scene = new Scene(root, 1080, 720);
        stage.setTitle("Airline Warehouse Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildContent(Stage stage) {
        FlowPane metrics = new FlowPane(14, 14);
        metrics.setPadding(new Insets(30, 0, 22, 0));
        metrics.setPrefWrapLength(1000);

        ComboBox<String> reportSelector = new ComboBox<>();
        reportSelector.getItems().addAll(reports.stream().map(Report::name).toList());
        reportSelector.setValue(reports.get(0).name());
        reportSelector.setPrefWidth(230);
        reportSelector.setStyle("-fx-font-size: 13px;");

        Button refreshButton = new Button("Refresh data");
        refreshButton.setStyle("-fx-background-color: #2f6fed; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        Button addBookingButton = new Button("Add booking");
        addBookingButton.setStyle("-fx-background-color: #172033; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        Button searchButton = new Button("Search booking");
        searchButton.setStyle("-fx-background-color: white; -fx-text-fill: #172033; -fx-font-weight: bold; -fx-border-color: #d0d7e2; -fx-background-radius: 6;");

        Button exportButton = new Button("Export CSV");
        exportButton.setStyle("-fx-background-color: white; -fx-text-fill: #172033; -fx-font-weight: bold; -fx-border-color: #d0d7e2; -fx-background-radius: 6;");

        Label status = new Label("Ready");
        status.setStyle("-fx-text-fill: #657189; -fx-font-size: 12px;");
        HBox controls = new HBox(10, reportSelector, refreshButton, addBookingButton, searchButton, exportButton, status);
        controls.setPadding(new Insets(0, 0, 14, 0));

        StackPane tableHost = new StackPane();
        VBox.setVgrow(tableHost, Priority.ALWAYS);

        Runnable refresh = () -> loadDashboard(metrics, reportSelector, tableHost, status);
        refreshButton.setOnAction(event -> refresh.run());
        reportSelector.setOnAction(event -> refresh.run());
        addBookingButton.setOnAction(event -> {
            if (showAddBookingDialog()) {
                refresh.run();
            }
        });
        searchButton.setOnAction(event -> searchBooking());
        exportButton.setOnAction(event -> exportReport(stage, reportSelector.getValue()));
        refresh.run();

        VBox content = new VBox(metrics, controls, tableHost);
        VBox.setVgrow(tableHost, Priority.ALWAYS);
        return content;
    }

    private void loadDashboard(FlowPane metrics, ComboBox<String> reportSelector,
                                StackPane tableHost, Label status) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            Summary summary = summary(connection);
            metrics.getChildren().setAll(
                metricCard("BOOKINGS", String.valueOf(count(connection, "Fact_Flight_Booking"))),
                metricCard("PASSENGERS", String.valueOf(count(connection, "Dim_Passenger"))),
                metricCard("FLIGHTS", String.valueOf(count(connection, "Dim_Flight"))),
                metricCard("AIRPORTS", String.valueOf(count(connection, "Dim_Airport"))),
                metricCard("REVENUE", money(summary.totalRevenue())),
                metricCard("AVG FARE", money(summary.averageFare())),
                metricCard("TAXES", money(summary.totalTaxes())),
                metricCard("AVG BAGGAGE", String.format("%.1f kg", summary.averageBaggage()))
            );

            Report selectedReport = reports.stream()
                .filter(report -> report.name().equals(reportSelector.getValue()))
                .findFirst()
                .orElse(reports.get(0));
            TableView<ObservableList<String>> table = buildTable(connection, selectedReport.sql());
            tableHost.getChildren().setAll(table);
            status.setText("Showing " + table.getItems().size() + " rows - updated just now");
        } catch (SQLException exception) {
            Label error = new Label("Could not refresh the warehouse: " + exception.getMessage());
            error.setStyle("-fx-text-fill: #b42318; -fx-font-size: 14px;");
            tableHost.getChildren().setAll(error);
            status.setText("Refresh failed");
        }
    }

    private VBox metricCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #172033; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: #657189; -fx-font-size: 11px; -fx-font-weight: bold;");
        VBox card = new VBox(5, valueLabel, nameLabel);
        card.setPadding(new Insets(16, 24, 16, 24));
        card.setMinWidth(170);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e7f0; -fx-border-radius: 8;");
        return card;
    }

    private Summary summary(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COALESCE(SUM(total_amount), 0), COALESCE(AVG(ticket_fare), 0), COALESCE(SUM(taxes_and_fees), 0), COALESCE(AVG(baggage_weight_kg), 0) FROM Fact_Flight_Booking")) {
            result.next();
            return new Summary(result.getDouble(1), result.getDouble(2), result.getDouble(3), result.getDouble(4));
        }
    }

    private String money(double value) {
        return String.format("%.2f", value);
    }

    private void searchBooking() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search booking");
        dialog.setHeaderText("Find a booking by Booking ID");
        dialog.setContentText("Booking ID:");
        dialog.showAndWait().ifPresent(bookingId -> {
            if (bookingId.isBlank()) {
                return;
            }
            try (Connection connection = DriverManager.getConnection(DATABASE_URL);
                 var statement = connection.prepareStatement("""
                     SELECT fb.booking_id, p.passenger_name, f.flight_number, d.full_date,
                            fb.ticket_fare, fb.taxes_and_fees, fb.total_amount
                     FROM Fact_Flight_Booking fb
                     JOIN Dim_Passenger p ON p.passenger_sk = fb.passenger_sk
                     JOIN Dim_Flight f ON f.flight_sk = fb.flight_sk
                     JOIN Dim_Date d ON d.date_sk = fb.booking_date_sk
                     WHERE fb.booking_id = ?
                     """)) {
                statement.setString(1, bookingId.trim());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        showInfo("Booking found", "ID: " + result.getString(1) + "\nPassenger: " + result.getString(2)
                            + "\nFlight: " + result.getString(3) + "\nDate: " + result.getString(4)
                            + "\nTotal: " + money(result.getDouble(7)));
                    } else {
                        showError("Booking not found", "No booking exists with ID " + bookingId.trim() + ".");
                    }
                }
            } catch (SQLException exception) {
                showError("Search failed", exception.getMessage());
            }
        });
    }

    private void exportReport(Stage stage, String reportName) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Export report as CSV");
        chooser.setInitialFileName(reportName.toLowerCase().replace(' ', '_') + ".csv");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV files", "*.csv"));
        java.io.File selectedFile = chooser.showSaveDialog(stage);
        Path output = selectedFile == null ? null : selectedFile.toPath();
        if (output == null) {
            return;
        }
        Report report = reports.stream().filter(item -> item.name().equals(reportName)).findFirst().orElse(reports.get(0));
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(report.sql());
             BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            ResultSetMetaData metadata = result.getMetaData();
            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                if (index > 1) writer.write(",");
                writer.write(csv(metadata.getColumnLabel(index)));
            }
            writer.newLine();
            while (result.next()) {
                for (int index = 1; index <= metadata.getColumnCount(); index++) {
                    if (index > 1) writer.write(",");
                    writer.write(csv(result.getString(index)));
                }
                writer.newLine();
            }
            showInfo("Export complete", "Report saved to:\n" + output);
        } catch (SQLException | IOException exception) {
            showError("Export failed", exception.getMessage());
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private boolean showAddBookingDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add booking");
        dialog.setHeaderText("Add a booking to the warehouse fact table");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField bookingId = new TextField();
        bookingId.setPromptText("Example: B022");
        ComboBox<String> passenger = new ComboBox<>();
        ComboBox<String> flight = new ComboBox<>();
        DatePicker bookingDate = new DatePicker(LocalDate.now());
        TextField fare = new TextField();
        fare.setPromptText("Example: 65000");
        TextField fees = new TextField("0");
        TextField baggage = new TextField("0");

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement();
             ResultSet passengers = statement.executeQuery("SELECT DISTINCT passenger_id FROM Dim_Passenger WHERE is_current = 1 ORDER BY passenger_id")) {
            while (passengers.next()) {
                passenger.getItems().add(passengers.getString(1));
            }
            passenger.getSelectionModel().selectFirst();
        } catch (SQLException exception) {
            showError("Could not load passengers", exception.getMessage());
            return false;
        }

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement();
             ResultSet flights = statement.executeQuery("""
                 SELECT DISTINCT f.flight_id || ' - ' || f.flight_number
                 FROM Dim_Flight f JOIN Fact_Flight_Booking fb ON fb.flight_sk = f.flight_sk
                 ORDER BY f.flight_id
                 """)) {
            while (flights.next()) {
                flight.getItems().add(flights.getString(1));
            }
            flight.getSelectionModel().selectFirst();
        } catch (SQLException exception) {
            showError("Could not load flights", exception.getMessage());
            return false;
        }

        VBox form = new VBox(8,
            fieldLabel("Booking ID", bookingId),
            fieldLabel("Passenger", passenger),
            fieldLabel("Flight", flight),
            fieldLabel("Booking date", bookingDate),
            fieldLabel("Ticket fare", fare),
            fieldLabel("Taxes and fees", fees),
            fieldLabel("Baggage weight (kg)", baggage)
        );
        form.setPadding(new Insets(8, 0, 0, 0));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(390);

        while (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                insertBooking(bookingId.getText(), passenger.getValue(), flight.getValue(), bookingDate.getValue(),
                    Double.parseDouble(fare.getText()), Double.parseDouble(fees.getText()), Double.parseDouble(baggage.getText()));
                return true;
            } catch (NumberFormatException exception) {
                showError("Invalid amount", "Fare, fees, and baggage must be numbers.");
            } catch (IllegalArgumentException | SQLException exception) {
                showError("Booking was not added", exception.getMessage());
            }
        }
        return false;
    }

    private VBox fieldLabel(String label, javafx.scene.Node input) {
        Label caption = new Label(label);
        caption.setStyle("-fx-font-weight: bold; -fx-text-fill: #344054;");
        return new VBox(3, caption, input);
    }

    private void insertBooking(String bookingId, String passengerId, String flightOption, LocalDate date,
                               double fare, double fees, double baggage) throws SQLException {
        if (bookingId == null || bookingId.isBlank() || passengerId == null || flightOption == null || date == null) {
            throw new IllegalArgumentException("Booking ID, passenger, flight, and date are required.");
        }
        if (fare < 0 || fees < 0 || baggage < 0) {
            throw new IllegalArgumentException("Amounts cannot be negative.");
        }

        String flightId = flightOption.substring(0, flightOption.indexOf(" - "));
        int dateKey = date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            connection.setAutoCommit(false);
            try {
                int passengerSk = lookupKey(connection, "SELECT passenger_sk FROM Dim_Passenger WHERE passenger_id = ? AND is_current = 1", passengerId);
                FlightKeys flightKeys = lookupFlight(connection, flightId);
                try (var dateInsert = connection.prepareStatement("""
                    INSERT OR IGNORE INTO Dim_Date (date_sk, full_date, day, month, month_name, quarter, year)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
                    dateInsert.setInt(1, dateKey);
                    dateInsert.setString(2, date.toString());
                    dateInsert.setInt(3, date.getDayOfMonth());
                    dateInsert.setInt(4, date.getMonthValue());
                    dateInsert.setString(5, date.getMonth().name());
                    dateInsert.setInt(6, (date.getMonthValue() - 1) / 3 + 1);
                    dateInsert.setInt(7, date.getYear());
                    dateInsert.executeUpdate();
                }
                try (var insert = connection.prepareStatement("""
                    INSERT INTO Fact_Flight_Booking
                    (booking_id, passenger_sk, flight_sk, departure_airport_sk, arrival_airport_sk,
                     booking_date_sk, flight_date_sk, ticket_fare, taxes_and_fees, baggage_weight_kg,
                     distance_km, total_amount)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """)) {
                    insert.setString(1, bookingId.trim());
                    insert.setInt(2, passengerSk);
                    insert.setInt(3, flightKeys.flightSk());
                    insert.setInt(4, flightKeys.departureAirportSk());
                    insert.setInt(5, flightKeys.arrivalAirportSk());
                    insert.setInt(6, dateKey);
                    insert.setInt(7, flightKeys.flightDateSk());
                    insert.setDouble(8, fare);
                    insert.setDouble(9, fees);
                    insert.setDouble(10, baggage);
                    insert.setDouble(11, flightKeys.distanceKm());
                    insert.setDouble(12, fare + fees);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private int lookupKey(Connection connection, String sql, String value) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }
        throw new IllegalArgumentException("Passenger was not found.");
    }

    private FlightKeys lookupFlight(Connection connection, String flightId) throws SQLException {
        try (var statement = connection.prepareStatement("""
            SELECT f.flight_sk, f.distance_km, fb.departure_airport_sk,
                   fb.arrival_airport_sk, fb.flight_date_sk
            FROM Dim_Flight f JOIN Fact_Flight_Booking fb ON fb.flight_sk = f.flight_sk
            WHERE f.flight_id = ? LIMIT 1
            """)) {
            statement.setString(1, flightId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new FlightKeys(result.getInt(1), result.getDouble(2), result.getInt(3),
                        result.getInt(4), result.getInt(5));
                }
            }
        }
        throw new IllegalArgumentException("Flight was not found.");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private int count(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private TableView<ObservableList<String>> buildTable(Connection connection, String sql) throws SQLException {
        TableView<ObservableList<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            ResultSetMetaData metadata = result.getMetaData();
            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                final int columnIndex = index - 1;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(metadata.getColumnLabel(index).toUpperCase());
                column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(columnIndex)));
                table.getColumns().add(column);
            }
            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int index = 1; index <= metadata.getColumnCount(); index++) {
                    row.add(result.getString(index));
                }
                table.getItems().add(row);
            }
        }
        return table;
    }

    private record Report(String name, String sql) {
    }

    private record Summary(double totalRevenue, double averageFare, double totalTaxes, double averageBaggage) {
    }

    private record FlightKeys(int flightSk, double distanceKm, int departureAirportSk,
                              int arrivalAirportSk, int flightDateSk) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
