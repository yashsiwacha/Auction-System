#!/bin/bash
# Render build script
echo "Starting build process..."

# Install Maven if not present
if ! command -v mvn &> /dev/null; then
    echo "Installing Maven..."
    curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz | tar xz
    export PATH=$PWD/apache-maven-3.8.6/bin:$PATH
fi

# Build the application
echo "Building Spring Boot application..."
mvn clean package -DskipTests

echo "Build completed successfully!"
