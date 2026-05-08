# Backend Docker Compose

Backend now starts without local `postgres`, `redis`, and `notification-service`.

Start from the project root:

```bash
docker compose up --build
```

Useful commands:

```bash
docker compose up --build -d
docker compose logs -f config
docker compose logs -f gateway
docker compose down
```

What starts:

- `config` on `8888`
- `discovery` on `8761`
- `gateway` on `8777`
- `auth-service` on `8077`
- `participant-service` on `8072`
- `tournament-service` on `8071`
- `match-service` on `8073`
- `game-service` on `8069`
- `integration-service` on `8125`

Entry point for the frontend:

```text
http://localhost:8777
```

Notes:

- Services take their config from Spring Config Server in `backend/Config/src/main/resources/config/`.
- `Postgres` and `Redis` are expected to already be running on the host machine.
- Docker containers use the Spring profile `docker`.
- In Docker, `Postgres` and `Redis` are accessed via `host.docker.internal`.
- `auth-service` can bootstrap the first admin from env vars: `APP_BOOTSTRAP_ADMIN_EMAIL` and `APP_BOOTSTRAP_ADMIN_PASSWORD`.
- Public links in auth emails use `APP_PUBLIC_URL`, for example `http://localhost:5173` locally or your production domain over `https`.
