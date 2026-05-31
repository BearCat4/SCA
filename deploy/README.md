# THSSCA production deploy

The production deployment used for `http://14.103.164.88/` runs:

- PostgreSQL in Docker Compose on `127.0.0.1:15432`
- Spring Boot backend as a systemd service on `127.0.0.1:18080`
- Nginx serving `frontend/dist` and proxying `/api/` to the backend

Host packages:

```bash
dnf install -y java-11-openjdk-headless git trivy
```

Runtime paths:

- App directory: `/opt/thssca`
- Backend environment: `/etc/thssca/backend.env`
- Backend service: `/etc/systemd/system/thssca-backend.service`
- Nginx config: `/etc/nginx/conf.d/thssca.conf`
