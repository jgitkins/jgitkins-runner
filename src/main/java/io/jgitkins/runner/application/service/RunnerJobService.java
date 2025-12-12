package io.jgitkins.runner.application.service;

import io.jgitkins.runner.application.port.in.RunnerJobUseCase;
import io.jgitkins.runner.application.port.out.JobFetchPort;
import io.jgitkins.runner.application.port.out.JobResultPort;
import io.jgitkins.runner.application.port.out.ContainerRunnerPort;
import io.jgitkins.runner.application.port.out.RepositorySyncPort;
import io.jgitkins.runner.domain.ExecutionResult;
import io.jgitkins.runner.domain.JobStatus;
import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.server.grpc.JobPayload;
import io.jgitkins.server.grpc.JobResultStatus;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunnerJobService implements RunnerJobUseCase {

    private final JobFetchPort jobFetchPort;
    private final JobResultPort jobResultPort;
    private final RepositorySyncPort repositorySyncPort;
    private final ContainerRunnerPort containerRunnerPort;
    private final RunnerConfigurationService configurationService;
    private final AtomicBoolean jobInProgress = new AtomicBoolean(false);

    @Override
    public void execute() {
        RunnerConfiguration configuration = configurationService.getCurrentConfiguration();
        if (configuration == null || !configurationService.isReadyForScheduling()) {
            return;
        }

        if (jobInProgress.get()) {
            log.debug("Job already running. Skipping poll.");
            return;
        }

        Optional<JobPayload> payloadOptional = jobFetchPort.fetchJob(configuration);
        log.debug("fetching job...");

        payloadOptional.ifPresent(payload -> executeJob(configuration, payload));
    }

    private void executeJob(RunnerConfiguration configuration, JobPayload payload) {
        jobInProgress.set(true);

        try {
            log.info("Starting job {} for repository {}", payload.getJobId(), payload.getRepositoryId());
            Path workspace = repositorySyncPort.syncRepository(payload.getCloneUrl(),
                                                               Long.toString(payload.getJobId()),
                                                               Long.toString(payload.getRepositoryId()),
                                                               payload.getBranchName());

            int exitCode = containerRunnerPort.run(workspace, configuration.getDefaultDockerImage());
            JobStatus jobStatus = exitCode == 0 ? JobStatus.SUCCESS : JobStatus.FAILURE;

            log.info("jobStatus: [{}]", jobStatus);
            ExecutionResult result = new ExecutionResult(jobStatus, exitCode);
            JobResultStatus status = result.getStatus() == JobStatus.SUCCESS ? JobResultStatus.JOB_RESULT_SUCCESS
                                                                             : JobResultStatus.JOB_RESULT_FAILED;
            jobResultPort.reportResult(configuration, payload.getJobId(), status);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Job {} interrupted", payload.getJobId(), ex);
            jobResultPort.reportResult(configuration, payload.getJobId(), JobResultStatus.JOB_RESULT_FAILED);
        } catch (Exception ex) {
            log.error("Job {} execution failed due to unexpected error", payload.getJobId(), ex);
            jobResultPort.reportResult(configuration, payload.getJobId(), JobResultStatus.JOB_RESULT_FAILED);
        } finally {
            jobInProgress.set(false);
        }
    }
}
