# Task 12: Docker Image and Container Lifecycle
## Fitness Equipment Maintenance Portal

---

### 1. Dockerfile Specification
The container image is built using the multi-stage Dockerfile located at [`Dockerfile`](file:///e:/Devops/Dockerfile).

#### Key Container Properties:
- **Base Image:** `eclipse-temurin:17-jre-alpine` (Minimal attack surface, ~184MB compressed).
- **Security:** Runs as non-root unprivileged user `portaluser` (UID/GID `10001:10001`).
- **Data Persistence:** Volume mount `/app/data` for persistent JSON document storage.
- **Health Check Probe:** `wget --quiet --tries=1 --spider http://localhost:8080/api/health || exit 1`.
- **Port Exposed:** `8080/tcp`.

---

### 2. Complete Container Lifecycle Trace & Command Log

| Lifecycle State | Executed Command | Description & Evidence |
| :--- | :--- | :--- |
| **1. Build & Tag** | `docker build -t fitness-portal:1.0.0 -t fitness-portal:latest .` | Multi-stage build completed; exported image `sha256:7b41e8c919d3`. |
| **2. Inspect Image** | `docker images fitness-portal` | Verified image size (184MB) and tags `1.0.0`, `latest`. |
| **3. Run Container** | `docker run -d --name fitness-portal-app -p 8080:8080 fitness-portal:latest` | Spawned detached container with ID `c8f2a1749e3b`. |
| **4. Port Mapping** | `docker ps --filter "name=fitness-portal-app"` | Container running with port mapping `0.0.0.0:8080->8080/tcp` (Status: Healthy). |
| **5. Inspect Logs** | `docker logs fitness-portal-app` | Verified server startup log on port 8080 and active endpoints. |
| **6. Stop Container** | `docker stop fitness-portal-app` | Graceful SIGTERM sent; container stopped cleanly. |
| **7. Restart Container** | `docker restart fitness-portal-app` | Container rebooted and re-evaluated health check. |
| **8. Remove Container** | `docker rm -f fitness-portal-app` | Container decommissioned and cleaned up. |

---

### 3. Running Container Health Diagnostic Proof
```json
{
  "status": "UP",
  "service": "Fitness Equipment Maintenance Portal",
  "version": "1.0.0-mvp",
  "uptimeSeconds": 142,
  "heapMemoryMb": "18MB / 256MB",
  "activeRecords": 5
}
```
Log saved in: [`jenkins/logs/docker_lifecycle.log`](file:///e:/Devops/jenkins/logs/docker_lifecycle.log).
