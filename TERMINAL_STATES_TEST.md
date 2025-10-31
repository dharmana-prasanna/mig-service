# Terminal States - Test Scenarios

## Test Scenario 1: All Accounts Migrated

### Migration API Response
```json
{
  "customerId": "CUST_ALL_MIGRATED",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC002",
      "accountType": "CD",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_ALL_MIGRATED" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_ALL_MIGRATED",
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
    },
    {
      "feature": "feature3",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    },
    {
      "feature": "feature4",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    }
  ]
}
```

**Why:** All accounts are MIGRATED (terminal state) - migration complete, full access enabled.

---

## Test Scenario 2: All Accounts Excluded (Lending/IRA Only)

### Migration API Response
```json
{
  "customerId": "CUST_LENDING_ONLY",
  "accounts": [
    {
      "accountId": "ACC003",
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    },
    {
      "accountId": "ACC004",
      "accountType": "IRA",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_LENDING_ONLY" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_LENDING_ONLY",
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
    },
    {
      "feature": "feature3",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    },
    {
      "feature": "feature4",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    }
  ]
}
```

**Why:** All accounts are EXCLUDED (terminal state) - never migrating, stays in BankA with full access.

---

## Test Scenario 3: Mixed Terminal States (Migrated + Excluded)

### Migration API Response
```json
{
  "customerId": "CUST_MIXED_TERMINAL",
  "accounts": [
    {
      "accountId": "ACC005",
      "accountType": "SAVINGS",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC006",
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_MIXED_TERMINAL" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_MIXED_TERMINAL",
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
    },
    {
      "feature": "feature3",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    },
    {
      "feature": "feature4",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    }
  ]
}
```

**Why:** All accounts are in terminal states (MIGRATED + EXCLUDED) - no active migration.

---

## Test Scenario 4: One Account IN_PROGRESS (Active Migration)

### Migration API Response
```json
{
  "customerId": "CUST_IN_PROGRESS",
  "accounts": [
    {
      "accountId": "ACC007",
      "accountType": "SAVINGS",
      "migrationStatus": "IN_PROGRESS",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC008",
      "accountType": "CD",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_IN_PROGRESS" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_IN_PROGRESS",
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
    },
    {
      "feature": "feature3",
      "enabled": false,
      "reason": "WAVE1: feature3 disabled"
    },
    {
      "feature": "feature4",
      "enabled": false,
      "reason": "WAVE1: feature4 disabled"
    }
  ]
}
```

**Why:** Savings account is IN_PROGRESS (active migration) - NOT all terminal, so Excel rules apply.

---

## Test Scenario 5: One Account SCHEDULED (Active Migration)

### Migration API Response
```json
{
  "customerId": "CUST_SCHEDULED",
  "accounts": [
    {
      "accountId": "ACC009",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC010",
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_SCHEDULED" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_SCHEDULED",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE2: feature1 disabled"
    },
    {
      "feature": "feature2",
      "enabled": false,
      "reason": "WAVE2: feature2 disabled"
    },
    {
      "feature": "feature3",
      "enabled": false,
      "reason": "WAVE2: feature3 disabled"
    },
    {
      "feature": "feature4",
      "enabled": false,
      "reason": "WAVE2: feature4 disabled"
    }
  ]
}
```

**Why:** Savings account is SCHEDULED (active migration) - NOT all terminal, so Excel rules apply.

---

## Test Scenario 6: Post-WAVE1 (WAVE1 Complete, WAVE2 Pending)

### Migration API Response
```json
{
  "customerId": "CUST_POST_WAVE1",
  "accounts": [
    {
      "accountId": "ACC011",
      "accountType": "SAVINGS",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC012",
      "accountType": "CHECKING",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    }
  ]
}
```

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_POST_WAVE1" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_POST_WAVE1",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE2: feature1 disabled"
    },
    {
      "feature": "feature2",
      "enabled": false,
      "reason": "WAVE2: feature2 disabled"
    },
    {
      "feature": "feature3",
      "enabled": false,
      "reason": "WAVE2: feature3 disabled"
    },
    {
      "feature": "feature4",
      "enabled": false,
      "reason": "WAVE2: feature4 disabled"
    }
  ]
}
```

**Why:** Checking is SCHEDULED for WAVE2 - active migration pending, Excel rules apply.

---

## Verification Checklist

When testing terminal states, verify:

- [ ] All MIGRATED accounts → All features enabled
- [ ] All EXCLUDED accounts → All features enabled
- [ ] Mixed terminal states → All features enabled
- [ ] ANY IN_PROGRESS account → Excel rules apply
- [ ] ANY SCHEDULED account → Excel rules apply
- [ ] Response reason matches expected (terminal vs. wave-specific)
- [ ] Log shows appropriate message (terminal state vs. active migration)

## Expected Log Output

### Terminal State Detected
```
INFO  - Customer CUST_ALL_MIGRATED has all accounts in terminal state - enabling all features
```

### Active Migration
```
DEBUG - Customer CUST_IN_PROGRESS has active migration (SCHEDULED or IN_PROGRESS) - applying rules
DEBUG - Fired 2 rules for customer CUST_IN_PROGRESS
```

## Summary Table

| Scenario | MIGRATED | EXCLUDED | NOT_MIGRATED | SCHEDULED | IN_PROGRESS | Result |
|----------|----------|----------|--------------|-----------|-------------|--------|
| All migrated | All | - | - | - | - | ✅ Enable all |
| All excluded | - | All | - | - | - | ✅ Enable all |
| Mixed terminal | Some | Some | - | - | - | ✅ Enable all |
| One in progress | Some | - | - | - | One | ⚙️ Apply rules |
| One scheduled | - | Some | - | One | - | ⚙️ Apply rules |
| Post-WAVE1 | Some | - | - | Some | - | ⚙️ Apply rules |

**Key:** ✅ = All features enabled (terminal) | ⚙️ = Excel rules applied (active migration)

## Performance Note

Terminal state customers skip Drools rule evaluation entirely, resulting in:
- ✅ Faster response time (similar to dropped customers)
- ✅ Less CPU usage
- ✅ Simpler logic flow
- ✅ Consistent post-migration experience

