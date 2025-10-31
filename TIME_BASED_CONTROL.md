# Time-Based Feature Control

## Overview

The system now supports **time-based feature control** that automatically enables or disables features based on the migration date and a configurable time window. This allows you to start applying migration restrictions at a specific time before migration begins (e.g., Friday 5PM for Saturday 12AM migration).

## How It Works

### Time Window Concept

**Time Window** = Period before migration when restrictions should apply

- **Migration Date:** Saturday, Nov 8, 2025 at 12:00 AM (midnight)
- **Hours Before Migration:** 7 hours
- **Window Starts:** Friday, Nov 7, 2025 at 5:00 PM
- **Window Ends:** Saturday, Nov 8, 2025 at 11:59 PM

### Timeline Visualization

```
Thursday             Friday                  Saturday
   |                   |                        |
   |                5PM|------WINDOW-------  12AM|
   |                   |                        |
Normal Operation    Restrictions Start    Migration Occurs
Features Enabled    Apply Excel Rules     Continue Rules
```

## Configuration in Excel

### Column C: isWithinMigrationWindow(int)

This column takes a **number of hours** before migration when the window should start.

| Value | Meaning | Example |
|-------|---------|---------|
| **7** | Start 7 hours before migration | Friday 5PM for Saturday 12AM |
| **12** | Start 12 hours before migration | Friday 12PM for Saturday 12AM |
| **24** | Start 24 hours before migration | Friday 12AM for Saturday 12AM |
| **48** | Start 48 hours before migration | Thursday 12AM for Saturday 12AM |
| **(blank)** | Don't check time window | Always apply (or never based on other conditions) |

### Excel Structure

| Row | Rule Name | hasNotMigrated | allTerminal | **isWithinWindow** | currentWave | ... | features |
|-----|-----------|----------------|-------------|--------------------|-------------|-----|----------|
| 10 | Priority 1 | true | | | | ... | enabled |
| 11 | Priority 2 | false | true | | | ... | enabled |
| 12 | WAVE1 Rule | false | false | **7** | WAVE1 | ... | disabled |
| 15 | WAVE2 Rule | false | false | **12** | WAVE2 | ... | disabled |

## Current Configuration

### WAVE1 Rules: 7 Hours Before
- **Window starts:** 7 hours before migration date
- **Example:** Migration Saturday 12AM → Window starts Friday 5PM
- **Rows 12-14** use `isWithinMigrationWindow(7)`

### WAVE2 Rules: 12 Hours Before  
- **Window starts:** 12 hours before migration date
- **Example:** Migration Saturday 12AM → Window starts Friday 12PM
- **Rows 15-16** use `isWithinMigrationWindow(12)`

### Priority Rules: No Time Check
- **Rows 10-11** don't check time window
- These always apply when conditions match (dropped/terminal state)

## Rule Evaluation Logic

```
1. Priority 1 - Dropped Customer (hasNotMigrated = true)
   → Enable all features
   → Time window doesn't matter
   → STOP

2. Priority 2 - Terminal States (allTerminal = true)
   → Enable all features
   → Time window doesn't matter
   → STOP

3. Time Window Check (isWithinMigrationWindow = configured hours)
   → If BEFORE window: Features enabled (normal operation)
   → If WITHIN window: Continue to WAVE rules

4. WAVE1/WAVE2 Rules
   → Apply feature-specific control

5. Default Rules
   → Enable all features
```

## Example Scenarios

### Scenario 1: Before Time Window

**Setup:**
- Current Time: **Thursday, Nov 6, 2025 at 10:00 PM**
- Migration Date: **Saturday, Nov 8, 2025 at 12:00 AM**
- Hours Before (WAVE1): 7
- Window Starts: Friday, Nov 7, 2025 at 5:00 PM

**Result:**
- Row 12 checks: `isWithinMigrationWindow(7)` = **false** (not yet within window)
- Row 12 doesn't match
- Falls through to default rules
- **All features ENABLED** (normal operation)

### Scenario 2: Within Time Window

**Setup:**
- Current Time: **Friday, Nov 7, 2025 at 8:00 PM**
- Migration Date: **Saturday, Nov 8, 2025 at 12:00 AM**
- Hours Before (WAVE1): 7
- Window Starts: Friday, Nov 7, 2025 at 5:00 PM

**Result:**
- Row 12 checks: `isWithinMigrationWindow(7)` = **true** (within window!)
- Row 12 matches: WAVE1 + other conditions
- **Apply migration rules** (features per Excel configuration)

### Scenario 3: After Migration Started

**Setup:**
- Current Time: **Saturday, Nov 8, 2025 at 6:00 AM**
- Migration Date: **Saturday, Nov 8, 2025 at 12:00 AM**
- Hours Before (WAVE1): 7

**Result:**
- Row 12 checks: `isWithinMigrationWindow(7)` = **true** (still within window)
- **Apply migration rules** (migration in progress)

### Scenario 4: After Migration Complete

**Setup:**
- Current Time: **Sunday, Nov 9, 2025 at 10:00 AM**
- Migration Date: **Saturday, Nov 8, 2025 at 12:00 AM**
- Account Status: **MIGRATED**

**Result:**
- Row 11 matches: `allAccountsInTerminalState()` = true
- **All features ENABLED** (migration complete)

## Time Calculation Details

### Window Start Time Calculation

```
Window Start = Migration Date - Hours Before Migration

Examples:
- Migration: Saturday 12:00 AM (00:00)
- Hours Before: 7
- Window Start: Friday 5:00 PM (17:00)

- Migration: Saturday 12:00 AM (00:00)
- Hours Before: 12
- Window Start: Friday 12:00 PM (12:00)

- Migration: Saturday 12:00 AM (00:00)
- Hours Before: 24
- Window Start: Friday 12:00 AM (00:00)
```

### Window End Time

Window ends at **migration date + 1 day** to cover the entire migration day.

```
Window End = Migration Date + 1 day

Example:
- Migration: Saturday 12:00 AM (00:00)
- Window End: Sunday 12:00 AM (00:00)
```

## Modifying Time Windows in Excel

### To Change WAVE1 Window (Currently 7 Hours)

1. Open `migration-rules.csv` in Excel
2. Find WAVE1 rules (Rows 12-14)
3. Change Column C from `7` to desired hours:
   - `12` → Start 12 hours before (Friday 12PM)
   - `24` → Start 24 hours before (Friday 12AM)
   - `48` → Start 48 hours before (Thursday 12AM)
4. Save and restart

### To Change WAVE2 Window (Currently 12 Hours)

1. Open `migration-rules.csv` in Excel
2. Find WAVE2 rules (Rows 15-16)
3. Change Column C from `12` to desired hours
4. Save and restart

### To Disable Time Window

Set Column C to blank (empty). Rule will match regardless of time.

**Use case:** Emergency - need to apply rules immediately without waiting for time window.

## Real-World Timeline Example

### Migration Weekend: November 8-9, 2025

**WAVE1 Migration:** Saturday 12AM  
**WAVE1 Window:** 7 hours before = Friday 5PM

| Day/Time | Status | Features |
|----------|--------|----------|
| **Thursday 11/6, 10PM** | Before window | ✅ All enabled |
| **Friday 11/7, 3PM** | Before window | ✅ All enabled |
| **Friday 11/7, 5PM** | **Window starts!** | ⚙️ Apply rules |
| **Friday 11/7, 8PM** | Within window | ⚙️ Apply rules |
| **Saturday 11/8, 12AM** | Migration starts | ⚙️ Apply rules |
| **Saturday 11/8, 6AM** | Migration ongoing | ⚙️ Apply rules |
| **Saturday 11/8, 2PM** | Migration complete | ✅ All enabled (if MIGRATED) |

### WAVE2 Migration: November 15-16, 2025

**WAVE2 Migration:** Saturday 12AM  
**WAVE2 Window:** 12 hours before = Friday 12PM

| Day/Time | Status | Features |
|----------|--------|----------|
| **Friday 11/14, 10AM** | Before window | ✅ All enabled |
| **Friday 11/14, 12PM** | **Window starts!** | ⚙️ Apply rules |
| **Friday 11/14, 5PM** | Within window | ⚙️ Apply rules |
| **Saturday 11/15, 12AM** | Migration starts | ⚙️ Apply rules |
| **Saturday 11/15, 10AM** | Migration complete | ✅ All enabled (if MIGRATED) |

## API Response Examples

### Before Time Window (Normal Operation)
```json
{
  "customerId": "CUST001",
  "features": [
    {
      "feature": "feature1",
      "enabled": true,
      "reason": "Default: Feature enabled (not specified in rules)"
    }
  ]
}
```

**Log:**
```
DEBUG - Rule WAVE1 - Suppress Savings/CD only did not match (time window condition failed)
```

### Within Time Window (Restrictions Applied)
```json
{
  "customerId": "CUST001",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE1: feature1 disabled"
    }
  ]
}
```

**Log:**
```
DEBUG - Rule WAVE1 - Suppress Savings/CD only matched
DEBUG - Customer within migration window
```

## Benefits

✅ **Automated Timing** - No manual intervention needed  
✅ **Seamless Transition** - Features automatically restricted at configured time  
✅ **Flexible Configuration** - Different hours per wave in Excel  
✅ **Normal Operation** - Features work until window starts  
✅ **Business Control** - Change hours without code deployment  
✅ **Predictable** - Based on server time, consistent behavior  

## Important Notes

### Time Source
- Uses **server's current time** (`LocalDateTime.now()`)
- Ensure server time is correct and synchronized (NTP)
- Consider timezone - uses server's default timezone

### Multiple Migration Dates
- If customer has multiple accounts with different migration dates
- Uses **ANY** account within window (if any account is within window, returns true)
- This ensures earliest migration date triggers the window

### Null Migration Dates
- Accounts with `migrationDate = null` are ignored in time calculation
- Rule won't match based on time if all dates are null

## Edge Cases

### Case 1: Mixed Migration Dates
Customer has savings (migrating Nov 8) and checking (migrating Nov 15).

**Current Time:** Friday Nov 7, 5PM
- Savings window (7 hours) starts Friday 5PM → Within window
- Checking window (12 hours) starts Friday 12PM → Within window
- **Result:** isWithinMigrationWindow returns true

### Case 2: Past Migration Date
Customer's migration date was last week.

**Current Time:** Nov 10
- Migration Date: Nov 8
- Window: Would have been Nov 7 5PM to Nov 9 12AM
- Current time is AFTER window end
- **Result:** isWithinMigrationWindow returns false
- Falls through to default or terminal state rules

### Case 3: Far Future Migration
Customer scheduled for Dec 15 migration.

**Current Time:** Nov 7, 5PM
- Migration Date: Dec 15 12AM
- Window starts: Dec 14 5PM
- **Result:** Before window, features enabled

## Testing Time-Based Rules

### Test 1: Before Window (Features Enabled)
```bash
# Assuming current time is before window
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_WAVE1_FUTURE" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** All features enabled (default rules)

### Test 2: Within Window (Restrictions Applied)
```bash
# Assuming current time is within 7-hour window
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_WAVE1_NOW" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** Features per WAVE1 rules (likely disabled)

### Test 3: After Migration (Terminal State)
```bash
# Assuming current time is after migration
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST_WAVE1_DONE" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** All features enabled (terminal state)

## Recommended Hours Configuration

| Wave | Recommended Hours | Window Start | Reason |
|------|-------------------|--------------|--------|
| WAVE1 | 7 | Friday 5PM | Give 1 evening before weekend |
| WAVE2 | 12 | Friday 12PM | Longer window for second wave |

**Note:** These are configurable in Excel Column C (rows 12-16).

## Troubleshooting

### Issue: Rules applying too early
**Solution:** Increase hours in Column C (e.g., change 24 to 7)

### Issue: Rules not applying when expected
**Solution:** 
- Check server time is correct
- Verify migration date in API response
- Check hours value in Column C
- Review logs for "within migration window" messages

### Issue: Rules still applying after migration
**Solution:** Check if account status changed to MIGRATED (should trigger terminal state logic)

## Integration with Existing Logic

### Priority Order (Complete Flow)

```
Priority 1: hasNotMigratedStatus() = true
  → Enable all features
  → Reason: "Features enabled, not migrating"
  → STOP (skip time window and all other checks)

Priority 2: allAccountsInTerminalState() = true  
  → Enable all features
  → Reason: "Features enabled, migration completed or not applicable"
  → STOP (skip time window and all other checks)

Priority 3: isWithinMigrationWindow(hours) check
  → If FALSE (before window):
      Falls through to default rules → All features enabled
  → If TRUE (within window):
      Continue to WAVE1/WAVE2 rules → Apply feature control

Priority 4: WAVE1/WAVE2 rules with account type conditions
  → Feature-specific control

Priority 5: Default rules
  → All features enabled
```

## Configuration Examples

### Conservative Approach (Long Window)
Start restrictions 24 hours before migration:
```
WAVE1 rules: isWithinMigrationWindow(24)
WAVE2 rules: isWithinMigrationWindow(24)
```

### Aggressive Approach (Short Window)
Start restrictions 4 hours before migration:
```
WAVE1 rules: isWithinMigrationWindow(4)
WAVE2 rules: isWithinMigrationWindow(4)
```

### Staggered Approach (Different Per Wave)
```
WAVE1 rules: isWithinMigrationWindow(7)  ← Current config
WAVE2 rules: isWithinMigrationWindow(12) ← Current config
```

### No Time Restriction
Leave column blank - always apply rules:
```
WAVE1 rules: isWithinMigrationWindow()  ← blank
```

## Modifying Time Windows

### Change All WAVE1 Rules
1. Open `migration-rules.csv` in Excel
2. Find rows 12-14 (WAVE1 rules)
3. Change Column C from `7` to desired hours
4. Save and restart

### Change Individual Rules
Each rule can have different hours if needed:
```
Row 12: WAVE1 - Suppress only | ... | 7  | ... (7 hours before)
Row 13: WAVE1 - Enable checking | ... | 12 | ... (12 hours before)
```

## Business Use Cases

### Use Case 1: Gradual Restriction Rollout
Start with short window, extend if needed:
- **Week 1:** 4 hours before
- **Week 2:** 7 hours before (if issues found)
- **Week 3:** 12 hours before (more conservative)

### Use Case 2: Weekend-Only Restrictions
Set hours to start Friday evening:
- Saturday 12AM migration
- 7 hours before = Friday 5PM
- Features work normally all week until Friday 5PM

### Use Case 3: Extended Maintenance Window
Need more preparation time:
- Saturday 12AM migration
- 48 hours before = Thursday 12AM
- Full two days of restricted operation

## Monitoring

### Log Messages

**Before Window:**
```
DEBUG - isWithinMigrationWindow(7) = false (current: 2025-11-07T15:00, window starts: 2025-11-07T17:00)
```

**Within Window:**
```
DEBUG - isWithinMigrationWindow(7) = true (current: 2025-11-07T19:00, window starts: 2025-11-07T17:00)
INFO  - Applying WAVE1 migration rules for customer CUST001
```

**After Window (Post-Migration):**
```
INFO  - Customer CUST001 has all accounts in terminal state - enabling all features
```

## FAQ

**Q: What timezone is used?**  
A: Server's default timezone. Ensure all servers use same timezone (UTC recommended).

**Q: Can I set hours to 0?**  
A: Yes. Window starts at exact migration date (12AM). Useful for testing.

**Q: What's the maximum hours I can set?**  
A: No limit, but practical range is 1-168 hours (1 hour to 7 days).

**Q: What happens if I remove the hours value (make it blank)?**  
A: Rule won't check time window. It will match based on other conditions only.

**Q: Can different rules in same wave have different hours?**  
A: Yes, but not recommended. Keep same hours per wave for consistency.

**Q: How precise is the timing?**  
A: Precise to the second based on server time. Window starts exactly at configured time.

**Q: What if migration date changes?**  
A: Migration API returns updated date, window automatically recalculates.

## Related Documentation

- `README.md` - Main system documentation
- `QUICKSTART.md` - Quick start guide
- `ALL_RULES_IN_CSV.md` - Complete CSV structure
- `BUSINESS_USER_GUIDE.md` - For non-technical users

