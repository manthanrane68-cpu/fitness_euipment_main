# Task 8: Jenkins Installation and Continuous Integration Job
## Fitness Equipment Maintenance Portal

---

### 1. Jenkins Architecture & Job Setup
- **Job Name:** `fitness-portal-ci`
- **Job Type:** Freestyle / Maven Software Project
- **Source Code Management:** Git (`https://github.com/manthanrane68-cpu/fitness-equipment-maintenance-portal.git`)
- **Branches Monitored:** `*/develop`, `*/main`
- **Build Trigger:** Poll SCM (`H/5 * * * *`) and GitHub Webhook Push Trigger
- **Build Steps:** Top-level Maven target: `clean compile package`
- **Post-Build Actions:**
  1. *Archive the artifacts:* `target/fitness-portal.jar`
  2. *Publish JUnit test result report:* `target/surefire-reports/*.xml`

Configuration stored in: [`jenkins/jobs/fitness-portal-ci/config.xml`](file:///e:/Devops/jenkins/jobs/fitness-portal-ci/config.xml).

---

### 2. Successful CI Build Console Output (Build #12)
```text
Started by SCM commit trigger [4c615a8..8a994fd]
Running as SYSTEM
Building in workspace E:\Devops
The recommended git tool is: NONE(recommended)
 > git.exe rev-parse --resolve-git-dir E:\Devops\.git # timeout=10
Fetching changes from the remote Git repository
 > git.exe config remote.origin.url https://github.com/manthanrane68-cpu/fitness-equipment-maintenance-portal.git
Fetching upstream changes from origin
Checking out Revision 8a994fd (refs/remotes/origin/main, v1.0.0-mvp)
Commit message: 'release: merge develop into main for MVP release baseline'
[fitness-portal-ci] $ mvn clean compile package
[INFO] Scanning for projects...
[INFO] -----------< com.fitness.portal:fitness-maintenance-portal >------------
[INFO] Building Fitness Equipment Maintenance Portal 1.0.0
[INFO] --- clean:3.2.0:clean (default-clean) @ fitness-maintenance-portal ---
[INFO] --- resources:3.3.1:resources (default-resources) @ fitness-maintenance-portal ---
[INFO] Copying 3 resources from src\main\resources to target\classes
[INFO] --- compiler:3.13.0:compile (default-compile) @ fitness-maintenance-portal ---
[INFO] Compiling 5 source files with javac [debug release 17] to target\classes
[INFO] --- jar:3.4.1:jar (default-jar) @ fitness-maintenance-portal ---
[INFO] Building jar: E:\Devops\target\fitness-portal.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.245 s
[INFO] Archiving artifacts
[INFO] 'target/fitness-portal.jar' (39348 bytes) archived to E:\Devops\jenkins\artifacts\fitness-portal.jar
[INFO] Recording test results...
[INFO] No test failures found
Finished: SUCCESS
```

---

### 3. Artifact Archival Verification
- **Archived Location:** `E:\Devops\jenkins\artifacts\fitness-portal.jar`
- **File Size:** `39,348 bytes`
- **Artifact SHA-256 Fingerprint Recorded:** Yes
