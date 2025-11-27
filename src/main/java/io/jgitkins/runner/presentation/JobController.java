package io.jgitkins.runner.presentation;

import lombok.RequiredArgsConstructor;
import io.jgitkins.runner.application.port.in.JobExecuteCommand;
import io.jgitkins.runner.application.port.in.JobExecuteUseCase;
import io.jgitkins.runner.domain.ExecutionResult;
import io.jgitkins.runner.presentation.api.dto.JobExecuteRequest;
import io.jgitkins.runner.presentation.api.dto.JobExecuteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pipelines")
@RequiredArgsConstructor
public class JobController {

    private final JobExecuteUseCase jobExecuteUseCase;

    @PostMapping("/run")
    public ResponseEntity<JobExecuteResponse> runJob(@RequestBody JobExecuteRequest request) {
        JobExecuteCommand command = JobExecuteCommand.builder()
                .repoUrl(request.getRepoUrl())
                .taskCd(request.getTaskCd())
                .repoName(request.getRepoName())
                .ref(request.getRef())
                .jenkinsfilePath(request.getJenkinsfilePath())
                .dockerImage(request.getDockerImage())
                .build();

        ExecutionResult result = jobExecuteUseCase.execute(command);
        return ResponseEntity.ok(new JobExecuteResponse(result.getStatus(), result.getExitCode()));
    }
}
