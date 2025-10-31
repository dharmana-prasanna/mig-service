# Time-Based Feature Control - Examples and Scenarios

## Quick Reference

### Time Window Formula
```
Window Start Time = Migration Date - Hours Before Migration
```

### Examples
| Migration Date | Hours Before | Window Starts |
|----------------|--------------|---------------|
| Sat Nov 8, 12AM | 7 | Fri Nov 7, 5PM |
| Sat Nov 8, 12AM | 12 | Fri Nov 7, 12PM |
| Sat Nov 8, 12AM | 24 | Fri Nov 7, 12AM |
| Sat Nov 8, 12AM | 48 | Thu Nov 6, 12AM |

## Complete Timeline Example

### WAVE1 Migration Weekend: November 8-9, 2025

**Configuration in Excel:**
- WAVE1 rules (rows 12-14): `isWithinMigrationWindow(7)`
- Migration Date: Saturday, Nov 8, 2025 at 12:00 AM

### Hour-by-Hour Timeline

| Date/Time | Within Window? | Rule Applied | Feature Status |
|-----------|----------------|--------------|----------------|
| **Thu Nov 6, 10:00 PM** | ❌ NO | Default | ✅ All enabled |
| **Fri Nov 7, 12:00 PM** | ❌ NO | Default | ✅ All enabled |
| **Fri Nov 7, 4:00 PM** | ❌ NO | Default | ✅ All enabled |
| **Fri Nov 7, 4:59 PM** | ❌ NO | Default | ✅ All enabled |
| **Fri Nov 7, 5:00 PM** | ✅ YES | WAVE1 Rules | ⚙️ Per Excel |
| **Fri Nov 7, 6:00 PM** | ✅ YES | WAVE1 Rules | ⚙️ Per Excel |
| **Fri Nov 7, 11:00 PM** | ✅ YES | WAVE1 Rules | ⚙️ Per Excel |
| **Sat Nov 8, 12:00 AM** | ✅ YES | WAVE1 Rules | ⚙️ Per Excel |
| **Sat Nov 8, 6:00 AM** | ✅ YES | WAVE1 Rules | ⚙️ Per Excel |
| **Sat Nov 8, 2:00 PM** | ✅ YES* | WAVE1 Rules* | ⚙️ Per Excel* |
| **Sat Nov 8, 6:00 PM** | ❌ NO** | Terminal State** | ✅ All enabled** |
| **Sun Nov 9, 10:00 AM** | ❌ NO | Terminal State | ✅ All enabled |

\* If migration still in progress  
** If migration completed (status = MIGRATED)

## Detailed Scenarios

### Scenario 1: Thursday Evening (Before Window)

**Current Time:** Thursday, Nov 6, 2025, 10:00 PM  
**Migration Date:** Saturday, Nov 8, 2025, 12:00 AM  
**Hours Before:** 7  
**Window Starts:** Friday, Nov 7, 2025, 5:00 PM  

**Calculation:**
- Current: 2025-11-06T22:00
- Window Start: 2025-11-07T17:00
- Is current >= window start? **NO** (current is before)

**Result:**
```
isWithinMigrationWindow(7) = false
→ WAVE1 rules don't match (time condition fails)
→ Falls to default rules
→ All features ENABLED
```

**Customer Experience:** Normal banking operations, all features available.

---

### Scenario 2: Friday 3:00 PM (2 Hours Before Window)

**Current Time:** Friday, Nov 7, 2025, 3:00 PM  
**Migration Date:** Saturday, Nov 8, 2025, 12:00 AM  
**Hours Before:** 7  
**Window Starts:** Friday, Nov 7, 2025, 5:00 PM  

**Calculation:**
- Current: 2025-11-07T15:00
- Window Start: 2025-11-07T17:00
- Is current >= window start? **NO** (2 hours early)

**Result:**
```
isWithinMigrationWindow(7) = false
→ All features ENABLED
```

**Customer Experience:** Still normal operations. Features work until 5PM.

---

### Scenario 3: Friday 5:00 PM (Window Just Started)

**Current Time:** Friday, Nov 7, 2025, 5:00 PM  
**Migration Date:** Saturday, Nov 8, 2025, 12:00 AM  
**Hours Before:** 7  
**Window Starts:** Friday, Nov 7, 2025, 5:00 PM  

**Calculation:**
- Current: 2025-11-07T17:00
- Window Start: 2025-11-07T17:00
- Is current >= window start? **YES** (exactly at start)
- Is current < migration date + 1 day? **YES**

**Result:**
```
isWithinMigrationWindow(7) = true
→ WAVE1 rules match
→ Apply feature control per Excel
→ Features DISABLED (or as configured)
```

**Customer Experience:** Migration restrictions kick in at exactly 5PM.

---

### Scenario 4: Friday 8:00 PM (Within Window)

**Current Time:** Friday, Nov 7, 2025, 8:00 PM  
**Migration Date:** Saturday, Nov 8, 2025, 12:00 AM  
**Hours Before:** 7  
**Window Starts:** Friday, Nov 7, 2025, 5:00 PM  

**Calculation:**
- Current: 2025-11-07T20:00
- Window Start: 2025-11-07T17:00
- Is current >= window start? **YES** (3 hours into window)

**Result:**
```
isWithinMigrationWindow(7) = true
→ WAVE1 rules apply
→ Features controlled per Excel
```

**Customer Experience:** Restrictions active, features per migration plan.

---

### Scenario 5: Saturday 6:00 AM (During Migration)

**Current Time:** Saturday, Nov 8, 2025, 6:00 AM  
**Migration Date:** Saturday, Nov 8, 2025, 12:00 AM  
**Hours Before:** 7  

**Calculation:**
- Current: 2025-11-08T06:00
- Window Start: 2025-11-07T17:00 (yesterday 5PM)
- Is current >= window start? **YES**
- Is current < migration date + 1 day? **YES** (before Sunday 12AM)

**Result:**
```
isWithinMigrationWindow(7) = true
→ WAVE1 rules apply
→ Migration in progress
```

**Customer Experience:** Restrictions continue during migration.

---

### Scenario 6: Saturday 6:00 PM (Migration Complete)

**Current Time:** Saturday, Nov 8, 2025, 6:00 PM  
**Account Status:** MIGRATED (migration completed)  

**Result:**
```
Priority 2: allAccountsInTerminalState() = true
→ All features ENABLED
→ Time window doesn't matter anymore
```

**Customer Experience:** Migration complete, full access restored.

---

## WAVE2 Example (Different Hours)

### Configuration
- WAVE2 Migration: Saturday, Nov 15, 2025, 12:00 AM
- Hours Before: **12** (different from WAVE1!)
- Window Starts: Friday, Nov 14, 2025, 12:00 PM (noon)

| Date/Time | Within Window? | Features |
|-----------|----------------|----------|
| **Fri Nov 14, 10:00 AM** | ❌ NO | ✅ All enabled |
| **Fri Nov 14, 11:59 AM** | ❌ NO | ✅ All enabled |
| **Fri Nov 14, 12:00 PM** | ✅ YES | ⚙️ Per rules |
| **Fri Nov 14, 5:00 PM** | ✅ YES | ⚙️ Per rules |
| **Sat Nov 15, 12:00 AM** | ✅ YES | ⚙️ Per rules |
| **Sat Nov 15, 3:00 PM** | ❌ NO (if MIGRATED) | ✅ All enabled |

## Changing Time Windows

### Scenario: Need to start restrictions earlier

**Current:** WAVE1 starts 7 hours before (Friday 5PM)  
**Want:** WAVE1 starts 24 hours before (Friday 12AM)

**Steps:**
1. Open `migration-rules.csv` in Excel
2. Find rows 12-14 (WAVE1 rules)
3. Change column C from `7` to `24`
4. Save file
5. Restart application

**Result:** Friday 12AM restrictions start instead of Friday 5PM

### Scenario: Emergency - Apply rules immediately

**Current:** WAVE1 starts 7 hours before  
**Want:** Apply rules now (regardless of time)

**Steps:**
1. Open `migration-rules.csv` in Excel
2. Find rows 12-14 (WAVE1 rules)
3. Delete value in column C (make it blank)
4. Save file
5. Restart application

**Result:** WAVE1 rules apply immediately for all WAVE1 customers

## Testing Different Times

To test time-based rules without waiting:

### Option 1: Change Hours in Excel
Set very small hours (e.g., `1` or `0.5`) to test immediately.

### Option 2: Modify Migration Date in Test Data
Have migration team API return migration date close to current time.

### Option 3: Mock System Time (For Testing)
Add configuration to inject current time for testing purposes.

## Summary

✅ **WAVE1:** 7 hours before migration (Friday 5PM for Saturday 12AM)  
✅ **WAVE2:** 12 hours before migration (Friday 12PM for Saturday 12AM)  
✅ **Before Window:** All features enabled (normal operation)  
✅ **Within Window:** Apply migration rules (feature control)  
✅ **Configurable:** Change hours in Excel column C  
✅ **Per-Rule:** Each rule can have different hours if needed  

