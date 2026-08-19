# Docker Startup Guide — Single Command

> Covers full-stack startup with `docker-compose up`

## Quick Start

```bash
# Clone the repository
git clone https://github.com/matiaspakua/notaire.git
cd notaire

# Start the full stack (all 4 services)
docker-compose up -d

# Check all services are healthy
docker-compose ps
```

## Services Started

| Service | URL | Description |
|---------|-----|-------------|
| PostgreSQL | `localhost:5432` | Database (notaire DB) |
| Backend API | `http://localhost:8080` | Spring Boot REST API |
| Frontend | `http://localhost:3000` | Next.js 16 UI |
| pgAdmin | `http://localhost:5050` | Database management UI |

## Default Credentials

| Service | Username | Password |
|---------|----------|----------|
| Application admin | `admin` | `admin` |
| PostgreSQL | `admin` | `admin` |
| pgAdmin | `admin@notaire.com` | `admin` |

## Service Dependencies

```
frontend
   └── depends_on: backend (healthy)
         └── depends_on: postgres (healthy)
pgadmin
   └── depends_on: postgres (healthy)
```

The `depends_on` with healthcheck conditions ensures services start in the correct order automatically.

## Logs

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

## Environment Variables

Default values are set in `docker-compose.yml`. Override by creating a `.env` file:

```env
POSTGRES_DB=notaire
POSTGRES_USER=admin
POSTGRES_PASSWORD=yourpassword
```

## Stopping the Stack

```bash
# Stop all services (keep data)
docker-compose down

# Stop and remove all data volumes
docker-compose down -v
```

## Building After Code Changes

```bash
# Rebuild all images and restart
docker-compose up -d --build

# Rebuild specific service only
docker-compose up -d --build frontend
docker-compose up -d --build backend
```

## Troubleshooting

### Frontend fails to start

Check if the backend healthcheck passes before frontend starts:
```bash
docker-compose logs backend | grep "Started\|ERROR"
```

### Backend fails with database error

Check if Flyway migrations ran successfully:
```bash
docker-compose logs backend | grep "Flyway\|migration"
```

### Port conflicts

If ports are already in use, stop conflicting processes or change ports in `docker-compose.yml`.
