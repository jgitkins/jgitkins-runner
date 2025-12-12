package io.jgitkins.runner.presentation.api;

import io.jgitkins.runner.application.service.RunnerConfigurationService;
import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.runner.presentation.dto.RunnerActivateRequest;
import io.jgitkins.runner.presentation.dto.RunnerLinkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runner")
@RequiredArgsConstructor
public class RunnerActivateController {

    private final RunnerConfigurationService configurationService;

    @PostMapping("/activate")
    public ResponseEntity<RunnerLinkResponse> activateRunner(@Valid @RequestBody RunnerActivateRequest request) {
        RunnerConfiguration configuration = configurationService.registerWithToken(request.getRunnerToken(), request.getActivationEndpoint());
        RunnerLinkResponse response = RunnerLinkResponse.builder()
                                                        .message("Runner가 서버와 연동되었습니다.")
                                                        .serverHost(configuration.getServerHost())
                                                        .serverPort(configuration.getServerPort())
                                                        .defaultDockerImage(configuration.getDefaultDockerImage())
                                                        .defaultJenkinsfilePath(configuration.getDefaultJenkinsfilePath())
                                                        .build();
        return ResponseEntity.ok(response);
    }
}
