# Internet-shop

Веб-приложение «Витрина интернет-магазина» с использованием Spring Boot

### Как запускать локально

1. Выполнить команду, чтобы поднять postgresql базу в докере:
    ```bush 
        docker compose --file docker-compose.local.yml up --build
    ```
2. Перейти в `App.class` и запустить его
3. Перейти по ссылке `http://localhost:8081/`

### Как запускать в Docker

1. Собрать приложение с помощтю `mvn clean package`
2. Выполнить команду:
    ```bush 
        docker compose up --build
    ```
3. Перейти по ссылке `http://localhost:8081/`