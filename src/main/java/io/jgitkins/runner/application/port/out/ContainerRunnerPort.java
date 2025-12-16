package io.jgitkins.runner.application.port.out;

public interface ContainerRunnerPort {
    int run(String workspace, String image, String pluginPath) throws InterruptedException;
}
