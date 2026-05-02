package com.travelagency.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService {

    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final HttpClient httpClient =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String fromAddress;

    public ResendEmailService(
            ObjectMapper objectMapper,
            @Value("${app.resend.api-key:}") String apiKey,
            @Value("${app.mail.from:}") String fromAddress) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.fromAddress = fromAddress == null ? "" : fromAddress.trim();
    }

    public boolean isAvailable() {
        return !apiKey.isBlank() && !fromAddress.isBlank();
    }

    public void sendPlain(String to, String subject, String body) {
        if (!isAvailable()) {
            throw new BadRequestException("Resend is not configured. Set app.resend.api-key and app.mail.from.");
        }
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "from", fromAddress,
                    "to", new String[] {to},
                    "subject", subject,
                    "text", body));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new BadRequestException("Resend error: " + status + " " + response.body());
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (HttpTimeoutException e) {
            throw new BadRequestException("Resend request timed out");
        } catch (Exception e) {
            throw new BadRequestException("Failed to send email via Resend");
        }
    }
}
