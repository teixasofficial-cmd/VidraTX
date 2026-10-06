FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package && cp target/vidratx-*.jar app.jar

FROM eclipse-temurin:25-jre

RUN useradd --system --uid 10001 --home-dir /app vidratx \
    && mkdir -p /app/midia-whatsapp \
    && chown -R vidratx:vidratx /app

WORKDIR /app
COPY --from=build /build/app.jar app.jar

USER vidratx

ENV WHATSAPP_MIDIA_DIR=/app/midia-whatsapp

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
