package io.jgitkins.runner.infrastructure.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {

    @Bean
    public DockerClient dockerClient() {

        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        DockerHttpClient httpClient = new OkDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        System.out.println(">>> ENV DOCKER_HOST=" + System.getenv("DOCKER_HOST"));
        System.out.println(">>> PROP docker.host=" + System.getProperty("docker.host"));
        System.out.println(">>> CONFIG dockerHost=" + config.getDockerHost());

        return DockerClientImpl.getInstance(config, httpClient);
    }
}
