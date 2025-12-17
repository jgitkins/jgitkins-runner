package io.jgitkins.runner.infrastructure.adapter;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.jgitkins.runner.application.port.out.ContainerRunnerPort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerRunnerAdapter implements ContainerRunnerPort {

    private final DockerClient dockerClient;

    @Override
    public int run(String repositoryPath, String image, String pluginPath) {
        logContainerInfo(image, repositoryPath);
        List<Bind> binds = buildBinds(repositoryPath, pluginPath);
        String containerId = createContainer(image, binds).getId();

        dockerClient.startContainerCmd(containerId).exec();
        streamContainerLogs(containerId);

        Integer exitCode = dockerClient.waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();
        log.info("Jenkinsfile Runner exit code: {}", exitCode);

        removeContainer(containerId);
        return exitCode;
    }

    private void logContainerInfo(String image, String repositoryPath) {
        log.info("Running Jenkinsfile Runner container: {}", image);
        if (StringUtils.hasText(repositoryPath)) {
            log.info("Running WorkSpace : {}", repositoryPath);
            return;
        }
        log.warn("Workspace path is not provided. Container will be started without workspace bind.");
    }

    private List<Bind> buildBinds(String repositoryPath, String pluginPath) {
        List<Bind> binds = new ArrayList<>();
        if (StringUtils.hasText(repositoryPath)) {
            binds.add(new Bind(repositoryPath, new Volume("/workspace")));
        }
        if (StringUtils.hasText(pluginPath)) {
            binds.add(new Bind(pluginPath, new Volume("/workspace/plugins.txt")));
        }
        return binds;
    }

    private CreateContainerResponse createContainer(String image, List<Bind> binds) {
        // 자동으로 workspace 내부에서 `Jenkinsfile` 을 찾는다. ★ CMD는 건드리지 않는다
        CreateContainerCmd cmd = dockerClient.createContainerCmd(image);
        if (!binds.isEmpty()) {
            cmd.withBinds(binds.toArray(new Bind[0]));
        }
        return cmd.exec();
    }

    private void streamContainerLogs(String containerId) {
        dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        log.info("[RUNNER LOG] {}", new String(frame.getPayload()));
                    }
                });
    }

    private void removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();
    }
}
