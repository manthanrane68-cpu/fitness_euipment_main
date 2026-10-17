# Fitness Equipment Maintenance Portal
> **Puppet-Provisioned DevOps Engineering Project (Tasks 2 – 15)**

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2026-orange.svg)]()
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36.svg)]()
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)]()
[![Puppet](https://img.shields.io/badge/Puppet-Provisioned-FFAE1A.svg)]()
[![Version](https://img.shields.io/badge/Release-v1.0.0--mvp-blue.svg)]()

---

## 1. Project Overview
The **Fitness Equipment Maintenance Portal** is a production-grade DevOps solution tailored for commercial fitness facilities, university recreational centers, and boutique gyms. It replaces slow, error-prone paper logs and verbal repair complaints with a streamlined digital workflow for reporting, triaging, repairing, and analyzing equipment failures.

---

## 2. Core MVP Features
- **Incident Intake:** Fast ticket submission (<30s) with machine ID, zone, category, defect description, and priority.
- **Interactive Registry:** Filterable and searchable table with status/priority badges and record inspector modal.
- **Role-Based Workflow:** State transition engine (*Reported* &rarr; *Triaged* &rarr; *In Repair* &rarr; *Awaiting Parts* &rarr; *Resolved* &rarr; *Closed*).
- **Executive KPI Dashboard:** Real-time metrics on active breakdowns, repair velocity, critical alert counts, zone breakdown, and total expenditure.
- **RESTful API:** Endpoints for health diagnostics, records CRUD, and statistical rollups.
- **DevOps Ecosystem:** Automated Maven builds, Jenkins CI/CD pipeline, Selenium automated regression tests, Docker containerization, and Puppet/Ansible infrastructure provisioning.

---

## 3. Technology Stack
- **Language & Platform:** Java SE (JDK 17/21/26)
- **Build Engine:** Apache Maven 3.9+
- **Frontend Presentation:** HTML5, Vanilla CSS3 (Glassmorphism Dark UI, CSS Variables), Modern ES6 JavaScript
- **Continuous Integration:** Jenkins CI Pipeline (`Jenkinsfile`)
- **Automated Testing:** Selenium WebDriver / Surefire Test Suite with Failure Screenshot Capture
- **Containerization:** Multi-stage Dockerfile
- **Configuration Management:** Puppet Manifests (`puppet/manifests/site.pp`) & Ansible Playbooks (`ansible/playbook.yml`)

---

## 4. Quick Start & Local Execution

### Prerequisites
- JDK 17, 21, or 26 installed and on `PATH`
- Maven 3.9+ installed and on `PATH`

### 1. Build the Application
```bash
mvn clean package
```

### 2. Run the Portal
```bash
java -jar target/fitness-portal.jar
```
Or specify a custom port:
```bash
java -Dserver.port=9090 -jar target/fitness-portal.jar
```

### 3. Access in Browser
Open your web browser and navigate to:
```
http://localhost:8080
```

---

## 5. End-to-End Task Deliverables Index

| Task # | Task Description | Status | Deliverable Link / Evidence |
| :---: | :--- | :---: | :--- |
| **Task 2** | Problem Definition and Scope | Complete | [TASK-02_PROBLEM_DEFINITION_AND_SCOPE.md](file:///e:/Devops/docs/TASK-02_PROBLEM_DEFINITION_AND_SCOPE.md) |
| **Task 3** | Agile Planning & DevOps Workflow | Complete | [TASK-03_AGILE_PLANNING_AND_DEVOPS_WORKFLOW.md](file:///e:/Devops/docs/TASK-03_AGILE_PLANNING_AND_DEVOPS_WORKFLOW.md) |
| **Task 4** | Requirements, Architecture & Tech Setup | Complete | [TASK-04_REQUIREMENTS_ARCHITECTURE_AND_TECH_SETUP.md](file:///e:/Devops/docs/TASK-04_REQUIREMENTS_ARCHITECTURE_AND_TECH_SETUP.md) |
| **Task 5** | Git/GitHub Repository Initialization | Complete | [docs/GIT_WORKFLOW_POLICY.md](file:///e:/Devops/docs/GIT_WORKFLOW_POLICY.md), Initial Skeleton |
| **Task 6** | Feature Branching & PR Workflow | Complete | Feature 1 (`feature/incident-intake`), PR & Merge Evidence |
| **Task 7** | Full MVP Completion & Conflict Resolution | Complete | Full CRUD, Role Workflow, Tag `v1.0.0-mvp`, Conflict Fix |
| **Task 8** | Jenkins CI Installation & CI Build Job | Complete | `jenkins/config.xml`, Automated Build Log & Artifact Archive |
| **Task 9** | Pipeline as Code (`Jenkinsfile`) & Server Deployment | Complete | `Jenkinsfile`, Parametric Deploy, Target Node Validation |
| **Task 10** | Selenium WebDriver Test Automation | Complete | Selenium Test Suite (4 Journeys), Failure Screenshot Tool |
| **Task 11** | Continuous Testing Quality Gate in Jenkins | Complete | Defect Injection Commit, Broken Build Log & Fix Verification |
| **Task 12** | Docker Image & Container Lifecycle | Complete | `Dockerfile`, Lifecycle Automation Script, Log Inspection |
| **Task 13** | Jenkins-Docker Continuous Deployment | Complete | `Jenkinsfile.docker`, Registry Publish, Automated Container CD |
| **Task 14** | Puppet Configuration Management Script | Complete | `puppet/manifests/site.pp`, Service Decl, Node Spec |
| **Task 15** | Automated Provisioning, Idempotency & Rollback | Complete | Provision Script, Idempotency Test, Health Check & Rollback Proof |

---

## 6. Repository & SCM Details
- **Default Branch:** `main`
- **Development Branch:** `develop`
- **Remote Origin:** `https://github.com/manthanrane68-cpu/fitness-equipment-maintenance-portal.git`
- **Author:** Manthan Rane (`manthanrane68@gmail.com`)
