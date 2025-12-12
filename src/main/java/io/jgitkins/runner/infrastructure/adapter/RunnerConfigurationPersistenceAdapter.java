package io.jgitkins.runner.infrastructure.adapter;

import io.jgitkins.runner.application.port.out.RunnerConfigurationPort;
import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.runner.infrastructure.persistence.mapper.RunnerConfigEntityMbgMapper;
import io.jgitkins.runner.infrastructure.persistence.mapper.RunnerEntityMbgMapper;
import io.jgitkins.runner.infrastructure.persistence.model.RunnerConfigEntity;
import io.jgitkins.runner.infrastructure.persistence.model.RunnerConfigEntityCondition;
import io.jgitkins.runner.infrastructure.persistence.model.RunnerEntity;
import io.jgitkins.runner.infrastructure.persistence.model.RunnerEntityCondition;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RunnerConfigurationPersistenceAdapter implements RunnerConfigurationPort {

    private static final String KEY_SERVER_HOST = "serverHost";
    private static final String KEY_SERVER_PORT = "serverPort";
    private static final String KEY_POLL_INTERVAL = "pollIntervalMillis";
    private static final String KEY_BUSY_INTERVAL = "busyWaitIntervalMillis";
    private static final String KEY_DOCKER_IMAGE = "defaultDockerImage";
    private static final String KEY_JENKINSFILE_PATH = "defaultJenkinsfilePath";

    private final RunnerEntityMbgMapper runnerMapper;
    private final RunnerConfigEntityMbgMapper configMapper;

    @Override
    public Optional<RunnerConfiguration> loadConfiguration() {
        RunnerEntity runner = findRunner().orElse(null);
        if (runner == null) {
            return Optional.empty();
        }

        Map<String, String> configMap = loadConfigs(runner.getId());
        return Optional.of(mapToDomain(runner, configMap));
    }

    @Override
    public void saveConfiguration(RunnerConfiguration configuration) {
        RunnerEntity runner = findRunner().orElseGet(RunnerEntity::new);
        runner.setToken(configuration.getRunnerToken());
        runner.setName(configuration.getRunnerName());
        runner.setStatus("ACTIVE");
        runner.setUpdatedAt(LocalDateTime.now());
        if (runner.getId() == null) {
            runner.setCreatedAt(LocalDateTime.now());
            runnerMapper.insertSelective(runner);
        } else {
            runnerMapper.updateByPrimaryKeySelective(runner);
        }

        Long runnerId = runner.getId();
        Map<String, String> configMap = new HashMap<>();
        putIfNotNull(configMap, KEY_SERVER_HOST, configuration.getServerHost());
        if (configuration.getServerPort() > 0) {
            configMap.put(KEY_SERVER_PORT, Integer.toString(configuration.getServerPort()));
        }
        if (configuration.getPollInterval() != null) {
            configMap.put(KEY_POLL_INTERVAL, Long.toString(configuration.getPollInterval().toMillis()));
        }
        if (configuration.getBusyWaitInterval() != null) {
            configMap.put(KEY_BUSY_INTERVAL, Long.toString(configuration.getBusyWaitInterval().toMillis()));
        }
        putIfNotNull(configMap, KEY_DOCKER_IMAGE, configuration.getDefaultDockerImage());
        putIfNotNull(configMap, KEY_JENKINSFILE_PATH, configuration.getDefaultJenkinsfilePath());

        configMap.forEach((key, value) -> upsertConfig(runnerId, key, value));
    }

    private Optional<RunnerEntity> findRunner() {
        RunnerEntityCondition condition = new RunnerEntityCondition();
        condition.setOrderByClause("ID ASC");
        List<RunnerEntity> runners = runnerMapper.selectByCondition(condition);
        if (runners.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(runners.get(0));
    }

    private Map<String, String> loadConfigs(Long runnerId) {
        RunnerConfigEntityCondition condition = new RunnerConfigEntityCondition();
        condition.createCriteria().andRunnerIdEqualTo(runnerId);
        return configMapper.selectByCondition(condition)
                           .stream()
                           .collect(Collectors.toMap(RunnerConfigEntity::getConfigKey,
                                                     RunnerConfigEntity::getConfigValue,
                                                     (existing, replacement) -> replacement));
    }

    private RunnerConfiguration mapToDomain(RunnerEntity runner, Map<String, String> configMap) {
        return RunnerConfiguration.builder()
                                  .runnerName(runner.getName())
                                  .runnerToken(runner.getToken())
                                  .serverHost(configMap.get(KEY_SERVER_HOST))
                                  .serverPort(parseInt(configMap.get(KEY_SERVER_PORT)))
                                  .pollInterval(parseDuration(configMap.get(KEY_POLL_INTERVAL)))
                                  .busyWaitInterval(parseDuration(configMap.get(KEY_BUSY_INTERVAL)))
                                  .defaultDockerImage(configMap.get(KEY_DOCKER_IMAGE))
                                  .defaultJenkinsfilePath(configMap.get(KEY_JENKINSFILE_PATH))
                                  .build();
    }

    private int parseInt(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Duration parseDuration(String millis) {
        try {
            return millis == null ? Duration.ZERO : Duration.ofMillis(Long.parseLong(millis));
        } catch (NumberFormatException ex) {
            return Duration.ZERO;
        }
    }

    private void upsertConfig(Long runnerId, String key, String value) {
        RunnerConfigEntityCondition condition = new RunnerConfigEntityCondition();
        condition.createCriteria()
                 .andRunnerIdEqualTo(runnerId)
                 .andConfigKeyEqualTo(key);
        List<RunnerConfigEntity> current = configMapper.selectByCondition(condition);

        RunnerConfigEntity entity = new RunnerConfigEntity();
        entity.setRunnerId(runnerId);
        entity.setConfigKey(key);
        entity.setConfigValue(value);
        entity.setUpdatedAt(LocalDateTime.now());

        if (current.isEmpty()) {
            configMapper.insertSelective(entity);
        } else {
            entity.setId(current.get(0).getId());
            configMapper.updateByPrimaryKeySelective(entity);
        }
    }

    private void putIfNotNull(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
