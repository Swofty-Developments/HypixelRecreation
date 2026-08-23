#!/bin/bash

# Check if script is run with sudo
if [ "$EUID" -ne 0 ]; then 
    echo "Please run with sudo"
    exit 1
fi

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check for required commands and install if missing
echo "Checking dependencies..."
PACKAGES_TO_INSTALL=""

if ! command_exists wget; then
    PACKAGES_TO_INSTALL="$PACKAGES_TO_INSTALL wget"
fi

if ! command_exists unzip; then
    PACKAGES_TO_INSTALL="$PACKAGES_TO_INSTALL unzip"
fi

if [ ! -z "$PACKAGES_TO_INSTALL" ]; then
    echo "Installing required packages: $PACKAGES_TO_INSTALL"
    apt-get update
    apt-get install -y $PACKAGES_TO_INSTALL
    
    if [ $? -ne 0 ]; then
        echo "Failed to install required packages"
        exit 1
    fi
fi

# Create temporary directory
TEMP_DIR=$(mktemp -d)
echo "Created temporary directory: $TEMP_DIR"

# Download repository
echo "Downloading configuration files..."
wget -q https://github.com/Swofty-Developments/HypixelRecreation/archive/refs/heads/master.zip -O "$TEMP_DIR/master.zip"

# Check if download was successful
if [ $? -ne 0 ]; then
    echo "Failed to download repository"
    rm -rf "$TEMP_DIR"
    exit 1
fi

# Unzip the repository
echo "Extracting files..."
unzip -q "$TEMP_DIR/master.zip" -d "$TEMP_DIR"

# Remove existing folders if they exist
echo "Removing existing configuration folders..."
rm -rf ./items
rm -rf ./collections

# Copy new configuration folders
echo "Copying new configuration files..."
cp -r "$TEMP_DIR/HypixelRecreation-master/configuration/skyblock/items" .
cp -r "$TEMP_DIR/HypixelRecreation-master/configuration/skyblock/collections" .

# Fix permissions to make files accessible to the original user
SUDO_USER=$(logname)
chown -R $SUDO_USER:$SUDO_USER ./items
chown -R $SUDO_USER:$SUDO_USER ./collections

# Cleanup
echo "Cleaning up..."
rm -rf "$TEMP_DIR"

echo "Configuration update complete!"