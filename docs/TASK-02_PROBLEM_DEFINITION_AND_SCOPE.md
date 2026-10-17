# Task 2: Problem Definition and Scope
## Puppet-Provisioned Fitness Equipment Maintenance Portal

---

### 1. Executive Summary & Real-Time Need
Modern commercial fitness centers, university gyms, and hospitality health clubs operate dozens to hundreds of electro-mechanical and cardiovascular exercise machines (e.g., commercial treadmills, ellipticals, stairmasters, selectorized weight stacks, and pneumatic cable machines). 

In real-world gym operations, equipment downtime leads to severe member dissatisfaction, membership cancellations, and liability risks due to unaddressed mechanical failures or electrical hazards. Currently, fitness centers rely on fragmented, manual communication channels—such as paper maintenance logbooks taped to reception desks, informal WhatsApp/Slack messages, and verbal notifications from gym-goers to floor trainers.

This lack of a centralized, traceable workflow results in:
1. **Extended Downtime:** Broken treadmills or cable pulleys sit idle for weeks because service technicians are unaware of the fault or missing required spare parts.
2. **Safety & Compliance Hazards:** Faulty cables, snapped belts, or unstable benches pose direct physical injury risks to patrons and staff.
3. **Absence of Accountability:** Facility managers cannot track Mean Time to Resolution (MTTR), recurring component failure rates, or contractor maintenance costs.
4. **Poor Asset Visibility:** No centralized inventory exists to identify warranty status, service history, or operational health across fitness zones (Cardio, Strength, Functional, Recovery).

The **Fitness Equipment Maintenance Portal** solves these critical challenges by delivering an automated, role-based, end-to-end incident intake, triage, technician dispatch, parts management, and dashboard analytics system.

---

### 2. Stakeholder Matrix & Target Users

| Stakeholder / User Role | Description | Key Responsibilities & Use Cases | Pain Points Addressed |
| :--- | :--- | :--- | :--- |
| **Gym Members & Visitors** | Patrons using workout equipment daily. | Scan QR code on faulty machine or report broken equipment via kiosk/portal. | Avoids workouts interrupted by broken machines; provides immediate reporting without searching for staff. |
| **Gym Floor Staff / Trainers** | Front-desk staff, personal trainers, and floor supervisors. | Log incidents, capture photo/defect description, assign initial severity, flag equipment as "Out of Order". | Eliminates messy paper logs and forgotten verbal complaints; instant ticket generation. |
| **Maintenance Technicians** | In-house service technicians and certified OEM vendors (e.g., LifeFitness, Technogym). | View assigned repair queue, update status (*In Repair*, *Awaiting Parts*, *Resolved*), log labor hours, part replacement details, and repair costs. | Removes phone tag; provides clear equipment history, fault symptoms, and serial numbers. |
| **Facility Managers / Gym Admins** | Operations directors, general managers, and gym owners. | Review equipment health dashboard, approve maintenance budgets, manage asset inventory, monitor MTTR, enforce SLAs, and view uptime metrics. | Replaces guesswork with real-time operational metrics and audit-ready maintenance logs. |
| **DevOps & IT Engineers** | Infrastructure and platform reliability engineers. | Automate code deployment, enforce CI/CD quality gates, manage infrastructure configuration via Puppet/Docker, and ensure high availability. | Eliminates manual server configuration drift; enables reproducible, idempotent provisioning. |

---

### 3. Existing Pain Points vs. Proposed Solutions

| Pain Point | Root Cause | Proposed Solution in Portal |
| :--- | :--- | :--- |
| **Delayed Incident Reporting** | Members tell front desk verbally; front desk forgets to log it. | Direct web-based rapid incident creation with equipment ID, category, zone, and urgency. |
| **Double Reporting / Redundant Tickets** | Multiple members report the same squeaking elliptical. | Unified search and status display showing current machine status (*Reported*, *In Repair*). |
| **Lack of Status Visibility** | Floor trainers do not know when a machine will be fixed. | Real-time status workflow (*Reported* &rarr; *Triaged* &rarr; *In Repair* &rarr; *Awaiting Parts* &rarr; *Resolved* &rarr; *Closed*). |
| **High Maintenance Costs** | Late intervention causes minor belt slips to burn out entire motors. | Preventive tracking and priority flagging (Low, Medium, High, Critical). |
| **Configuration Drift & Deployment Outages** | Portal deployed manually with inconsistent Tomcat/Java versions. | Infrastructure as Code (Puppet/Ansible), Docker containerization, and automated Jenkins CI/CD. |

---

### 4. Measurable Success Criteria (SMART Goals)

1. **Incident Intake Efficiency:** Reduce ticket creation time to **< 30 seconds** for gym floor trainers and staff.
2. **Mean Time to Resolution (MTTR):** Decrease average equipment downtime from **9.2 days to < 48 hours** for standard issues and **< 8 hours** for critical safety hazards.
3. **Data Completeness & Auditability:** 100% of maintenance requests must record equipment ID, fault classification, reporter role, technician assignment, and final cost.
4. **DevOps Automation Metric:** Automated Jenkins CI pipeline executes build, unit/integration tests, and packaging within **< 3 minutes**.
5. **System Availability:** Portal uptime of **99.9%** with idempotent Puppet configuration management ensuring zero configuration drift.

---

### 5. Constraints

- **Technical Constraints:** Must run on standard Java runtime (JDK 17/21/26), packaged via Maven, deployable as a standalone executable JAR or on Tomcat/Nginx reverse proxy, and containerizable via standard Dockerfile.
- **Operational Constraints:** Non-technical gym floor staff must be able to navigate the portal intuitively without requiring specialized software training.
- **Security & RBAC Constraints:** Strict role-based workflow controls—only Technicians and Admins can advance tickets to *Resolved* or *Closed*, preventing unauthorized sign-offs.
- **Data Integrity Constraints:** Equipment IDs and records must be persistent, searchable, filterable, and exportable for maintenance auditing.

---

### 6. Approved 15-Task MVP Scope Breakdown

To ensure a disciplined, traceable engineering delivery, the project scope is frozen into 15 structured tasks:

1. **Task 1:** Project Charter & Feasibility Benchmark
2. **Task 2:** Problem Definition and Scope Analysis *(Completed)*
3. **Task 3:** Agile Planning, User Stories, Product Backlog, and DevOps Workflow
4. **Task 4:** System Architecture, Requirements Specification, and Technology Setup
5. **Task 5:** Git and GitHub Repository Initialization, Branch Policies, and Baseline Skeleton
6. **Task 6:** Feature Development with Git Branching (*Feature 1: Incident Intake*) & PR Workflow
7. **Task 7:** Full MVP Implementation (*CRUD, Role Workflow, Dashboard KPI Analytics*) & Merge Conflict Resolution
8. **Task 8:** Jenkins CI Installation, Repository Webhook/Polling, and Automated Maven Build Job
9. **Task 9:** Pipeline as Code (*Jenkinsfile*) with Multistage Build, Test, and Server Deployment
10. **Task 10:** Automated Selenium WebDriver Test Design for Critical User Journeys with Failure Screenshots
11. **Task 11:** Continuous Testing Quality Gate in Jenkins with Defect Injection & Correction Verification
12. **Task 12:** Dockerfile Creation, Image Tagging, and Container Lifecycle Automation
13. **Task 13:** Jenkins-to-Docker Continuous Deployment Pipeline with Image Registry Management
14. **Task 15:** Puppet Configuration Management Manifests, Declarative Services, and Target Provisioning
15. **Task 15:** Automated Provisioning, Idempotency Verification, Health Checks, and Rollback/Recovery Testing
