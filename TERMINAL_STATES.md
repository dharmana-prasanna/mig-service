# Terminal States - Migration Completed or Not Applicable

## Overview

This document explains how the system handles customers whose accounts are all in "terminal states" - meaning there's no active migration happening. All features are automatically enabled for these customers.

## What Are Terminal States?

Terminal states are migration statuses that indicate no active migration is occurring:

| Status | Meaning | Example |
|--------|---------|---------|
| **MIGRATED** | Migration completed to BankB | Savings account successfully moved to BankB |
| **EXCLUDED** | Never migrating (account type) | Lending or IRA accounts (stay in BankA) |
| **NOT_MIGRATED** | Dropped or never scheduled | Customer dropped from migration |

### Why "Terminal"?

These states represent the **end state** of an account - no further migration actions are planned or in progress.

## Three-Tier Decision Logic

The system uses a three-tier priority system to decide feature enablement:

```
Priority 1 (HIGHEST): ANY account with NOT_MIGRATED
  → Enable all features
  → Reason: "Features enabled, not migrating"
  → Skip rules

Priority 2: ALL accounts in terminal states (MIGRATED, EXCLUDED, NOT_MIGRATED)
  → Enable all features
  → Reason: "Features enabled, migration completed or not applicable"
  → Skip rules

Priority 3: Active migration (SCHEDULED or IN_PROGRESS)
  → Apply Excel rules
  → Feature-by-feature control per WAVE1/WAVE2
```

## When Are Features Enabled Automatically?

### Scenario 1: All Accounts Migrated
Customer's migration is complete - all accounts moved to BankB.

```json
{
  "customerId": "CUST001",
  "accounts": [
    {
      "accountType": "SAVINGS",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1"
    },
    {
      "accountType": "CD",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1"
    }
  ]
}
```

**Result:** ✅ All features enabled  
**Reason:** "Features enabled, migration completed or not applicable"  
**Why:** Migration is complete, customer can use all BankB features

### Scenario 2: All Accounts Excluded
Customer only has account types that never migrate.

```json
{
  "customerId": "CUST002",
  "accounts": [
    {
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE"
    },
    {
      "accountType": "IRA",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE"
    }
  ]
}
```

**Result:** ✅ All features enabled  
**Reason:** "Features enabled, migration completed or not applicable"  
**Why:** Customer never migrating, stays in BankA with full access

### Scenario 3: Mixed Terminal States
Customer has combination of migrated and excluded accounts.

```json
{
  "customerId": "CUST003",
  "accounts": [
    {
      "accountType": "SAVINGS",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1"
    },
    {
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE"
    }
  ]
}
```

**Result:** ✅ All features enabled  
**Reason:** "Features enabled, migration completed or not applicable"  
**Why:** Savings already migrated, lending stays in BankA - no active migration

### Scenario 4: One Dropped Account
Priority 1 kicks in (NOT_MIGRATED gets highest priority).

```json
{
  "customerId": "CUST004",
  "accounts": [
    {
      "accountType": "SAVINGS",
      "migrationStatus": "NOT_MIGRATED",
      "migrationWave": "WAVE1"
    },
    {
      "accountType": "CD",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1"
    }
  ]
}
```

**Result:** ✅ All features enabled  
**Reason:** "Features enabled, not migrating"  
**Why:** Priority 1 - ANY NOT_MIGRATED triggers immediate feature enablement

## When Are Excel Rules Applied?

Rules are only applied when there's **active migration** happening.

### Active Migration States

| Status | Meaning | Action |
|--------|---------|--------|
| **SCHEDULED** | Selected for migration, not yet started | Apply rules |
| **IN_PROGRESS** | Currently migrating | Apply rules |

### Scenario 5: Active Migration
One or more accounts are actively migrating.

```json
{
  "customerId": "CUST005",
  "accounts": [
    {
      "accountType": "SAVINGS",
      "migrationStatus": "IN_PROGRESS",
      "migrationWave": "WAVE1"
    },
    {
      "accountType": "CD",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1"
    }
  ]
}
```

**Result:** ⚙️ Apply Excel rules  
**Why:** Savings account is still IN_PROGRESS - migration not complete

### Scenario 6: Scheduled for Future
Account scheduled but not yet migrating.

```json
{
  "customerId": "CUST006",
  "accounts": [
    {
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2"
    },
    {
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE"
    }
  ]
}
```

**Result:** ⚙️ Apply Excel rules  
**Why:** Savings account is SCHEDULED - active migration pending

## Technical Implementation

### Code Location
**File:** `src/main/java/com/bank/migration/service/FeatureDecisionService.java`

```java
// Priority 1: Check for dropped customers
boolean hasNotMigrated = context.hasNotMigratedStatus();
if (hasNotMigrated) {
    // Enable all features
    return;
}

// Priority 2: Check if all accounts in terminal states
boolean allTerminal = context.allAccountsInTerminalState();
if (allTerminal) {
    // Enable all features
    return;
}

// Priority 3: Active migration - apply Excel rules
KieSession kieSession = kieContainer.newKieSession();
...
```

### Helper Method
**File:** `src/main/java/com/bank/migration/model/rules/CustomerMigrationContext.java`

```java
public boolean allAccountsInTerminalState() {
    return accounts.stream()
            .allMatch(account -> 
                account.getMigrationStatus() == MigrationStatus.MIGRATED ||
                account.getMigrationStatus() == MigrationStatus.EXCLUDED ||
                account.getMigrationStatus() == MigrationStatus.NOT_MIGRATED
            );
}
```

## Benefits

✅ **Post-Migration Experience** - Customers get full access after migration completes  
✅ **Excluded Account Support** - Lending/IRA-only customers get full access  
✅ **Performance** - Skips rule evaluation when not needed  
✅ **Clear Messaging** - Explicit reasons in response  
✅ **Logical Grouping** - Terminal states handled together  
✅ **Future-Proof** - Easy to add new terminal states  

## API Response Examples

### Terminal State Response
```json
{
  "customerId": "CUST001",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    },
    {
      "feature": "feature2",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    }
  ]
}
```

### Active Migration Response (with Excel rules)
```json
{
  "customerId": "CUST005",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE1: feature1 disabled"
    },
    {
      "feature": "feature2",
      "enabled": false,
      "reason": "WAVE1: feature2 disabled"
    }
  ]
}
```

## Decision Matrix

| Scenario | NOT_MIGRATED? | All Terminal? | Has SCHEDULED/IN_PROGRESS? | Result |
|----------|---------------|---------------|---------------------------|--------|
| Dropped customer | ✅ Yes | - | - | Enable all (Priority 1) |
| All migrated | ❌ No | ✅ Yes | ❌ No | Enable all (Priority 2) |
| All excluded | ❌ No | ✅ Yes | ❌ No | Enable all (Priority 2) |
| Mixed terminal | ❌ No | ✅ Yes | ❌ No | Enable all (Priority 2) |
| Active migration | ❌ No | ❌ No | ✅ Yes | Apply rules (Priority 3) |

## Log Output

### Terminal State Detected
```
INFO - Customer CUST001 has all accounts in terminal state - enabling all features
```

### Active Migration
```
DEBUG - Customer CUST005 has active migration (SCHEDULED or IN_PROGRESS) - applying rules
DEBUG - Fired 2 rules for customer CUST005
```

## Testing

### Test 1: All Migrated
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_ALL_MIGRATED" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** All features enabled

### Test 2: All Excluded
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_LENDING_ONLY" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** All features enabled

### Test 3: Active Migration
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_IN_PROGRESS" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** Features per Excel rules (likely disabled)

## FAQ

**Q: What happens after WAVE1 completes but before WAVE2 starts?**  
A: WAVE1 customers' accounts will be MIGRATED (terminal state) → All features enabled. WAVE2 customers still SCHEDULED → Excel rules apply.

**Q: Customer has savings MIGRATED and checking SCHEDULED. What happens?**  
A: Checking is SCHEDULED (active migration) → Excel rules apply. Not all accounts are terminal.

**Q: All accounts are MIGRATED. Can I still customize in Excel?**  
A: No. Terminal state logic runs before Excel rules and skips rule evaluation. All features are enabled automatically.

**Q: What if a new terminal state is added in the future?**  
A: Add it to the `allAccountsInTerminalState()` method in CustomerMigrationContext.java.

## Related Documentation

- `DROPPED_CUSTOMERS.md` - Priority 1 logic (NOT_MIGRATED)
- `README.md` - Main system documentation
- `IMPLEMENTATION_COMPLETE.md` - Complete feature summary

