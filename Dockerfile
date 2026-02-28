# syntax=docker/dockerfile:1
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar file from the builder stage
COPY --from=builder /app/target/trading-risk-system-1.0-SNAPSHOT.jar app.jar

# Expose the API port
EXPOSE 8080

# Command to run the application
CMD ["java", "-cp", "app.jar", "me.diepdao.Main"]
