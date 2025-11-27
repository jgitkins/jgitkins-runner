package io.jgitkins.runner.presentation.api.dto;

import lombok.Data;

@Data
public class JobExecuteRequest {
    private String repoUrl;
    private String taskCd;
    private String repoName;
    private String ref;
    private String jenkinsfilePath;
    private String dockerImage;
}
