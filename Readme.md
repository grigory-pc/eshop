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