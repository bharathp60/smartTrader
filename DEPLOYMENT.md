# Deployment Guide

## Prerequisites
- Docker & Docker Compose
- Minimum 4GB RAM (8GB recommended for ML/AI operations)

## Docker Compose Quick Start
```bash
docker-compose up -d
```
This spins up the Spring Boot app, PostgreSQL, and Redis.

## Environment Variables
| Variable | Description | Default |
|---|---|---|
| `TRADING_MODE` | PAPER or LIVE | PAPER |
| `LIVE_TRADING_ENABLED` | Master switch for live trading | false |
| `OPENAI_API_KEY` | Required for Spring AI | none |
| `DB_URL` | Postgres connection string | jdbc:postgresql://... |

## Database Setup
Flyway migrations (`classpath:db/migration`) run automatically on startup. The `ddl-auto` is set to `validate`.

## Redis Configuration
Used for caching and ShedLock. Ensure `REDIS_HOST` is properly configured.

## JVM Tuning
Recommended settings for Java 25:
`-Xms2g -Xmx4g -XX:+UseZGC -XX:MaxGCPauseMillis=10`
Low latency GC is crucial for the trading engine.

## Health Checks
Spring Boot Actuator is exposed at `/actuator/health`.
Docker Compose utilizes this for container health status.

## Monitoring
Prometheus metrics are exposed at `/actuator/prometheus`.
Recommended to run a Grafana instance alongside for dashboards visualizing PnL and AI token usage.

## Backup Recommendations
- PostgreSQL: Daily pg_dump.
- Redis: AOF enabled for durability (configured in docker-compose.yml).
