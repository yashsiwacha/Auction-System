#!/bin/bash
# Render build script
set -e

echo "Starting build process..."

# Prefer project wrapper when available for repeatable builds
MVN_CMD="mvn"
if [ -x "./mvnw" ]; then
    MVN_CMD="./mvnw"
fi

# Build the application
echo "Building Spring Boot application..."
$MVN_CMD clean package -DskipTests

echo "Build completed successfully!"
