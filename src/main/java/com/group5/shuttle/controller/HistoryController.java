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

// Controller für die Wartungshistorie.
// Zeigt alle durchgeführten Reparaturen der aktuellen Sitzung als Tabelle an.
// Die Daten werden nur im Arbeitsspeicher gehalten und beim Schließen der App gelöscht.
public class HistoryController extends BaseController {

    // Zurück-Button zum Haupt-Dashboard.
    @FXML private Button btnBack;

    // Die Tabelle mit allen abgeschlossenen Reparaturtickets.
    @FXML private TableView<MaintenanceTicket> historyTable;

    // Spalte für Datum und Uhrzeit der Reparatur.
    @FXML private TableColumn<MaintenanceTicket, String> colDate;

    // Spalte für den Shuttle-Teil, z. B. "Orbiter".
    @FXML private TableColumn<MaintenanceTicket, String> colPart;

    // Spalte für den Sensornamen, der repariert wurde.
    @FXML private TableColumn<MaintenanceTicket, String> colSensor;

    // Spalte für den ursprünglichen Fehlerstatus: "WARNING" oder "REPLACE".
    @FXML private TableColumn<MaintenanceTicket, String> colOldStatus;

    // Spalte für die durchgeführte Maßnahme inkl. verwendetem Bauteil.
    @FXML private TableColumn<MaintenanceTicket, String> colAction;

    // Spalte für den Namen des Technikers, der die Reparatur durchgeführt hat.
    @FXML private TableColumn<MaintenanceTicket, String> colTechnician;

    private final TicketStore ticketStore = TicketStore.getInstance();

    // Wird automatisch aufgerufen, sobald die FXML-Datei vollständig geladen ist.
    @FXML
    public void initialize() {
        // Zurück-Button: navigiert zurück zum Haupt-Dashboard.
        setupBackButton(btnBack);

        // Tabelle einrichten und mit den aktuellen Tickets befüllen.
        setupTable();
        loadHistory();
    }

    // Bindet jede Spalte an das entsprechende Feld in MaintenanceTicket
    // und definiert die farbige Darstellung der Status-Spalte.
    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPart.setCellValueFactory(new PropertyValueFactory<>("part"));
        colSensor.setCellValueFactory(new PropertyValueFactory<>("sensor"));
        colOldStatus.setCellValueFactory(new PropertyValueFactory<>("oldStatus"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTechnician.setCellValueFactory(new PropertyValueFactory<>("technician"));

        // Benutzerdefinierte Status-Zellen: farbige Texte je nach Schweregrad.
        colOldStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forSensorStatus));
    }
//KI-Generiert Ende 

    // Lädt alle Tickets der aktuellen Sitzung aus dem Arbeitsspeicher
    // und gibt sie an die Tabelle weiter.
    private void loadHistory() {
        List<MaintenanceTicket> tickets = ticketStore.getTickets();
        historyTable.setItems(FXCollections.observableArrayList(tickets));
    }

    // Gibt den Zurück-Button zurück – wird von BaseController.loadView() benötigt.
    @Override
    protected Button getNavigationButton() {
        return btnBack;
    }
}
