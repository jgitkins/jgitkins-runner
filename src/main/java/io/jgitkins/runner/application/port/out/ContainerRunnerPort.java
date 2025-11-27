package io.jgitkins.runner.application.port.out;

import java.nio.file.Path;

public interface ContainerRunnerPort {
    int run(Path workspace, String image) throws InterruptedException;
}
