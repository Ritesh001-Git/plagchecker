FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

COPY . .

EXPOSE 8080

CMD ["mvn", "exec:java", "-Dexec.mainClass=com.detector.server.MiniServer"]