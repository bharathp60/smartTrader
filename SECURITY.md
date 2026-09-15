# Security

Security is critical for an autonomous trading system holding API keys to a brokerage.

## JWT Authentication Flow
1. User POSTs to `/api/auth/login`.
2. Validated against the users table.
3. Receives a JWT signed with `JWT_SECRET`.
4. Subsequent requests include `Authorization: Bearer <token>`.

## Role-Based Access Control
- **ADMIN**: Can manage users, trigger kill switch, update risk parameters.
- **TRADER**: Can manually intervene in trades, view portfolios.
- **VIEWER**: Read-only access to dashboards.

## API Key Management
**NEVER log API secrets.**
The `application.yml` uses `${BROKER_API_KEY}`. In production, these should be injected via Kubernetes Secrets or AWS Secrets Manager.

## Environment Variable Security
Avoid placing the `.env` file in source control. The provided `.env.example` contains fake values.

## Production Security Checklist
- [ ] Ensure `JWT_SECRET` is exactly 256 bits or higher and securely generated.
- [ ] Disable Actuator sensitive endpoints or protect them behind Spring Security.
- [ ] Run Docker containers as a non-root user (configured in Dockerfile).
- [ ] Place the application behind a WAF (Web Application Firewall) to protect against DDoS.

## Kill Switch Access
The `/api/system/kill-switch` endpoint is heavily protected. It requires `ADMIN` role. It should also be network-restricted (e.g., only accessible from internal VPN IPs if possible).
