package io.jgitkins.runner.application.port.out;

import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.server.grpc.JobResultStatus;

public interface JobResultPort {

    void reportResult(RunnerConfiguration configuration, long jobId, JobResultStatus status);
}
