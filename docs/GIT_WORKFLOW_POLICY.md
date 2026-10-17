# Git Branching Strategy & Contribution Policy
## Fitness Equipment Maintenance Portal

---

### 1. Branch Architecture & Hierarchy

```
[main] (Production Stable / Tagged Releases: v1.0.0, v1.1.0)
   ▲
   │ Merge via PR only (Requires DoD & Passing CI)
   │
[develop] (Integration Branch for Features & CI Pipeline)
   ▲
   │ Features branch off 'develop' and merge back into 'develop'
   ├──────────────────────────────┬──────────────────────────────┐
   │                              │                              │
[feature/incident-intake]    [feature/dashboard-analytics]   [feature/role-workflow]
```

### 2. Branch Naming Rules
All Git branches must strictly follow standardized prefixes:
- **`main`**: Production-ready code only. Directly protected. No direct commits allowed.
- **`develop`**: Primary integration branch where verified features converge.
- **`feature/<short-description>`**: New user features (e.g., `feature/incident-intake`, `feature/kpi-dashboard`).
- **`bugfix/<ticket-id>-<description>`**: Defect corrections (e.g., `bugfix/TKT-104-null-pointer-search`).
- **`ci/<description>`**: Jenkins, Docker, and automation pipeline updates (e.g., `ci/jenkins-docker-stage`).
- **`infra/<description>`**: Puppet manifests and Ansible provisioning playbooks.
- **`release/<version>`**: Final stabilization before merging into `main` (e.g., `release/v1.0.0`).

### 3. Conventional Commit Standards
All commits must follow the **Conventional Commits** specification:
`<type>(<scope>): <subject>`

**Allowed Types:**
- `feat`: A new feature for the user
- `fix`: A bug fix
- `docs`: Documentation updates only
- `style`: Formatting, CSS improvements, visual polish
- `refactor`: Code restructure without changing behavior
- `test`: Adding or correcting Selenium / unit tests
- `ci`: CI/CD scripts, Jenkinsfile, Dockerfile
- `infra`: Puppet manifests, provisioning scripts, server configuration

**Examples:**
- `feat(intake): implement rapid maintenance incident logging form`
- `fix(search): prevent crash when query contains special characters`
- `ci(jenkins): add Selenium test stage and test report archive`
- `infra(puppet): declare java and tomcat service dependencies in site.pp`

### 4. Code Review & Pull Request Gates
1. Minimum 1 peer code review approval required.
2. Maven clean compile and unit test suite must pass 100%.
3. No unresolved merge conflicts.
4. Definition of Done checklist checked off.

### 5. Tagging Policy
Production releases merged into `main` must be annotated with semantic versions:
- `git tag -a v1.0.0-mvp -m "Release v1.0.0: Approved MVP baseline for Fitness Equipment Maintenance Portal"`
