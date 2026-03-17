FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app
COPY . .

RUN mvn clean install

CMD ["mvn", "exec:java", "-Dexec.mainClass=com.detector.server.MiniServer"]