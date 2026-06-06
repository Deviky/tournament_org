# Docker-запуск backend

Этот документ описывает запуск backend-части проекта через `docker compose`.

## Что важно заранее

Текущий `docker-compose.yml` поднимает только Java-сервисы проекта. Внешние зависимости должны быть доступны отдельно:

- `PostgreSQL`
- `Redis`

`Notification_Service` присутствует в репозитории, но в текущий сценарий Docker-запуска не включен.

## Запуск

Запускать нужно из корня проекта:

```bash
docker compose up --build
```

Фоновый запуск:

```bash
docker compose up --build -d
```

Остановка:

```bash
docker compose down
```

## Какие контейнеры поднимаются

- `config` — `8888`
- `discovery` — `8761`
- `gateway` — `8777`
- `auth-service` — `8077`
- `participant-service` — `8072`
- `tournament-service` — `8071`
- `match-service` — `8073`
- `game-service` — `8069`
- `integration-service` — `8125`

## Точка входа

Frontend обычно работает через `Gateway`:

```text
http://localhost:8777
```

## Полезные команды

Просмотр логов:

```bash
docker compose logs -f config
docker compose logs -f discovery
docker compose logs -f gateway
docker compose logs -f auth-service
```

Пересборка конкретного сервиса:

```bash
docker compose up --build gateway
```

## Как устроена конфигурация в Docker

- сервисы используют профиль Spring `docker`;
- конфигурация подтягивается из `Config` сервера;
- файлы конфигурации лежат в `backend/Config/src/main/resources/config/`;
- в контейнерах `PostgreSQL` и `Redis` по умолчанию доступны через `host.docker.internal`.

## Переменные окружения

Наиболее важные переменные из корневого `.env`:

- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `REDIS_DATABASE`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS_ENABLE`
- `APP_PUBLIC_URL`
- `APP_BOOTSTRAP_ADMIN_EMAIL`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`

## Дополнительные замечания

- `auth-service` может автоматически создать первого администратора через `APP_BOOTSTRAP_ADMIN_EMAIL` и `APP_BOOTSTRAP_ADMIN_PASSWORD`;
- ссылки в письмах формируются на основе `APP_PUBLIC_URL`;
- для Gmail обычно используются:
  - `MAIL_HOST=smtp.gmail.com`
  - `MAIL_PORT=587`
  - `MAIL_SMTP_AUTH=true`
  - `MAIL_SMTP_STARTTLS_ENABLE=true`

Если нужен более полный обзор backend-части, смотрите [README.md](README.md).
