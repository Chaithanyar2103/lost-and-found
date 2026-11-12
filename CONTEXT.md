# CONTEXT.md - project memory between AI sessions

## Project goal
DevOps course assignment. The app is a throwaway college lost-and-found site; only the pipeline matters. No database, auth or frontend framework.

Pipeline: code -> Git -> Jenkins CI -> Ansible -> Docker -> Kubernetes

Pipeline files were adapted from a previous finished project (allowed by the teacher); only names and the app were replaced.

## Chosen enhancements
- CI: automated tests in Jenkins plus a GitHub Actions workflow
- Deployment: multi-stage Dockerfile, ConfigMap, liveness/readiness probes, rollback demo

## Tech decisions
- Spring Boot 3.3.5, Java 17, Maven (wrapper, `./mvnw`); artifact and app name `lost-found`, package `com.example.lostfound`
- `<finalName>lost-found</finalName>` -> `target/lost-found.jar`
- In-memory storage (CopyOnWriteArrayList + AtomicLong)
- `server.port=${PORT:8080}`; only `/actuator/health` exposed
- Validation done manually in the controller (no validation starter)
- 10 JUnit 5 MockMvc tests in `ItemControllerTest` (`@DirtiesContext` after each test so the in-memory store resets)

## Endpoints
- `GET /items[?status=]`; `POST /items` (name, location required -> else 400; status LOST/FOUND, default LOST; invalid status -> 400; 201)
- `POST /items/{id}/claim` -> 200, CLAIMED, idempotent; 404 unknown
- `DELETE /items/{id}` -> 204 / 404
- `GET /info` -> `{"pod":"<HOSTNAME>","version":"1.0"}`
- `GET /` -> `src/main/resources/static/index.html`; `GET /actuator/health`
- Item: id, name, description, location, status, contact, createdAt

## Ports
Container 8080, Ansible `app_port` 8090, NodePort 30080, Jenkins 8081, smoke test 8095.

## Build / run (macOS)
```
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw clean package
PORT=9090 java -jar target/lost-found.jar
```

## CI
- Jenkinsfile: `agent any`, `pollSCM('* * * * *')`, stages Checkout -> Test -> Build -> Archive; `junit` post step.
- `.github/workflows/ci.yml`: test, package, upload artifact `lost-found`.

## Docker
Multi-stage: `eclipse-temurin:17-jdk` build stage, `eclipse-temurin:17-jre` runtime, non-root user `app`, jar at `/app/app.jar`, EXPOSE 8080. Image `lost-found:1.0`.

## Kubernetes
`k8s/configmap.yaml` (`lost-found-config`), `deployment.yaml` (3 replicas, image `lost-found:1.0`, probes on `/actuator/health`), `service.yaml` (`lost-found-service`, NodePort 30080, `sessionAffinity: ClientIP` because data is per pod).

## Ansible
Role `lost_found`, service `lost-found`, user `lostfound`, dir `/opt/lost-found`, port 8090, handler `Restart lost-found`. Target: Ubuntu with systemd (uses apt).

## Progress log
- Session 1: Copied pipeline structure from the previous project, renamed everything, replaced the app (Item model, controller, UI, 10 tests). `./mvnw clean package` passes; jar smoke-tested with curl on port 9090; Docker image built and smoke-tested on port 8095. Nothing pushed; no Git repo initialised yet.

## Remaining
Git init and push, Jenkins job, Ansible run on an Ubuntu host, kubectl demo, screenshots.
