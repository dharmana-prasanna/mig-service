package com.bank.migration.service;

import com.bank.migration.config.MigrationApiConfig;
import com.bank.migration.exception.MigrationApiException;
import com.bank.migration.model.migration.AccountInfo;
import com.bank.migration.model.migration.MigrationApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationApiClient {
    
    private final RestTemplate restTemplate;
    private final MigrationApiConfig config;
    
    public List<AccountInfo> getAccountStatuses(String customerId) {
        try {
            String url = config.getBaseUrl() + config.getEndpoint().replace("{customerId}", customerId);
            log.debug("Calling migration API for customer: {} at URL: {}", customerId, url);
            
            MigrationApiResponse response = restTemplate.getForObject(url, MigrationApiResponse.class);
            
            if (response == null || response.getAccounts() == null) {
                throw new MigrationApiException("Empty response from migration API for customer: " + customerId);
            }
            
            log.debug("Retrieved {} accounts for customer: {}", response.getAccounts().size(), customerId);
            return response.getAccounts();
            
        } catch (RestClientException e) {
            log.error("Error calling migration API for customer: {}", customerId, e);
            throw new MigrationApiException("Failed to retrieve migration data for customer: " + customerId, e);
        }
    }
}

