@echo off
:: Check for admin permissions
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo This script requires administrator privileges.
    echo Please run this script as administrator.
    pause
    exit /b 1
)

:: Create a temporary directory
mkdir temp 2>nul

:: Download and extract required files
echo Downloading configuration files...
powershell -Command "& {Invoke-WebRequest -Uri 'https://github.com/Swofty-Developments/HypixelRecreation/archive/refs/heads/master.zip' -OutFile 'temp\master.zip'}"

:: Extract the zip file
echo Extracting files...
powershell -Command "& {Expand-Archive -Path 'temp\master.zip' -DestinationPath 'temp' -Force}"

:: Remove existing folders if they exist
echo Removing existing configuration folders...
if exist "items" rmdir /s /q "items"
if exist "collections" rmdir /s /q "collections"

:: Copy new configuration folders
echo Copying new configuration files...
xcopy "temp\HypixelRecreation-master\configuration\skyblock\items" "items\" /e /i /y
xcopy "temp\HypixelRecreation-master\configuration\skyblock\collections" "collections\" /e /i /y

:: Clean up temporary files
echo Cleaning up...
rmdir /s /q temp

echo Configuration update complete!
pause