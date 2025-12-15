package io.jgitkins.runner.infrastructure.adapter;

import com.github.dockerjava.api.DockerClient;
import io.jgitkins.runner.application.port.out.ContainerRunnerPort;
import io.jgitkins.runner.application.port.out.RepositorySyncPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Test double for {@link ContainerRunnerPort} that bypasses Docker interactions during integration tests.
 */
@Component
@Primary
@Profile("test")
@SpringBootTest
public class ContainerRunnerPortTest {

    @Autowired
    private RepositorySyncPort repositorySyncPort;

    @Autowired
    private DockerClient dockerClient;
    private ContainerRunnerPort containerRunnerPort;

    @BeforeEach
    void setUp() {
        containerRunnerPort = new DockerRunnerAdapter(dockerClient);
    }

    @Test
    void test_run_successful() {

        String image = "jenkins/jenkinsfile-runner";
        Path workspace = null;
        try {
            String cloneUrl = "http://localhost:8084/git/demo-org/ccc.git";
            String taskCd = "demo-org";
            String repoName = "ccc";
            String ref = "main";
            workspace = repositorySyncPort.syncRepository(cloneUrl, taskCd, repoName, ref);
            containerRunnerPort.run(workspace, image);
        } catch (Exception e) {
//            System.out.println("error : , e.getMessage(), e)k;

            System.out.println("error: " + e);
        }


    }

    //    private final AtomicInteger exitCode = new AtomicInteger(0);
//
//    @Override
//    public int run(Path workspace, String image) {
////        log.info("[TEST] Pretending to run container image {} for workspace {}", image, workspace);
//        return exitCode.get();
//    }
//
//    public void setExitCode(int exitCode) {
//        this.exitCode.set(exitCode);
//    }
}

