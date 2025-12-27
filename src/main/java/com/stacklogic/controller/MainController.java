package com.stacklogic.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main application view.
 *
 * Handles:
 * - Tab navigation and status updates
 */
public class MainController implements Initializable {

    @FXML private TabPane mainTabPane;
    @FXML private Label statusLabel;
    @FXML private Label versionLabel;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Update status bar
        statusLabel.setText("Welcome to StackLogic! Select a tab to get started.");

        // Tab change listener
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldTab, newTab) -> {
                if (newTab != null) {
                    statusLabel.setText("Viewing: " + newTab.getText());
                }
            }
        );
    }
}
