@echo off
TITLE WarrantyWise - 1-Click Server Launcher
COLOR 0A
CLS

echo =========================================================================
echo               WARRANTYWISE - PRODUCT WARRANTY AND RENEWAL PLATFORM
echo                           1-Click Server Launcher
echo =========================================================================
echo.

echo [1/4] Checking Java environment...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH! Please install Java 21.
    pause
    exit /b 1
)
echo       Java runtime detected.

echo.
echo [2/4] Checking Maven build tool...
call mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH!
    pause
    exit /b 1
)
echo       Maven build tool detected.

echo.
echo [3/4] Starting WarrantyWise Spring Boot Server on port 8080...
echo       (Logs will run in background. Press Ctrl+C in server window to stop manually)

start "WarrantyWise Backend Server" cmd /k "mvn spring-boot:run"

echo.
echo [4/4] Waiting for backend server to become ready on http://localhost:8080...
set MAX_WAIT=30
set COUNT=0

:WAIT_LOOP
powershell -Command "$r = Invoke-WebRequest -Uri 'http://localhost:8080/pages/login.html' -UseBasicParsing -ErrorAction SilentlyContinue; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 }" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo.
    echo =========================================================================
    echo [SUCCESS] WarrantyWise Server is LIVE on http://localhost:8080!
    echo [ACTION] Launching default web browser...
    echo =========================================================================
    start http://localhost:8080/pages/login.html
    echo.
    echo To stop the application anytime, double-click stop.bat
    echo.
    timeout /t 5 >nul
    exit /b 0
)

set /a COUNT+=1
if %COUNT% GEQ %MAX_WAIT% (
    echo.
    echo [NOTICE] Server is taking a bit longer to start. Opening browser now...
    start http://localhost:8080/pages/login.html
    exit /b 0
)

echo       Waiting for port 8080... (%COUNT%/%MAX_WAIT%s)
timeout /t 2 >nul
goto WAIT_LOOP
