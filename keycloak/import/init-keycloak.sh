#!/bin/bash

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

KEYCLOAK_URL="http://keycloak:8080"
REALM_NAME="kyskfilms"

log_info "Starting Keycloak initialization..."

if ! command -v jq &> /dev/null; then
    apk add --no-cache curl jq
fi

ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=admin" \
    -d "password=admin_secure_pass_change_me" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    log_error "Failed to get admin token"
    exit 1
fi

log_success "Admin token obtained"

REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM_NAME")

if [ "$REALM_EXISTS" == "200" ]; then
    log_info "Realm '$REALM_NAME' already exists"
    exit 0
fi

log_info "Creating realm '$REALM_NAME'..."

curl -s -X POST "$KEYCLOAK_URL/admin/realms" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d @/keycloak/import/keycloak-realm-config.json > /dev/null

log_success "Realm '$REALM_NAME' created successfully!"