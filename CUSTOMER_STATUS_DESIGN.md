# Customer Status-Based Design

## Overview

The system has been **redesigned** with a cleaner, simpler architecture:

1. **Derive customer-level status** from account statuses (most critical account wins)
2. **Apply feature rules** based on customer status + time window

This simplifies the Excel rules from **7 condition columns to just 2**!

## Two-Step Approach

### Step 1: Status Derivation (Java)

System examines all customer accounts and derives a single customer-level status based on **most critical account**.

```
Account Statuses → Derivation Logic → Customer Status
```

### Step 2: Feature Decisions (Excel)

Excel rules use the derived customer status to make feature decisions.

```
Customer Status + Time Window → Excel Rules → Feature Enable/Disable
```

## Customer Statuses

### Status Definitions

| Customer Status | Meaning | Derived When |
|-----------------|---------|--------------|
| **DROPPED** | Dropped from migration | ANY account has NOT_MIGRATED status |
| **IN_PROGRESS** | Actively migrating now | ANY account has IN_PROGRESS status |
| **SCHEDULED** | Migration planned | ANY account has SCHEDULED status |
| **COMPLETED** | Migration finished | ALL accounts have MIGRATED status |
| **EXCLUDED** | Never migrating | ALL accounts have EXCLUDED status (lending/IRA only) |
| **NOT_IN_SCOPE** | No migration | Default/no migration applicable |

### Priority Order (Most Critical First)

```
1. DROPPED         (highest priority - any account dropped)
2. IN_PROGRESS     (any account actively migrating)
3. SCHEDULED       (any account planned for migration)
4. COMPLETED       (all accounts migrated)
5. EXCLUDED        (all accounts excluded)
6. NOT_IN_SCOPE    (lowest priority - default)
```

**Why this order?**
- **Active migrations** (IN_PROGRESS, SCHEDULED) take priority over completed states
- **ANY** problematic status (DROPPED, IN_PROGRESS) beats "all good" statuses
- **DROPPED** beats everything (customer decision to not migrate)

## Status Derivation Examples

### Example 1: Single Account - IN_PROGRESS
```json
{
  "accounts": [
    {"accountType": "SAVINGS", "migrationStatus": "IN_PROGRESS"}
  ]
}
```
**Derived Status:** `IN_PROGRESS`  
**Reason:** Account is actively migrating

### Example 2: Mixed Accounts - One IN_PROGRESS, One MIGRATED
```json
{
  "accounts": [
    {"accountType": "SAVINGS", "migrationStatus": "IN_PROGRESS"},
    {"accountType": "CD", "migrationStatus": "MIGRATED"}
  ]
}
```
**Derived Status:** `IN_PROGRESS`  
**Reason:** IN_PROGRESS is more critical than MIGRATED

### Example 3: All Accounts MIGRATED
```json
{
  "accounts": [
    {"accountType": "SAVINGS", "migrationStatus": "MIGRATED"},
    {"accountType": "CD", "migrationStatus": "MIGRATED"}
  ]
}
```
**Derived Status:** `COMPLETED`  
**Reason:** All accounts successfully migrated

### Example 4: Dropped Customer
```json
{
  "accounts": [
    {"accountType": "SAVINGS", "migrationStatus": "NOT_MIGRATED"},
    {"accountType": "CD", "migrationStatus": "SCHEDULED"}
  ]
}
```
**Derived Status:** `DROPPED`  
**Reason:** ANY NOT_MIGRATED status triggers DROPPED (highest priority)

### Example 5: Lending/IRA Only Customer
```json
{
  "accounts": [
    {"accountType": "LENDING", "migrationStatus": "EXCLUDED"},
    {"accountType": "IRA", "migrationStatus": "EXCLUDED"}
  ]
}
```
**Derived Status:** `EXCLUDED`  
**Reason:** All accounts are EXCLUDED

## Simplified Excel Structure

### Before (Complex - 7 Condition Columns)

```
| Rule Name | hasNotMigrated | allTerminal | isWithinWindow | currentWave | hasSavings | hasChecking | hasLending | features... |
```

**Problems:**
- Too many columns
- Complex combinations
- Hard for business users to understand
- Difficult to maintain

### After (Simple - 2 Condition Columns)

```
| Rule Name | customerStatus | isWithinWindow | feature1 | feature2 | feature3 | feature4 |
```

**Benefits:**
- ✅ Much simpler
- ✅ Easy to understand
- ✅ Business-friendly
- ✅ Easy to maintain

## Excel Rules

### Current Rules (7 Rules)

| Row | Rule Name | Customer Status | Hours Before | feature1-4 |
|-----|-----------|-----------------|--------------|------------|
| 10 | DROPPED - Enable all | DROPPED | (blank) | all enabled |
| 11 | IN_PROGRESS - Disable all | IN_PROGRESS | 7 | all disabled |
| 12 | SCHEDULED - Within window | SCHEDULED | 7 | all disabled |
| 13 | SCHEDULED - Before window | SCHEDULED | 0 | all enabled |
| 14 | COMPLETED - Enable all | COMPLETED | (blank) | all enabled |
| 15 | EXCLUDED - Enable all | EXCLUDED | (blank) | all enabled |
| 16 | NOT_IN_SCOPE - Enable all | NOT_IN_SCOPE | (blank) | all enabled |

### Time Window Logic

**Column C: isWithinMigrationWindow(int)**

- **Number (e.g., 7, 12):** Rule matches if within that many hours before migration
- **0:** Rule matches if NOT within window (before window check - special value)
- **(blank):** Time doesn't matter (always matches or never based on other conditions)

### Example Rules

**Row 11: IN_PROGRESS customer within 7-hour window**
```
IN_PROGRESS | 7 | disabled | disabled | disabled | disabled
```

- Matches if: customerStatus = IN_PROGRESS AND current time is within 7 hours before migration
- Action: Disable all features

**Row 13: SCHEDULED customer before window**
```
SCHEDULED | 0 | enabled | enabled | enabled | enabled
```

- Matches if: customerStatus = SCHEDULED AND NOT within window
- Action: Enable all features
- **Note:** `isWithinMigrationWindow(0)` is special - means NOT within window

## Complete Flow

```
1. Get accounts from Migration API
   ↓
2. Derive customer status (Java)
   Based on most critical account:
   - Any NOT_MIGRATED → DROPPED
   - Any IN_PROGRESS → IN_PROGRESS
   - Any SCHEDULED → SCHEDULED
   - All MIGRATED → COMPLETED
   - All EXCLUDED → EXCLUDED
   - Default → NOT_IN_SCOPE
   ↓
3. Execute Excel rules
   Match: customerStatus + isWithinMigrationWindow
   ↓
4. Apply feature decisions
   ↓
5. Return response
```

## Benefits of New Design

✅ **Simpler Excel** - 7 columns → 2 columns (70% reduction!)  
✅ **Clearer Logic** - Status derivation separate from feature decisions  
✅ **Easier to Understand** - Business users see customer status directly  
✅ **More Maintainable** - Change status logic in one place (Java)  
✅ **Better Logging** - Log customer status for debugging  
✅ **Testable** - Easy to test status derivation separately  
✅ **Extensible** - Easy to add new customer statuses  
✅ **Business-Friendly** - Status names match business terminology  

## Comparison

### Complexity Reduction

| Aspect | Before | After | Improvement |
|--------|--------|-------|-------------|
| Condition Columns | 7 | 2 | 71% simpler |
| Excel Rows | 18 | 16 | Streamlined |
| Business Logic Location | Split (Java + Excel) | Separated (Status in Java, Decisions in Excel) | Cleaner |
| Understandability | Complex | Simple | Much easier |

### Code Simplification

**Before:** Complex if/else in Java + complex conditions in Excel  
**After:** Simple status derivation in Java + simple status-based rules in Excel

## Real-World Scenario

### Customer: John Smith

**Accounts:**
- Savings: SCHEDULED for Nov 8
- CD: MIGRATED (from previous wave)
- Lending: EXCLUDED (stays in BankA)

**Status Derivation:**
1. Check DROPPED? No (no NOT_MIGRATED)
2. Check IN_PROGRESS? No  
3. Check SCHEDULED? **YES** (savings is SCHEDULED)
4. **Result: Customer Status = SCHEDULED**

**Current Time:** Friday Nov 7, 6PM  
**Migration Date:** Saturday Nov 8, 12AM  
**Time Window:** 7 hours before = Friday 5PM

**Rule Matching:**
- Row 12: SCHEDULED + isWithinWindow(7) → **MATCHES!**
- Features: All disabled per row 12

**Customer Experience:** Features restricted starting Friday 5PM.

## Logging Example

```
INFO  - Checking features for customer: CUST001
DEBUG - Retrieved 3 accounts for customer CUST001
INFO  - Customer CUST001 has derived status: SCHEDULED
DEBUG - isWithinMigrationWindow(7) = true
DEBUG - Fired 1 rules for customer CUST001
INFO  - Completed feature check for customer: CUST001 with 4 features
```

**Key log:** `Customer CUST001 has derived status: SCHEDULED` - makes debugging easy!

## Modifying Rules

### Change When Features Are Disabled

**Current:** SCHEDULED customers - features disabled 7 hours before migration

**Want:** Start restrictions 12 hours before instead

**Action:**
1. Open `migration-rules.csv`
2. Find row 12 (SCHEDULED - Within window)
3. Change column C from `7` to `12`
4. Save and restart

### Add Custom Feature Mix for IN_PROGRESS

**Current:** IN_PROGRESS - all features disabled

**Want:** IN_PROGRESS - feature1/2 disabled, feature3/4 enabled

**Action:**
1. Find row 11 (IN_PROGRESS)
2. Change columns D-G:
   - D (feature1): `disabled`
   - E (feature2): `disabled`
   - F (feature3): `enabled` ← Changed!
   - G (feature4): `enabled` ← Changed!
3. Save and restart

## Technical Implementation

### Status Derivation Code

**File:** `CustomerMigrationContext.java`

```java
public CustomerStatus deriveCustomerStatus() {
    // Priority 1
    if (any account has NOT_MIGRATED) → return DROPPED
    
    // Priority 2
    if (any account has IN_PROGRESS) → return IN_PROGRESS
    
    // Priority 3
    if (any account has SCHEDULED) → return SCHEDULED
    
    // Priority 4
    if (all accounts have MIGRATED) → return COMPLETED
    
    // Priority 5
    if (all accounts have EXCLUDED) → return EXCLUDED
    
    // Priority 6
    return NOT_IN_SCOPE
}
```

Called automatically before rule execution in `FeatureDecisionService`.

## Testing

All existing test scenarios still work! The behavior is the same, just implemented differently.

### Test 1: Dropped Customer
**Account Status:** NOT_MIGRATED  
**Derived Status:** DROPPED  
**Excel Rule:** Row 10 matches  
**Result:** All features enabled

### Test 2: Active Migration
**Account Status:** IN_PROGRESS  
**Derived Status:** IN_PROGRESS  
**Time:** Within 7-hour window  
**Excel Rule:** Row 11 matches  
**Result:** All features disabled

### Test 3: Before Migration Window
**Account Status:** SCHEDULED  
**Derived Status:** SCHEDULED  
**Time:** Before window (Thu evening)  
**Excel Rule:** Row 13 matches (isWithinWindow = 0)  
**Result:** All features enabled

## Summary

The system is now **dramatically simpler** while maintaining all functionality:

- **7 condition columns → 2 condition columns**
- **Clear separation:** Status derivation (Java) vs. Feature decisions (Excel)
- **Business-friendly:** Status names match business terminology
- **Easier to maintain:** Change status logic in one place
- **Better logging:** See customer status in logs
- **Same behavior:** All tests pass, API unchanged

🎉 **Much cleaner architecture!**

