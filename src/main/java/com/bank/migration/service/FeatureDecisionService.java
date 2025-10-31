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
        
        // Step 3: Check for dropped customers (highest priority)
        boolean hasNotMigrated = context.hasNotMigratedStatus();
        if (hasNotMigrated) {
            log.info("Customer {} has NOT_MIGRATED status - enabling all features", customerId);
            features.forEach(feature -> {
                context.setFeatureStatus(feature, true, "Features enabled, not migrating");
            });
            // Skip Drools rules for dropped customers
        } else {
            // Step 4: Check if all accounts are in terminal states (no active migration)
            boolean allTerminal = context.allAccountsInTerminalState();
            if (allTerminal) {
                log.info("Customer {} has all accounts in terminal state - enabling all features", customerId);
                features.forEach(feature -> {
                    context.setFeatureStatus(feature, true, "Features enabled, migration completed or not applicable");
                });
                // Skip Drools rules - no active migration
            } else {
                // Step 5: Active migration - execute Drools rules
                log.debug("Customer {} has active migration (SCHEDULED or IN_PROGRESS) - applying rules", customerId);
                KieSession kieSession = kieContainer.newKieSession();
                try {
                    kieSession.insert(context);
                    int rulesFired = kieSession.fireAllRules();
                    log.debug("Fired {} rules for customer {}", rulesFired, customerId);
                } finally {
                    kieSession.dispose();
                }
                
                // Step 6: Apply defaults for features not set by rules (enabled by default)
                context.applyDefaults();
                log.debug("Applied defaults for unspecified features");
            }
        }
        
        // Step 7: Build response from context decisions
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

