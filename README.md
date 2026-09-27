# Linux Remediation Engine

An automated linux incident remediation platform built with Spring Boot, Spring Data JPA, and SSHJ. The engine ingests infrastructure monitoring alerts (via direct webhooks or ServiceNow push notifications), determines the appropriate remediation strategy using an extensible **Rule-Engine & Strategy Design Pattern**, executes multi-stage Linux SSH diagnostics and remediations, and maintains comprehensive audit logs.

---

## Key Features

* **Configurable Alert Ingestion:** Toggle between direct monitoring webhooks (Prometheus, Nagios, Datadog) and **ServiceNow (SNOW) push webhooks** via `application.properties` feature flags to keep ticket tracking strictly aligned.
* **Autonomous Remediation Strategies:** Strategy based Pattern implementation allowing custom multi-step Linux diagnostics, scope-boundary checks (e.g., OS `/var/log` vs. Application `/app`), and automated recovery execution.
* **Extensibility** You can create your own **RemediationStrategy** implementation that supports alerts based on your existing infrastructure.
* **Customizable Domain-Driven Reports:** Generates structured `RemediationReport` domain objects containing execution timelines, pre-checks, post-check verifications, CSV artifacts, and pre-drafted escalation emails for human intervention.
* **Idempotent Execution:** Prevents duplicate concurrent remediation attempts on the same target host using alert deduplication.
* **Secure SSH Transport:** Uses SSHJ with key-based authentication (`PKCS8` / `OpenSSH`) and `PromiscuousVerifier` host verification to run remote commands cleanly without password dependencies.
* **Complete Audit Trail:** Persists step-by-step SSH stdout, stderr, exit codes, and raw JSON execution reports in relational storage for compliance and reporting.
