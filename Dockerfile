# Lightweight Java runtime
FROM eclipse-temurin:17-jre

# App directory
WORKDIR /app

# Copy jar
COPY target/ai-content-detector-1.0.0-shaded.jar

# Default port (same as MiniServer)
EXPOSE 8080

# Run server
ENTRYPOINT ["java","-jar","app.jar"]
