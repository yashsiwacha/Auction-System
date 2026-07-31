# Multi-stage Dockerfile for Enterprise Distributed Auction System (Java 21)

# Stage 1: Build stage with Maven and Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies for caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production JRE 21 Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create unprivileged application user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy compiled JAR artifact
COPY --from=build /app/target/*.jar app.jar

# Expose server HTTP port
EXPOSE 8080

# Environment defaults for production execution
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Launch Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod,postgres} -Dserver.port=${PORT:-8080} -jar app.jar"]
