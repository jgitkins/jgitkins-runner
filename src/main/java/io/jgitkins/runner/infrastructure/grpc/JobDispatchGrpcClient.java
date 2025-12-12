package io.jgitkins.runner.infrastructure.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.jgitkins.runner.application.port.out.JobFetchPort;
import io.jgitkins.runner.application.port.out.JobResultPort;
import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.server.grpc.JobDispatchRequest;
import io.jgitkins.server.grpc.JobDispatchResponse;
import io.jgitkins.server.grpc.JobDispatchServiceGrpc;
import io.jgitkins.server.grpc.JobPayload;
import io.jgitkins.server.grpc.JobResultRequest;
import io.jgitkins.server.grpc.JobResultResponse;
import io.jgitkins.server.grpc.JobResultStatus;
import jakarta.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobDispatchGrpcClient implements JobFetchPort, JobResultPort {

    private ManagedChannel channel;
    private JobDispatchServiceGrpc.JobDispatchServiceBlockingStub blockingStub;
    private String configuredHost;
    private int configuredPort;

    @Override
    public Optional<JobPayload> fetchJob(RunnerConfiguration configuration) {
//        if (configuration == null || !configuration.isReadyForScheduling()) {
//            return Optional.empty();
//        }

        ensureChannel(configuration);
        if (blockingStub == null) {
            return Optional.empty();
        }

        JobDispatchRequest dispatchRequest = JobDispatchRequest.newBuilder()
                                                       .setRunnerToken(configuration.getRunnerToken())
                                                       .build();
        try {
            JobDispatchResponse jobDispatchResponse = blockingStub.requestJob(dispatchRequest);
            if (jobDispatchResponse.getHasJob()) {
                return Optional.of(jobDispatchResponse.getJob());
            }
            return Optional.empty();
        } catch (StatusRuntimeException e) {
            log.error("Job request failed", e);
            return Optional.empty();
        }
    }

    @Override
    public void reportResult(RunnerConfiguration configuration, long jobId, JobResultStatus status) {
        if (configuration == null || !configuration.isReadyForScheduling()) {
            log.warn("Runner 설정이 완료되지 않아 Job 결과를 보고할 수 없습니다.");
            return;
        }
        ensureChannel(configuration);
        if (blockingStub == null) {
            return;
        }

        JobResultRequest request = JobResultRequest.newBuilder()
                                                   .setRunnerToken(configuration.getRunnerToken())
                                                   .setJobId(jobId)
                                                   .setStatus(status)
                                                   .build();
        try {
            JobResultResponse response = blockingStub.reportJobResult(request);
            log.info("Job result acknowledged? {}", response.getAccepted());
        } catch (StatusRuntimeException e) {
            log.error("Failed to report job result for job {}", jobId, e);
        }
    }

    private synchronized void ensureChannel(RunnerConfiguration configuration) {
        if (configuration == null || configuration.getServerHost() == null || configuration.getServerHost().isBlank()) {
            return;
        }
        if (channel != null && configuration.getServerHost().equals(configuredHost) && configuration.getServerPort() == configuredPort) {
            return;
        }
        shutdownChannel();
        channel = ManagedChannelBuilder.forAddress(configuration.getServerHost(), configuration.getServerPort())
                                       .usePlaintext()
                                       .build();
        blockingStub = JobDispatchServiceGrpc.newBlockingStub(channel);
        configuredHost = configuration.getServerHost();
        configuredPort = configuration.getServerPort();
    }

    private void shutdownChannel() {
        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            channel = null;
            blockingStub = null;
        }
    }

    @PreDestroy
    public void shutdown() {
        shutdownChannel();
    }
}
