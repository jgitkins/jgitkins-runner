package io.jgitkins.runner.presentation.api;

import io.jgitkins.runner.application.dto.RunnerActivateResult;
import io.jgitkins.runner.application.port.in.RunnerActivationUseCase;
import io.jgitkins.runner.presentation.common.ApiResponse;
import io.jgitkins.runner.presentation.dto.RunnerActivateRequest;
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

    private final RunnerActivationUseCase activationUseCase;

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<RunnerActivateResult>> activateRunner(@Valid @RequestBody RunnerActivateRequest request) {

        RunnerActivateResult result = activationUseCase.activate(request.getToken(), request.getBaseUrl());
        return ResponseEntity.ok(ApiResponse.success(result));

    }
}
