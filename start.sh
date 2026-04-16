#!/bin/bash
# Render start script
set -e

echo "Starting Auction System..."

# Let Render override profile, default to prod + postgres
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod,postgres}"

# Resolve the packaged application jar dynamically
JAR_FILE=$(find target -maxdepth 1 -name "*.jar" ! -name "*original*" | head -n 1)
if [ -z "$JAR_FILE" ]; then
	echo "No runnable jar found under target/."
	exit 1
fi

# Start the application
java ${JAVA_OPTS:-} -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" -Dserver.port="${PORT:-8080}" -jar "$JAR_FILE"
