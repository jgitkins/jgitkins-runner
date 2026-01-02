package io.jgitkins.runner.infrastructure.adapter;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import io.jgitkins.runner.application.port.out.ContainerRunnerPort;
import io.jgitkins.runner.application.port.out.RepositorySyncPort;
import io.jgitkins.runner.infrastructure.git.GitRepositorySyncAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
//@SpringBootTest
public class ContainerRunnerPortTest {

    @Autowired
    private RepositorySyncPort repositorySyncPort;

    @Autowired
    private DockerClient dockerClient;
    private ContainerRunnerPort containerRunnerPort;

    @BeforeEach
    void setUp() {
        repositorySyncPort = new GitRepositorySyncAdapter();
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        DockerHttpClient httpClient = new OkDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);

        containerRunnerPort = new DockerRunnerAdapter(dockerClient);
    }

    @Test
    void test_run_successful() {

        String image = "jenkins/jenkinsfile-runner";
        String pluginConfigPath = "/Users/hwiryungkim/TMP/runner/plugins.txt";
        Path workspace = null;
        try {
            String cloneUrl = "http://localhost:8084/git/demo-org/ccc.git";
            String taskCd = "demo-org";
            String repoName = "ccc";
            String ref = "378e2aff9a5bddf5f2db16d1e9182cfdf230ac29";
            workspace = repositorySyncPort.syncRepository(cloneUrl, taskCd, repoName, ref);
            containerRunnerPort.run(workspace.toAbsolutePath().toString(), image, pluginConfigPath);
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
