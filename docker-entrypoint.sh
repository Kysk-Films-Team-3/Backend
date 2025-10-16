#!/bin/bash

set -euo pipefail

# ============ COLORS ============
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} [INFO] $*"
}

log_success() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')]${NC} [✓] $*"
}

log_warning() {
    echo -e "${YELLOW}[$(date +'%H:%M:%S')]${NC} [WARN] $*"
}

log_error() {
    echo -e "${RED}[$(date +'%H:%M:%S')]${NC} [ERROR] $*" >&2
}

# ============ DETECT SERVICE ============
# Якщо це Keycloak контейнер - роби Keycloak setup
if [ "${KEYCLOAK_ADMIN:-}" != "" ]; then
    log_info "Detected Keycloak container. Starting initialization..."

    KEYCLOAK_URL="http://localhost:8080"
    REALM_NAME="kyskfilms"
    ADMIN_USER="admin"
    ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin_secure_pass_change_me}"

    # Встав睡眠перед перевіркою
    sleep 5

    # Перевіри що конфіг існує
    if [ ! -f /keycloak/import/keycloak-realm-config.json ]; then
        log_error "Config file not found: /keycloak/import/keycloak-realm-config.json"
        exit 1
    fi

    log_info "Installing jq..."
    apk add --no-cache curl jq > /dev/null 2>&1 || true

    log_info "Attempting to get admin token..."

    # Спроби отримати токен (max 30 спроб)
    ADMIN_TOKEN=""
    for i in {1..30}; do
        log_info "Token attempt $i/30..."

        ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "username=$ADMIN_USER" \
            -d "password=$ADMIN_PASSWORD" \
            -d "grant_type=password" \
            -d "client_id=admin-cli" 2>/dev/null | jq -r '.access_token' 2>/dev/null || echo "null")

        if [ "$ADMIN_TOKEN" != "null" ] && [ -n "$ADMIN_TOKEN" ]; then
            log_success "Admin token obtained"
            break
        fi

        if [ $i -lt 30 ]; then
            sleep 2
        fi
    done

    if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
        log_error "Failed to get admin token after 30 attempts"
        log_warning "Continuing without realm import..."
        exit 0
    fi

    # Перевіри чи realm існує
    log_info "Checking if realm '$REALM_NAME' exists..."

    REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        "$KEYCLOAK_URL/admin/realms/$REALM_NAME" 2>/dev/null || echo "000")

    if [ "$REALM_EXISTS" == "200" ]; then
        log_warning "Realm '$REALM_NAME' already exists. Skipping import."
        exit 0
    fi

    log_info "Creating realm '$REALM_NAME'..."

    # Імпортуємо realm
    IMPORT_RESULT=$(curl -s -w "\n%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d @/keycloak/import/keycloak-realm-config.json 2>/dev/null)

    HTTP_CODE=$(echo "$IMPORT_RESULT" | tail -n1)

    if [ "$HTTP_CODE" == "201" ]; then
        log_success "Realm '$REALM_NAME' created successfully!"
        log_success "=========================================="
        log_success "Keycloak setup complete!"
        log_success "Realm: $REALM_NAME"
        log_success "Clients: kyskfilms-frontend, kyskfilms-backend"
        log_success "Users: admin@kyskfilms.com, user@kyskfilms.com"
        log_success "=========================================="
    else
        log_warning "Realm creation returned HTTP $HTTP_CODE"
        log_warning "Realm may already exist or import had issues"
    fi

    exit 0

# Інакше це Backend контейнер - запускаємо Spring Boot
else
    log_info "Detected Backend container. Starting Spring Boot..."

    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
    JAVA_OPTS=${JAVA_OPTS:--Xmx1024m -Xms512m}
    APP_JAR="/app/app.jar"

    if [ ! -f "$APP_JAR" ]; then
        log_error "JAR not found at $APP_JAR"
        ls -la /app || true
        exit 1
    fi

    log_info "Starting application with profile '$SPRING_PROFILES_ACTIVE'"
    exec java $JAVA_OPTS \
        -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -jar "$APP_JAR"
fi