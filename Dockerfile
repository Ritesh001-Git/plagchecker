# Step 1: Use Java Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Step 2: Copy the jar from your target folder to the container
# We rename it to app.jar so the ENTRYPOINT always works
COPY target/ai-content-detector-1.0.0-shaded.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]