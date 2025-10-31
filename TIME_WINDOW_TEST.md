# Time-Based Feature Control - Test Scenarios

## Overview

These test scenarios demonstrate how features behave before, during, and after the migration time window.

## Setup

All tests assume:
- **WAVE1 Migration Date:** Saturday, Nov 8, 2025 at 12:00 AM (midnight)
- **WAVE1 Hours Before:** 7 (configured in Excel row 12-14, column D)
- **WAVE1 Window Starts:** Friday, Nov 7, 2025 at 5:00 PM
- **WAVE2 Migration Date:** Saturday, Nov 15, 2025 at 12:00 AM (midnight)
- **WAVE2 Hours Before:** 12 (configured in Excel row 15-16, column D)
- **WAVE2 Window Starts:** Friday, Nov 14, 2025 at 12:00 PM

## WAVE1 Test Scenarios

### Test 1: Thursday Evening - Before Window (Normal Operation)

**Current Server Time:** Thursday, Nov 6, 2025, 10:00 PM

**Migration API Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
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

**Feature Check Request:**
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_WAVE1_01" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    },
    {
      "feature": "feature2",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    },
    {
      "feature": "feature3",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    },
    {
      "feature": "feature4",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    }
  ]
}
```

**Why:** Current time (Thu 10PM) is before window start (Fri 5PM). Time condition fails, WAVE1 rules don't match.

---

### Test 2: Friday 3:00 PM - 2 Hours Before Window

**Current Server Time:** Friday, Nov 7, 2025, 3:00 PM

**Migration API Response:** (Same as Test 1)

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    }
  ]
}
```

**Why:** Current time (Fri 3PM) is still before window start (Fri 5PM). Normal operation continues.

---

### Test 3: Friday 5:00 PM - Window Just Started!

**Current Server Time:** Friday, Nov 7, 2025, 5:00 PM (17:00)

**Migration API Response:** (Same as Test 1)

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
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

**Why:** Current time (Fri 5PM) equals window start. isWithinMigrationWindow(7) = true. WAVE1 rule matches, features disabled.

---

### Test 4: Friday 8:00 PM - Within Window

**Current Server Time:** Friday, Nov 7, 2025, 8:00 PM

**Migration API Response:** (Same as Test 1)

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE1: feature1 disabled"
    }
  ]
}
```

**Why:** Well within window. Migration restrictions active.

---

### Test 5: Saturday 6:00 AM - During Migration

**Current Server Time:** Saturday, Nov 8, 2025, 6:00 AM

**Migration API Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "IN_PROGRESS",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE1: feature1 disabled"
    }
  ]
}
```

**Why:** Still within window, migration in progress, restrictions continue.

---

### Test 6: Saturday 6:00 PM - Migration Complete

**Current Server Time:** Saturday, Nov 8, 2025, 6:00 PM

**Migration API Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE1_01",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Features enabled, migration completed or not applicable"
    }
  ]
}
```

**Why:** Account status is MIGRATED (terminal state). Priority 2 kicks in before time window check. All features enabled.

---

## WAVE2 Test Scenarios

### Test 7: Friday 10:00 AM - Before WAVE2 Window

**Current Server Time:** Friday, Nov 14, 2025, 10:00 AM

**WAVE2 Configuration:**
- Migration Date: Saturday, Nov 15, 2025 at 12:00 AM
- Hours Before: 12
- Window Starts: Friday, Nov 14, 2025 at 12:00 PM

**Migration API Response:**
```json
{
  "customerId": "CUST_WAVE2_01",
  "accounts": [
    {
      "accountId": "ACC002",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC003",
      "accountType": "CHECKING",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    }
  ]
}
```

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE2_01",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    }
  ]
}
```

**Why:** Current time (Fri 10AM) is before window start (Fri 12PM). Normal operation.

---

### Test 8: Friday 12:00 PM - WAVE2 Window Just Started

**Current Server Time:** Friday, Nov 14, 2025, 12:00 PM (noon)

**Migration API Response:** (Same as Test 7)

**Expected Response:**
```json
{
  "customerId": "CUST_WAVE2_01",
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

**Why:** Window started at noon. isWithinMigrationWindow(12) = true. WAVE2 rules apply.

---

## Mixed Wave Scenario

### Test 9: Customer with WAVE1 (Migrated) and WAVE2 (Scheduled)

**Current Server Time:** Friday, Nov 14, 2025, 10:00 AM

**Migration API Response:**
```json
{
  "customerId": "CUST_MIXED",
  "accounts": [
    {
      "accountId": "ACC004",
      "accountType": "SAVINGS",
      "migrationStatus": "MIGRATED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC005",
      "accountType": "CHECKING",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    }
  ]
}
```

**Expected Response:**
```json
{
  "customerId": "CUST_MIXED",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    }
  ]
}
```

**Why:** 
- Savings is MIGRATED (terminal) but checking is SCHEDULED (active)
- Not all accounts are terminal → Priority 2 doesn't match
- Checking migration is Nov 15, but current time is before WAVE2 window (12PM)
- isWithinMigrationWindow(12) = false for WAVE2 rules
- Falls through to default → Features enabled

---

## Verification Checklist

When testing time-based rules, verify:

- [ ] Before window: All features enabled (default)
- [ ] At exact window start time: Rules begin applying
- [ ] Within window: Features controlled per Excel rules
- [ ] During migration: Rules continue applying
- [ ] After migration (MIGRATED status): Terminal state logic (all enabled)
- [ ] WAVE1 and WAVE2 have different window timings
- [ ] Log messages indicate time window checks
- [ ] Server time is correct and synchronized

## Monitoring and Logs

### Expected Log Messages

**Before Window:**
```
DEBUG - isWithinMigrationWindow(7) evaluated: false
DEBUG - Current: 2025-11-07T15:00, Window Start: 2025-11-07T17:00
```

**Window Starts:**
```
DEBUG - isWithinMigrationWindow(7) evaluated: true
DEBUG - Current: 2025-11-07T17:00, Window Start: 2025-11-07T17:00
INFO  - Applying WAVE1 rules for customer CUST001
```

**Within Window:**
```
DEBUG - isWithinMigrationWindow(7) evaluated: true
DEBUG - Fired 1 rules for customer CUST001
```

**Terminal State (Post-Migration):**
```
INFO  - Customer CUST001 has all accounts in terminal state - enabling all features
(No time window check performed)
```

## Summary

| Time Period | isWithinMigrationWindow(7) | Result |
|-------------|---------------------------|--------|
| Thu 10PM | false | ✅ Features enabled |
| Fri 3PM | false | ✅ Features enabled |
| Fri 5PM | true | ⚙️ Apply rules |
| Fri 8PM | true | ⚙️ Apply rules |
| Sat 6AM | true | ⚙️ Apply rules |
| Sat 6PM (MIGRATED) | N/A | ✅ Features enabled (terminal) |

**Key Point:** Time window only matters for active migrations (SCHEDULED/IN_PROGRESS). Terminal states (MIGRATED/EXCLUDED) take priority and skip time checks.

