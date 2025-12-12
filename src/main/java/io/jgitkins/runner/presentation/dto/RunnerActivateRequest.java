package io.jgitkins.runner.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RunnerActivateRequest {

    @NotBlank
    private String runnerToken;
    @NotBlank
    private String activationEndpoint;
}
