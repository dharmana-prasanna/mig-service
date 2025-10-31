# Dropped Customers - Test Scenarios

## Test Scenario 1: Single Dropped Account

### Migration API Response
```json
{
  "customerId": "CUST_DROPPED_01",
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

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_DROPPED_01" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_DROPPED_01",
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

## Test Scenario 2: Mixed Status - One Dropped, One Scheduled

### Migration API Response
```json
{
  "customerId": "CUST_DROPPED_02",
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

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_DROPPED_02" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_DROPPED_02",
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

**Note:** Even though one account is SCHEDULED, the presence of ANY NOT_MIGRATED account triggers full feature enablement.

## Test Scenario 3: Normal Customer (No Dropped Accounts)

### Migration API Response
```json
{
  "customerId": "CUST_NORMAL_01",
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

### Feature Check Request
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_NORMAL_01" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_NORMAL_01",
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

**Note:** No NOT_MIGRATED status, so normal WAVE1 rules apply (features disabled).

## Test Scenario 4: Dropped Customer with Lending Account

### Migration API Response
```json
{
  "customerId": "CUST_DROPPED_03",
  "accounts": [
    {
      "accountId": "ACC005",
      "accountType": "SAVINGS",
      "migrationStatus": "NOT_MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": null
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
  -H "customerId: CUST_DROPPED_03" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

### Expected Response
```json
{
  "customerId": "CUST_DROPPED_03",
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

**Note:** NOT_MIGRATED takes priority over EXCLUDED status.

## Verification Checklist

When testing dropped customers, verify:

- [ ] Response has all features enabled
- [ ] All features have reason: "Features enabled, not migrating"
- [ ] Response returns immediately (no long processing)
- [ ] Log shows: "Customer X has NOT_MIGRATED status - enabling all features"
- [ ] Works regardless of account types
- [ ] Works regardless of migration wave
- [ ] Works even with mixed statuses (one dropped, others scheduled)

## Expected Log Output

```
INFO  c.b.m.s.FeatureDecisionService - Checking features for customer: CUST_DROPPED_01
DEBUG c.b.m.s.MigrationApiClient - Retrieved 1 accounts for customer CUST_DROPPED_01
INFO  c.b.m.s.FeatureDecisionService - Customer CUST_DROPPED_01 has NOT_MIGRATED status - enabling all features
INFO  c.b.m.s.FeatureDecisionService - Completed feature check for customer: CUST_DROPPED_01 with 4 features
```

Note: No "Fired X rules" log entry (rules are skipped for dropped customers).

## Performance Note

Dropped customers skip Drools rule evaluation entirely, resulting in:
- ✅ Faster response time
- ✅ Less CPU usage
- ✅ Simpler logic flow
- ✅ Consistent behavior

## Summary

| Scenario | NOT_MIGRATED Present? | Result |
|----------|----------------------|--------|
| Single dropped account | Yes | All features enabled |
| Mixed (dropped + scheduled) | Yes | All features enabled |
| Normal migration customer | No | Rules apply (likely disabled) |
| Dropped with lending | Yes | All features enabled |

**Key Point:** The presence of ANY NOT_MIGRATED account triggers full feature enablement, regardless of other account statuses.

