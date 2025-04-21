# Internet-shop

Веб-приложение «Витрина интернет-магазина» с использованием Spring WebFlux

### Архитектура

В проекте используется `Feature-Based Architecture`, подход к организации кода, при котором классы группируются 
по функциональным возможностям (фичам) приложения, а не по техническим слоям (`controller`, `service`, `repository`)

### Как собрать проект

Для сборки проекта достаточно запустить команду `mvn clean package` или `mvn -N wrapper:wrapper` если `maven` не установлен

### Как запускать в Docker

1. Собрать приложение с помощью `mvn clean package` или `mvn -N wrapper:wrapper`
2. Выполнить команду:
    ```bush 
        docker compose up --build
    ```
3. Перейти по ссылке `http://localhost:8081/`