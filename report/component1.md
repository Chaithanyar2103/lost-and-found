# Component 1: Version Control, Application, Build and Continuous Integration

## Abstract
This component covers the Git repository, the Spring Boot lost-and-found application, the Maven build, the Jenkins pipeline and the chosen CI enhancement. The application is a small college lost-and-found website with an in-memory store; its purpose is to carry a DevOps pipeline, not to be a complete product. The CI enhancement is a set of automated tests that run in a Test stage before the Build stage in the Jenkinsfile, plus a GitHub Actions workflow.

## Introduction
The project follows the pipeline code -> Git -> Jenkins CI -> Ansible -> Docker -> Kubernetes. This component covers the first three steps. The GitHub repository is named `lost-and-found`; the application and artifact are named `lost-found`.

The Jenkinsfile, the GitHub Actions workflow, the Ansible role, the Dockerfile and the Kubernetes manifests were adapted from an earlier project of the author. This reuse was permitted by the course instructor. Only names and the application were replaced.

## Implementation

### Development Environment
- Control machine: macOS, using zsh and Homebrew.
- Application: Spring Boot 3.3.5, Java 17 (`java.version` 17 in `pom.xml`), built with the Maven Wrapper (`./mvnw`).
- Jenkins: Jenkins LTS installed with Homebrew and running natively on the laptop on port 8081. It was started with JDK 21. The project itself targets Java 17; Jenkins did not run on JDK 17.
- GitHub Actions: `ubuntu-latest` runner with Temurin Java 17 (`actions/setup-java@v4`).
- Versions of Git, Maven Wrapper distribution, Jenkins LTS and macOS: [VERIFY].

### Code
- `pom.xml`: parent `spring-boot-starter-parent` 3.3.5; dependencies `spring-boot-starter-web`, `spring-boot-starter-actuator` and `spring-boot-starter-test` (test scope); `<finalName>lost-found</finalName>`, so the artifact is `target/lost-found.jar`.
- `src/main/java/com/example/lostfound/`: `LostFoundApplication`, `Item`, `ItemRequest`, `ItemController`, `InfoController`.
- `src/main/resources/static/index.html`: single-page UI; `application.properties` sets `server.port=${PORT:8080}`.
- `src/test/java/com/example/lostfound/ItemControllerTest.java`: 10 `@Test` methods (MockMvc).
- `Jenkinsfile`: declarative pipeline with `agent any`, `pollSCM('* * * * *')`, and stages Checkout, Test (`./mvnw -B test`), Build (`./mvnw -B package -DskipTests`) and Archive (`target/lost-found.jar`, fingerprinted). A `post` step publishes `target/surefire-reports/*.xml` with `junit`. The Jenkinsfile uses `sh` only because the agent is macOS.
- `.github/workflows/ci.yml`: runs on push and pull request; steps checkout, set up Java 17 with Maven cache, test, package, and upload the artifact `lost-found`.

### Description
**Git.** The repository has the branches `main`, `feature/item-api`, `feature/ci-pipeline`, `feature/ansible-config`, `feature/kubernetes`, `feature/ansible-env`, `feature/readme-update` and `feature/report`. Feature merges were done locally with `--no-ff`. GitHub pull request #4 merged `feature/ansible-config` into `main`. Pull request #1 had accidentally targeted `feature/ansible-config` because the repository's default branch had been set wrongly; the default branch was then corrected to `main`.

**Application.** Endpoints: `GET /items` (optional `status` filter), `POST /items` (`name` and `location` required, otherwise 400; status LOST or FOUND, default LOST), `POST /items/{id}/claim` (idempotent, 404 if unknown), `DELETE /items/{id}` (204 or 404), `GET /info`, `GET /` (UI) and `GET /actuator/health`. Items are held in memory (`CopyOnWriteArrayList` and `AtomicLong`). There is no database or authentication.

**Jenkins.** Jenkins triggers builds by SCM polling every minute (`pollSCM`). No webhook was configured. Build #1 was run manually to register polling. Build #2 started automatically after a push.

## Enhancement chosen and why
Automated tests plus GitHub Actions. The 10 JUnit 5 MockMvc tests in `ItemControllerTest` run in a Test stage placed before the Build stage in the Jenkinsfile, so packaging happens only after the test stage has completed. Jenkins publishes the surefire reports through `junit`. The GitHub Actions workflow runs the same test and package commands on every push and pull request and uploads the jar as an artifact, giving a second CI system independent of the laptop.

## Results
Each placeholder below is where a screenshot will be inserted; the description says what it is meant to show.

[Fig 0: Local build with ./mvnw clean package]

[Fig 1: Pull request checks on GitHub]

[Fig 2: Merged pull request]

[Fig 3: Jenkins build started by an SCM change]

[Fig 4: Jenkins console output]

[Fig 5: Jenkins Stage View]

[Fig 6: Jenkins Test Result page]

[Fig 7: Archived lost-found.jar in the Jenkins build]

[Fig 8: Jenkins job configuration]

[Fig 9: Jenkinsfile on GitHub]

[Fig 10: GitHub Actions run with the lost-found artifact]

No failing build or failing test was demonstrated in this project.

## Conclusion
The repository, Maven build and Jenkins pipeline are defined in the files described above, with tests running before the build and the jar archived. A GitHub Actions workflow provides a second CI path. Limitations: Jenkins runs on a laptop and uses polling rather than a webhook, and the application stores data in memory only.

## References
- Git documentation: https://git-scm.com/doc
- Apache Maven: https://maven.apache.org/guides/
- Jenkins documentation: https://www.jenkins.io/doc/
- Jenkins Pipeline syntax: https://www.jenkins.io/doc/book/pipeline/syntax/
- GitHub Actions documentation: https://docs.github.com/en/actions
- Spring Boot reference: https://docs.spring.io/spring-boot/index.html
