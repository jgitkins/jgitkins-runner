# JGitkins Runner Getting Started

## PreRequirements

### 1) Java Runtime (JRE 17)
- This runner is built and executed with Java 17.
- JRE 17 must be installed on the server/agent host.

### 2) Docker Engine + DooD Access
- The runner uses the host Docker via the DooD pattern.
- Requirements:
  - Docker Engine installed
  - Runner process can access `/var/run/docker.sock`
  - Typically requires `docker` group membership or root privileges

### 3) Network Access
- Outbound access to SaaS REST/gRPC endpoints is required.
- Network access is required to clone Git repositories.
- Network access is required to pull Docker images from registries.

### 4) Disk/Filesystem Permissions
- Local H2 DB is created at `~/runner`.
- The runtime user must have write access to the home directory.
- Ensure enough disk space for workspace and logs.

### 5) Time Synchronization
- If you enforce token/certificate expiration policies,
  time sync between server and runner is required (NTP recommended).

### 6) Security Considerations
- Docker socket access is effectively root-equivalent.
- Use dedicated hosts or isolated nodes for runners in production.

## Configuration

### Using application-example.yml
- Copy `application-example.yml` to `application.yml` and edit values.
- Or inject a config file at runtime.

```bash
java -jar runner.jar start --spring.config.location=./application.yml
```

### Docker Configuration Injection
- Use `runner.docker.*` to override Docker connectivity.
- If unset, `DOCKER_HOST`, `DOCKER_TLS_VERIFY`, and `DOCKER_CERT_PATH` are used.

## CLI Exit Codes
- `0`: Success
- `2`: Invalid arguments (missing `--token` or `--server`)
- `3`: Auth/token rejected (401/403/409)
- `4`: Other 4xx client errors
- `5`: 5xx server errors
- `10`: Unexpected errors
