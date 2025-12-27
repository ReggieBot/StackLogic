# Building StackLogic Windows Executable

## Prerequisites
1. Install JDK 17+ on Windows (not WSL)
   - Download from: https://adoptium.net/temurin/releases/
   - Or use: https://www.oracle.com/java/technologies/downloads/

2. Add Java to your Windows PATH

## Steps to Create EXE

### Option 1: Using jpackage (Recommended)

Open Windows Command Prompt (not WSL) and run:

```cmd
cd C:\path\to\StackLogic

rem First build the JAR (can do this in WSL)
rem The JAR is at: target\stacklogic-1.0-SNAPSHOT.jar

rem Create the EXE
jpackage --type app-image ^
  --name StackLogic ^
  --input target ^
  --main-jar stacklogic-1.0-SNAPSHOT.jar ^
  --main-class com.stacklogic.Launcher ^
  --dest dist ^
  --icon path\to\your\poker-icon.ico ^
  --app-version 1.0 ^
  --vendor "StackLogic" ^
  --description "Poker Training Application"
```

### Getting a Poker Icon

For a poker-themed icon (not red spade), you can:
1. Search for "poker chip icon ico" or "playing cards icon ico"
2. Use free icon sites like:
   - https://icon-icons.com/icon/poker-chip/117628
   - https://www.flaticon.com/search?word=poker+chip
3. Convert PNG to ICO at: https://convertio.co/png-ico/

Good icon ideas (not red spade):
- Black/green poker chip
- Club symbol (black clover)
- Playing cards fanned out
- Diamond symbol (if not red)
- Stack of chips

### Option 2: Simple Batch Launcher

If jpackage isn't available, create a `StackLogic.bat` file:

```batch
@echo off
java -jar "%~dp0target\stacklogic-1.0-SNAPSHOT.jar"
```

## Running the App

After building:
- The EXE will be in the `dist/StackLogic` folder
- Run `StackLogic.exe` to launch the app

## Notes

- The shaded JAR (22MB) includes all dependencies
- jpackage bundles a Java runtime, making the final app ~150-200MB
- Users won't need Java installed if you use jpackage
