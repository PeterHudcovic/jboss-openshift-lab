# OpenShift Donation Lab

A small Java 21 Servlet and JSP application for learning WildFly administration.
It builds a traditional `donation-app.war` for WildFly 41. It has no embedded server.

## Build locally

Use JDK 21 and Maven 3.9 or later. In Windows PowerShell:

```powershell
Set-Location 'C:\Projects\jboss-openshift-lab\app'
java -version
mvn -version
mvn --batch-mode --no-transfer-progress package
```

The output is `target\donation-app.war`. Building does not deploy anything.
Validation tests run during the build. JSP compilation and live HTTP checks require
a later, separately approved WildFly deployment; this build does not run a server.

### Portable tools in this workspace

This workspace also has verified Temurin JDK 21.0.12.1+1 and Apache Maven 3.9.16
under the ignored `.tools` directory. They are local build prerequisites, not WAR
contents or repository dependencies. The system Java installation is unchanged.
To use these tools from Windows PowerShell:

```powershell
Set-Location 'C:\Projects\jboss-openshift-lab\app'
$env:JAVA_HOME = "$PWD\.tools\jdk-21.0.12.1+1"
& '.\.tools\apache-maven-3.9.16\bin\mvn.cmd' --batch-mode --no-transfer-progress '-Dmaven.repo.local=.tools/m2' package
```

The `JAVA_HOME` assignment affects only the current PowerShell process. The
portable tools and Maven cache are not tracked by Git; other checkouts need their
own JDK 21 and Maven installation.

## Endpoints

The expected default context path is `/donation-app`, derived from the WAR name.

| Method | Path within the application | Behavior |
| --- | --- | --- |
| GET | `/` | Home page |
| GET | `/donate` | Donation form |
| POST | `/donate` | Validate amount; redirect with 303 to `/success`, or render the form with 400 |
| GET | `/success` | Demo confirmation page; no payment is taken |
| GET | `/cancel` | Demo cancellation page |
| GET | `/health` | HTTP 200 with plain-text `OK` |

For example, the health URL after deployment will be `/donation-app/health`.
The health endpoint only confirms that the servlet can respond; it checks no external dependencies.
No OpenShift probes are configured by this application.

## Form behavior

Choose EUR 5, 10, or 25, or enter a custom amount from EUR 0.01 to EUR 10,000.00.
A nonblank custom amount overrides the preset. Use a decimal point and at most two
decimal places. Server-side validation uses `BigDecimal`, not floating-point arithmetic.
The form does not preserve submitted amounts after validation errors.

The confirmation page is a preview and can be opened directly. It is not evidence of a payment.
There is no payment provider, database, donation storage, authentication, or session state.

## Runtime and packaging

- Maven coordinates: `tech.peterhudcovic:donation-app:0.1.0-SNAPSHOT`.
- Jakarta Servlet API 6.1.0 has `provided` scope; WildFly supplies it at runtime.
- WildFly also supplies JSP and Expression Language support.
- JUnit Jupiter 5.11.4 is test-only and is not packaged in the WAR.
- Annotated servlets handle `/donate` and `/health`.
- `WEB-INF/web.xml` maps the success/cancel JSP pages and selects `index.jsp` as the welcome file.
- Internal JSP views are under `WEB-INF` and cannot be requested directly.
- CSS is local; no external fonts, scripts, or frontend frameworks are used.

This phase creates a local artifact only. It does not change the existing OpenShift infrastructure.
