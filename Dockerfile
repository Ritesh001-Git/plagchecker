FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app
COPY . .

# Build once
RUN mvn clean compile

# Run using compiled classes (no jar confusion)
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.detector.server.MiniServer"]
