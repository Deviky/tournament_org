# Cybertournament

`Cybertournament` — веб-сервис для организации киберспортивных турниров. Проект состоит из React-клиента и набора Spring Boot микросервисов, которые покрывают регистрацию пользователей, управление командами, турнирами, матчами и интеграцию с внешними платформами статистики.

## Возможности

- регистрация, вход, подтверждение почты и восстановление пароля;
- создание и управление командами;
- создание турниров, открытие и закрытие регистрации участников;
- генерация турнирной сетки и управление матчами;
- просмотр профилей игроков, команд и турниров;
- получение внешней игровой статистики;
- разграничение доступа по ролям.

## Структура проекта

```text
Cybertournament/
├─ backend/                     # микросервисы и инфраструктурные сервисы Spring Boot
│  ├─ Auth_Service/
│  ├─ Participant_Service/
│  ├─ Tournament_Service/
│  ├─ Match_Service/
│  ├─ Game_Service/
│  ├─ Integration_Service/
│  ├─ Gateway/
│  ├─ Discovery/
│  ├─ Config/
│  └─ Notification_Service/     # присутствует в репозитории, но не включен в текущий docker-compose
├─ frontend/
│  └─ cybertournament/          # React + Vite клиент
├─ docker-compose.yml           # контейнерный запуск backend-части
├─ demo_seed.py                 # заполнение демо-данными
└─ .env                         # локальные переменные окружения
```

## Технологии

- frontend: `React`, `Vite`, `React Router`, `Zustand`, `Axios`;
- backend: `Java 21`, `Spring Boot`, `Spring Cloud Gateway`, `Spring Cloud Config`, `Eureka`;
- данные: `PostgreSQL`, `Redis`;
- инфраструктура: `Docker Compose`;
- интеграции: внешние API статистики и игровых профилей.

## Быстрый запуск

### 1. Подготовить зависимости

Для работы проекта нужны:

- `Java 21`;
- `Node.js` и `npm`;
- `PostgreSQL`;
- `Redis`;
- при необходимости почтовых сценариев — SMTP-сервер.

### 2. Настроить `.env`

В корне проекта используется файл `.env` со следующими переменными:

- `APP_PUBLIC_URL`
- `APP_BOOTSTRAP_ADMIN_EMAIL`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`
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

### 3. Запустить backend

Контейнерный запуск:

```bash
docker compose up --build
```

По умолчанию поднимаются:

- `Config` — `http://localhost:8888`
- `Discovery` — `http://localhost:8761`
- `Gateway` — `http://localhost:8777`
- `Auth Service` — `http://localhost:8077`
- `Tournament Service` — `http://localhost:8071`
- `Participant Service` — `http://localhost:8072`
- `Match Service` — `http://localhost:8073`
- `Game Service` — `http://localhost:8069`
- `Integration Service` — `http://localhost:8125`

Важно: в текущем `docker-compose` база данных и Redis не создаются автоматически, они должны быть доступны отдельно.

Подробности — в [backend/README.md](backend/README.md) и [backend/README-docker.md](backend/README-docker.md).

### 4. Запустить frontend

```bash
cd frontend/cybertournament
npm install
npm run dev
```

Клиентское приложение будет доступно по адресу:

```text
http://localhost:5173
```

Frontend по умолчанию обращается к API Gateway на `http://localhost:8777`.

Подробности — в [frontend/cybertournament/README.md](frontend/cybertournament/README.md).

## Локальный запуск без Docker

Если нужно запускать сервисы вручную, их удобнее поднимать в таком порядке:

1. `backend/Config`
2. `backend/Discovery`
3. `backend/Gateway`
4. доменные сервисы: `Auth`, `Participant`, `Tournament`, `Match`, `Game`, `Integration`

Пример для Windows:

```powershell
cd backend/Config
.\gradlew.bat bootRun
```

Аналогично запускаются остальные сервисы из своих каталогов.

## Демо-данные

В проекте есть скрипт [demo_seed.py](demo_seed.py), который:

- читает параметры подключения из `.env`;
- создает демо-пользователей, команды и турниры;
- использует прямое подключение к PostgreSQL и API сервисов турниров и матчей.

Для запуска нужны зависимости Python:

```bash
pip install "psycopg[binary]" bcrypt
python demo_seed.py
```

## Основные пользовательские сценарии

- гость просматривает турниры, команды и страницы участников;
- игрок регистрируется, создает команду, вступает в турниры и смотрит статистику;
- организатор создает турнир, управляет регистрацией, формирует сетку и контролирует матчи;
- система получает и отображает внешнюю статистику по игрокам.

## Дополнительно

- [backend/README.md](backend/README.md) — описание сервисов и backend-архитектуры;
- [backend/README-docker.md](backend/README-docker.md) — контейнерный запуск backend;
- [frontend/cybertournament/README.md](frontend/cybertournament/README.md) — описание frontend-части.
