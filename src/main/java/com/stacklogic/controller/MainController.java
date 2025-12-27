package com.stacklogic.controller;

import com.stacklogic.App;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main application view.
 *
 * Handles:
 * - Custom title bar drag functionality
 * - Window minimize/maximize/close buttons
 * - Window resizing for undecorated window
 * - Tab navigation and status updates
 */
public class MainController implements Initializable {

    // Root pane for resize detection
    @FXML private VBox rootPane;

    // Custom title bar for window dragging
    @FXML private HBox titleBar;
    @FXML private Button minimizeBtn;
    @FXML private Button maximizeBtn;
    @FXML private Button closeBtn;

    // Main content
    @FXML private TabPane mainTabPane;
    @FXML private Label statusLabel;
    @FXML private Label versionLabel;

    // For smooth window dragging (gap-based approach)
    private double gapX = 0;
    private double gapY = 0;
    private boolean isMaximized = false;

    // For window resizing
    private static final int RESIZE_MARGIN = 6;
    private boolean isResizing = false;
    private ResizeDirection resizeDirection = ResizeDirection.NONE;
    private double resizeStartX, resizeStartY;
    private double resizeStartStageX, resizeStartStageY;
    private double resizeStartWidth, resizeStartHeight;

    // Store window state before maximize
    private double prevX, prevY, prevWidth, prevHeight;

    private enum ResizeDirection {
        NONE, N, S, E, W, NE, NW, SE, SW
    }

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

        // Set up resize handlers on root pane
        Platform.runLater(this::setupResizeHandlers);
    }

    /**
     * Set up mouse handlers for window resizing.
     */
    private void setupResizeHandlers() {
        if (rootPane == null || rootPane.getScene() == null) return;

        rootPane.getScene().setOnMouseMoved(this::handleMouseMoved);
        rootPane.getScene().setOnMousePressed(this::handleMousePressed);
        rootPane.getScene().setOnMouseDragged(this::handleMouseDragged);
        rootPane.getScene().setOnMouseReleased(this::handleMouseReleased);
    }

    /**
     * Update cursor based on mouse position for resize indication.
     */
    private void handleMouseMoved(MouseEvent event) {
        if (isMaximized) {
            rootPane.getScene().setCursor(Cursor.DEFAULT);
            return;
        }

        ResizeDirection dir = getResizeDirection(event);
        Cursor cursor = getCursorForDirection(dir);
        rootPane.getScene().setCursor(cursor);
    }

    /**
     * Handle mouse press for resizing.
     */
    private void handleMousePressed(MouseEvent event) {
        Stage stage = App.getPrimaryStage();
        if (stage == null) return;

        resizeDirection = getResizeDirection(event);

        if (resizeDirection != ResizeDirection.NONE && !isMaximized) {
            isResizing = true;
            resizeStartX = event.getScreenX();
            resizeStartY = event.getScreenY();
            resizeStartStageX = stage.getX();
            resizeStartStageY = stage.getY();
            resizeStartWidth = stage.getWidth();
            resizeStartHeight = stage.getHeight();
        }
    }

    /**
     * Handle mouse drag for resizing.
     */
    private void handleMouseDragged(MouseEvent event) {
        if (!isResizing || isMaximized) return;

        Stage stage = App.getPrimaryStage();
        if (stage == null) return;

        double deltaX = event.getScreenX() - resizeStartX;
        double deltaY = event.getScreenY() - resizeStartY;

        double newX = resizeStartStageX;
        double newY = resizeStartStageY;
        double newWidth = resizeStartWidth;
        double newHeight = resizeStartHeight;

        switch (resizeDirection) {
            case N:
                newY = resizeStartStageY + deltaY;
                newHeight = resizeStartHeight - deltaY;
                break;
            case S:
                newHeight = resizeStartHeight + deltaY;
                break;
            case E:
                newWidth = resizeStartWidth + deltaX;
                break;
            case W:
                newX = resizeStartStageX + deltaX;
                newWidth = resizeStartWidth - deltaX;
                break;
            case NE:
                newY = resizeStartStageY + deltaY;
                newHeight = resizeStartHeight - deltaY;
                newWidth = resizeStartWidth + deltaX;
                break;
            case NW:
                newX = resizeStartStageX + deltaX;
                newY = resizeStartStageY + deltaY;
                newWidth = resizeStartWidth - deltaX;
                newHeight = resizeStartHeight - deltaY;
                break;
            case SE:
                newWidth = resizeStartWidth + deltaX;
                newHeight = resizeStartHeight + deltaY;
                break;
            case SW:
                newX = resizeStartStageX + deltaX;
                newWidth = resizeStartWidth - deltaX;
                newHeight = resizeStartHeight + deltaY;
                break;
            default:
                break;
        }

        // Apply minimum size constraints
        if (newWidth >= stage.getMinWidth()) {
            stage.setX(newX);
            stage.setWidth(newWidth);
        }
        if (newHeight >= stage.getMinHeight()) {
            stage.setY(newY);
            stage.setHeight(newHeight);
        }
    }

    /**
     * Handle mouse release to stop resizing.
     */
    private void handleMouseReleased(MouseEvent event) {
        isResizing = false;
    }

    /**
     * Determine resize direction based on mouse position.
     */
    private ResizeDirection getResizeDirection(MouseEvent event) {
        double x = event.getSceneX();
        double y = event.getSceneY();
        double width = rootPane.getWidth();
        double height = rootPane.getHeight();

        boolean onLeft = x < RESIZE_MARGIN;
        boolean onRight = x > width - RESIZE_MARGIN;
        boolean onTop = y < RESIZE_MARGIN;
        boolean onBottom = y > height - RESIZE_MARGIN;

        if (onTop && onLeft) return ResizeDirection.NW;
        if (onTop && onRight) return ResizeDirection.NE;
        if (onBottom && onLeft) return ResizeDirection.SW;
        if (onBottom && onRight) return ResizeDirection.SE;
        if (onTop) return ResizeDirection.N;
        if (onBottom) return ResizeDirection.S;
        if (onLeft) return ResizeDirection.W;
        if (onRight) return ResizeDirection.E;

        return ResizeDirection.NONE;
    }

    /**
     * Get the appropriate cursor for the resize direction.
     */
    private Cursor getCursorForDirection(ResizeDirection dir) {
        switch (dir) {
            case N: case S: return Cursor.N_RESIZE;
            case E: case W: return Cursor.E_RESIZE;
            case NE: case SW: return Cursor.NE_RESIZE;
            case NW: case SE: return Cursor.NW_RESIZE;
            default: return Cursor.DEFAULT;
        }
    }

    // ========================================
    // TITLE BAR DRAG HANDLERS (Gap-based smooth dragging)
    // ========================================

    /**
     * Calculate the gap between mouse and stage position.
     * This enables smooth dragging without the window jumping.
     */
    @FXML
    private void handleTitleBarPressed(MouseEvent event) {
        if (isMaximized) return;

        Stage stage = App.getPrimaryStage();
        if (stage != null) {
            gapX = event.getScreenX() - stage.getX();
            gapY = event.getScreenY() - stage.getY();
        }
    }

    /**
     * Move the window smoothly using the calculated gap.
     */
    @FXML
    private void handleTitleBarDragged(MouseEvent event) {
        if (isMaximized) return;

        Stage stage = App.getPrimaryStage();
        if (stage != null) {
            stage.setX(event.getScreenX() - gapX);
            stage.setY(event.getScreenY() - gapY);
        }
    }

    /**
     * Ensure window doesn't go past top of screen.
     */
    @FXML
    private void handleTitleBarReleased(MouseEvent event) {
        Stage stage = App.getPrimaryStage();
        if (stage != null && stage.getY() < 0) {
            stage.setY(0);
        }
    }

    // ========================================
    // WINDOW CONTROL HANDLERS
    // ========================================

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
