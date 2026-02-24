# STAGE 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# Copy the pom and source code
COPY pom.xml .
COPY src ./src
# Build the JAR
RUN mvn clean package -DskipTests

# STAGE 2: Run the application
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the JAR from the "build" stage
COPY --from=build /app/target/ai-content-detector-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
