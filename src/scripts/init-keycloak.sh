#!/bin/bash

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Waiting for Keycloak to start...${NC}"

# Ждем пока Keycloak запустится
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080/health/ready > /dev/null; then
        echo -e "${GREEN}Keycloak is ready!${NC}"
        break
    fi
    echo -e "${YELLOW}Attempt $((attempt+1))/$max_attempts - Keycloak not ready yet...${NC}"
    sleep 5
    attempt=$((attempt+1))
done

if [ $attempt -eq $max_attempts ]; then
    echo -e "${RED}Keycloak failed to start within timeout${NC}"
    exit 1
fi

# Получаем токен админа
echo -e "${YELLOW}Getting admin token...${NC}"

ADMIN_TOKEN=$(curl -s -X POST "http://localhost:8080/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=admin" \
    -d "password=admin123" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}Failed to get admin token${NC}"
    exit 1
fi

echo -e "${GREEN}Admin token obtained${NC}"

# Проверяем существование realm
echo -e "${YELLOW}Checking if realm 'kyskfilms' exists...${NC}"

REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "http://localhost:8080/admin/realms/kyskfilms")

if [ "$REALM_EXISTS" == "200" ]; then
    echo -e "${YELLOW}Realm 'kyskfilms' already exists. Skipping import.${NC}"
else
    echo -e "${YELLOW}Creating realm 'kyskfilms'...${NC}"

    # Импортируем конфигурацию
    IMPORT_RESULT=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST "http://localhost:8080/admin/realms" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -H "Content-Type: application/json" \
        -d @/keycloak/import/keycloak-realm-config.json)

    if [ "$IMPORT_RESULT" == "201" ]; then
        echo -e "${GREEN}✓ Realm 'kyskfilms' created successfully!${NC}"
        echo -e "${GREEN}✓ Clients configured:${NC}"
        echo -e "   - kyskfilms-backend (Bearer-only)"
        echo -e "   - kyskfilms-frontend (Public client with PKCE)"
        echo -e "${GREEN}✓ Test users created:${NC}"
        echo -e "   - admin@kyskfilms.com / admin123 (ADMIN + USER roles)"
        echo -e "   - user@kyskfilms.com / user123 (USER role)"
        echo -e "${GREEN}✓ Roles configured: USER, ADMIN${NC}"
    else
        echo -e "${RED}Failed to create realm. HTTP Status: $IMPORT_RESULT${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}Keycloak initialization complete!${NC}"
echo -e "${YELLOW}Access Keycloak Admin Console at: http://localhost:8080${NC}"
echo -e "${YELLOW}Admin credentials: admin / admin123${NC}"