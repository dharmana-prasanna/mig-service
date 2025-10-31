# Simplified Excel Guide - Customer Status Based Rules

## For Business Users

The Excel rules file is now **MUCH SIMPLER**! We've reduced from 7 confusing columns to just 2 easy-to-understand columns.

## New Excel Structure

### What You See in Excel

| Rule Name | Customer Status | Hours Before Migration | feature1 | feature2 | feature3 | feature4 |
|-----------|-----------------|------------------------|----------|----------|----------|----------|
| DROPPED - Enable all | DROPPED | (blank) | enabled | enabled | enabled | enabled |
| IN_PROGRESS - Disable all | IN_PROGRESS | 7 | disabled | disabled | disabled | disabled |
| SCHEDULED - Within window | SCHEDULED | 7 | disabled | disabled | disabled | disabled |
| SCHEDULED - Before window | SCHEDULED | 0 | enabled | enabled | enabled | enabled |
| COMPLETED - Enable all | COMPLETED | (blank) | enabled | enabled | enabled | enabled |
| EXCLUDED - Enable all | EXCLUDED | (blank) | enabled | enabled | enabled | enabled |
| NOT_IN_SCOPE - Enable all | NOT_IN_SCOPE | (blank) | enabled | enabled | enabled | enabled |

**That's it! Just 7 simple rules.**

## Column Explanations

### Column B: Customer Status

The system automatically determines the customer's overall migration status by looking at all their accounts:

| Status | What It Means | When You See It |
|--------|---------------|-----------------|
| **DROPPED** | Customer won't be migrated (dropped) | Any account marked NOT_MIGRATED |
| **IN_PROGRESS** | Customer is migrating RIGHT NOW | Any account being migrated |
| **SCHEDULED** | Customer will migrate soon | Any account scheduled for migration |
| **COMPLETED** | Customer already migrated | All accounts finished migrating |
| **EXCLUDED** | Customer never migrates (lending/IRA only) | All accounts are lending or IRA |
| **NOT_IN_SCOPE** | Customer not affected by migration | No migration planned |

**You don't set this** - the system calculates it automatically!

### Column C: Hours Before Migration

When should migration restrictions start?

| Value | Meaning | Example |
|-------|---------|---------|
| **7** | Start 7 hours before migration | Friday 5PM for Saturday 12AM |
| **12** | Start 12 hours before migration | Friday 12PM for Saturday 12AM |
| **24** | Start 24 hours before migration | Friday 12AM for Saturday 12AM |
| **0** | Before the window (special value) | Normal operation before restrictions |
| **(blank)** | Time doesn't matter | Always apply this rule |

### Columns D-G: Features (Same as Before)

- **enabled** - Feature is ON
- **disabled** - Feature is OFF
- **(blank)** - Use default (ON)

## How It Works

### Example: Customer Being Migrated

**John Smith's Accounts:**
- Savings: IN_PROGRESS
- CD: MIGRATED  
- Lending: EXCLUDED

**Step 1: System Derives Status**
- Has IN_PROGRESS account? **YES**
- **Customer Status = IN_PROGRESS**

**Step 2: System Checks Time Window**
- Current time: Friday 7PM
- Migration date: Saturday 12AM
- Hours before: 7 (window starts Friday 5PM)
- Within window? **YES**

**Step 3: Excel Rule Matches**
- Row 11: IN_PROGRESS + within 7-hour window
- Action: Disable all features

**Result:** All features disabled

### Example: Customer Before Migration Window

**Jane Doe's Accounts:**
- Savings: SCHEDULED
- Checking: SCHEDULED

**Step 1: System Derives Status**
- Has SCHEDULED account? **YES**
- **Customer Status = SCHEDULED**

**Step 2: System Checks Time Window**
- Current time: Thursday 10PM
- Migration date: Saturday 12AM
- Hours before: 7 (window starts Friday 5PM)
- Within window? **NO** (too early)

**Step 3: Excel Rule Matches**
- Row 13: SCHEDULED + NOT within window (0 means before window)
- Action: Enable all features

**Result:** All features enabled (normal operations)

## Modifying Rules

### Change When Restrictions Start

**Want:** Start SCHEDULED restrictions earlier (12 hours instead of 7)

1. Open Excel
2. Find row 12 (SCHEDULED - Within window)
3. Change column C from `7` to `12`
4. Save
5. Ask IT to restart

**Result:** Restrictions now start Friday 12PM instead of Friday 5PM

### Change Features for IN_PROGRESS Customers

**Want:** IN_PROGRESS customers can use feature3 and feature4

1. Open Excel
2. Find row 11 (IN_PROGRESS)
3. Change:
   - Column F (feature3): `disabled` → `enabled`
   - Column G (feature4): `disabled` → `enabled`
4. Save and restart

**Result:** IN_PROGRESS customers have feature1/2 disabled, feature3/4 enabled

### Add New Customer Status Rule

**Want:** Special rule for COMPLETED customers with custom features

1. Insert new row after row 14
2. Fill in:
   - A: `COMPLETED - Special Case`
   - B: `COMPLETED`
   - C: (blank)
   - D-G: Your feature mix
3. Save and restart

## Benefits vs. Old Design

| Aspect | Old Design | New Design |
|--------|------------|------------|
| **Excel Columns** | 11 columns (7 conditions!) | 7 columns (2 conditions) |
| **Understandability** | Complex - multiple conditions | Simple - customer status + time |
| **Business Terminology** | Technical (hasNotMigrated, allTerminal) | Business (DROPPED, COMPLETED) |
| **Ease of Modification** | Hard - many conditions to track | Easy - just status and hours |
| **Debugging** | Difficult - which condition failed? | Easy - see customer status in logs |

## Customer Status Meanings in Plain English

- **DROPPED:** "Customer changed their mind, not migrating"
- **IN_PROGRESS:** "Customer is migrating right now"
- **SCHEDULED:** "Customer will migrate soon"
- **COMPLETED:** "Customer already moved to new bank"
- **EXCLUDED:** "Customer only has lending/IRA (never moves)"
- **NOT_IN_SCOPE:** "Customer not part of migration"

## Quick Reference Card

### What You Can Change in Excel

✅ **Hours before migration** (column C) - when restrictions start  
✅ **Feature status** (columns D-G) - which features are on/off  
✅ **Add new rules** - insert rows for custom scenarios  

### What You CAN'T Change in Excel

❌ **Customer Status** (column B) - system calculates this automatically  
❌ **Status derivation logic** - hardcoded in Java (requires developer)  

But that's okay! The status derivation is simple and stable. You have full control over feature decisions.

## Summary

The new design is **dramatically simpler**:
- Just 2 condition columns (customer status + time window)
- Business-friendly terminology
- Easy to understand and modify
- Same powerful functionality
- Better logging and debugging

**You can now manage migration rules without understanding complex technical conditions!** 🎉

