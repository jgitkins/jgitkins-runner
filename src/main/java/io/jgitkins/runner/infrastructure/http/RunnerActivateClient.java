package io.jgitkins.runner.infrastructure.http;

import io.jgitkins.runner.application.exception.RunnerRegistrationException;
import io.jgitkins.runner.application.mapper.RunnerConfigurationResponseMapper;
import io.jgitkins.runner.application.port.out.RunnerActivatePort;
import io.jgitkins.runner.domain.RunnerConfiguration;
import io.jgitkins.runner.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunnerActivateClient implements RunnerActivatePort {

    private final RunnerConfigurationResponseMapper responseMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RunnerConfiguration activateRunner(String runnerToken, String baseUrl) {

        RunnerActivationRequest request = new RunnerActivationRequest(runnerToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String endpoint= String.format("%s%s", baseUrl, "/api/runners/activate");

        try {
            ResponseEntity<ApiResponse<ActivationResult>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    new ParameterizedTypeReference<>() {}
            );

            ApiResponse<ActivationResult> body = Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new IllegalStateException("Empty activation response"));
            ActivationResult result = Optional.ofNullable(body.getData())
                                              .orElseThrow(() -> new IllegalStateException("Activation result is missing"));

            return responseMapper.toDomain(result, runnerToken, baseUrl);
//            return null;
        } catch (HttpStatusCodeException e) {
            HttpStatus status = (HttpStatus) e.getStatusCode();
            if (status == HttpStatus.CONFLICT) {
                log.warn("Runner registration failed due to unusable token.");
                throw new RunnerRegistrationException(HttpStatus.CONFLICT, "등록 토큰을 사용할 수 없습니다.", e);
            }
            String message = String.format("Runner registration failed with status %s", status);
            log.error(message);
            throw new RunnerRegistrationException(status, message, e);
        } catch (RestClientException e) {
            String message = "Runner registration request failed: " + e.getMessage();
            log.error(message);
            throw new RunnerRegistrationException(HttpStatus.INTERNAL_SERVER_ERROR, message, e);
        }
    }


}
