package ru.practicum.shareit.gateway.client;

import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.UriComponentsBuilder;


@Slf4j
@Component
public class BaseClient {

    protected final RestTemplate rest;
    private final String serverUrl;

    public BaseClient(@Value("${shareit-server.url}") String serverUrl) {
        this.rest = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault())
        );
        this.serverUrl = serverUrl;
        log.info("✅ BaseClient created. Server: {}, RestTemplate: OK", serverUrl);
    }

    @PostConstruct
    public void init() {
        log.info("BaseClient initialized with server URL: {}", serverUrl);
    }

    protected ResponseEntity<Object> get(String path) {
        return get(path, null, null);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return get(path, userId, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
        return post(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId, @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId, T body) {
        return put(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId, @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.PUT, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, T body) {
        return patch(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId) {
        return patch(path, userId, null, null);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return patch(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId, @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path) {
        return delete(path, null, null);
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        return delete(path, userId, null);
    }

    protected ResponseEntity<Object> delete(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, parameters, null);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, Long userId,
                                                          @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));

        String url = serverUrl + path;
        if (parameters != null && !parameters.isEmpty()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            parameters.forEach((key, value) -> builder.queryParam(key, value));
            url = builder.toUriString();
        }

        ResponseEntity<Object> shareitServerResponse;
        try {
            log.info("📤 Gateway -> Server: {} {} (userId: {})", method, url, userId);
            log.debug("Request headers: {}", requestEntity.getHeaders());
            log.debug("Request body: {}", body);
            log.debug("Query parameters: {}", parameters != null ? parameters : "none");

            shareitServerResponse = rest.exchange(url, method, requestEntity, Object.class);

            log.info("📥 Server -> Gateway: {} {}", shareitServerResponse.getStatusCode(), url);
            log.debug("Response headers: {}", shareitServerResponse.getHeaders());
            log.debug("Response body: {}", shareitServerResponse.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("❌ Server error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.error("🔌 Connection error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createGatewayError("Gateway error", e.getMessage()));
        }

        return prepareGatewayResponse(shareitServerResponse);
    }

    private Map<String, Object> createGatewayError(String error, String message) {
        return Map.of(
                "error", error,
                "message", message,
                "timestamp", java.time.LocalDateTime.now().toString(),
                "gatewayError", true
        );
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
