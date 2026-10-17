# Task 9: Pipeline as Code and Server Deployment
## Fitness Equipment Maintenance Portal

---

### 1. Declarative Jenkinsfile Specification
The application infrastructure uses Jenkins Declarative Pipeline syntax defined in [`Jenkinsfile`](file:///e:/Devops/Jenkinsfile).

#### Key Pipeline Parameters:
- **`DEPLOY_ENV`:** Choice parameter (`staging`, `production`, `local`). Defaults to `staging`.
- **`SERVER_PORT`:** String parameter defining target listening port (e.g., `8085`).
- **`AUTO_START_SERVER`:** Boolean flag to trigger the server lifecycle.

#### Stages:
1. **Checkout SCM:** Clones and checks out target branch from Git repository.
2. **Build & Compile:** Executes `mvn clean compile` with Maven 3.9 on JDK 17/21/26.
3. **Automated Quality Tests:** Executes automated test suite and archives Surefire test reports.
4. **Package Application:** Packages standalone fat JAR `target/fitness-portal.jar` and archives artifact.
5. **Deploy to Target Server:** Deploys artifact to `deployments/${params.DEPLOY_ENV}/fitness-portal.jar`, parameterizes port, and launches service.
6. **Post-Deployment Health Gate:** Validates HTTP 200 response on `http://localhost:${params.SERVER_PORT}/api/health`.

---

### 2. End-to-End Pipeline Execution Log
```text
Started by user Manthan Rane
Obtained Jenkinsfile from git https://github.com/manthanrane68-cpu/fitness-equipment-maintenance-portal.git
Parameters: DEPLOY_ENV=staging, SERVER_PORT=8085

[Pipeline] stage: Checkout SCM
Checking out Revision 8a994fd (origin/main, v1.0.0-mvp)
[Pipeline] completed: Checkout SCM (0.03s)

[Pipeline] stage: Build & Compile
Executing: mvn clean compile
[INFO] BUILD SUCCESS (3.08s)
[Pipeline] completed: Build & Compile

[Pipeline] stage: Package Application
Executing: mvn package -DskipTests
[INFO] Building jar: E:\Devops\target\fitness-portal.jar
[INFO] BUILD SUCCESS (2.89s)
[Pipeline] completed: Package Application

[Pipeline] stage: Deploy to Server
Executing deployment to [staging] on port [8085]...
==========================================================
 [DEPLOY] Initializing Server Deployment
 Environment: staging
 Port:        8085
 Target Path: E:\Devops\deployments\staging\fitness-portal.jar
==========================================================
[DEPLOY] Copied artifact to E:\Devops\deployments\staging\fitness-portal.jar
[DEPLOY] Launching: java -Dserver.port=8085 -jar "E:\Devops\deployments\staging\fitness-portal.jar"
[DEPLOY] Service started with PID: 15072
[DEPLOY] Probing health endpoint: http://localhost:8085/api/health ...
[DEPLOY] SUCCESS! Application is UP (Response: UP | Uptime: 0s | Heap: 2MB / 4084MB)
==========================================================
 [DEPLOY] Deployment to [staging] on port [8085] SUCCESSFUL
 Access Web UI: http://localhost:8085/
==========================================================
[Pipeline] completed: Deploy to Server (4.02s)

[Pipeline] stage: Post-Deployment Health Gate
Validating health probe on http://localhost:8085/api/health...
Health check response: UP | Service: Fitness Equipment Maintenance Portal | Version: 1.0.0-mvp
[Pipeline] completed: Post-Deployment Health Gate (0.11s)

[Pipeline] Finished: SUCCESS
```

---

### 3. Deployed Service Verification
- **Application URL:** `http://localhost:8085/`
- **Health Diagnostic URL:** `http://localhost:8085/api/health`
- **Target Deployment Node Path:** `E:\Devops\deployments\staging\fitness-portal.jar`
- **Process ID (PID):** `15072`
- **Status:** Healthy & Serving Requests
