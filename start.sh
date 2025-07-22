#!/bin/bash
# Render start script
echo "Starting Auction System..."

# Set production profile
export SPRING_PROFILES_ACTIVE=prod

# Start the application
java -jar target/auction-system-1.0-SNAPSHOT.jar
