# All Rules in CSV - Updated Structure

## Overview

**ALL business logic is now in the CSV file!** This includes:
- Priority 1: Dropped customers (NOT_MIGRATED)
- Priority 2: Terminal states (all MIGRATED/EXCLUDED/NOT_MIGRATED)
- Priority 3: WAVE1/WAVE2 active migration rules

## New CSV Structure

### Excel Layout (With Time-Based Control!)

| Row | Col A | Col B | Col C | Col D | Col E | Col F | Col G | Col H | Col I | Col J | Col K | Col L |
|-----|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|
| 7 | NAME | CONDITION | CONDITION | **CONDITION** | CONDITION | CONDITION | CONDITION | CONDITION | ACTION | ACTION | ACTION | ACTION |
| 8 | Rule Name | hasNotMigratedStatus() | allAccountsInTerminalState() | **isWithinMigrationWindow(int)** | currentWave | hasSavingsOrCD() | hasChecking() | hasLendingOrIRA() | feature1 | feature2 | feature3 | feature4 |
| 9 | | $context : CustomerMigrationContext | $context | **$context** | $context | $context | $context | $context | $context | $context | $context | $context |
| 10 | Priority 1 - Dropped Customer | **true** | | | | | | | enabled | enabled | enabled | enabled |
| 11 | Priority 2 - Terminal States | **false** | **true** | | | | | | enabled | enabled | enabled | enabled |
| 12 | WAVE1 - Suppress Savings/CD only | false | false | **7** | WAVE1 | true | false | false | disabled | disabled | disabled | disabled |
| 13 | WAVE1 - Enable Savings/CD with Checking | false | false | **7** | WAVE1 | true | true | | enabled | enabled | enabled | enabled |
| 14 | WAVE1 - Suppress Savings/CD with Lending/IRA | false | false | **7** | WAVE1 | true | | true | disabled | disabled | disabled | disabled |
| 15 | WAVE2 - Suppress Savings/CD/Checking | false | false | **12** | WAVE2 | true | true | false | disabled | disabled | disabled | disabled |
| 16 | WAVE2 - Suppress with Lending/IRA | false | false | **12** | WAVE2 | true | true | true | disabled | disabled | disabled | disabled |
| 17 | Default - Not in scope | false | false | | NOT_APPLICABLE | | | | enabled | enabled | enabled | enabled |
| 18 | Default - No Savings or CD | false | false | | | false | | | enabled | enabled | enabled | enabled |

## New Condition Columns

### Column B: hasNotMigratedStatus()
- **true** = ANY account has NOT_MIGRATED status (dropped customer)
- **false** = No accounts with NOT_MIGRATED status
- **(blank)** = Don't check this condition

### Column C: allAccountsInTerminalState()
- **true** = ALL accounts are (MIGRATED OR EXCLUDED OR NOT_MIGRATED)
- **false** = At least one account is SCHEDULED or IN_PROGRESS
- **(blank)** = Don't check this condition

### Column D: isWithinMigrationWindow(int) - NEW!
- **Number (e.g., 7, 12, 24)** = Hours before migration when window starts
  - `7` = Window starts 7 hours before migration (Fri 5PM for Sat 12AM)
  - `12` = Window starts 12 hours before migration (Fri 12PM for Sat 12AM)
  - `24` = Window starts 24 hours before migration (Fri 12AM for Sat 12AM)
- **(blank)** = Don't check time window (always apply or never based on other conditions)

**How it works:**
- Compares current server time with (migration date - hours)
- Returns TRUE if within the window
- Returns FALSE if before window or after migration complete

### Columns E-H: Existing Conditions (Same as Before)
- **Column E:** currentWave (WAVE1, WAVE2, NOT_APPLICABLE)
- **Column F:** hasSavingsOrCD() (true/false/blank)
- **Column G:** hasChecking() (true/false/blank)
- **Column H:** hasLendingOrIRA() (true/false/blank)

### Columns I-L: Feature Actions (Same as Before)
- **Column I:** feature1 (enabled/disabled/blank)
- **Column J:** feature2 (enabled/disabled/blank)
- **Column K:** feature3 (enabled/disabled/blank)
- **Column L:** feature4 (enabled/disabled/blank)

## Rule Evaluation Order

Rules are evaluated **top to bottom**. First matching rule wins.

### Row 10 - Priority 1 (Highest Priority)
```
IF hasNotMigratedStatus() = true
THEN enable all features
REASON: "Features enabled, not migrating"
```

This catches dropped customers before anything else.

### Row 11 - Priority 2
```
IF hasNotMigratedStatus() = false 
AND allAccountsInTerminalState() = true
THEN enable all features
REASON: "Features enabled, migration completed or not applicable"
```

This catches customers with all accounts in terminal states (post-migration or excluded-only).

### Rows 12-16 - WAVE1/WAVE2 Rules
```
IF hasNotMigratedStatus() = false
AND allAccountsInTerminalState() = false
AND [WAVE1 or WAVE2 conditions]
THEN apply feature-specific rules
```

These only fire for customers with active migrations (SCHEDULED or IN_PROGRESS).

### Rows 17-18 - Default Rules
Catch-all rules for any remaining scenarios.

## Benefits of All Rules in CSV

✅ **Complete Visibility** - All logic visible in one spreadsheet  
✅ **Business Control** - Business users can modify even priority logic  
✅ **Single Source of Truth** - No logic split between Java and Excel  
✅ **Easier Testing** - All scenarios testable by editing Excel  
✅ **Better Documentation** - Rules are self-documenting  
✅ **Audit Trail** - All changes tracked in version control  

## How to Modify

### To Change Priority 1 Behavior
Edit Row 10. For example, to disable specific features for dropped customers:
```
Row 10: ... | enabled | disabled | enabled | enabled
```

### To Change Priority 2 Behavior
Edit Row 11. For example, to disable feature1 for terminal state customers:
```
Row 11: ... | disabled | enabled | enabled | enabled
```

### To Add a New Priority Rule
Insert a new row after Row 11 (before WAVE1 rules) with appropriate conditions.

### To Modify WAVE1/WAVE2 Rules
Edit Rows 12-16 as before.

## Example Scenarios

### Scenario 1: Dropped Customer
- **hasNotMigratedStatus()** = true
- **Matches:** Row 10 (Priority 1)
- **Result:** All features enabled
- **Rows 11-18:** Skipped (first match wins)

### Scenario 2: All Accounts Migrated
- **hasNotMigratedStatus()** = false (no dropped accounts)
- **allAccountsInTerminalState()** = true (all MIGRATED)
- **Matches:** Row 11 (Priority 2)
- **Result:** All features enabled
- **Rows 12-18:** Skipped

### Scenario 3: Active WAVE1 Migration
- **hasNotMigratedStatus()** = false
- **allAccountsInTerminalState()** = false (has SCHEDULED/IN_PROGRESS)
- **currentWave** = WAVE1
- **hasSavingsOrCD()** = true
- **hasChecking()** = false
- **Matches:** Row 12 (WAVE1 - Suppress Savings/CD only)
- **Result:** All features disabled per row 12

## Code Changes

### Java Code Simplified
The FeatureDecisionService is now much simpler:

```java
// Just fire rules - all logic in CSV
KieSession kieSession = kieContainer.newKieSession();
try {
    kieSession.insert(context);
    kieSession.fireAllRules();
} finally {
    kieSession.dispose();
}

// Apply defaults
context.applyDefaults();

// Build response
return buildResponse(...);
```

No more if/else preprocessing logic in Java!

## Testing

All test scenarios remain the same. The behavior is identical, just the implementation location changed from Java to CSV.

### Test 1: Dropped Customer
Migration API returns NOT_MIGRATED → Row 10 fires → All features enabled

### Test 2: All Migrated
Migration API returns all MIGRATED → Row 11 fires → All features enabled

### Test 3: Active Migration
Migration API returns SCHEDULED → Rows 12-16 fire → Feature control per rules

## Migration from Previous Version

### What Changed
**Before:** Priority 1 and 2 logic in Java, WAVE1/WAVE2 in CSV  
**After:** ALL logic in CSV

### Impact
- ✅ Behavior is identical
- ✅ No API changes
- ✅ Same test scenarios work
- ✅ Just implementation location changed

## Summary

**ALL rules are now in the CSV file!** Business users have complete control over:
- Priority 1: Dropped customer handling
- Priority 2: Terminal state handling
- Priority 3: WAVE1/WAVE2 feature control
- Default behaviors

This makes the system more transparent and gives business users full control over all decision logic without requiring code changes.

