package com.example.finance.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fraud.detection.api.url:http://localhost:5000}")
    private String fraudDetectionApiUrl;

    @Value("${fraud.detection.api.timeout:5000}")
    private int apiTimeout;

    @Data
    public static class FraudPredictionRequest {
        private String type;
        private BigDecimal amount;
        private BigDecimal oldbalanceOrg;
        private BigDecimal newbalanceOrig;
        private BigDecimal oldbalanceDest;
        private BigDecimal newbalanceDest;
    }

    @Data
    public static class FraudPredictionResponse {
        @JsonProperty("is_fraud")
        private boolean isFraud;

        @JsonProperty("fraud_probability")
        private double fraudProbability;
        private String confidence;
        private String error;
    }

    public FraudPredictionResponse checkFraud(
            String transactionType,
            BigDecimal amount,
            BigDecimal oldBalanceOrg,
            BigDecimal newBalanceOrig,
            BigDecimal oldBalanceDest,
            BigDecimal newBalanceDest) {

        try {
            // Prepare request
            FraudPredictionRequest request = new FraudPredictionRequest();
            request.setType(mapTransactionType(transactionType));
            request.setAmount(amount);
            request.setOldbalanceOrg(oldBalanceOrg);
            request.setNewbalanceOrig(newBalanceOrig);
            request.setOldbalanceDest(oldBalanceDest);
            request.setNewbalanceDest(newBalanceDest);

            // Convert request to JSON for logging
            String jsonPayload = objectMapper.writeValueAsString(request);
            log.info("Fraud detection payload: {}", jsonPayload);
            log.info("Calling Fraud Detection API at: {}", fraudDetectionApiUrl + "/predict");

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FraudPredictionRequest> entity = new HttpEntity<>(request, headers);

            // Make API call and log raw response
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    fraudDetectionApiUrl + "/predict",
                    HttpMethod.POST,
                    new HttpEntity<>(jsonPayload, headers),
                    String.class
            );
            log.info("Raw fraud detection response: {}", rawResponse.getBody());

            // Parse the response into our POJO
            if (rawResponse.getStatusCode().is2xxSuccessful() && rawResponse.getBody() != null) {
                FraudPredictionResponse parsedResponse =
                        objectMapper.readValue(rawResponse.getBody(), FraudPredictionResponse.class);

                log.info("Fraud detection result parsed: is_fraud={}, probability={}, confidence={}",
                        parsedResponse.isFraud, parsedResponse.fraudProbability, parsedResponse.confidence);
                return parsedResponse;
            } else {
                log.error("Failed to get fraud detection response. Status: {}", rawResponse.getStatusCode());
                return createErrorResponse("Failed to get prediction from fraud detection service");
            }

        } catch (ResourceAccessException e) {
            log.error("Fraud detection service is not available: {}", e.getMessage());
            return createErrorResponse("Fraud detection service is not available");
        } catch (Exception e) {
            log.error("Error during fraud detection: {}", e.getMessage(), e);
            return createErrorResponse("Error during fraud detection: " + e.getMessage());
        }
    }

    private String mapTransactionType(String type) {
        switch (type.toUpperCase()) {
            case "TRANSFER":
                return "TRANSFER";
            case "PAYMENT":
                return "PAYMENT";
            case "DEBIT":
                return "DEBIT";
            case "CASH_OUT":
                return "CASH_OUT";
            case "CASH_IN":
                return "CASH_IN";
            default:
                log.warn("Unknown transaction type: {}, defaulting to TRANSFER", type);
                return "TRANSFER";
        }
    }

    private FraudPredictionResponse createErrorResponse(String errorMessage) {
        FraudPredictionResponse response = new FraudPredictionResponse();
        response.setFraud(false);
        response.setFraudProbability(0.0);
        response.setConfidence("error");
        response.setError(errorMessage);
        return response;
    }

    public boolean isHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    fraudDetectionApiUrl + "/health",
                    String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }
}
