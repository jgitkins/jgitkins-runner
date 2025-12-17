package io.jgitkins.runner;

//import io.jgitkins.runner.infrastructure.config.RunnerProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
//@EnableConfigurationProperties(RunnerProperties.class)
public class JGitkinsRunnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(JGitkinsRunnerApplication.class, args);
    }
}
