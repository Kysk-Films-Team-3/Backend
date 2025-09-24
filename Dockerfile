# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk as build

WORKDIR /app

# Копируем gradle wrapper и скрипты сборки
COPY . .

# Собираем проект с активным профилем developer
RUN ./gradlew clean build -Dspring.profiles.active=developer -x test

# Диагностика: покажем, что реально собрано
RUN ls -l api/build/libs/ && ls -l jobs/build/libs/

RUN mkdir jars && \
    API_JAR=$(ls api/build/libs/*-boot.jar 2>/dev/null || ls api/build/libs/*.jar 2>/dev/null | head -n 1) && \
    JOBS_JAR=$(ls jobs/build/libs/*-boot.jar 2>/dev/null || ls jobs/build/libs/*.jar 2>/dev/null | head -n 1) && \
    [ -n "$API_JAR" ] && cp "$API_JAR" jars/api.jar || (echo "api.jar not found!" && exit 1) && \
    [ -n "$JOBS_JAR" ] && cp "$JOBS_JAR" jars/jobs.jar || (echo "jobs.jar not found!" && exit 1)

FROM eclipse-temurin:21-jdk as runtime
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*
WORKDIR /app

COPY --from=build /app/jars ./jars
COPY docker-entrypoint.sh .

EXPOSE 9040 9045

ENTRYPOINT ["bash", "docker-entrypoint.sh"]