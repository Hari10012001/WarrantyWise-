@echo off
TITLE WarrantyWise - 1-Click Server Shutdown
COLOR 0C
CLS

echo =========================================================================
echo               WARRANTYWISE - PRODUCT WARRANTY AND RENEWAL PLATFORM
echo                           1-Click Server Shutdown
echo =========================================================================
echo.

echo [1/2] Searching for active process on port 8080...
powershell -Command "$pids = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; if ($pids) { foreach ($pidNum in $pids) { Write-Host 'Stopping process ID:' $pidNum; Stop-Process -Id $pidNum -Force -ErrorAction SilentlyContinue } exit 0 } else { Write-Host 'No active process found on port 8080.'; exit 1 }"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =========================================================================
    echo [SUCCESS] WarrantyWise Server has been STOPPED cleanly.
    echo =========================================================================
) else (
    echo.
    echo [INFO] WarrantyWise Server was not currently running on port 8080.
)

echo.
timeout /t 4 >nul
exit /b 0
