package io.jgitkins.runner.presentation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.jgitkins.runner.domain.JobStatus;

@Data
@AllArgsConstructor
public class JobExecuteResponse {
    private JobStatus status;
    private int exitCode;
}
