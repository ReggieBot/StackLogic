package com.stacklogic.controller;

import com.stacklogic.App;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main application view.
 *
 * Handles:
 * - Custom title bar drag functionality
 * - Window minimize/maximize/close buttons
 * - Tab navigation and status updates
 */
public class MainController implements Initializable {

    // Custom title bar for window dragging
    @FXML private HBox titleBar;
    @FXML private Button minimizeBtn;
    @FXML private Button maximizeBtn;
    @FXML private Button closeBtn;

    // Main content
    @FXML private TabPane mainTabPane;
    @FXML private Label statusLabel;
    @FXML private Label versionLabel;

    // For window dragging
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;

    // Store window state before maximize
    private double prevX, prevY, prevWidth, prevHeight;

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

        // Double-click title bar to maximize/restore
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleMaximize();
            }
        });
    }

    // ========================================
    // WINDOW CONTROL HANDLERS
    // ========================================

    /**
     * Store the mouse position when title bar is pressed.
     * This is needed for smooth window dragging.
     */
    @FXML
    private void handleTitleBarPressed(MouseEvent event) {
        Stage stage = App.getPrimaryStage();
        if (stage != null && !isMaximized) {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        }
    }

    /**
     * Move the window as the mouse drags the title bar.
     */
    @FXML
    private void handleTitleBarDragged(MouseEvent event) {
        Stage stage = App.getPrimaryStage();
        if (stage != null && !isMaximized) {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        }
    }

    /**
     * Minimize the window to taskbar.
     */
    @FXML
    private void handleMinimize() {
        Stage stage = App.getPrimaryStage();
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    /**
     * Toggle between maximized and normal window state.
     */
    @FXML
    private void handleMaximize() {
        Stage stage = App.getPrimaryStage();
        if (stage == null) return;

        if (isMaximized) {
            // Restore to previous size
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            maximizeBtn.setText("□");
            isMaximized = false;
        } else {
            // Store current size
            prevX = stage.getX();
            prevY = stage.getY();
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();

            // Maximize to screen (accounting for taskbar)
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            maximizeBtn.setText("❐");
            isMaximized = true;
        }
    }

    /**
     * Close the application.
     */
    @FXML
    private void handleClose() {
        Platform.exit();
        System.exit(0);
    }
}
