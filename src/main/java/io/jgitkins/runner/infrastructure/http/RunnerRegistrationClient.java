package io.jgitkins.runner.infrastructure.http;

import io.jgitkins.runner.application.port.out.RunnerRegistrationPort;
import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.runner.infrastructure.config.RunnerProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunnerRegistrationClient implements RunnerRegistrationPort {

    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private static final Duration DEFAULT_BUSY_INTERVAL = Duration.ofSeconds(1);
    private static final String DEFAULT_DOCKER_IMAGE = "jenkins/jenkinsfile-runner";
    private static final String DEFAULT_JENKINSFILE = "Jenkinsfile";
    private static final String DEFAULT_REGISTRATION_PATH = "/api/runner/register";

    private final RunnerProperties runnerProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RunnerConfiguration requestConfiguration(String runnerToken, String activationEndpoint) {

        ServerRegistrationRequest request = new ServerRegistrationRequest(runnerToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ServerRegistrationResponse response = restTemplate.postForObject(activationEndpoint, new HttpEntity<>(request, headers), ServerRegistrationResponse.class);
            if (response == null) {
                throw new IllegalStateException("Registration response is empty");
            }
            return RunnerConfiguration.builder()
//                                      .runnerName(response.runnerName() != null ? response.runnerName() : runnerName)
                                      .runnerToken(response.runnerToken() != null ? response.runnerToken() : runnerToken)
                                      .serverHost(response.serverHost() != null ? response.serverHost() : runnerProperties.getServerHost())
                                      .serverPort(response.serverPort() > 0 ? response.serverPort() : runnerProperties.getServerPort())
                                      .pollInterval(Duration.ofMillis(response.pollIntervalMillis() > 0 ? response.pollIntervalMillis() : DEFAULT_POLL_INTERVAL.toMillis()))
                                      .busyWaitInterval(Duration.ofMillis(response.busyWaitIntervalMillis() > 0 ? response.busyWaitIntervalMillis() : DEFAULT_BUSY_INTERVAL.toMillis()))
                                      .defaultDockerImage(defaultIfBlank(response.defaultDockerImage(), DEFAULT_DOCKER_IMAGE))
                                      .defaultJenkinsfilePath(defaultIfBlank(response.defaultJenkinsfilePath(), DEFAULT_JENKINSFILE))
                                      .build();
        } catch (RestClientException e) {
            log.error("Runner registration request failed: {}", e.getMessage());
            throw e;
        }
    }

    private String buildRegistrationUrl() {
        return String.format("http://%s:%d%s",
                             runnerProperties.getServerHost(),
                             runnerProperties.getServerPort(),
                             DEFAULT_REGISTRATION_PATH);
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record ServerRegistrationRequest(String token) {
    }

    private record ServerRegistrationResponse(
        String runnerToken,
        String runnerName,
        String serverHost,
        int serverPort,
        long pollIntervalMillis,
        long busyWaitIntervalMillis,
        String defaultDockerImage,
        String defaultJenkinsfilePath
    ) {
    }
}
