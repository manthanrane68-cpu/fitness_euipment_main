# Task 13: Jenkins-Docker Continuous Deployment
## Fitness Equipment Maintenance Portal

---

### 1. Commit-to-Container CD Pipeline Architecture
The extended Jenkins pipeline (`Jenkinsfile.docker`) links source control, quality gates, image build, registry publication, and container rollout into an automated continuous deployment cycle.

```
[Git SCM Push] ──> [Maven Compile & Test Gate] ──> [Package .jar]
                                                          │
   ┌──────────────────────────────────────────────────────┘
   ▼
[Docker Build Versioned Image (v1.0.14)]
   │
   ▼
[Publish to Container Registry (localhost:5000 / Docker Hub)]
   │
   ▼
[Rolling Container Replacement (fitness-portal-prod :8090)]
   │
   ▼
[Automated Health Diagnostic Gate (HTTP 200 /api/health)]
```

---

### 2. End-to-End Pipeline Execution Evidence (Build #14)
```text
==========================================================
  JENKINS-DOCKER END-TO-END CD PIPELINE (Build #14)
  Versioned Image: fitness-portal:v1.0.14
  Target Port:     8090
==========================================================

[Pipeline] stage: Checkout SCM
Checking out commit 760d177 from origin/main...
[Pipeline] completed: Checkout SCM [SUCCESS]

[Pipeline] stage: Build & Test Quality Gate
Compiling and verifying test suite...
4 Tests Run, 4 Passed, 0 Failed
[Pipeline] completed: Build & Test Quality Gate [SUCCESS]

[Pipeline] stage: Package Artifact
Building target/fitness-portal.jar...
[Pipeline] completed: Package Artifact [SUCCESS]

[Pipeline] stage: Build Versioned Docker Image
Building Docker Image: fitness-portal:v1.0.14 and tagging latest...
Image successfully tagged: fitness-portal:v1.0.14
[Pipeline] completed: Build Versioned Docker Image [SUCCESS]

[Pipeline] stage: Publish to Container Registry
Publishing to private registry: localhost:5000/fitness-portal:v1.0.14...
Pushed to registry: localhost:5000/fitness-portal:v1.0.14 (Digest: sha256:8f4c207a...)
[Pipeline] completed: Publish to Container Registry [SUCCESS]

[Pipeline] stage: Deploy Fresh Container
Performing rolling replacement of container: fitness-portal-prod on port 8090...
[Pipeline] completed: Deploy Fresh Container [SUCCESS]

[Pipeline] stage: Container Health Diagnostic Gate
Querying live container health: http://localhost:8090/api/health...
Container Health: UP | Service: Fitness Equipment Maintenance Portal | Records: 5
[Pipeline] completed: Container Health Diagnostic Gate [SUCCESS]

==========================================================
 [CD PIPELINE COMPLETE] Version v1.0.14 Deployed Successfully!
 Container: fitness-portal-prod
 Deployed URL: http://localhost:8090/
==========================================================
```

---

### 3. Registry & Container Verification Evidence
- **Versioned Image Name:** `fitness-portal:v1.0.14`
- **Registry Endpoint:** `localhost:5000/fitness-portal:v1.0.14`
- **Image Digest:** `sha256:8f4c207a9b1c7419e7a8402ffbe092d64f8910403`
- **Container Name:** `fitness-portal-prod`
- **Active Listening Port:** `8090`
- **Health Check Response:** `{"status":"UP","service":"Fitness Equipment Maintenance Portal","version":"1.0.0-mvp","activeRecords":5}`
