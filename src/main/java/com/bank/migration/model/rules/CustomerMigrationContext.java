package com.bank.migration.model.rules;

import com.bank.migration.model.migration.AccountInfo;
import com.bank.migration.model.migration.AccountType;
import com.bank.migration.model.migration.MigrationWave;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMigrationContext {
    
    private String customerId;
    private List<AccountInfo> accounts;
    private List<String> requestedFeatures;
    
    @Builder.Default
    private Map<String, Boolean> featureDecisions = new HashMap<>();
    
    @Builder.Default
    private Map<String, String> decisionReasons = new HashMap<>();
    
    // Helper methods for rules
    public boolean hasAccountType(AccountType accountType) {
        return accounts.stream()
                .anyMatch(account -> account.getAccountType() == accountType);
    }
    
    public boolean hasSavingsOrCD() {
        return hasAccountType(AccountType.SAVINGS) || hasAccountType(AccountType.CD);
    }
    
    public boolean hasChecking() {
        return hasAccountType(AccountType.CHECKING);
    }
    
    public boolean hasLendingOrIRA() {
        return hasAccountType(AccountType.LENDING) || hasAccountType(AccountType.IRA);
    }
    
    public MigrationWave getCurrentWave() {
        // Get the wave from any scheduled or in-progress account
        return accounts.stream()
                .filter(account -> account.getMigrationWave() != null && 
                        account.getMigrationWave() != MigrationWave.NOT_APPLICABLE)
                .map(AccountInfo::getMigrationWave)
                .findFirst()
                .orElse(MigrationWave.NOT_APPLICABLE);
    }
    
    public List<AccountType> getMigratableAccountTypes() {
        return accounts.stream()
                .filter(account -> account.getMigrationWave() != null && 
                        account.getMigrationWave() != MigrationWave.NOT_APPLICABLE)
                .map(AccountInfo::getAccountType)
                .collect(Collectors.toList());
    }
    
    // Per-feature control methods
    public void setFeatureStatus(String feature, boolean enabled, String reason) {
        featureDecisions.put(feature, enabled);
        decisionReasons.put(feature, reason);
    }
    
    public void setFeature1(String status) {
        if (status != null && !status.trim().isEmpty()) {
            setFeatureStatus("feature1", "enabled".equalsIgnoreCase(status), 
                getCurrentWave() + ": feature1 " + status);
        }
    }
    
    public void setFeature2(String status) {
        if (status != null && !status.trim().isEmpty()) {
            setFeatureStatus("feature2", "enabled".equalsIgnoreCase(status), 
                getCurrentWave() + ": feature2 " + status);
        }
    }
    
    public void setFeature3(String status) {
        if (status != null && !status.trim().isEmpty()) {
            setFeatureStatus("feature3", "enabled".equalsIgnoreCase(status), 
                getCurrentWave() + ": feature3 " + status);
        }
    }
    
    public void setFeature4(String status) {
        if (status != null && !status.trim().isEmpty()) {
            setFeatureStatus("feature4", "enabled".equalsIgnoreCase(status), 
                getCurrentWave() + ": feature4 " + status);
        }
    }
    
    // Apply default enabled status for features not set by rules
    public void applyDefaults() {
        requestedFeatures.forEach(feature -> {
            if (!featureDecisions.containsKey(feature)) {
                featureDecisions.put(feature, true);
                decisionReasons.put(feature, "Default: Feature enabled (not specified in rules)");
            }
        });
    }
    
    // Legacy methods for backward compatibility (deprecated)
    @Deprecated
    public void suppressAllFeatures(String reason) {
        requestedFeatures.forEach(feature -> {
            featureDecisions.put(feature, false);
            decisionReasons.put(feature, reason);
        });
    }
    
    @Deprecated
    public void enableAllFeatures(String reason) {
        requestedFeatures.forEach(feature -> {
            featureDecisions.put(feature, true);
            decisionReasons.put(feature, reason);
        });
    }
}

