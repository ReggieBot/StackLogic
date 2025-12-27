package com.stacklogic.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and initialization.
 *
 * WHAT IS SQLITE?
 * ===============
 * SQLite is a "file-based" database - the entire database lives in a single file.
 * No server needed! This makes it perfect for desktop apps.
 *
 * The database file (stacklogic.db) is created automatically in your project folder.
 *
 * HOW JDBC WORKS:
 * ===============
 * JDBC (Java Database Connectivity) is a standard API for connecting to databases.
 * The basic flow is:
 *   1. Get a Connection to the database
 *   2. Create a Statement to run SQL
 *   3. Execute SQL queries (SELECT, INSERT, UPDATE, DELETE)
 *   4. Process results
 *   5. Close the connection when done
 *
 * SQL BASICS:
 * ===========
 * SQL (Structured Query Language) is how you talk to databases.
 *   - CREATE TABLE: Define a new table structure
 *   - INSERT: Add new rows
 *   - SELECT: Read data
 *   - UPDATE: Modify existing rows
 *   - DELETE: Remove rows
 */
public class DatabaseManager {

    // Database file location - stored in user's home directory
    private static final String DB_PATH = System.getProperty("user.home") + "/stacklogic.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    // Singleton connection (one shared connection for the app)
    private static Connection connection = null;

    /**
     * Get the database connection, creating it if necessary.
     * Also initializes the database tables on first connection.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to database: " + DB_PATH);
            initializeTables();
        }
        return connection;
    }

    /**
     * Create database tables if they don't exist.
     *
     * SQL TABLE DESIGN:
     * =================
     * Each table has columns with types:
     *   - INTEGER: Whole numbers
     *   - REAL: Decimal numbers (like double)
     *   - TEXT: Strings
     *   - BLOB: Binary data
     *
     * PRIMARY KEY: Unique identifier for each row
     * AUTOINCREMENT: Database automatically assigns IDs
     * NOT NULL: Column cannot be empty
     * DEFAULT: Value to use if none provided
     */
    private static void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // Sessions table - stores poker session data
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    start_time TEXT,
                    end_time TEXT,
                    duration_minutes INTEGER,
                    stakes TEXT NOT NULL,
                    table_size TEXT NOT NULL,
                    hands_played INTEGER,
                    buy_in REAL NOT NULL,
                    cash_out REAL NOT NULL,
                    profit REAL NOT NULL,
                    bb_won REAL,
                    tags TEXT,
                    notes TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Bankroll transactions table - deposits/withdrawals
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bankroll_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    notes TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Hand notes table - for the notes feature
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS hand_notes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    tag TEXT,
                    note_text TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            System.out.println("Database tables initialized");
        }
    }

    /**
     * Close the database connection.
     * Call this when the app shuts down.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the path to the database file.
     */
    public static String getDatabasePath() {
        return DB_PATH;
    }
}
