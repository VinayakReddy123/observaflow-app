# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first (layer caching — deps won't re-download unless pom changes)
COPY pom.xml ./
COPY auth-service/pom.xml auth-service/pom.xml
COPY ingest-service/pom.xml ingest-service/pom.xml
COPY processor-service/pom.xml processor-service/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY websocket-service/pom.xml websocket-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml


RUN mvn -pl auth-service -am dependency:go-offline -B

# Copy source and build
COPY auth-service/src auth-service/src
RUN mvn -pl auth-service -am clean package -DskipTests -B

# ---- Stage 2: Run ----
FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=builder /app/auth-service/target/*.jar app.jar

USER 1001
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]