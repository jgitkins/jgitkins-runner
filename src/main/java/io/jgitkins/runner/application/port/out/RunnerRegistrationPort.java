package io.jgitkins.runner.application.port.out;

import io.jgitkins.runner.domain.RunnerConfiguration;

public interface RunnerRegistrationPort {

    RunnerConfiguration requestConfiguration(String runnerToken, String runnerName);
}
