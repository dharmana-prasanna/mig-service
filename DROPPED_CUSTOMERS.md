# Dropped Customers - NOT_MIGRATED Status Handling

## Overview

This document explains how the system handles customers who were initially scheduled for migration but were later dropped due to miscellaneous reasons.

## Scenario

During the migration planning process, some customers may be:
1. **Initially identified** for migration (savings/CD accounts)
2. **Scheduled** for a migration wave (WAVE1 or WAVE2)
3. **Later dropped** from migration due to various reasons (compliance, technical issues, customer request, etc.)

When a customer is dropped, their account status is flipped from `SCHEDULED` to `NOT_MIGRATED`.

## System Behavior

### Priority 1 Processing (Highest Priority)

The system checks for `NOT_MIGRATED` status as **Priority 1** - before terminal state checks and before evaluating any Drools rules. This ensures dropped customers are handled consistently with highest priority.

See `TERMINAL_STATES.md` for Priority 2 (terminal states) and Priority 3 (active migration) logic.

### Decision Logic

```
IF any customer account has migrationStatus = NOT_MIGRATED:
  THEN enable ALL features
  REASON: "Features enabled, not migrating"
  SKIP all other rules
ELSE:
  Evaluate WAVE1/WAVE2 rules normally
```

### Why All Features Are Enabled

Customers with `NOT_MIGRATED` status are staying in BankA, so they should have full access to all features without any migration-related restrictions.

## Example Scenarios

### Scenario 1: Customer Dropped from WAVE1

**Initial State:**
```json
{
  "customerId": "CUST001",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

**Action:** Customer dropped from migration

**Updated State:**
```json
{
  "customerId": "CUST001",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "NOT_MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": null
    }
  ]
}
```

**Feature Check Response:**
```json
{
  "customerId": "CUST001",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Features enabled, not migrating"
    },
    {
      "feature": "feature2",
      "enabled": true,
      "reason": "Features enabled, not migrating"
    },
    {
      "feature": "feature3",
      "enabled": true,
      "reason": "Features enabled, not migrating"
    },
    {
      "feature": "feature4",
      "enabled": true,
      "reason": "Features enabled, not migrating"
    }
  ]
}
```

### Scenario 2: Customer with Mixed Statuses

**State:**
```json
{
  "customerId": "CUST002",
  "accounts": [
    {
      "accountId": "ACC002",
      "accountType": "SAVINGS",
      "migrationStatus": "NOT_MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": null
    },
    {
      "accountId": "ACC003",
      "accountType": "CD",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

**Result:** Customer has **at least one** `NOT_MIGRATED` account, so **all features are enabled**.

This is the safe approach - if any account is not migrating, enable all features.

### Scenario 3: Normal Migration Customer

**State:**
```json
{
  "customerId": "CUST003",
  "accounts": [
    {
      "accountId": "ACC004",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

**Result:** No `NOT_MIGRATED` status detected. System evaluates WAVE1 rules normally. Features likely disabled during migration.

## Technical Implementation

### Code Location

**File:** `src/main/java/com/bank/migration/service/FeatureDecisionService.java`

```java
// Step 3: Check for dropped customers (high priority - checked first)
boolean hasNotMigrated = context.hasNotMigratedStatus();
if (hasNotMigrated) {
    log.info("Customer {} has NOT_MIGRATED status - enabling all features", customerId);
    features.forEach(feature -> {
        context.setFeatureStatus(feature, true, "Features enabled, not migrating");
    });
    // Skip Drools rules for dropped customers
} else {
    // Execute Drools rules for normal migration scenarios
    ...
}
```

### Helper Method

**File:** `src/main/java/com/bank/migration/model/rules/CustomerMigrationContext.java`

```java
public boolean hasNotMigratedStatus() {
    return accounts.stream()
            .anyMatch(account -> account.getMigrationStatus() == MigrationStatus.NOT_MIGRATED);
}
```

## Processing Order

The system processes customers in this order:

1. **Get accounts** from Migration API
2. **Check for NOT_MIGRATED** status (highest priority)
   - If found: Enable all features, return response
   - If not found: Continue to step 3
3. **Execute Drools rules** (WAVE1, WAVE2, etc.)
4. **Apply defaults** for unspecified features
5. **Return response**

## Benefits

✅ **High Priority** - Checked before any other rules  
✅ **Simple Logic** - Just check for NOT_MIGRATED status  
✅ **Safe Behavior** - All features enabled for non-migrating customers  
✅ **Consistent** - Same treatment regardless of account types  
✅ **Fast** - Skips rule evaluation when not needed  
✅ **Clear Messaging** - Explicit reason in response  

## Migration API Contract

The Migration API must return accurate `migrationStatus` values:

| Status | Meaning |
|--------|---------|
| `NOT_MIGRATED` | Customer dropped or never scheduled |
| `EXCLUDED` | Account type never migrates (lending, IRA) |
| `SCHEDULED` | Selected for migration |
| `IN_PROGRESS` | Currently migrating |
| `MIGRATED` | Migration completed |

## Edge Cases

### Case 1: All Accounts NOT_MIGRATED
**Result:** All features enabled

### Case 2: One Account NOT_MIGRATED, Others SCHEDULED
**Result:** All features enabled (safe approach - any NOT_MIGRATED triggers full enablement)

### Case 3: All Accounts SCHEDULED or IN_PROGRESS
**Result:** Normal rule evaluation (WAVE1/WAVE2 logic applies)

### Case 4: Account Status Changes During Weekend
If a customer's status changes from `SCHEDULED` to `NOT_MIGRATED` during the migration weekend:
- Next feature check will detect `NOT_MIGRATED`
- All features will be enabled immediately
- Customer can continue using BankA services normally

## Testing

### Test 1: Dropped Customer
```bash
# Customer with NOT_MIGRATED status
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_DROPPED" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** All features enabled with reason "Features enabled, not migrating"

### Test 2: Normal Migration Customer
```bash
# Customer with SCHEDULED status
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_SCHEDULED" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** Features per WAVE1/WAVE2 rules (likely disabled)

## Monitoring

Watch for these log messages:

```
INFO  - Customer CUST001 has NOT_MIGRATED status - enabling all features
```

This indicates the dropped customer logic was triggered.

## FAQ

**Q: What if a customer has both NOT_MIGRATED and SCHEDULED accounts?**  
A: All features are enabled. The presence of ANY NOT_MIGRATED account triggers full feature enablement.

**Q: Can I customize feature behavior for dropped customers?**  
A: Currently, all features are enabled for dropped customers. To customize, modify the logic in `FeatureDecisionService.java`.

**Q: Does this work with the Excel decision table?**  
A: Yes, but this logic runs **before** the Excel rules are evaluated. Dropped customers skip the Excel rules entirely.

**Q: What happens if status changes back from NOT_MIGRATED to SCHEDULED?**  
A: Next feature check will not trigger the dropped customer logic. Normal WAVE1/WAVE2 rules will apply.

**Q: Can I see the dropped customer logic in Excel?**  
A: No, this is hardcoded in Java for performance and consistency. It's a preprocessing step that runs before Excel rules.

## Related Documentation

- `README.md` - Main system documentation
- `IMPLEMENTATION_COMPLETE.md` - Complete feature summary
- `MIGRATION_SCENARIOS.md` - Test scenarios
- `QUICKSTART.md` - Quick start guide

