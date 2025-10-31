package com.bank.migration.service;

import com.bank.migration.model.dto.FeatureCheckResponse;
import com.bank.migration.model.dto.FeatureStatus;
import com.bank.migration.model.migration.AccountInfo;
import com.bank.migration.model.rules.CustomerMigrationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureDecisionService {
    
    private final KieContainer kieContainer;
    private final MigrationApiClient migrationApiClient;
    
    public FeatureCheckResponse checkFeatures(String customerId, List<String> features) {
        log.info("Checking features for customer: {}", customerId);
        
        // Step 1: Get account statuses from migration API
        List<AccountInfo> accounts = migrationApiClient.getAccountStatuses(customerId);
        log.debug("Retrieved {} accounts for customer {}", accounts.size(), customerId);
        
        // Step 2: Create context for Drools
        CustomerMigrationContext context = CustomerMigrationContext.builder()
                .customerId(customerId)
                .accounts(accounts)
                .requestedFeatures(features)
                .build();
        
        // Step 3: Execute Drools rules
        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(context);
            int rulesFired = kieSession.fireAllRules();
            log.debug("Fired {} rules for customer {}", rulesFired, customerId);
        } finally {
            kieSession.dispose();
        }
        
        // Step 4: Apply defaults for features not set by rules (enabled by default)
        context.applyDefaults();
        log.debug("Applied defaults for unspecified features");
        
        // Step 5: Build response from context decisions
        List<FeatureStatus> featureStatuses = features.stream()
                .map(feature -> FeatureStatus.builder()
                        .feature(feature)
                        .enabled(context.getFeatureDecisions().getOrDefault(feature, true))
                        .reason(context.getDecisionReasons().getOrDefault(feature, "Default: Feature enabled"))
                        .build())
                .collect(Collectors.toList());
        
        log.info("Completed feature check for customer: {} with {} features", customerId, featureStatuses.size());
        
        return FeatureCheckResponse.builder()
                .customerId(customerId)
                .features(featureStatuses)
                .build();
    }
}

