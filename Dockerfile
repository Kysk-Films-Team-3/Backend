# syntax=docker/dockerfile:1

# ============ BUILD STAGE ============
FROM eclipse-temurin:21-jdk as build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src src

RUN ./gradlew --no-daemon clean bootJar -x test

# ============ RUNTIME STAGE ============
FROM eclipse-temurin:21-jdk

LABEL maintainer="kyskfilms@example.com"
LABEL description="KYSKFilms Backend"

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl netcat-traditional ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN useradd -m -u 9999 appuser && mkdir -p /app/logs && chown -R appuser:appuser /app

COPY --from=build --chown=appuser:appuser /app/build/libs/*.jar /app/app.jar
COPY --chown=appuser:appuser docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

USER appuser

EXPOSE 8081

ENTRYPOINT ["/app/docker-entrypoint.sh"]