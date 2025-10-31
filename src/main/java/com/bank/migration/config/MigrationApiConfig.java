package com.bank.migration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "migration.api")
@Data
public class MigrationApiConfig {
    
    private String baseUrl;
    private String endpoint;
    private int timeout;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

