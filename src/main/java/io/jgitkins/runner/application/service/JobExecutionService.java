package io.jgitkins.runner.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.jgitkins.runner.application.port.in.JobExecuteCommand;
import io.jgitkins.runner.application.port.in.JobExecuteUseCase;
import io.jgitkins.runner.application.port.out.ContainerRunnerPort;
import io.jgitkins.runner.application.port.out.WorkspacePort;
import io.jgitkins.runner.domain.ExecutionResult;
import io.jgitkins.runner.domain.JobStatus;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutionService implements JobExecuteUseCase {

    private final WorkspacePort workspacePort;
    private final ContainerRunnerPort containerRunnerPort;

    @Override
    public ExecutionResult execute(JobExecuteCommand command) {
        try {
            Path workspace = workspacePort.prepareWorkspace(
                    command.getRepoUrl(),
                    command.getTaskCd(),
                    command.getRepoName(),
                    command.getRef());

            int exitCode = containerRunnerPort.run(workspace, command.getDockerImage());
            JobStatus status = exitCode == 0 ? JobStatus.SUCCESS : JobStatus.FAILURE;
            return new ExecutionResult(status, exitCode);
        } catch (Exception e) {
            log.error("Job execution error", e);
            return new ExecutionResult(JobStatus.ERROR, -1);
        }
    }
}
