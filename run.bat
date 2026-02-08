@echo off
REM AI Content Detector - Build and Run Script for Windows

echo ========================================
echo AI Content Detector - Build and Run
echo ========================================

REM Check Java version
echo.
echo Checking Java version...
java -version 2>&1 | findstr /C:"version"

REM Clean and build
echo.
echo Building project...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed
    pause
    exit /b 1
)

echo Build successful

REM Run tests if --test flag provided
if "%1"=="--test" (
    echo.
    echo Running tests...
    call mvn test
)

REM Start server
echo.
echo Starting server...
echo Server will be available at http://localhost:8080
echo Browser will open automatically
echo.
echo Press Ctrl+C to stop the server
echo.

call mvn exec:java -Dexec.mainClass="com.detector.server.MiniServer"
