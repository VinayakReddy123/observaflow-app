# Stage 1 — Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml first (dependency caching trick)
COPY pom.xml .
COPY ingest-service/pom.xml ingest-service/
COPY processor-service/pom.xml processor-service/
COPY alert-service/pom.xml alert-service/
COPY websocket-service/pom.xml websocket-service/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
RUN mvn -pl ingest-service -am dependency:go-offline -B

# Now copy source code
COPY ingest-service/src ingest-service/src

# Build the jar, skip tests
RUN mvn -pl ingest-service -am clean package -DskipTests -B

# Stage 2 — Run (distroless)
FROM gcr.io/distroless/java21-debian12
WORKDIR /app

# Copy only the final jar from builder stage
COPY --from=builder /app/ingest-service/target/*.jar app.jar

USER 1001
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]