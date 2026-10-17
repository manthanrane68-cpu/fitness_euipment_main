# Task 11: Continuous Testing in Jenkins
## Fitness Equipment Maintenance Portal

---

### 1. Quality Gate Architecture in Jenkins
The continuous testing strategy enforces that any regression or broken assertion immediately stops the pipeline execution, preventing corrupt builds from reaching deployment.

```
[Git Commit] ──> [Build & Compile] ──> [Quality Gate: Automated Test Suite]
                                                      │
                       ┌──────────────────────────────┴──────────────────────────────┐
                       ▼ PASS                                                        ▼ FAIL
            [Package Application]                                       [PIPELINE ABORTED]
                       │                                                - Deployment Blocked
            [Deploy to Server]                                          - JUnit Report Published
                       │                                                - Failure Snapshot Captured
            [Health Diagnostics]
```

---

### 2. Defect Injection & Pipeline Failure Evidence

#### Deliberate Defect Injected:
- **Location:** `src/main/java/com/fitness/portal/repository/RecordRepository.java`
- **Defect:** Overrode default status on ticket creation from `"Reported"` to `"DEFECT_FAILED_STATUS"`.
- **Expected Failure:** `testJourney1_IncidentIntake` assertion fails expecting default status `"Reported"`.

#### Jenkins Failed Pipeline Log:
```text
==========================================================
 [JENKINS CI] Continuous Testing & Deployment Pipeline
 Environment: staging | Port: 8086
==========================================================

[Pipeline] stage: Build & Compile
[Pipeline] completed: Build & Compile [SUCCESS]

[Pipeline] stage: Automated Quality Tests
==================================================================
  RUNNING FITNESS PORTAL AUTOMATED TEST SUITE
==================================================================
[TEST] Running testJourney1_IncidentIntake ... FAILED [ERROR: Initial status must default to Reported]
  [SCREENSHOT] Saved failure DOM dump to: target/screenshots/failed_testJourney1_IncidentIntake.html
[TEST] Running testJourney2_SearchAndFilter ... PASSED [OK]
[TEST] Running testJourney3_RoleWorkflowTransition ... PASSED [OK]
[TEST] Running testJourney4_KpiDashboardAndHealth ... PASSED [OK]
==================================================================
  TEST EXECUTION SUMMARY: 4 Tests Run, 3 Passed, 1 Failed
==================================================================

==========================================================
 [QUALITY GATE TRIGGERED] Automated Test Failures Detected!
 Pipeline execution STOPPED. Deployment stage will NOT proceed.
 Publishing JUnit test failure reports to Jenkins dashboard...
 Check screenshots in target/screenshots/ for failure snapshots.
==========================================================
[Pipeline] Finished: FAILURE
```

---

### 3. Defect Correction & Successful Rerun

#### Defect Correction Commit:
- **Commit:** `760d177`
- **Commit Message:** `fix(defect): restore default ticket status to Reported and add shutdown null safety`
- **File Restored:** `RecordRepository.java` correctly sets `record.setStatus("Reported");`.

#### Successful Rerun Pipeline Log:
```text
==========================================================
 [JENKINS CI] Continuous Testing & Deployment Pipeline
 Environment: staging | Port: 8086
==========================================================

[Pipeline] stage: Build & Compile
[Pipeline] completed: Build & Compile [SUCCESS]

[Pipeline] stage: Automated Quality Tests
==================================================================
  RUNNING FITNESS PORTAL AUTOMATED TEST SUITE
==================================================================
[TEST] Running testJourney1_IncidentIntake ... PASSED [OK]
[TEST] Running testJourney2_SearchAndFilter ... PASSED [OK]
[TEST] Running testJourney3_RoleWorkflowTransition ... PASSED [OK]
[TEST] Running testJourney4_KpiDashboardAndHealth ... PASSED [OK]
==================================================================
  TEST EXECUTION SUMMARY: 4 Tests Run, 4 Passed, 0 Failed
==================================================================
Stopping Fitness Portal Server...
[Pipeline] completed: Automated Quality Tests [SUCCESS]
[INFO] Publishing JUnit test report: target/surefire-reports/TEST-com.fitness.portal.SeleniumE2ETest.xml

[Pipeline] stage: Package Application
[Pipeline] completed: Package Application [SUCCESS]

[Pipeline] stage: Deploy to Server
==========================================================
 [DEPLOY] Initializing Server Deployment
 Environment: staging
 Port:        8086
 Target Path: E:\Devops\deployments\staging\fitness-portal.jar
==========================================================
[DEPLOY] Copied artifact to E:\Devops\deployments\staging\fitness-portal.jar
[DEPLOY] Launching: java -Dserver.port=8086 -jar "E:\Devops\deployments\staging\fitness-portal.jar"
[DEPLOY] Service started with PID: 9384
[DEPLOY] Probing health endpoint: http://localhost:8086/api/health ...
[DEPLOY] SUCCESS! Application is UP (Response: UP | Uptime: 0s | Heap: 2MB / 4084MB)
==========================================================
 [DEPLOY] Deployment to [staging] on port [8086] SUCCESSFUL
 Access Web UI: http://localhost:8086/
==========================================================

[Pipeline] Finished: SUCCESS
```
