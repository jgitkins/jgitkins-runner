package io.jgitkins.runner.presentation.cli;

import io.jgitkins.runner.application.dto.RunnerActivateResult;
import io.jgitkins.runner.application.port.in.RunnerActivationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunnerCliApplicationRunner implements ApplicationRunner {

    private final RunnerActivationUseCase activationUseCase;
    private final ApplicationContext context;

    @Value("${runner.mode:start}")
    private String mode;

    @Value("${runner.activate.token:}")
    private String token;

    @Value("${runner.activate.server:}")
    private String server;

    @Override
    public void run(ApplicationArguments args) {
        if (!"activate".equalsIgnoreCase(mode)) {
            return;
        }

        if (token == null || token.isBlank() || server == null || server.isBlank()) {
            log.error("Missing activation arguments. Expected --token and --server.");
            exit(2);
            return;
        }

        try {
            RunnerActivateResult result = activationUseCase.activate(token, server);
            log.info("Runner activated. server={}", result.getMasterBaseUrl());
            exit(0);
        } catch (Exception ex) {
            log.error("Runner activation failed.", ex);
            exit(1);
        }
    }

    private void exit(int code) {
        int exitCode = SpringApplication.exit(context, () -> code);
        System.exit(exitCode);
    }
}
