# Migration Scenarios and Decision Matrix

This document provides detailed scenarios for testing the migration feature management service.

## Migration API Response Format

The migration team's API returns account information for each customer:

```json
{
  "customerId": "CUST123",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS|CHECKING|CD|LENDING|IRA",
      "migrationStatus": "NOT_MIGRATED|EXCLUDED|SCHEDULED|IN_PROGRESS|MIGRATED",
      "migrationWave": "WAVE1|WAVE2|NOT_APPLICABLE",
      "migrationDate": "2025-11-15"
    }
  ]
}
```

## WAVE1 Scenarios

### Scenario 1: Savings Only - Features SUPPRESSED
**Customer:** Has only savings account  
**Migration:** WAVE1 - Being migrated  
**Decision:** Suppress ALL features

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

**Expected Result:**
```json
{
  "customerId": "CUST001",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE1: Customer with savings/cd only is being migrated to BankB"
    }
  ]
}
```

---

### Scenario 2: CD Only - Features SUPPRESSED
**Customer:** Has only CD account  
**Migration:** WAVE1 - Being migrated  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST002",
  "accounts": [
    {
      "accountId": "ACC002",
      "accountType": "CD",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

---

### Scenario 3: Savings + CD - Features SUPPRESSED
**Customer:** Has savings and CD accounts  
**Migration:** WAVE1 - Being migrated  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST003",
  "accounts": [
    {
      "accountId": "ACC003",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC004",
      "accountType": "CD",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ]
}
```

---

### Scenario 4: Savings + Checking - Features ENABLED
**Customer:** Has savings and checking accounts  
**Migration:** NOT in WAVE1 (will be in WAVE2)  
**Decision:** Enable ALL features

```json
{
  "customerId": "CUST004",
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
      "accountType": "CHECKING",
      "migrationStatus": "NOT_MIGRATED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    }
  ]
}
```

**Expected Result:**
```json
{
  "customerId": "CUST004",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "WAVE1: Customer has checking account, not migrating in this wave"
    }
  ]
}
```

---

### Scenario 5: Savings + Lending - Features SUPPRESSED
**Customer:** Has savings and lending accounts  
**Migration:** WAVE1 - Savings migrated, Lending stays in BankA  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST005",
  "accounts": [
    {
      "accountId": "ACC007",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC008",
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

**Expected Result:**
```json
{
  "customerId": "CUST005",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE1: Customer has lending/ira accounts, partial migration to BankB"
    }
  ]
}
```

---

### Scenario 6: CD + IRA - Features SUPPRESSED
**Customer:** Has CD and IRA accounts  
**Migration:** WAVE1 - CD migrated, IRA stays in BankA  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST006",
  "accounts": [
    {
      "accountId": "ACC009",
      "accountType": "CD",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
    {
      "accountId": "ACC010",
      "accountType": "IRA",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

---

## WAVE2 Scenarios

### Scenario 7: Savings + Checking - Features SUPPRESSED
**Customer:** Has savings and checking accounts  
**Migration:** WAVE2 - Being migrated  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST007",
  "accounts": [
    {
      "accountId": "ACC011",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
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

**Expected Result:**
```json
{
  "customerId": "CUST007",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE2: Customer with savings/cd/checking is being migrated to BankB"
    }
  ]
}
```

---

### Scenario 8: CD + Checking - Features SUPPRESSED
**Customer:** Has CD and checking accounts  
**Migration:** WAVE2 - Being migrated  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST008",
  "accounts": [
    {
      "accountId": "ACC013",
      "accountType": "CD",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC014",
      "accountType": "CHECKING",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    }
  ]
}
```

---

### Scenario 9: Savings + CD + Checking - Features SUPPRESSED
**Customer:** Has savings, CD, and checking accounts  
**Migration:** WAVE2 - All being migrated  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST009",
  "accounts": [
    {
      "accountId": "ACC015",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC016",
      "accountType": "CD",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC017",
      "accountType": "CHECKING",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    }
  ]
}
```

---

### Scenario 10: Savings + Checking + Lending - Features SUPPRESSED
**Customer:** Has savings, checking, and lending accounts  
**Migration:** WAVE2 - Savings/Checking migrated, Lending stays in BankA  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST010",
  "accounts": [
    {
      "accountId": "ACC018",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC019",
      "accountType": "CHECKING",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC020",
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

**Expected Result:**
```json
{
  "customerId": "CUST010",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE2: Customer has lending/ira accounts, partial migration (savings/cd/checking) to BankB"
    }
  ]
}
```

---

### Scenario 11: CD + Checking + IRA - Features SUPPRESSED
**Customer:** Has CD, checking, and IRA accounts  
**Migration:** WAVE2 - CD/Checking migrated, IRA stays in BankA  
**Decision:** Suppress ALL features

```json
{
  "customerId": "CUST011",
  "accounts": [
    {
      "accountId": "ACC021",
      "accountType": "CD",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC022",
      "accountType": "CHECKING",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    },
    {
      "accountId": "ACC023",
      "accountType": "IRA",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

---

## Edge Cases

### Scenario 12: Lending Only - Features ENABLED
**Customer:** Has only lending account (never migrates)  
**Migration:** NOT_APPLICABLE  
**Decision:** Enable ALL features

```json
{
  "customerId": "CUST012",
  "accounts": [
    {
      "accountId": "ACC024",
      "accountType": "LENDING",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

**Expected Result:**
```json
{
  "customerId": "CUST012",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Customer not in migration scope, all features enabled"
    }
  ]
}
```

---

### Scenario 13: IRA Only - Features ENABLED
**Customer:** Has only IRA account (never migrates)  
**Migration:** NOT_APPLICABLE  
**Decision:** Enable ALL features

```json
{
  "customerId": "CUST013",
  "accounts": [
    {
      "accountId": "ACC025",
      "accountType": "IRA",
      "migrationStatus": "EXCLUDED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

---

### Scenario 14: Checking Only - Features ENABLED
**Customer:** Has only checking account (no savings/CD to qualify)  
**Migration:** NOT_APPLICABLE  
**Decision:** Enable ALL features

```json
{
  "customerId": "CUST014",
  "accounts": [
    {
      "accountId": "ACC026",
      "accountType": "CHECKING",
      "migrationStatus": "NOT_MIGRATED",
      "migrationWave": "NOT_APPLICABLE",
      "migrationDate": null
    }
  ]
}
```

---

## Decision Matrix

| Account Types | WAVE1 | WAVE2 | Decision | Reason |
|--------------|-------|-------|----------|--------|
| Savings only | ✅ Migrated | - | **SUPPRESS** | Full migration |
| CD only | ✅ Migrated | - | **SUPPRESS** | Full migration |
| Savings + CD | ✅ Migrated | - | **SUPPRESS** | Full migration |
| Savings + Checking | ❌ Not migrated | ✅ Migrated | **ENABLE (W1) / SUPPRESS (W2)** | Moved to WAVE2 |
| CD + Checking | ❌ Not migrated | ✅ Migrated | **ENABLE (W1) / SUPPRESS (W2)** | Moved to WAVE2 |
| Savings + Lending | ✅ Partial | - | **SUPPRESS** | Partial migration |
| Savings + IRA | ✅ Partial | - | **SUPPRESS** | Partial migration |
| CD + Lending | ✅ Partial | - | **SUPPRESS** | Partial migration |
| CD + IRA | ✅ Partial | - | **SUPPRESS** | Partial migration |
| Savings + Checking + Lending | ❌ Not migrated | ✅ Partial | **ENABLE (W1) / SUPPRESS (W2)** | Partial migration in W2 |
| Savings + Checking + IRA | ❌ Not migrated | ✅ Partial | **ENABLE (W1) / SUPPRESS (W2)** | Partial migration in W2 |
| Checking only | ❌ Not eligible | ❌ Not eligible | **ENABLE** | Not in scope |
| Lending only | ❌ Excluded | ❌ Excluded | **ENABLE** | Never migrates |
| IRA only | ❌ Excluded | ❌ Excluded | **ENABLE** | Never migrates |

## Migration Status Timeline

```
NOT_MIGRATED → SCHEDULED → IN_PROGRESS → MIGRATED
                    ↓
                EXCLUDED (Lending/IRA only)
```

## Testing Notes

1. **WAVE1 Testing:** Focus on savings/CD only customers and those with checking (should not migrate)
2. **WAVE2 Testing:** Focus on savings/CD + checking customers
3. **Partial Migration:** Test customers with lending/IRA to ensure features are suppressed
4. **Edge Cases:** Test customers with only lending/IRA or checking (should have features enabled)

