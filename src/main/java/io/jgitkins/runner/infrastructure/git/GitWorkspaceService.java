package io.jgitkins.runner.infrastructure.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;

import static io.jgitkins.runner.infrastructure.Constants.WORK_SPACE_ROOT;

@Slf4j
@Component
public class GitWorkspaceService {


//    public Path prepareWorkspace(String repoUrl, String commitId, String branch) throws Exception {
    public Path prepareWorkspace(String repoUrl, String taskCd, String repoName, String branch) throws Exception {

//        Path workSpace = Paths.get(String.format("%s/%s/%s", WORK_SPACE_ROOT, taskCd, repoName));
//        Files.createDirectories(workSpace);
        Path workspace = Paths.get(WORK_SPACE_ROOT, taskCd, repoName);
        Files.createDirectories(workspace);


        if (!Files.exists(workspace.resolve(".git"))) {
            log.info("Cloning repo... {} into {}", repoUrl, workspace);

            try (Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(workspace.toFile())  // ★ 소스+ .git 이 이 경로에 생성
                    .setBranch(branch)                 // 필요 없으면 제거 가능
                    .call()) {
                // 필요시 여기서 추가 작업 가능 (예: 특정 커밋 체크아웃 등)
            }
        } else {
            log.info("Workspace already exists: {}", workspace);
        }
//        String folder = repoUrl.replaceAll("[^a-zA-Z0-9]", "_");
//        Path workspace = workSpace.resolve(folder);
//
//
//        if (!Files.exists(workspace.resolve(".git"))){
//            log.info("Cloning repo... {}", repoUrl);
//
//            try (Git git = Git.cloneRepository()
//                    .setURI(repoUrl)
//            ){
//
//            }
//
//        }


//        if (!Files.exists(workspace)) {
//            log.info("Cloning repo... {}", repoUrl);
//            Git.cloneRepository()
//                    .setURI(repoUrl)
////                    .setDirectory(workspace.toString())
//                    .setGitDir(workspace.toFile())
//                    .call();
//        }

        try (Git git = Git.open(workspace.toFile())) {
//            if (commitId != null && !commitId.isBlank()) {
//                git.checkout().setName(commitId).call();
//            } else if (branch != null && !branch.isBlank()) {
//                git.checkout().setName(branch).call();
//            }
            git.checkout().setName(branch).call();
        }

        log.info("Workspace ready: {}", workspace);
        return workspace;
    }
}
