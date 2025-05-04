#!/bin/bash

# Запускаем Keycloak
docker compose up -d keycloak --build

# Ждем пока Keycloak станет доступен
while ! curl -s http://localhost:8080; do
  sleep 5
done

# Настраиваем клиента и получаем секрет
export CLIENT_SECRET=$(./setup-keycloak.sh | grep "Client Secret:" | awk '{print $3}')

# Запускаем приложение с переменной окружения
docker compose up -d --build

echo "Приложение доступно на http://localhost:8081"