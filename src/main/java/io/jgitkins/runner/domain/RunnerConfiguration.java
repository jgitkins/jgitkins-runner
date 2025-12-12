package io.jgitkins.runner.domain;

import io.jgitkins.runner.infrastructure.config.RunnerProperties;
import java.time.Duration;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RunnerConfiguration {

    String runnerName;
    String runnerToken;
    String serverHost;
    int serverPort;
    Duration pollInterval;
    Duration busyWaitInterval;
    String defaultDockerImage;
    String defaultJenkinsfilePath;

    public boolean isLinked() {
        return runnerToken != null && !runnerToken.isBlank();
    }

    public boolean isReadyForScheduling() {
        return isLinked()
               && serverHost != null && !serverHost.isBlank()
               && serverPort > 0
               && defaultDockerImage != null && !defaultDockerImage.isBlank()
               && defaultJenkinsfilePath != null && !defaultJenkinsfilePath.isBlank();
    }

    public static RunnerConfiguration fromProperties(RunnerProperties properties) {
        return RunnerConfiguration.builder()
                                  .runnerName(properties.getRunnerName())
                                  .serverHost(properties.getServerHost())
                                  .serverPort(properties.getServerPort())
                                  .build();
    }
}
