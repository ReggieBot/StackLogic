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
 * WHAT IS A CONTROLLER?
 * =====================
 * The controller is the "brain" that connects your UI (View) to your data (Model).
 * When a user clicks a button or types in a field, the controller handles it.
 *
 * HOW FXML CONNECTS TO CONTROLLERS:
 * ---------------------------------
 * 1. FXML file specifies: fx:controller="com.stacklogic.controller.MainController"
 * 2. FXML elements with fx:id="something" can be accessed in Java with @FXML annotation
 * 3. Methods referenced in FXML with #methodName must have @FXML annotation
 *
 * EXAMPLE:
 *   FXML: <Button fx:id="myButton" onAction="#handleClick"/>
 *   Java: @FXML private Button myButton;
 *         @FXML private void handleClick() { ... }
 */
public class MainController implements Initializable {

    /*
     * @FXML annotation marks fields/methods that FXML can access.
     * The field name must match the fx:id in the FXML file.
     *
     * These are injected (filled in) automatically when the FXML loads.
     * Before initialize() runs, these will be null!
     */

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Label statusLabel;

    /**
     * Called automatically after FXML is loaded.
     *
     * This is where you do initial setup:
     * - Load data from database
     * - Set up listeners
     * - Initialize default values
     *
     * @param url The location of the FXML file (usually not needed)
     * @param resources Resource bundle for internationalization (usually not needed)
     */
    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Log that we loaded successfully (you'll see this in the console)
        System.out.println("StackLogic initialized successfully!");

        // Update status bar to show we're ready
        statusLabel.setText("Welcome to StackLogic! Select a tab to get started.");

        // Set up a listener to update status when tab changes
        // This demonstrates how to react to user actions
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldTab, newTab) -> {
                if (newTab != null) {
                    statusLabel.setText("Viewing: " + newTab.getText());
                }
            }
        );
    }

    /**
     * Example of an event handler method.
     * If you had a button in FXML with onAction="#handleExampleAction",
     * this method would be called when clicked.
     */
    @FXML
    private void handleExampleAction() {
        System.out.println("Button clicked!");
    }
}
