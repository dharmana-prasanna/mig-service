# Business User Guide - Managing Features with Excel

## For Non-Technical Users

This guide explains how to control individual features during migration using Microsoft Excel or Google Sheets. **No programming knowledge required!**

## What Changed - Per-Feature Control!

**Before:** All features were either ON or OFF together.  
**Now:** Each feature can be independently enabled or disabled!

Example: You can now have feature1=OFF, feature2=OFF, feature3=ON, feature4=ON

## What You Need

- Microsoft Excel, Google Sheets, or any spreadsheet application
- Access to the file: `src/main/resources/rules/migration-rules.csv` (or `.xlsx`)

## Opening the Rules File

### Option 1: Using Microsoft Excel
1. Navigate to project folder: `src/main/resources/rules/`
2. Double-click `migration-rules.csv`
3. Excel will open the file

### Option 2: Using Google Sheets
1. Go to Google Sheets
2. File → Open → Upload the `migration-rules.csv` file
3. Make changes
4. Download as Excel (.xlsx) or CSV

## Understanding the Spreadsheet

### Do NOT Modify (Rows 1-9)
These are configuration rows. Leave them as-is.

### Safe to Modify (Rows 10 and below)
These are the actual business rules you can change.

## Spreadsheet Layout

```
Row 1-5:  Configuration (DON'T TOUCH)
Row 6:    Table header
Row 7-9:  Column definitions (DON'T TOUCH)
Row 10+:  YOUR BUSINESS RULES (Safe to modify)
```

## The Business Rules (Starting Row 10)

Each row is one rule. Here's what each column means:

| Column | Name | What It Means | Values You Can Use |
|--------|------|---------------|-------------------|
| **A** | Rule Name | Description of the rule | Any text |
| **B** | Migration Wave | Which migration wave | WAVE1, WAVE2, NOT_APPLICABLE, or blank |
| **C** | Has Savings/CD? | Does customer have savings or CD account? | true, false, or blank |
| **D** | Has Checking? | Does customer have checking account? | true, false, or blank |
| **E** | Has Lending/IRA? | Does customer have lending or IRA account? | true, false, or blank |
| **F** | feature1 | Feature 1 status | enabled, disabled, or blank |
| **G** | feature2 | Feature 2 status | enabled, disabled, or blank |
| **H** | feature3 | Feature 3 status | enabled, disabled, or blank |
| **I** | feature4 | Feature 4 status | enabled, disabled, or blank |

### Important: Blank Cells

**In Condition Columns (B-E):**
- Blank = "I don't care about this condition"

**In Feature Columns (F-I):**
- Blank = Use default (enabled)
- `enabled` = Feature is ON
- `disabled` = Feature is OFF

## Common Scenarios

### Example 1: Disable Only Feature 1 and Feature 2

**Current Rule (Row 10):**
```
Columns: ... | disabled | disabled | disabled | disabled
```

**Change to disable only feature1 and feature2:**
```
Columns: ... | disabled | disabled | enabled | enabled
```

Now feature1 and feature2 are OFF, but feature3 and feature4 are ON!

### Example 2: Enable Only Feature 3

Want only feature3 to be available?

```
Columns: ... | disabled | disabled | enabled | disabled
```

Result: feature1=OFF, feature2=OFF, feature3=ON, feature4=OFF

### Example 3: Leave Some Features as Default

```
Columns: ... | disabled | | | enabled
```

Result: feature1=OFF, feature2=Default(ON), feature3=Default(ON), feature4=ON

### Example 4: Add a New Rule with Mixed Features

Want WAVE1 customers with savings only to have mixed feature access?

1. **Insert a new row** after row 16
2. Fill in:
   - A: `WAVE1 - Special Savings Rule`
   - B: `WAVE1`
   - C: `true`
   - D: `false`
   - E: (blank)
   - F: `disabled` ← feature1 OFF
   - G: `disabled` ← feature2 OFF
   - H: `enabled` ← feature3 ON
   - I: `enabled` ← feature4 ON

## Current Rules Summary

| Row | Rule Purpose | Wave | feature1 | feature2 | feature3 | feature4 |
|-----|--------------|------|----------|----------|----------|----------|
| 10 | Savings/CD only in WAVE1 | WAVE1 | OFF | OFF | OFF | OFF |
| 11 | Savings/CD + Checking in WAVE1 | WAVE1 | ON | ON | ON | ON |
| 12 | Savings/CD + Lending/IRA in WAVE1 | WAVE1 | OFF | OFF | OFF | OFF |
| 13 | Savings/CD + Checking in WAVE2 | WAVE2 | OFF | OFF | OFF | OFF |
| 14 | Savings/CD + Checking + Lending/IRA in WAVE2 | WAVE2 | OFF | OFF | OFF | OFF |
| 15 | Customer not in any wave | N/A | ON | ON | ON | ON |
| 16 | Customer without savings/CD | N/A | ON | ON | ON | ON |

## Step-by-Step: Changing Feature Status

### Scenario: Enable feature3 for WAVE1 savings customers

**Goal:** WAVE1 customers with savings only should have feature1/2/4 OFF but feature3 ON

1. **Open the file** in Excel
2. **Find Row 10** (WAVE1 - Suppress Savings/CD only)
3. **Current values:**
   - F10: `disabled`
   - G10: `disabled`
   - H10: `disabled`
   - I10: `disabled`
4. **Change H10 (feature3)** from `disabled` to `enabled`
5. **New values:**
   - F10: `disabled`
   - G10: `disabled`
   - H10: `enabled` ← CHANGED!
   - I10: `disabled`
6. **Save the file**
7. **Tell your IT team** to restart the application

## Step-by-Step: Adding More Features

### Scenario: Add feature5

1. **Open the file** in Excel
2. **In Row 7, Column J:** Type `ACTION`
3. **In Row 8, Column J:** Type `feature5`
4. **In Row 9, Column J:** Type `$context`
5. **For each rule row (10+):** Add `enabled` or `disabled` in column J
6. **Save the file**
7. **Tell your IT team** to restart the application and update the code to handle feature5

## Visual Guide

### Before (All or Nothing):
```
| Rule | Wave | ... | Single Action |
| Rule1 | WAVE1 | ... | All OFF |
```

### After (Granular Control):
```
| Rule | Wave | ... | feat1 | feat2 | feat3 | feat4 |
| Rule1 | WAVE1 | ... | OFF | OFF | ON | OFF |
```

## Common Mistakes to Avoid

❌ **DON'T** modify rows 1-9 (configuration rows)  
❌ **DON'T** use anything other than `enabled` or `disabled` in feature columns  
❌ **DON'T** add extra spaces before or after `enabled`/`disabled`  
❌ **DON'T** use capital letters inconsistently (use all lowercase: `enabled`, not `Enabled`)  
✅ **DO** keep backups before making changes  
✅ **DO** test changes with IT before going to production  
✅ **DO** use blank cells for default behavior (enabled)  
✅ **DO** document your changes in Column A (rule name)  

## Validation Checklist

Before saving your changes, verify:

- [ ] Rows 1-9 are unchanged
- [ ] Feature columns (F-I) have only `enabled`, `disabled`, or are blank
- [ ] No typos in `enabled`/`disabled` (all lowercase)
- [ ] Row 8 still shows feature names (feature1, feature2, feature3, feature4)
- [ ] No extra spaces in cells

## Real-World Examples

### Example 1: Gradual Feature Rollout

You want to gradually enable features for WAVE1 customers:
- Week 1: Only feature1 enabled
- Week 2: feature1 and feature2 enabled
- Week 3: All features enabled

**Week 1 (Row 10):**
```
| ... | enabled | disabled | disabled | disabled |
```

**Week 2 (update Row 10):**
```
| ... | enabled | enabled | disabled | disabled |
```

**Week 3 (update Row 10):**
```
| ... | enabled | enabled | enabled | enabled |
```

### Example 2: A/B Testing

Test different feature combinations for different customer segments:

**Group A (Row 10):**
```
| WAVE1 - Group A | WAVE1 | true | false | false | enabled | enabled | disabled | disabled |
```

**Group B (New Row):**
```
| WAVE1 - Group B | WAVE1 | true | false | false | disabled | disabled | enabled | enabled |
```

### Example 3: Emergency Disable

Need to quickly disable a problematic feature?

Find all rows and change that feature column to `disabled`:
- feature2 causing issues? Change all Column G values to `disabled`

## Getting Help

If you see errors after restarting:
1. Check the application logs with IT
2. Look for "Error building rules" messages
3. Compare your file with the backup
4. Verify spelling of `enabled`/`disabled`
5. Refer to `EXCEL_RULES_TEMPLATE.md` for detailed structure

## Quick Reference Card

### Valid Feature Values

**Columns F, G, H, I (feature1-4):**
- `enabled` - Feature is ON
- `disabled` - Feature is OFF
- (blank) - Use default (ON)

### Example Rule
```
| WAVE1 Mixed | WAVE1 | true | false | false | disabled | disabled | enabled | enabled |
```
Means: In WAVE1, for customers with savings but no checking:
- feature1: OFF
- feature2: OFF
- feature3: ON
- feature4: ON

## Need More Help?

Contact your IT team and reference:
- This guide: `BUSINESS_USER_GUIDE.md`
- Detailed template: `EXCEL_RULES_TEMPLATE.md`
- Technical docs: `README.md`

## Benefits of Per-Feature Control

✅ **Flexibility** - Fine-tune feature availability  
✅ **Gradual Rollout** - Enable features one at a time  
✅ **A/B Testing** - Test different feature combinations  
✅ **Emergency Response** - Quickly disable problematic features  
✅ **Business Control** - No code changes needed  
