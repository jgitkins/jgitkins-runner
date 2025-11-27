package io.jgitkins.runner.infrastructure.adapter;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.jgitkins.runner.application.port.out.ContainerRunnerPort;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DockerRunnerAdapter implements ContainerRunnerPort {

    private final DockerClient dockerClient;

    @Override
    public int run(Path workspace, String image) throws InterruptedException {
        log.info("Running Jenkinsfile Runner container: {}", image);
        log.info("Running WorkSpace : {}", workspace.toString());

        Bind bind = new Bind(workspace.toAbsolutePath().toString(), new Volume("/workspace"));

        // 자동으로 workspace 내부에서 `Jenkinsfile` 을 찾는다.
        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withBinds(bind)    // ★ CMD는 건드리지 않는다
                .exec();

        String containerId = container.getId();
        dockerClient.startContainerCmd(containerId).exec();

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

        Integer exit = dockerClient.waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode();

        log.info("Jenkinsfile Runner exit code: {}", exit);

        dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();

        return exit;
    }
}
