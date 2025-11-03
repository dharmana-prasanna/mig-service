# Customer Accounts Endpoint

## Overview

A new endpoint that retrieves customer account information from the migration API with optional feature suppression details in a single call.

## Endpoint Specification

**Path:** `POST /api/features/customers/{customerId}/accounts`

**Method:** POST

**Path Parameter:**
- `customerId` - The customer identifier

**Query Parameter:**
- `withFeatures` - Boolean (default: false)
  - `true` - Include feature suppression information
  - `false` - Return only accounts and customer status

**Request Body (optional):**
- Only required if `withFeatures=true`
- Contains list of features to check

```json
{
  "features": ["feature1", "feature2", "feature3", "feature4"]
}
```

## Use Cases

### Use Case 1: Get Accounts Only

**Request:**
```bash
POST /api/features/customers/CUST001/accounts?withFeatures=false
Content-Type: application/json
```

**Response:**
```json
{
  "customerId": "CUST001",
  "customerStatus": "SCHEDULED",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    },
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

**Use Case:** Display customer's accounts and migration status without feature details.

---

### Use Case 2: Get Accounts with All Features

**Request:**
```bash
POST /api/features/customers/CUST001/accounts?withFeatures=true
Content-Type: application/json

{
  "features": ["feature1", "feature2", "feature3", "feature4"]
}
```

**Response:**
```json
{
  "customerId": "CUST001",
  "customerStatus": "IN_PROGRESS",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "IN_PROGRESS",
      "migrationWave": "WAVE1",
      "migrationDate": "2025-11-08"
    }
  ],
  "featureSuppressionInfo": {
    "feature1": {
      "feature": "feature1",
      "enabled": false,
      "reason": "IN_PROGRESS: feature1 disabled"
    },
    "feature2": {
      "feature": "feature2",
      "enabled": false,
      "reason": "IN_PROGRESS: feature2 disabled"
    },
    "feature3": {
      "feature": "feature3",
      "enabled": false,
      "reason": "IN_PROGRESS: feature3 disabled"
    },
    "feature4": {
      "feature": "feature4",
      "enabled": false,
      "reason": "IN_PROGRESS: feature4 disabled"
    }
  }
}
```

**Use Case:** Show complete migration context - accounts, status, and feature availability.

---

### Use Case 3: Get Accounts with Specific Features

**Request:**
```bash
POST /api/features/customers/CUST001/accounts?withFeatures=true
Content-Type: application/json

{
  "features": ["feature1", "feature3"]
}
```

**Response:**
```json
{
  "customerId": "CUST001",
  "customerStatus": "SCHEDULED",
  "accounts": [
    {
      "accountId": "ACC001",
      "accountType": "SAVINGS",
      "migrationStatus": "SCHEDULED",
      "migrationWave": "WAVE2",
      "migrationDate": "2025-11-15"
    }
  ],
  "featureSuppressionInfo": {
    "feature1": {
      "feature": "feature1",
      "enabled": false,
      "reason": "SCHEDULED: feature1 disabled"
    },
    "feature3": {
      "feature": "feature3",
      "enabled": true,
      "reason": "SCHEDULED: feature3 enabled"
    }
  }
}
```

**Use Case:** Check specific features without querying all features.

## Response Structure

### Always Included

- **customerId** - Customer identifier
- **customerStatus** - Derived status (DROPPED, IN_PROGRESS, SCHEDULED, COMPLETED, EXCLUDED, NOT_IN_SCOPE)
- **accounts** - Array of account objects from migration API

### Conditionally Included (only if withFeatures=true)

- **featureSuppressionInfo** - Map of feature names to feature status objects
  - Key: Feature name (e.g., "feature1")
  - Value: FeatureStatus object with enabled flag and reason

## Benefits

✅ **Single API Call** - Get both accounts and feature information together  
✅ **Flexible** - Consumer controls whether to include feature info  
✅ **Efficient** - Feature evaluation only when requested  
✅ **Complete Context** - Shows customer status, accounts, and features in one response  
✅ **Reusable** - Leverages existing feature decision logic  
✅ **Performance** - Avoid unnecessary feature calculations when not needed  

## Comparison with /check Endpoint

### /api/features/check
- **Purpose:** Check feature availability only
- **Input:** customerId (header) + features (body)
- **Output:** Feature status list
- **Use Case:** When you only need to know feature availability

### /api/features/customers/{customerId}/accounts
- **Purpose:** Get accounts with optional feature info
- **Input:** customerId (path) + withFeatures (query) + features (body, optional)
- **Output:** Accounts + customer status + optional feature suppression info
- **Use Case:** When you need complete migration context

## Examples

### Example 1: Display Migration Dashboard

Show customer their accounts and what features are available:

```bash
POST /api/features/customers/CUST001/accounts?withFeatures=true
Body: {"features": ["online_transfer", "bill_pay", "mobile_deposit", "alerts"]}
```

Display:
- Account list with migration status
- Which features are currently available
- Customer's overall migration status

### Example 2: Account List Only

Just show customer their accounts during migration:

```bash
POST /api/features/customers/CUST001/accounts?withFeatures=false
```

Display:
- Account list
- Migration status per account
- Overall customer status

### Example 3: Feature Availability Check

Check if specific high-priority features are available:

```bash
POST /api/features/customers/CUST001/accounts?withFeatures=true
Body: {"features": ["online_transfer", "bill_pay"]}
```

Display:
- Account status
- Just the critical features availability

## Error Handling

### 400 Bad Request
- Invalid customerId in path
- withFeatures=true but no features in body

### 503 Service Unavailable
- Migration API is down
- Cannot retrieve account information

## Testing

### Test 1: Accounts Only
```bash
curl -X POST "http://localhost:8080/api/features/customers/CUST001/accounts?withFeatures=false" \
  -H "Content-Type: application/json"
```

**Expected:** Accounts + customer status (no featureSuppressionInfo)

### Test 2: Accounts with Features
```bash
curl -X POST "http://localhost:8080/api/features/customers/CUST001/accounts?withFeatures=true" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature2", "feature3", "feature4"]}'
```

**Expected:** Accounts + customer status + featureSuppressionInfo

### Test 3: Specific Features
```bash
curl -X POST "http://localhost:8080/api/features/customers/CUST001/accounts?withFeatures=true" \
  -H "Content-Type: application/json" \
  -d '{"features": ["feature1", "feature3"]}'
```

**Expected:** Accounts + customer status + suppression info for feature1 and feature3 only

## Implementation Details

### Service Method
`FeatureDecisionService.getAccountsWithFeatures()`

**Flow:**
1. Call migration API to get accounts
2. Derive customer status from accounts
3. If withFeatures=true:
   - Execute Drools rules
   - Build feature suppression info map
4. Return combined response

### Controller Endpoint
`FeatureController.getAccountsWithFeatures()`

**Validation:**
- customerId from path parameter (required)
- withFeatures from query parameter (optional, default: false)
- Request body only required if withFeatures=true

## Related Documentation

- `README.md` - Main API documentation
- `CUSTOMER_STATUS_DESIGN.md` - Customer status derivation logic
- `TIME_BASED_CONTROL.md` - Time window configuration
- `sample-requests.http` - HTTP test examples

