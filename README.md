# lost-found

A small college lost-and-found website (Spring Boot 3.3, Java 17) used to demonstrate a full DevOps pipeline:

**code -> Git -> Jenkins CI -> Ansible -> Docker -> Kubernetes**

> The pipeline files (Jenkinsfile, GitHub Actions workflow, Ansible role, Dockerfile, Kubernetes manifests) were adapted from a previous project; only the application and names were replaced.

## Features
- Report lost or found items, list them, filter by status, claim and delete them.
- Single-page UI at `/` (one HTML file, vanilla JS).
- In-memory storage only (no database, no auth, no frontend framework).

## API
| Method | Path | Notes |
|--------|------|-------|
| GET | `/items` | optional `?status=LOST\|FOUND\|CLAIMED` |
| POST | `/items` | `name`, `location` required (else 400); `status` LOST or FOUND, default LOST |
| POST | `/items/{id}/claim` | sets CLAIMED (idempotent); 404 if unknown |
| DELETE | `/items/{id}` | 204 or 404 |
| GET | `/info` | `{"pod":"<hostname>","version":"1.0"}` |
| GET | `/actuator/health` | health probe |

## Enhancements
- CI: automated JUnit tests run in Jenkins and GitHub Actions before the build.
- Deployment: multi-stage Dockerfile, ConfigMap, liveness/readiness probes, rollback demo.

## Commands (macOS, zsh)
Requires JDK 17.
```
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw clean package
PORT=9090 java -jar target/lost-found.jar
curl -X POST -H "Content-Type: application/json" -d '{"name":"Keys","location":"Library","status":"FOUND"}' localhost:9090/items
curl localhost:9090/items
curl localhost:9090/actuator/health
```
Default port is 8080 (`PORT` env var overrides it).

### Docker
```
docker build -t lost-found:1.0 .
docker run --rm -p 8095:8080 lost-found:1.0
```

### Jenkins
Jenkins runs on port 8081. Create a Pipeline job from SCM pointing at this repo; the `Jenkinsfile` polls every minute and runs Checkout -> Test -> Build -> Archive (`target/lost-found.jar`). The agent needs JDK 17.

### Ansible
See `ansible/README.md` (service `lost-found`, port 8090; Ubuntu target with systemd).

### Kubernetes
See `k8s/README.md` (3 replicas, NodePort 30080).

## Layout
```
src/            Spring Boot app, tests, static UI
Jenkinsfile     Jenkins pipeline
.github/        GitHub Actions workflow
ansible/        playbook + role lost_found
Dockerfile      multi-stage build
k8s/            ConfigMap, Deployment, Service
```

## Status
Pipeline stages are being added step by step; see CONTEXT.md for the progress log.
Jenkins SCM polling test.
