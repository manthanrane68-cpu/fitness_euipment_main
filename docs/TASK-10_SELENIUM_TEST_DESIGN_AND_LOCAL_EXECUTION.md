# Task 10: Selenium Test Design and Local Execution
## Fitness Equipment Maintenance Portal

---

### 1. Test Automation Plan & Critical User Journeys

| Journey # | Journey Name | Target Role | Actions & Interactions | Success Assertions |
| :---: | :--- | :--- | :--- | :--- |
| **Journey 1** | Incident Intake & Validation | Floor Trainer / Staff | Opens intake modal, submits `TM-99` (Woodway Curve Treadmill), Zone A, High Priority. | HTTP 201 Created; Record contains generated `TKT-*` ID; Default status is `Reported`. |
| **Journey 2** | Search & Multi-Criteria Filter | Manager / Tech | Queries search `Matrix`, filters status `In Repair`, filters priority `Critical`. | HTTP 200; Response contains only matching records; Empty state triggered when non-matching. |
| **Journey 3** | Role-Based Workflow Transition | Technician | Selects `TKT-1001`, changes status to `Resolved`, assigns technician `Dave Miller`, adds notes & cost. | HTTP 200; Status updated to `Resolved`; Technician notes and parts cost persisted. |
| **Journey 4** | KPI Dashboard & Health Diagnostics | Admin / Auditor | Probes `/api/health` and `/api/dashboard`. | Health status `UP`, JVM memory valid; KPI counters match fleet distribution. |

---

### 2. Failure Screenshot & State Dump Mechanism
In the event of an assertion failure, the test framework automatically invokes `captureFailureScreenshot(testName, error)` which writes a complete DOM state and stack trace snapshot to:
`target/screenshots/failed_<testName>.html` / `.png`.

---

### 3. Local Test Execution Log
```text
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
```

---

### 4. Test Report Deliverables
- **JUnit / Surefire XML Report:** [`target/surefire-reports/TEST-com.fitness.portal.SeleniumE2ETest.xml`](file:///e:/Devops/target/surefire-reports/TEST-com.fitness.portal.SeleniumE2ETest.xml)
- **HTML Visual Report:** [`target/test-reports/selenium_e2e_report.html`](file:///e:/Devops/target/test-reports/selenium_e2e_report.html)
- **Test Source Code:** [`src/test/java/com/fitness/portal/FitnessPortalServerTest.java`](file:///e:/Devops/src/test/java/com/fitness/portal/FitnessPortalServerTest.java) and [`src/test/java/com/fitness/portal/SeleniumE2ETest.java`](file:///e:/Devops/src/test/java/com/fitness/portal/SeleniumE2ETest.java)
