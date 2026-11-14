# Component 2: Configuration Management, Containers and Orchestration

## Abstract
This component covers deployment of the lost-and-found application with Ansible to a Multipass Ubuntu VM, packaging in a multi-stage Docker image, and running on Kubernetes with manual scaling. The chosen deployment enhancements are a multi-stage Dockerfile, a ConfigMap, liveness and readiness probes, and a rollback demo.

## Introduction
This component covers the Ansible, Docker and Kubernetes steps of the pipeline code -> Git -> Jenkins CI -> Ansible -> Docker -> Kubernetes. The GitHub repository is `lost-and-found`; the application is `lost-found`.

The Ansible role, Dockerfile and Kubernetes manifests (like the Jenkinsfile and GitHub Actions workflow) were adapted from an earlier project of the author. This reuse was permitted by the course instructor. Only names and the application were replaced.

## Implementation

### Development Environment
- Control machine: macOS (zsh, Homebrew).
- Ansible target: Multipass Ubuntu VM, actual instance name `ansible-node` (Ubuntu 24.04.5 LTS), addressed in the Ansible inventory by the alias `lostfound-vm` at `192.168.252.2`. SSH user `ubuntu`, key-based authentication (`~/.ssh/ansible_node`), interpreter `/usr/bin/python3`.
- Containers and Kubernetes: Docker Desktop with its built-in Kubernetes.
- Base images: `eclipse-temurin:17-jdk` (build) and `eclipse-temurin:17-jre` (runtime).
- Ansible Core: 2.21.6.
- Multipass: 1.16.4+mac.
- Ubuntu VM: 24.04.5 LTS.
- Docker CLI: 29.9.2.
- Docker Desktop: 4.96.3 (244472).
- Kubernetes: v1.36.1 (Docker Desktop, kind, single-node cluster).

### Code
- `ansible/inventory.ini`, `ansible/playbook.yml`, and role `ansible/roles/lost_found/` (tasks, handlers, defaults, template `lost-found.service.j2`).
  - Variables: `app_name` lost-found, `app_port` 8090, `app_user` lostfound, `app_dir` /opt/lost-found, `jar_src` (`target/lost-found.jar` on the control machine), `java_package` openjdk-17-jre-headless.
  - Tasks: install Java with apt, create system user, create directory, copy jar, install systemd unit, enable and start the service. Handler: `Restart lost-found`.
  - Pre-tasks check on the control machine (`delegate_to: localhost`, `become: false`) that the jar exists and fail otherwise. A post-task health check runs on the VM against `http://127.0.0.1:8090/actuator/health` with retries.
- `Dockerfile`: multi-stage, see below.
- `k8s/configmap.yaml` (`lost-found-config`, `PORT: "8080"`), `k8s/deployment.yaml` (3 replicas, image `lost-found:1.0`, `imagePullPolicy: IfNotPresent`, env `PORT` from the ConfigMap, CPU/memory requests and limits, readiness and liveness probes on `/actuator/health`), `k8s/service.yaml` (`lost-found-service`, NodePort 30080 to container port 8080, `sessionAffinity: ClientIP`).

### Description
**Ansible.** From the repository root the playbook is run with `ansible-playbook -i ansible/inventory.ini ansible/playbook.yml`. The jar is built on the macOS control machine and copied to the VM; the service runs as the `lostfound` user under systemd on port 8090. The Ansible tasks are idempotent. The second playbook run completed with `ok=10`, `changed=0`, `unreachable=0`, `failed=0` and `skipped=1`, confirming that no additional configuration changes were required. This is shown in Fig 12.

**Docker.** Stage 1 (`eclipse-temurin:17-jdk`) copies `mvnw`, `pom.xml` and `.mvn/`, runs `dependency:go-offline`, then copies `src/` and runs `package -DskipTests`. Stage 2 (`eclipse-temurin:17-jre`) creates a non-root user `app`, copies the jar to `/app/app.jar`, exposes 8080 and starts `java -jar`. The image is tagged `lost-found:1.0`.

**Kubernetes.** The Deployment runs three replicas whose pod labels (`app: lost-found`) match the Service selector. The Service is a NodePort on 30080. Readiness probe: initial delay 20 s, period 5 s. Liveness probe: initial delay 40 s, period 10 s, failure threshold 3. Warnings from the readiness probe in `kubectl describe` during startup are normal while the JVM boots.

**Scaling.** Scaling was manual (`kubectl scale`, 3 to 5 to 3), not autoscaling.

**Data.** Data is stored in memory per pod, so each pod has its own list. `sessionAffinity: ClientIP` is a workaround that keeps a client on one pod. A shared database is future work.

## Enhancement chosen and why
Multi-stage Dockerfile, ConfigMap, liveness and readiness probes, and a rollback demo.
- **Multi-stage Dockerfile:** no local JDK or Maven is needed to build the image, dependencies sit in a cached layer that source changes do not invalidate, and the runtime container runs as a non-root user. No claim is made about image size.
- **ConfigMap:** the port is configuration kept outside the pod spec.
- **Probes:** readiness keeps traffic away from pods that are still starting; liveness restarts a container after repeated failures.
- **Rollback demo:** the deployment image was set to the nonexistent tag `lost-found:broken`, and then `kubectl rollout undo` was used to return to the previous revision. No failing build or failing test was demonstrated.

## Results
Each placeholder below is where a screenshot will be inserted; the description says what it is meant to show.

[Fig 11: First playbook run against lostfound-vm]

[Fig 12: Second playbook run (changed=0)]

[Fig 13: Service status and process user on the VM]

[Fig 14: Endpoints on port 8090]

[Fig 15: ansible directory tree]

[Fig 17: Dockerfile]

[Fig 18: Docker image build]

[Fig 19: docker images listing]

[Fig 19A: Container run]

[Fig 20: kubectl apply]

[Fig 21: Deployments, services and pods]

[Fig 21A: Service selector and pod labels match]

[Fig 22: Browser UI]

[Fig 23: curl API calls]

[Fig 24: kubectl describe service]

[Fig 25: Scale to 5 replicas]

[Fig 26: Five pods running]

[Fig 27: Scale back to 3 replicas]

[Fig 28: kubectl describe pod]

[Fig 29: Kubernetes events]

[Fig 30: ConfigMap]

[Fig 31: Deployment manifest]

[Fig 32: Broken image tag lost-found:broken]

[Fig 33: Rollback with kubectl rollout undo]

[Fig 34: Git graph]

[Fig 35: Repository network graph (optional)]

## Conclusion
The repository contains an Ansible role for deploying to a Multipass Ubuntu VM, a multi-stage Dockerfile and Kubernetes manifests with a ConfigMap and probes. Limitations: scaling is manual, data is in memory per pod (session affinity is only a workaround and a shared database is future work), and Jenkins is not connected to the Ansible or Kubernetes steps in the Jenkinsfile.

## References
- Ansible documentation: https://docs.ansible.com/
- Docker documentation: https://docs.docker.com/
- Docker multi-stage builds: https://docs.docker.com/build/building/multi-stage/
- Kubernetes documentation: https://kubernetes.io/docs/home/
- Kubernetes probes: https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
- Kubernetes ConfigMaps: https://kubernetes.io/docs/concepts/configuration/configmap/
- Kubernetes Deployments: https://kubernetes.io/docs/concepts/workloads/controllers/deployment/
- Spring Boot reference: https://docs.spring.io/spring-boot/index.html
