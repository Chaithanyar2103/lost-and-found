# Project summary
Lost and Found is a small college website built with Spring Boot 3.3.5 and Java 17 to demonstrate a DevOps pipeline.
Users can report, list, filter, claim and delete items; data is kept in memory with no database.
Code is kept in Git with feature branches merged locally using --no-ff, and in the GitHub repository lost-and-found.
Jenkins (polling every minute) and GitHub Actions run the 10 unit tests before packaging the jar.
Ansible deploys the jar as a systemd service to a Multipass Ubuntu VM over SSH from macOS.
A multi-stage Dockerfile builds the image, which runs on Kubernetes (Docker Desktop) with a ConfigMap, probes and manual scaling.
A rollback demo used a nonexistent image tag followed by kubectl rollout undo.
The pipeline files were adapted from an earlier project of the author, as permitted by the course instructor.
