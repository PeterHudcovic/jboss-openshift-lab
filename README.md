# JBoss / WildFly OpenShift Troubleshooting Lab

Hands-on application administration and troubleshooting lab built around WildFly, Java, PostgreSQL and Red Hat OpenShift.

The goal is not simply to deploy an application. The project is designed to practice application support and administration across the complete request path, from an HTTPS request through OpenShift and WildFly down to a persistent PostgreSQL database.

## Current Architecture

```text
Browser
  |
HTTPS
  |
OpenShift Route
  |
WildFly Service
  |
WildFly Pod
  |
WildFly 41
  |
donation-app.war
  |
JNDI
  |
DonationDS
  |
JBoss JDBC Connection Pool
  |
PostgreSQL JDBC Driver
  |
PostgreSQL Service
  |
PostgreSQL Pod
  |
PVC
  |
Persistent Volume
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
- OpenShift Deployment, Service and HTTPS Route
- PostgreSQL 16
- PostgreSQL JDBC driver 42.7.13
- JBoss Datasource `DonationDS`
- JNDI name `java:/jdbc/DonationDS`
- JDBC connection pool
- OpenShift Secret based database credentials
- PostgreSQL ClusterIP Service
- 5 GiB PersistentVolumeClaim using the `gp3` StorageClass
- Persistent database storage verified by Pod recreation
- WildFly deployment scanner
- JBoss CLI administration
- WildFly `server.log` troubleshooting
- End-to-end database INSERT verified

## Donation Application

The lab contains a small Java web application packaged as:

```text
donation-app.war
```

The application provides a simple donation flow while keeping the application itself small enough to focus on infrastructure, application-server administration and troubleshooting.

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

## Application Flow

A successful donation request follows this path:

```text
Browser
  |
OpenShift Route
  |
Service
  |
WildFly
  |
DonateServlet
  |
JNDI lookup
  |
java:/jdbc/DonationDS
  |
JBoss Connection Pool
  |
PostgreSQL JDBC Driver
  |
PostgreSQL Service
  |
PostgreSQL
  |
INSERT INTO donations
```

The application does not contain PostgreSQL credentials or a direct database hostname configuration.

It requests the datasource from WildFly through:

```text
java:/jdbc/DonationDS
```

WildFly manages the database connection and connection pool.

## PostgreSQL

The application uses PostgreSQL 16 running as a separate OpenShift workload.

Database:

```text
donations
```

Application user:

```text
donation
```

Table:

```sql
CREATE TABLE donations (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

A donation submitted through the web application is stored using a prepared SQL statement.

## JBoss Datasource

WildFly provides the application with:

```text
java:/jdbc/DonationDS
```

Datasource configuration:

```text
JDBC URL:
jdbc:postgresql://postgresql:5432/donations

Driver:
postgresql-42.7.13.jar

Connection pool:
min 2
max 10
```

Database credentials are supplied to the WildFly Pod through an OpenShift Secret.

The WildFly configuration references environment variables rather than storing the password directly in the container image:

```text
${env.POSTGRES_USER}
${env.POSTGRES_PASSWORD}
```

Datasource connectivity was verified through JBoss CLI:

```text
/subsystem=datasources/data-source=DonationDS:test-connection-in-pool
```

Successful result:

```text
"outcome" => "success"
"result" => [true]
```

## Container Deployment

The application uses a custom WildFly image based on:

```text
quay.io/wildfly/wildfly:41.0.1.Final-jdk21
```

The image contains:

```text
WildFly 41
Java 21
donation-app.war
PostgreSQL JDBC driver
DonationDS configuration
```

The datasource is configured during the container image build using JBoss CLI.

The resulting image is stored in the OpenShift internal image registry and referenced by the WildFly Deployment.

## Persistent Storage

PostgreSQL uses a PersistentVolumeClaim:

```text
Name: postgresql-data
Size: 5 GiB
StorageClass: gp3
Access mode: ReadWriteOnce
```

The persistence test was performed by:

```text
INSERT test record
  |
Delete PostgreSQL Pod
  |
OpenShift creates new PostgreSQL Pod
  |
Same PVC is mounted
  |
SELECT test record
```

The test record remained available after Pod recreation, confirming that database data is independent of the Pod lifecycle.

## WildFly Administration

Deployment state can be inspected through JBoss CLI:

```text
deployment-info
```

A healthy application deployment reports:

```text
donation-app.war
ENABLED: true
STATUS: OK
```

Installed JDBC drivers can be inspected using:

```text
/subsystem=datasources:installed-drivers-list
```

The management model can be queried directly:

```text
/deployment=donation-app.war:read-resource
```

During application deployment, WildFly logs include:

```text
WFLYSRV0027  Starting deployment of "donation-app.war"
WFLYUT0021   Registered web context: '/donation-app'
WFLYSRV0010  Deployed "donation-app.war"
```

## Troubleshooting Method

Incidents are investigated layer by layer:

```text
Client
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
JNDI / Datasource
  |
JDBC
  |
PostgreSQL Service
  |
PostgreSQL
  |
Persistent Storage
```

Typical tools used in the lab:

```text
oc get
oc describe
oc logs
oc rsh
oc exec
JBoss CLI
psql
curl
WildFly server.log
Docker
Git
```

## Troubleshooting Incidents

### 1. WAR disappeared after Pod recreation

Initial deployment used:

```text
oc cp
```

The application initially worked, but after OpenShift recreated the WildFly Pod:

```text
WildFly          Running
Route            OK
Service          OK
Donation App     HTTP 404
JBoss deployment Missing
```

Root cause:

The WAR existed only in the ephemeral filesystem of the old container.

Resolution:

Build `donation-app.war` into a custom immutable WildFly container image.

Result:

A newly created WildFly Pod automatically contains and deploys the application.

### 2. PostgreSQL CrashLoopBackOff

The initial PostgreSQL deployment used the standard `postgres:16` image.

The Pod entered:

```text
CrashLoopBackOff
```

Logs showed:

```text
Operation not permitted
initdb: could not change permissions
```

OpenShift was running the container under the `restricted-v2` Security Context Constraint with an arbitrary UID.

Resolution:

Use an OpenShift-compatible PostgreSQL 16 container image.

Result:

```text
PostgreSQL Pod
1/1 Running
0 restarts
```

### 3. JDBC driver deployment failure

The PostgreSQL JDBC JAR worked during local Docker testing but failed in OpenShift.

WildFly created:

```text
postgresql-42.7.13.jar.failed
```

The runtime UID assigned by OpenShift could not read the JAR because of container filesystem permissions.

Resolution:

Adjust image permissions for OpenShift's arbitrary UID model and group `0`.

Result:

```text
postgresql-42.7.13.jar.deployed
donation-app.war.deployed
```

### 4. WildFly filesystem permission failure

After configuring the datasource during the image build, the OpenShift container failed with:

```text
server.log: Permission denied
WFLYSRV0289: Unable to create auth dir
```

Root cause:

Build-time WildFly execution created runtime files and directories with permissions incompatible with the arbitrary UID used by OpenShift.

Resolution:

Clean build-time WildFly runtime artifacts and configure the standalone directory for group `0` access.

Result:

```text
WildFly Pod
1/1 Running
0 restarts
```

### 5. PostgreSQL persistence verification

PostgreSQL was initially running with ephemeral Pod storage.

A 5 GiB `gp3` PersistentVolumeClaim was added.

Persistence was verified by:

```text
INSERT 99.99
Delete PostgreSQL Pod
New PostgreSQL Pod created
SELECT FROM donations
99.99 still present
```

This confirmed that database storage survives Pod replacement.

## End-to-End Verification

The final application flow was tested through the public web interface.

A donation submitted through the browser successfully produced a row in PostgreSQL.

This verifies the complete path:

```text
Browser
→ OpenShift Route
→ Service
→ WildFly
→ donation-app.war
→ JNDI
→ DonationDS
→ JDBC Connection Pool
→ PostgreSQL JDBC Driver
→ PostgreSQL Service
→ PostgreSQL Pod
→ Persistent Storage
```

## Next Phase

The infrastructure is now ready for deliberate troubleshooting scenarios, including:

- incorrect PostgreSQL password
- incorrect JDBC URL
- unavailable PostgreSQL
- failed datasource
- connection pool exhaustion
- failed WAR deployment
- HTTP 500 errors
- readiness and liveness probe failures
- incorrect ConfigMap or Secret
- JVM memory pressure
- rollout and rollback scenarios

The objective is to diagnose each incident using evidence from the application, WildFly, container runtime and OpenShift rather than simply applying a fix.