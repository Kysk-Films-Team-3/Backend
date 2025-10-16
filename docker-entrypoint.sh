#!/bin/bash

set -euo pipefail

SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
JAVA_OPTS=${JAVA_OPTS:--Xmx1024m -Xms512m}
APP_JAR="/app/app.jar"

if [ ! -f "$APP_JAR" ]; then
  echo "[ERROR] JAR not found at $APP_JAR"
  ls -la /app || true
  exit 1
fi

echo "Starting application with profile '$SPRING_PROFILES_ACTIVE'"
exec java $JAVA_OPTS \
  -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar "$APP_JAR"