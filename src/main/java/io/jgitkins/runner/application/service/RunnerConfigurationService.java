package io.jgitkins.runner.application.service;

import io.jgitkins.runner.application.port.out.RunnerConfigurationPort;
import io.jgitkins.runner.application.port.out.RunnerRegistrationPort;
import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.runner.infrastructure.config.RunnerProperties;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunnerConfigurationService {

    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private static final Duration DEFAULT_BUSY_INTERVAL = Duration.ofSeconds(1);
    private static final String DEFAULT_DOCKER_IMAGE = "jenkins/jenkinsfile-runner";
    private static final String DEFAULT_JENKINSFILE = "Jenkinsfile";

    private final RunnerConfigurationPort configurationPort;
    private final RunnerProperties runnerProperties;
    private final RunnerRegistrationPort registrationPort;
    private final AtomicReference<RunnerConfiguration> currentConfiguration = new AtomicReference<>();

    @PostConstruct
    public void initialize() {
        RunnerConfiguration configuration = configurationPort.loadConfiguration()
                                                                   .map(this::mergeWithDefaults)
                                                                   .orElseGet(() -> mergeWithDefaults(RunnerConfiguration.fromProperties(runnerProperties)));
        currentConfiguration.set(configuration);
        logConfigurationStatus(configuration);
    }

    public RunnerConfiguration getCurrentConfiguration() {
        return currentConfiguration.get();
    }

    public boolean isReadyForScheduling() {
        RunnerConfiguration configuration = currentConfiguration.get();
        return configuration != null && configuration.isReadyForScheduling();
    }

    public long pollIntervalMillis() {
        RunnerConfiguration configuration = currentConfiguration.get();
        Duration duration = configuration != null && configuration.getPollInterval() != null
            ? configuration.getPollInterval()
            : DEFAULT_POLL_INTERVAL;
        return duration.toMillis();
    }

    public RunnerConfiguration applyServerConfiguration(RunnerConfiguration serverConfiguration) {
        RunnerConfiguration merged = mergeWithDefaults(serverConfiguration);
        configurationPort.saveConfiguration(merged);
        currentConfiguration.set(merged);
        log.info("Activated Successful");
        return merged;
    }

    public RunnerConfiguration registerWithToken(String runnerToken, String activateEndpoint) {
        RunnerConfiguration configuration = registrationPort.requestConfiguration(runnerToken, activateEndpoint);
        log.info("Activating...");
        return applyServerConfiguration(configuration);
    }

    private void logConfigurationStatus(RunnerConfiguration configuration) {
        if (!configuration.isLinked()) {
            log.warn("서버 연동이 필요합니다. Runner 토큰을 등록하고 서버 연동 API를 호출하세요.");
        } else if (!configuration.isReadyForScheduling()) {
            log.warn("Runner 설정이 아직 완전하지 않아 스케줄링이 비활성화되어 있습니다.");
        } else {
            log.info("Runner 설정 로딩 완료. 서버: {}:{}, 토큰: {}", configuration.getServerHost(), configuration.getServerPort(), configuration.getRunnerToken());
        }
    }

    private RunnerConfiguration mergeWithDefaults(RunnerConfiguration configuration) {
        if (configuration == null) {
            return RunnerConfiguration.fromProperties(runnerProperties);
        }
        Duration pollInterval = configuration.getPollInterval() == null || configuration.getPollInterval().isZero()
            ? DEFAULT_POLL_INTERVAL
            : configuration.getPollInterval();

        Duration busyInterval = configuration.getBusyWaitInterval() == null || configuration.getBusyWaitInterval().isZero()
            ? DEFAULT_BUSY_INTERVAL
            : configuration.getBusyWaitInterval();

        return configuration.toBuilder()
                            .runnerName(defaultIfBlank(configuration.getRunnerName(), runnerProperties.getRunnerName()))
                            .serverHost(defaultIfBlank(configuration.getServerHost(), runnerProperties.getServerHost()))
                            .serverPort(configuration.getServerPort() > 0 ? configuration.getServerPort() : runnerProperties.getServerPort())
                            .pollInterval(pollInterval)
                            .busyWaitInterval(busyInterval)
                            .defaultDockerImage(defaultIfBlank(configuration.getDefaultDockerImage(), DEFAULT_DOCKER_IMAGE))
                            .defaultJenkinsfilePath(defaultIfBlank(configuration.getDefaultJenkinsfilePath(), DEFAULT_JENKINSFILE))
                            .build();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
