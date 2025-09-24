# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk as build

WORKDIR /app

# Копируем файлы сборки и зависимости для кеширования
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts ./

# Делаем gradlew исполняемым и устраняем возможные CRLF
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Предзагрузка зависимостей
RUN ./gradlew --no-daemon dependencies > /dev/null || true

# Копируем исходники и ресурсы
COPY src src

# Собираем bootJar без тестов
RUN ./gradlew --no-daemon clean bootJar -x test


FROM eclipse-temurin:17-jdk as runtime
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копируем итоговый JAR
COPY --from=build /app/build/libs/*.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8081

ENTRYPOINT ["/app/docker-entrypoint.sh"]