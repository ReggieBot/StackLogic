package com.stacklogic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for StackLogic.
 *
 * KEY CONCEPT: JavaFX Application Lifecycle
 * -----------------------------------------
 * Every JavaFX app extends Application and overrides the start() method.
 * The lifecycle is:
 *   1. Constructor is called
 *   2. init() is called (optional, we don't override it here)
 *   3. start() is called - this is where you set up your UI
 *   4. The app runs until the window is closed
 *   5. stop() is called (optional cleanup)
 *
 * KEY TERMS:
 * - Stage: The window itself (like JFrame in Swing)
 * - Scene: The content inside the window
 * - Parent/Node: UI elements (buttons, labels, containers, etc.)
 */
public class App extends Application {

    // We store the scene so we can access it later to switch views
    private static Scene scene;

    /**
     * This is where your app starts.
     * We load the main layout from an FXML file and display it.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Load the main view from FXML
        // FXMLLoader reads the XML file and creates Java objects from it
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));

        // Create a scene with the loaded content
        // 1000x700 is the initial window size in pixels
        scene = new Scene(root, 1000, 700);

        // Load our CSS stylesheet for custom styling
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Configure the window (Stage)
        stage.setTitle("StackLogic - Poker Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(800);   // Minimum window size
        stage.setMinHeight(600);
        stage.show();  // Display the window
    }

    /**
     * Helper method to change the root content of the scene.
     * Useful when switching between different views (screens).
     *
     * @param fxml The name of the FXML file (without path or extension)
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Helper method to load an FXML file and return the root element.
     *
     * @param fxml The name of the FXML file (without path or extension)
     * @return The root Parent node from the FXML
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return loader.load();
    }

    /**
     * The main() method is the entry point.
     * We just call launch() which starts the JavaFX runtime.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
