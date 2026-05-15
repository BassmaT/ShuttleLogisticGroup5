package com.group5.shuttle.controller;

import com.group5.shuttle.model.MaintenanceTicket;
import com.group5.shuttle.service.TicketStore;
import com.group5.shuttle.util.ColoredTableCell;
import com.group5.shuttle.util.StatusColors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

// Controller for the maintenance history.
// Displays all completed repairs of the current session as a table.
// The data is held in memory only and is deleted when the app is closed.
public class HistoryController extends BaseController {

    // Back button to the main dashboard.
    @FXML private Button btnBack;

    // The table with all completed repair tickets.
    @FXML private TableView<MaintenanceTicket> historyTable;

    // Column for the date and time of the repair.
    @FXML private TableColumn<MaintenanceTicket, String> colDate;

    // Column for the shuttle part, e.g. "Orbiter".
    @FXML private TableColumn<MaintenanceTicket, String> colPart;

    // Column for the sensor name that was repaired.
    @FXML private TableColumn<MaintenanceTicket, String> colSensor;

    // Column for the original fault status: "WARNING" or "REPLACE".
    @FXML private TableColumn<MaintenanceTicket, String> colOldStatus;

    // Column for the action taken including the part used.
    @FXML private TableColumn<MaintenanceTicket, String> colAction;

    // Column for the name of the technician who carried out the repair.
    @FXML private TableColumn<MaintenanceTicket, String> colTechnician;

    private final TicketStore ticketStore = TicketStore.getInstance();

    // Called automatically once the FXML file is fully loaded.
    @FXML
    public void initialize() {
        // Back button: navigates back to the main dashboard.
        setupBackButton(btnBack);

        // Set up the table and populate it with the current tickets.
        setupTable();
        loadHistory();
    }

    // Binds each column to the corresponding field in MaintenanceTicket
    // and defines the colored rendering of the status column.
    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPart.setCellValueFactory(new PropertyValueFactory<>("part"));
        colSensor.setCellValueFactory(new PropertyValueFactory<>("sensor"));
        colOldStatus.setCellValueFactory(new PropertyValueFactory<>("oldStatus"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTechnician.setCellValueFactory(new PropertyValueFactory<>("technician"));

        // Custom status cells: colored text depending on severity.
        colOldStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forSensorStatus));
    }

    // Loads all tickets of the current session from memory
    // and passes them to the table.
    private void loadHistory() {
        List<MaintenanceTicket> tickets = ticketStore.getTickets();
        historyTable.setItems(FXCollections.observableArrayList(tickets));
    }

    // Returns the back button – required by BaseController.loadView().
    @Override
    protected Button getNavigationButton() {
        return btnBack;
    }
}
