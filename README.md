# 🏃 JGitkins Runner

**A Lightweight, Customizable CI Runner for Jenkinsfiles**

JGitkins Runner is a specialized CI execution application built on top of [Jenkinsfile Runner](https://github.com/jenkinsci/jenkinsfile-runner).
It wraps the official `jenkins/jenkinsfile-runner` within a Spring Boot application, orchestrating builds using Docker.

## 🎯 Target Audience
This project is designed for:
- organizations using [jgitkins-server](https://github.com/jgitkins/jgitkins-server) sefl hosted Git server
- Teams familiar with **Jenkins** syntax who want a lighter, more modern execution model than a full Jenkins Master.

## 🏗 Architecture & Design

### 🤝 Similar to GitLab Runner
If you are familiar with **GitLab Runner**, you will feel right at home. JGitkins Runner adopts a similar **Executor** pattern:
- Just as GitLab Runner asks the GitLab Server for jobs and executes them using a specific executor (Shell, Docker, K8s),
- **JGitkins Runner** polls the JGitkins Server for pending builds and executes them using the **Docker Executor**.
- It provides the same "Coordinator (Server) ↔ Runner (Agent)" architecture that has proven scalable and robust in the industry.

### Core Concept: Docker out of Docker (DooD)
JGitkins Runner does not run a Docker daemon inside itself. Instead, it utilizes the **Docker out of Docker (DooD)** pattern:
- The runner application mounts the host's Docker socket (`/var/run/docker.sock`).
- It spawns ephemeral containers (using `jenkins/jenkinsfile-runner`) on the host to execute pipelines.
- **Benefit**: Faster startup, shared image cache, and lower resource overhead compared to DinD.

### Tech Stack
- **Language**: Java (Spring Boot)
- **Execution Engine**: [Jenkinsfile Runner](https://github.com/jenkinsci/jenkinsfile-runner)
- **Containerization**: Docker (DooD)

---

## 🚀 Features & Roadmap

### ✅ Implemented (POC)
- [x] **Basic Pipeline Execution**: Runs a `Jenkinsfile` using a Docker container.
- [x] **DooD Integration**: Successfully binds host Docker socket for execution.

### 📅 Planned Features (Roadmap)

#### 1. Runner Management
- [ ] **Runner Registration API**: Logic to register this runner with the JGitkins Server (similar to GitLab Runner registration).
- [ ] **CLI Support**: Command-line interface for easy setup and registration.

#### 2. Pipeline & Plugin Management
- [ ] **YAML-based Plugin Management**: Define required Jenkins plugins in a `plugins.yaml` file to be pre-loaded.
- [ ] **Git Fetching**: Native ability to fetch git repositories upon receiving a trigger event.

#### 3. Workspace & Resource Management
- [ ] **Workspace Cleansing**: Automatic cleanup of workspace directories after build completion.
- [ ] **Resource Limits**: CPU/Memory constraints for build containers.

#### 4. Integration
- [ ] **Message Queue (MQ)**: Asynchronous communication with JGitkins Server for job distribution (Modular design).

---

## ❓ FAQ / Design Decisions

**Q: Why Spring Boot wrapping Jenkinsfile Runner?**
A: To provide a robust control layer (API, MQ listeners, security) around the raw execution engine, making it manageable like a daemon.

**Q: How are plugins managed?**
A: We aim to move away from the UI-based plugin management of traditional Jenkins. Plugins will be defined declaratively (Code-as-Configuration).