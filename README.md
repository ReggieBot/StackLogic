# StackLogic - Poker Dashboard

A desktop poker training and session tracking tool for grinding online microstakes poker.

## Features (Planned)
- **Preflop Range Viewer** - 13x13 grid with color-coded hands by action
- **Pot Odds Calculator** - Calculate pot odds and EV for calling decisions
- **Session Tracker** - Log poker sessions with profit/loss tracking
- **Bankroll Tracker** - Track deposits, withdrawals, and recommended stakes
- **Charts & Analytics** - Visualize your results over time
- **Quick Notes** - Hand journal for recording plays and leaks

## Prerequisites

### Installing Java 17 (WSL/Ubuntu)

Open your WSL terminal and run:

```bash
# Update package list
sudo apt update

# Install OpenJDK 17
sudo apt install openjdk-17-jdk

# Verify installation
java --version
```

You should see output like: `openjdk 17.0.x ...`

### Installing Maven (WSL/Ubuntu)

```bash
# Install Maven
sudo apt install maven

# Verify installation
mvn --version
```

You should see Maven version info with Java 17 listed.

## Running the Application

### Using Maven (Development)

```bash
# Navigate to project directory
cd ~/StackLogic

# Compile and run
mvn clean javafx:run
```

### Building a JAR (Distribution)

```bash
# Create the packaged JAR
mvn clean package

# Run the JAR
java -jar target/stacklogic-1.0-SNAPSHOT.jar
```

## Project Structure

```
StackLogic/
├── pom.xml                          # Maven configuration
├── src/main/
│   ├── java/com/stacklogic/
│   │   ├── App.java                 # Main application entry
│   │   ├── Launcher.java            # JAR launcher helper
│   │   ├── controller/              # FXML controllers
│   │   ├── model/                   # Data classes
│   │   ├── util/                    # Helper utilities
│   │   └── view/                    # View-related code
│   └── resources/
│       ├── fxml/                    # UI layouts
│       ├── css/                     # Stylesheets
│       ├── ranges/                  # Poker range JSON files
│       └── images/                  # Icons and images
└── src/test/java/                   # Unit tests
```

## Tech Stack

- **Java 17** - Programming language
- **JavaFX 21** - Desktop UI framework
- **SQLite** - Embedded database for session data
- **Gson** - JSON parsing for range files
- **Maven** - Build tool and dependency management

## Development Notes

This project follows the **MVC (Model-View-Controller)** pattern:
- **Model**: Data classes in `model/` package
- **View**: FXML files in `resources/fxml/`
- **Controller**: Controller classes in `controller/` package

### Useful Maven Commands

| Command | Description |
|---------|-------------|
| `mvn clean` | Delete compiled files |
| `mvn compile` | Compile source code |
| `mvn test` | Run unit tests |
| `mvn package` | Create JAR file |
| `mvn javafx:run` | Run the app |
| `mvn clean javafx:run` | Clean, compile, and run |
