#!/bin/sh

set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://keycloak:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin_secure_pass_change_me}"
REALM_NAME="${KEYCLOAK_REALM:-kyskfilms}"

echo "Waiting for Keycloak at $KEYCLOAK_URL..."

# Чекаємо поки Keycloak буде ready
for i in $(seq 1 30); do
    if curl -sf "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1; then
        echo "✓ Keycloak is ready"
        break
    fi

    echo "Attempt $i/30: Keycloak not ready yet..."
    sleep 2

    if [ $i -eq 90 ]; then
        echo "ERROR: Keycloak did not start within 180 seconds"
        exit 1
    fi
done

echo "Getting admin token..."

# Отримуємо токен
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$ADMIN_USER" \
    -d "password=$ADMIN_PASSWORD" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "ERROR: Failed to get admin token"
    exit 1
fi

echo "✓ Token obtained"

# Перевіряємо чи realm існує
echo "Checking if realm '$REALM_NAME' exists..."

REALM_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM_NAME")

if [ "$REALM_STATUS" = "200" ]; then
    echo "✓ Realm '$REALM_NAME' already exists. Skipping import."
    exit 0
fi

# Імпортуємо realm
echo "Importing realm from /keycloak/import/keycloak-realm-config.json..."

if [ ! -f /keycloak/import/keycloak-realm-config.json ]; then
    echo "ERROR: Config file not found"
    exit 1
fi

IMPORT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d @/keycloak/import/keycloak-realm-config.json)

if [ "$IMPORT_STATUS" = "201" ]; then
    echo "✓ Realm '$REALM_NAME' imported successfully!"
    exit 0
else
    echo "WARNING: Import returned HTTP $IMPORT_STATUS"
    echo "Realm may already exist or import had issues"
    exit 0
fi