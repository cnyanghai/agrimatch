# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src

# Cache dependencies first
COPY pom.xml .
COPY agrimatch-common/pom.xml agrimatch-common/
COPY agrimatch-service/pom.xml agrimatch-service/
RUN mvn dependency:go-offline -B || true

# Copy source and build
COPY . .
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /src/agrimatch-service/target/agrimatch-service-*.jar app.jar

RUN mkdir -p /app/uploads && chown -R app:app /app
USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
