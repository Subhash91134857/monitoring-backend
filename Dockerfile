# STAGE 1: Build & Compile (Maven)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build
# 1. Copy pom.xml to download and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# 2. Copy the source code and compile the executable JAR
COPY src ./src
RUN mvn clean package -DskipTests


# STAGE 2: Lightweight Runtime (JRE)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the compiled JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose your web server port
EXPOSE 8080

# Execute the JAR file as the primary entry point
ENTRYPOINT ["java", "-jar", "app.jar"]