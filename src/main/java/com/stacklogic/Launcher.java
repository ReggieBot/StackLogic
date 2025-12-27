package com.stacklogic;

/**
 * Launcher class for StackLogic.
 *
 * WHY DO WE NEED THIS?
 * --------------------
 * This seems redundant, but it's required for JavaFX applications
 * packaged as a "fat JAR" (a single JAR with all dependencies).
 *
 * The issue: When JavaFX checks if it's on the module path, it fails
 * if the main class extends Application. This non-Application launcher
 * class works around that issue.
 *
 * The shade plugin in pom.xml points to this class, which then calls
 * the real App.main().
 *
 * This is one of those "just do it this way" quirks of JavaFX.
 * Don't worry too much about understanding why - just know that
 * if you ever have trouble running a JavaFX JAR, this is the fix.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
