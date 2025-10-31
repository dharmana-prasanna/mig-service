package com.bank.migration.model.rules;

/**
 * Rolled-up customer-level migration status derived from individual account statuses.
 * Based on the "most critical" account status using priority order.
 */
public enum CustomerStatus {
    /**
     * Any account has NOT_MIGRATED status (dropped from migration)
     * Priority: HIGHEST
     */
    DROPPED,
    
    /**
     * Any account has IN_PROGRESS status (actively migrating)
     * Priority: HIGH
     */
    IN_PROGRESS,
    
    /**
     * Any account has SCHEDULED status (planned for migration)
     * Priority: MEDIUM
     */
    SCHEDULED,
    
    /**
     * All accounts have MIGRATED status (migration completed)
     * Priority: LOW
     */
    COMPLETED,
    
    /**
     * All accounts have EXCLUDED status (lending/IRA only customers)
     * Priority: LOW
     */
    EXCLUDED,
    
    /**
     * Default status when no migration applicable
     * Priority: LOWEST
     */
    NOT_IN_SCOPE
}

