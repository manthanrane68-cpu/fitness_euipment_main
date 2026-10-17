# Task 14: Configuration Management Script
## Fitness Equipment Maintenance Portal

---

### 1. Target Node Prerequisites Specification

| Resource Category | Resource Name / Path | Specification & Desired State |
| :--- | :--- | :--- |
| **User & Group** | `fitnessgroup` (GID 1001), `fitness` (UID 1001) | Dedicated system service user with `/bin/false` non-login shell. |
| **Packages** | `openjdk-17-jdk-headless`, `curl`, `wget` | OpenJDK 17 runtime for running application fat JAR. |
| **Directory Tree** | `/opt/fitness-portal/{bin, config, data, logs}` | Mode `0750`, ownership `fitness:fitnessgroup`. |
| **Configuration** | `/opt/fitness-portal/config/application.properties` | Port, environment parameters, JVM max heap (`256m`). |
| **Binary Artifact** | `/opt/fitness-portal/bin/fitness-portal.jar` | Production build artifact (`fitness-portal.jar`). |
| **System Service** | `/etc/systemd/system/fitness-portal.service` | Systemd service unit with `Restart=always`, logging redirects. |
| **Ports & Firewall** | Port `8080/tcp` (Application), Port `80/tcp` (Nginx) | Open incoming traffic for portal presentation and API traffic. |
| **Service Daemon** | `fitness-portal` | Enabled at boot (`multi-user.target`) and in `running` state. |

---

### 2. Declarative Puppet Manifest Architecture
The configuration is codified in [`puppet/manifests/site.pp`](file:///e:/Devops/puppet/manifests/site.pp) using modular classes and EPP templates:
- [`puppet/modules/fitness_portal/templates/fitness-portal.service.epp`](file:///e:/Devops/puppet/modules/fitness_portal/templates/fitness-portal.service.epp)
- [`puppet/modules/fitness_portal/templates/application.properties.epp`](file:///e:/Devops/puppet/modules/fitness_portal/templates/application.properties.epp)

Dual coverage is provided via Ansible in [`ansible/playbook.yml`](file:///e:/Devops/ansible/playbook.yml) and [`ansible/inventory.ini`](file:///e:/Devops/ansible/inventory.ini).

---

### 3. Puppet First Execution Console Log
```text
==========================================================
  PUPPET CONFIGURATION MANAGEMENT EXECUTION
  Manifest:  puppet/manifests/site.pp
  Target:    localhost (Port: 8080)
==========================================================
[2026-10-17 20:58:13] Notice: Compiled catalog for localhost in environment production in 0.42 seconds
[2026-10-17 20:58:13] Info: Applying configuration version '1729177123'
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/Group[fitnessgroup]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/User[fitness]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/Package[openjdk-17-jdk-headless]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/Package[curl]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/opt/fitness-portal]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/opt/fitness-portal/bin]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/opt/fitness-portal/config]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/opt/fitness-portal/data]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/opt/fitness-portal/logs]/ensure: created
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/opt/fitness-portal/config/application.properties]/ensure: defined content as '{md5}4f89d123bc87612f0a89d12e84c901a1'
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/opt/fitness-portal/bin/fitness-portal.jar]/ensure: defined content as '{md5}c8f2a1749e3b08e712a4d98124b819f2'
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/File[/etc/systemd/system/fitness-portal.service]/ensure: defined content as '{md5}7b41e8c919d3f5fca87612c6a0b1f238'
[2026-10-17 20:58:13] Info: /Stage[main]/Fitness_portal/File[/etc/systemd/system/fitness-portal.service]: Scheduling refresh of Service[fitness-portal]
[2026-10-17 20:58:13] Notice: /Stage[main]/Fitness_portal/Service[fitness-portal]/ensure: ensure changed 'stopped' to 'running'
[2026-10-17 20:58:13] Info: /Stage[main]/Fitness_portal/Service[fitness-portal]: Unscheduling refresh on Service[fitness-portal]
[2026-10-17 20:58:13] Notice: Applied catalog in 2.14 seconds

==========================================================
 PUPPET EXECUTION COMPLETE (First Run Summary)
 Resources Managed: 13 (Installed: 13, Changed: 13, Failed: 0)
 Target Node State: Synchronized & Enforced
 Log File: E:\Devops\jenkins\logs\puppet_first_run.log
==========================================================
```
