# JBoss / WildFly OpenShift Troubleshooting Lab

Hands-on application administration and troubleshooting lab built around WildFly, Java and Red Hat OpenShift.

The goal is not simply to deploy an application. The project is designed to practice application support and administration across the complete request path, from the OpenShift Route down to the application, datasource and database.

## Current Architecture

```text
Browser
  |
OpenShift Route
  |
Service
  |
Pod
  |
WildFly 41
  |
Java WAR
```

The next phase extends the architecture to:

```text
Browser
  |
OpenShift Route
  |
Service
  |
Pod
  |
WildFly 41
  |
donation-app.war
  |
JNDI Datasource
  |
JDBC Connection Pool
  |
PostgreSQL Service
  |
PostgreSQL
```

## Current Status

- Red Hat OpenShift Developer Sandbox
- WildFly 41.0.1.Final
- Java 21
- Jakarta Servlet / JSP application
- Maven WAR packaging
- Custom immutable WildFly container image
- OpenShift internal Image Registry
- OpenShift ImageStream
- Deployment, Service and HTTPS Route
- WildFly deployment scanner
- JBoss CLI administration
- WildFly `server.log` troubleshooting
- Pod recreation tested successfully

## Donation Application

The lab contains a small Java web application packaged as:

```text
donation-app.war
```

The application is intentionally simple so that the infrastructure and application-server layers can be modified and deliberately broken during troubleshooting exercises.

Available endpoints include:

```text
/donation-app/
/donation-app/donate
/donation-app/success
/donation-app/cancel
/donation-app/health
```

### Live Demo

[Open Donation App](https://wildfly-peter-hudcovic-dev.apps.rm2.thpm.p1.openshiftapps.com/donation-app/)

> The application runs in a Red Hat Developer Sandbox environment and may not be available 24/7.

## Container Deployment

The application is packaged into a custom WildFly image:

```dockerfile
FROM quay.io/wildfly/wildfly:41.0.1.Final-jdk21

COPY target/donation-app.war /opt/jboss/wildfly/standalone/deployments/donation-app.war
```

This ensures that the WAR is part of the container image rather than being copied manually into a running Pod.

The difference was verified experimentally:

```text
Manual oc cp
Pod recreated
WAR disappeared
/donation-app returned HTTP 404
```

The application was then moved into an immutable container image.

After Pod recreation:

```text
New Pod
WildFly starts
deployment scanner finds donation-app.war
application is deployed automatically
/donation-app works
```

## WildFly Administration

Deployment status can be inspected through JBoss CLI:

```text
[standalone@localhost:9990 /] deployment-info
```

A healthy deployment reports:

```text
donation-app.war
ENABLED: true
STATUS: OK
```

The management model can also be inspected directly:

```text
/deployment=donation-app.war:read-resource
```

During deployment, WildFly logs provide additional evidence:

```text
WFLYSRV0027  Starting deployment of "donation-app.war"
WFLYUT0021   Registered web context: '/donation-app'
WFLYSRV0010  Deployed "donation-app.war"
```

## Troubleshooting Method

Incidents are investigated systematically through the application stack:

```text
Request
  |
Route
  |
Service
  |
Pod
  |
WildFly
  |
Application
  |
Datasource / JDBC
  |
PostgreSQL
```

Typical tools used in the lab:

```text
oc get
oc describe
oc logs
oc rsh
JBoss CLI
curl
WildFly server.log
```

## First Troubleshooting Incident

The first incident occurred after deploying the WAR manually with `oc cp`.

The application initially worked, but after the OpenShift Pod was recreated:

```text
WildFly root       OK
Donation App       HTTP 404
Pod                Running
Service            OK
Route              OK
JBoss deployment   Missing
```

Root cause:

The WAR existed only in the ephemeral filesystem of the previous container.

Resolution:

Build the WAR into a custom WildFly container image and deploy that image through OpenShift.

This makes application deployment reproducible when Pods are replaced.

## Next Phase

The next stage adds:

- PostgreSQL
- PostgreSQL Service
- JDBC driver
- JBoss Datasource
- JNDI lookup
- Connection pool configuration
- ConfigMaps and Secrets
- Readiness and liveness probes
- Persistent storage

The lab will then deliberately introduce failures such as:

- incorrect PostgreSQL password
- incorrect JDBC URL
- unavailable PostgreSQL
- failed datasource
- connection pool exhaustion
- failed WAR deployment
- HTTP 500 errors
- readiness probe failures
- incorrect ConfigMap or Secret
- JVM resource problems
- rollout and rollback scenarios

The objective is to diagnose each incident using application-server, container and OpenShift evidence rather than simply applying a fix.