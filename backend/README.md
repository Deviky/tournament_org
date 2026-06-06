# Backend Cybertournament

Backend-часть проекта построена на микросервисной архитектуре и реализована на `Spring Boot` с использованием `Spring Cloud`.

## Состав backend

### Инфраструктурные сервисы

- `Config` — Spring Cloud Config Server, источник централизованных конфигураций;
- `Discovery` — Eureka Server для регистрации сервисов;
- `Gateway` — единая точка входа для клиентских запросов.

### Доменные сервисы

- `Auth_Service` — аутентификация, авторизация, JWT, подтверждение почты, восстановление пароля;
- `Participant_Service` — игроки, организации, команды и составы команд;
- `Tournament_Service` — создание турниров, управление регистрацией и турнирной сеткой;
- `Match_Service` — матчи, их статусы и результаты;
- `Game_Service` — игровые дисциплины, параметры и алгоритмы;
- `Integration_Service` — интеграция с внешними платформами статистики;
- `Notification_Service` — уведомления и WebSocket/Kafka-логика.

## Архитектурная схема взаимодействия

Типовой маршрут запроса:

1. frontend отправляет запрос в `Gateway`;
2. `Gateway` маршрутизирует его в нужный доменный сервис;
3. сервисы получают конфигурацию через `Config`;
4. сервисы регистрируются в `Discovery`;
5. данные сохраняются в `PostgreSQL`, кэш и служебные данные — в `Redis`.

## Используемый стек

- `Java 21`
- `Spring Boot 3.5.x`
- `Spring Cloud 2025.0.x`
- `Spring Data JPA`
- `Spring Security`
- `Spring Cloud Gateway`
- `Spring Cloud Config`
- `Netflix Eureka`
- `PostgreSQL`
- `Redis`
- `Kafka` — для `Notification_Service`

## Порты сервисов

При типовой конфигурации используются следующие порты:

- `Config` — `8888`
- `Discovery` — `8761`
- `Gateway` — `8777`
- `Auth_Service` — `8077`
- `Tournament_Service` — `8071`
- `Participant_Service` — `8072`
- `Match_Service` — `8073`
- `Game_Service` — `8069`
- `Integration_Service` — `8125`

`Notification_Service` присутствует в репозитории, но в текущий `docker-compose` не включен.

## Конфигурация

Основные конфигурации лежат в:

```text
backend/Config/src/main/resources/config/
```

Ключевые файлы:

- `application.yml` — базовая общая конфигурация;
- `application-docker.yml` — настройки для контейнерного профиля;
- `gateway.yml` — маршруты API Gateway;
- `AUTH-SERVICE.yml`
- `PARTICIPANT-SERVICE.yml`
- `TOURNAMENT-SERVICE.yml`
- `MATCH-SERVICE.yml`
- `GAME-SERVICE.yml`
- `INTEGRATION-SERVICE.yml`
- `DISCOVERY.yml`

## Необходимые внешние зависимости

Для нормальной работы backend требуются:

- `PostgreSQL`;
- `Redis`;
- SMTP-сервер для почтовых сценариев `Auth_Service`;
- при необходимости — ключи доступа к внешним API статистики.

Переменные обычно берутся из корневого `.env`:

- `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_DATABASE`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS_ENABLE`
- `APP_PUBLIC_URL`
- `APP_BOOTSTRAP_ADMIN_EMAIL`, `APP_BOOTSTRAP_ADMIN_PASSWORD`

## Локальный запуск

Сервисы рекомендуется поднимать в таком порядке:

1. `Config`
2. `Discovery`
3. `Gateway`
4. `Auth_Service`
5. `Participant_Service`
6. `Tournament_Service`
7. `Match_Service`
8. `Game_Service`
9. `Integration_Service`

Пример запуска:

```powershell
cd backend/Config
.\gradlew.bat bootRun
```

Затем аналогично:

```powershell
cd backend/Discovery
.\gradlew.bat bootRun
```

и так далее для нужных сервисов.

## Docker-запуск

Для контейнерного запуска backend используйте корневой `docker-compose.yml`:

```bash
docker compose up --build
```

Подробная инструкция вынесена в [README-docker.md](README-docker.md).

## Тесты

В большинстве сервисов присутствуют базовые тесты Spring Boot.

Пример запуска тестов для конкретного сервиса:

```powershell
cd backend/Tournament_Service
.\gradlew.bat test
```

## Полезные каталоги

- `backend/docker/` — Dockerfile для Java-сервисов;
- `backend/Config/` — централизованная конфигурация;
- `backend/*_Service/` — бизнес-сервисы;
- `backend/Gateway/` — API Gateway;
- `backend/Discovery/` — сервис-дискавери.
