# Internet-shop

Веб-приложение «Витрина интернет-магазина» с использованием Spring WebFlux

### Как запускать локально

1. Выполнить команду, чтобы поднять postgresql базу в докере:
    ```bush 
        docker compose up postgres --build
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

docker run --hostname=3e66c924d0fc --mac-address=02:42:ac:11:00:04 --env=PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin --env=GOSU_VERSION=1.17 --env=REDIS_VERSION=7.4.2 --env=REDIS_DOWNLOAD_URL=http://download.redis.io/releases/redis-7.4.2.tar.gz --env=REDIS_DOWNLOAD_SHA=4ddebbf09061cbb589011786febdb34f29767dd7f89dbe712d2b68e808af6a1f --volume=/data --network=bridge --workdir=/data -p 6379 --restart=no --label='org.testcontainers=true' --label='org.testcontainers.lang=java' --label='org.testcontainers.sessionId=a9bf802f-6800-4205-8ca2-740e969f20c0' --label='org.testcontainers.version=1.20.5' --runtime=runc -d redis:latest