package io.jgitkins.runner.application.port.out;

import java.nio.file.Path;

public interface WorkspacePort {
    Path prepareWorkspace(String repoUrl, String taskCd, String repoName, String ref) throws Exception;
}
