package com.ccs3307.airline;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class App extends Application {
    @Override
    public void start(Stage stage) {
        Label title = new Label("Airline Flight Booking");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField passengerName = new TextField();
        passengerName.setPromptText("Passenger name");

        ComboBox<String> origin = new ComboBox<>();
        origin.getItems().addAll("CMB", "DXB", "LHR", "SIN");
        origin.setPromptText("Origin");

        ComboBox<String> destination = new ComboBox<>();
        destination.getItems().addAll("CMB", "DXB", "LHR", "SIN");
        destination.setPromptText("Destination");

        DatePicker travelDate = new DatePicker();
        travelDate.setPromptText("Travel date");

        Label status = new Label();
        Button searchButton = new Button("Search flights");
        searchButton.setOnAction(event -> status.setText(
                "Search requested for " + passengerName.getText().trim()
                        + " from " + origin.getValue()
                        + " to " + destination.getValue()));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Passenger"), 0, 0);
        form.add(passengerName, 1, 0);
        form.add(new Label("From"), 0, 1);
        form.add(origin, 1, 1);
        form.add(new Label("To"), 0, 2);
        form.add(destination, 1, 2);
        form.add(new Label("Date"), 0, 3);
        form.add(travelDate, 1, 3);
        form.add(searchButton, 1, 4);

        VBox root = new VBox(16, title, form, status);
        root.setPadding(new Insets(24));

        stage.setTitle("CCS3307 Airline Booking");
        stage.setScene(new Scene(root, 520, 330));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
