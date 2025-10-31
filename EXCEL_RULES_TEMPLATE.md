# Excel Decision Table Template for Migration Rules (Granular Feature Control)

## Overview

The business rules are now defined in an Excel file (`migration-rules.xlsx`) with **per-feature control**. Each feature can be individually enabled or disabled for different migration scenarios. This gives you maximum flexibility in controlling which features are available during migration.

## Excel File Structure

Create a file named `migration-rules.xlsx` in `src/main/resources/rules/`

### Sheet Structure

The Excel file has the following structure (each row number is important):

| Row | Column A | Column B | Column C | Column D | Column E | Column F | Column G | Column H | Column I |
|-----|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| 1 | **RuleSet** | com.bank.migration.rules | | | | | | | |
| 2 | **Sequential** | false | | | | | | | |
| 3 | **Import** | com.bank.migration.model.rules.CustomerMigrationContext | | | | | | | |
| 4 | **Import** | com.bank.migration.model.migration.MigrationWave | | | | | | | |
| 5 | | | | | | | | | |
| 6 | **RuleTable Migration Feature Rules** | | | | | | | | |
| 7 | **NAME** | **CONDITION** | **CONDITION** | **CONDITION** | **CONDITION** | **ACTION** | **ACTION** | **ACTION** | **ACTION** |
| 8 | Rule Name | currentWave | hasSavingsOrCD() | hasChecking() | hasLendingOrIRA() | feature1 | feature2 | feature3 | feature4 |
| 9 | | $context : CustomerMigrationContext | $context | $context | $context | $context | $context | $context | $context |
| 10 | WAVE1 - Suppress Savings/CD only | WAVE1 | true | false | false | disabled | disabled | disabled | disabled |
| 11 | WAVE1 - Enable Savings/CD with Checking | WAVE1 | true | true | | enabled | enabled | enabled | enabled |
| 12 | WAVE1 - Suppress Savings/CD with Lending/IRA | WAVE1 | true | | true | disabled | disabled | disabled | disabled |
| 13 | WAVE2 - Suppress Savings/CD/Checking | WAVE2 | true | true | false | disabled | disabled | disabled | disabled |
| 14 | WAVE2 - Suppress with Lending/IRA | WAVE2 | true | true | true | disabled | disabled | disabled | disabled |
| 15 | Default - Not in scope | NOT_APPLICABLE | | | | enabled | enabled | enabled | enabled |
| 16 | Default - No Savings or CD | | false | | | enabled | enabled | enabled | enabled |

## Detailed Excel Setup Instructions

### Step 1: Create Excel File
1. Open Excel or Google Sheets
2. Create a new workbook
3. Name the first sheet "Migration Rules" (or any name)

### Step 2: Configure RuleSet (Rows 1-4)

**Row 1:**
- Cell A1: `RuleSet`
- Cell B1: `com.bank.migration.rules`

**Row 2:**
- Cell A2: `Sequential`
- Cell B2: `false`

**Row 3:**
- Cell A3: `Import`
- Cell B3: `com.bank.migration.model.rules.CustomerMigrationContext`

**Row 4:**
- Cell A4: `Import`
- Cell B4: `com.bank.migration.model.migration.MigrationWave`

### Step 3: Leave Row 5 Empty

### Step 4: Create Rule Table Header (Row 6)

**Row 6:**
- Cell A6: `RuleTable Migration Feature Rules`

### Step 5: Define Columns (Row 7)

**Row 7 (Column Headers):**
- A7: `NAME`
- B7: `CONDITION`
- C7: `CONDITION`
- D7: `CONDITION`
- E7: `CONDITION`
- F7: `ACTION`
- G7: `ACTION`
- H7: `ACTION`
- I7: `ACTION`

### Step 6: Define Field Mappings (Row 8)

**Row 8:**
- A8: `Rule Name`
- B8: `currentWave`
- C8: `hasSavingsOrCD()`
- D8: `hasChecking()`
- E8: `hasLendingOrIRA()`
- F8: `feature1`
- G8: `feature2`
- H8: `feature3`
- I8: `feature4`

### Step 7: Define Object Binding (Row 9)

**Row 9:**
- A9: (empty)
- B9: `$context : CustomerMigrationContext`
- C9: `$context`
- D9: `$context`
- E9: `$context`
- F9: `$context`
- G9: `$context`
- H9: `$context`
- I9: `$context`

### Step 8: Define Business Rules (Rows 10-16)

**Row 10 - WAVE1: Savings/CD only (All Features Disabled)**
- A10: `WAVE1 - Suppress Savings/CD only`
- B10: `WAVE1`
- C10: `true`
- D10: `false`
- E10: `false`
- F10: `disabled`
- G10: `disabled`
- H10: `disabled`
- I10: `disabled`

**Row 11 - WAVE1: Savings/CD with Checking (All Features Enabled)**
- A11: `WAVE1 - Enable Savings/CD with Checking`
- B11: `WAVE1`
- C11: `true`
- D11: `true`
- E11: (empty)
- F11: `enabled`
- G11: `enabled`
- H11: `enabled`
- I11: `enabled`

**Row 12 - WAVE1: Savings/CD with Lending/IRA (All Features Disabled)**
- A12: `WAVE1 - Suppress Savings/CD with Lending/IRA`
- B12: `WAVE1`
- C12: `true`
- D12: (empty)
- E12: `true`
- F12: `disabled`
- G12: `disabled`
- H12: `disabled`
- I12: `disabled`

**Row 13 - WAVE2: Savings/CD/Checking (All Features Disabled)**
- A13: `WAVE2 - Suppress Savings/CD/Checking`
- B13: `WAVE2`
- C13: `true`
- D13: `true`
- E13: `false`
- F13: `disabled`
- G13: `disabled`
- H13: `disabled`
- I13: `disabled`

**Row 14 - WAVE2: With Lending/IRA (All Features Disabled)**
- A14: `WAVE2 - Suppress with Lending/IRA`
- B14: `WAVE2`
- C14: `true`
- D14: `true`
- E14: `true`
- F14: `disabled`
- G14: `disabled`
- H14: `disabled`
- I14: `disabled`

**Row 15 - Default: Not Applicable Wave (All Features Enabled)**
- A15: `Default - Not in scope`
- B15: `NOT_APPLICABLE`
- C15-E15: (empty)
- F15: `enabled`
- G15: `enabled`
- H15: `enabled`
- I15: `enabled`

**Row 16 - Default: No Savings/CD (All Features Enabled)**
- A16: `Default - No Savings or CD`
- B16-E16: Various (see above table)
- F16: `enabled`
- G16: `enabled`
- H16: `enabled`
- I16: `enabled`

## Column Explanations

### Columns A-E: Conditions (Same as Before)

**Column A: NAME** - The descriptive name of the rule

**Column B: currentWave** - Migration wave (WAVE1, WAVE2, NOT_APPLICABLE, or empty)

**Column C: hasSavingsOrCD()** - true/false/empty

**Column D: hasChecking()** - true/false/empty

**Column E: hasLendingOrIRA()** - true/false/empty

### Columns F-I: Feature Controls (NEW!)

**Column F: feature1** - Feature 1 status
- `enabled` - Feature1 is enabled
- `disabled` - Feature1 is disabled
- (empty) - Use default (enabled)

**Column G: feature2** - Feature 2 status
- `enabled` - Feature2 is enabled
- `disabled` - Feature2 is disabled
- (empty) - Use default (enabled)

**Column H: feature3** - Feature 3 status
- `enabled` - Feature3 is enabled
- `disabled` - Feature3 is disabled
- (empty) - Use default (enabled)

**Column I: feature4** - Feature 4 status
- `enabled` - Feature4 is enabled
- `disabled` - Feature4 is disabled
- (empty) - Use default (enabled)

## Important Notes

1. **Empty cells in feature columns** mean "use default" (enabled)
2. **Feature values** must be exactly `enabled` or `disabled` (case-insensitive)
3. **Add more features** by adding more ACTION columns (J, K, L, etc.)
4. **Row order matters** - Rules are evaluated in order from top to bottom
5. **First matching rule wins** - Later rules are not evaluated

## Adding More Features

To add `feature5`:

1. Add `ACTION` in column J, row 7
2. Add `feature5` in column J, row 8
3. Add `$context` in column J, row 9
4. Add `enabled` or `disabled` in column J for each rule row (10+)

## Example: Mixed Feature States

Want WAVE1 customers with savings only to have feature1 and feature2 disabled, but feature3 and feature4 enabled?

**Row 10 (modified):**
- A10: `WAVE1 - Mixed Features for Savings Only`
- B10: `WAVE1`
- C10: `true`
- D10: `false`
- E10: `false`
- F10: `disabled` ← feature1 OFF
- G10: `disabled` ← feature2 OFF
- H10: `enabled` ← feature3 ON
- I10: `enabled` ← feature4 ON

## Modifying Rules

### To Change Feature Status
1. Find the rule row
2. Change the feature column value from `enabled` to `disabled` or vice versa
3. Save the file and restart the application

### To Add a New Rule
1. Insert a new row after the last rule (after row 16)
2. Fill in the rule name, conditions, and feature statuses
3. Save the file and restart the application

### To Disable a Rule
1. Delete the entire row or
2. Prefix the rule name with "DISABLED -" for documentation

### To Change Default Behavior
The default for features not specified in rules is **enabled**. This is coded in the Java service and cannot be changed via Excel. To change the default, modify `CustomerMigrationContext.applyDefaults()`.

## Example Scenarios

### Scenario 1: Disable Only feature1 for WAVE1 Savings Customers

```
| WAVE1 Special | WAVE1 | true | false | false | disabled | enabled | enabled | enabled |
```

Result: feature1=OFF, feature2/3/4=ON

### Scenario 2: Enable Only feature3 for WAVE2 Checking Customers

```
| WAVE2 Special | WAVE2 | true | true | false | disabled | disabled | enabled | disabled |
```

Result: feature1/2/4=OFF, feature3=ON

### Scenario 3: Leave Some Features as Default

```
| Default Test | WAVE1 | true | false | false | disabled | | | enabled |
```

Result: feature1=OFF, feature2/3=Default(ON), feature4=ON

## Validation Checklist

Before saving your changes, verify:

- [ ] Rows 1-9 are unchanged
- [ ] Column B has valid values (WAVE1, WAVE2, NOT_APPLICABLE, or blank)
- [ ] Columns C, D, E have true, false, or are blank
- [ ] Columns F-I have enabled, disabled, or are blank
- [ ] All feature column values are spelled correctly (enabled/disabled)
- [ ] Row 8 has correct feature names (feature1, feature2, feature3, feature4)

## Troubleshooting

**Issue:** Features not working as expected
**Solution:** Check spelling of `enabled`/`disabled`, ensure no extra spaces

**Issue:** All features defaulting to enabled
**Solution:** Verify feature columns (F-I) have correct values, check row 8 has correct field names

**Issue:** Rules not firing
**Solution:** Check conditions in columns B-E match your test scenario

## Quick Reference Card

### Valid Feature Values

**Columns F, G, H, I (Features):**
- `enabled` - Feature is ON
- `disabled` - Feature is OFF
- (blank) - Use default (ON)

### Adding Features

To add more features beyond feature4:
- Column J: feature5
- Column K: feature6
- etc.

Just add more ACTION columns and update row 7-9 accordingly.

## Migration from Old Format

If you have the old format with a single ACTION column:

**Old format (Column F - ACTION):**
```
$context.suppressAllFeatures("reason");
```

**New format (Columns F-I - Per Feature):**
```
disabled | disabled | disabled | disabled
```

To convert:
- `suppressAllFeatures` → all features = `disabled`
- `enableAllFeatures` → all features = `enabled`
