# Dockerfile for Render deployment
FROM maven:3.8.6-openjdk-11 as build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/auction-system-1.0-SNAPSHOT.jar app.jar

# Expose port
EXPOSE $PORT

# Run application
CMD ["java", "-Dspring.profiles.active=prod", "-Dserver.port=${PORT}", "-jar", "app.jar"]
