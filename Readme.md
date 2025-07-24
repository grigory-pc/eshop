# eshop

yandex practicun eshop project

## Запуск контейнеров

### Запуск контейнера Redis
```
docker run --name redis-server --rm -p 6379:6379 redis:7.4.2-bookworm
```
### Запуск контейнера Postgres
```
docker run --name postgres-17 -e POSTGRES_DB=eshop-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -v postgres-data:/var/lib/postgresql/data -d postgres:17
```
### Запуск контейнера Keycloack
```
docker run -d -p 8082:8080 --name keycloak -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.1.3 start-dev 
```