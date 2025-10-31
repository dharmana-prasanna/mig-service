# Quick Start Guide

## What You Have

A complete Spring Boot service with Drools rules engine for managing feature flags during bank migration.

## Project Files

<!-- @import "[TOC]" {cmd="toc" depthFrom=1 depthTo=6 orderedList=false} -->

<!-- code_chunk_output -->

- [Quick Start Guide](#quick-start-guide)
  - [What You Have](#what-you-have)
  - [Project Files](#project-files)
    - [Core Application Files](#core-application-files)
    - [Business Rules](#business-rules)
    - [REST API](#rest-api)
    - [Services](#services)
    - [Configuration](#configuration)
    - [Models](#models)
    - [Error Handling](#error-handling)
    - [Documentation](#documentation)
  - [5-Minute Setup](#5-minute-setup)
    - [1. Configure Migration API URL](#1-configure-migration-api-url)
    - [2. Build the Project](#2-build-the-project)
    - [3. Run the Service](#3-run-the-service)
    - [4. Test the API](#4-test-the-api)
  - [How It Works](#how-it-works)
    - [Request Flow (Three-Tier Logic)](#request-flow-three-tier-logic)
    - [Business Logic (in Excel Decision Table with Granular Control)](#business-logic-in-excel-decision-table-with-granular-control)
  - [Key Configuration Points](#key-configuration-points)
    - [1. Drools Rules Location (Excel Decision Table)](#1-drools-rules-location-excel-decision-table)
    - [2. Migration API Configuration](#2-migration-api-configuration)
    - [3. Server Port](#3-server-port)
  - [Testing Scenarios](#testing-scenarios)
  - [API Specification](#api-specification)
    - [Endpoint](#endpoint)
    - [Headers](#headers)
    - [Request Body](#request-body)
    - [Response (Success - 200)](#response-success---200)
    - [Response (Error - 400)](#response-error---400)
    - [Response (Error - 503)](#response-error---503)
  - [Modifying Business Rules (Excel)](#modifying-business-rules-excel)
    - [Excel Rule Structure](#excel-rule-structure)
  - [Health Check](#health-check)
  - [Troubleshooting](#troubleshooting)
    - [Issue: Migration API not reachable](#issue-migration-api-not-reachable)
    - [Issue: Rules not firing](#issue-rules-not-firing)
    - [Issue: Validation errors](#issue-validation-errors)
    - [Issue: Wrong feature decisions](#issue-wrong-feature-decisions)
    - [Issue: Excel file not loading](#issue-excel-file-not-loading)
  - [Next Steps](#next-steps)
  - [Support](#support)
  - [Dependencies](#dependencies)
  - [Production Considerations](#production-considerations)

<!-- /code_chunk_output -->


### Core Application Files
- `pom.xml` - Maven configuration with Spring Boot and Drools dependencies
- `src/main/java/com/bank/migration/MigrationFeatureManagementApplication.java` - Main application entry point
- `src/main/resources/application.yml` - Configuration file

### Business Rules
- `src/main/resources/rules/migration-rules.xlsx` - **Excel decision table for WAVE1 and WAVE2 logic** (business-friendly!)
- `src/main/resources/rules/migration-rules.csv` - CSV format (opens in Excel, better for version control)
- `src/main/resources/META-INF/kmodule.xml` - Drools configuration
- `EXCEL_RULES_TEMPLATE.md` - **Instructions for creating/modifying the Excel file**

### REST API
- `src/main/java/com/bank/migration/controller/FeatureController.java` - POST endpoint `/api/features/check`

### Services
- `src/main/java/com/bank/migration/service/FeatureDecisionService.java` - Orchestrates Drools execution
- `src/main/java/com/bank/migration/service/MigrationApiClient.java` - Calls migration team's API

### Configuration
- `src/main/java/com/bank/migration/config/DroolsConfig.java` - Drools KieContainer setup
- `src/main/java/com/bank/migration/config/MigrationApiConfig.java` - Migration API configuration

### Models
- `src/main/java/com/bank/migration/model/dto/` - Request/Response DTOs
- `src/main/java/com/bank/migration/model/migration/` - Migration API models and enums
- `src/main/java/com/bank/migration/model/rules/CustomerMigrationContext.java` - Drools fact object

### Error Handling
- `src/main/java/com/bank/migration/exception/GlobalExceptionHandler.java` - Global exception handler
- `src/main/java/com/bank/migration/exception/MigrationApiException.java` - Custom exception

### Documentation
- `README.md` - Comprehensive documentation with architecture and API details
- `MIGRATION_SCENARIOS.md` - 14 test scenarios with expected results
- `sample-requests.http` - HTTP request examples for testing
- `QUICKSTART.md` - This file

## 5-Minute Setup

### 1. Configure Migration API URL
Edit `src/main/resources/application.yml`:
```yaml
migration:
  api:
    base-url: http://your-migration-api-server:9090
```

Or set environment variable:
```bash
export MIGRATION_API_BASE_URL=http://your-migration-api-server:9090
```

### 2. Build the Project
```bash
mvn clean package
```

### 3. Run the Service
```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/migration-feature-management-1.0.0-SNAPSHOT.jar
```

### 4. Test the API
```bash
curl -X POST http://localhost:8080/api/features/check \
  -H "customerId: CUST001" \
  -H "Content-Type: application/json" \
  -d '{
    "features": ["feature1", "feature2", "feature3", "feature4"]
  }'
```

## How It Works

### Request Flow (Three-Tier Logic)
1. Client sends POST request with `customerId` header and list of features
2. Service calls migration API to get customer's account statuses
3. **Priority 1 - Dropped Customers:** If ANY account has `NOT_MIGRATED` status:
   - Enable ALL features with reason "Features enabled, not migrating"
   - Skip rule evaluation (customer staying in BankA)
   - Return response
4. **Priority 2 - Terminal States:** If ALL accounts are (`MIGRATED` OR `EXCLUDED` OR `NOT_MIGRATED`):
   - Enable ALL features with reason "Features enabled, migration completed or not applicable"
   - Skip rule evaluation (no active migration)
   - Return response
5. **Priority 3 - Active Migration:** Otherwise (has `SCHEDULED` or `IN_PROGRESS` accounts):
   - Create `CustomerMigrationContext` with account data
   - Drools engine evaluates rules in Excel decision table
   - Rules determine which features to enable/suppress based on:
     - Account types (savings, checking, CD, lending, IRA)
     - Migration wave (WAVE1, WAVE2)
     - Migration status
   - Return feature status list with reasons

### Business Logic (in Excel Decision Table with Granular Control)

The rules are defined in an **Excel spreadsheet** with **per-feature control**. Each feature can be individually enabled or disabled!

**WAVE1 Default Rules:**
- Customer with savings/CD only → All features disabled (but customizable!)
- Customer with savings/CD + checking → All features enabled (but customizable!)
- Customer with savings/CD + lending/IRA → All features disabled (but customizable!)

**WAVE2 Default Rules:**
- Customer with savings/CD + checking → All features disabled (but customizable!)
- Customer with savings/CD + checking + lending/IRA → All features disabled (but customizable!)

**NEW! Per-Feature Control:**
You can now set each feature independently. Example:
- feature1: disabled
- feature2: disabled
- feature3: enabled ← Different!
- feature4: disabled

**To modify rules:** Open `migration-rules.xlsx` in Excel, change feature columns, save, and restart. See `EXCEL_RULES_TEMPLATE.md` for detailed instructions and `GRANULAR_FEATURE_UPDATE.md` for per-feature control examples.

## Key Configuration Points

### 1. Drools Rules Location (Excel Decision Table)
File: `src/main/resources/rules/migration-rules.xlsx` (or `.csv` which opens in Excel)

**To modify business logic:**
1. Open the Excel/CSV file
2. Edit the rule rows (rows 10+)
3. Save the file
4. Restart the application

See `EXCEL_RULES_TEMPLATE.md` for detailed Excel structure and modification instructions.

**Note:** Currently using CSV format. To convert to Excel:
- Open `migration-rules.csv` in Excel
- Save As → Excel Workbook (.xlsx)
- Place in `src/main/resources/rules/migration-rules.xlsx`

### 2. Migration API Configuration
File: `src/main/resources/application.yml`

```yaml
migration:
  api:
    base-url: http://migration-api:9090
    endpoint: /api/customer/{customerId}/accounts
    timeout: 5000
```

### 3. Server Port
Default: 8080. Change in `application.yml`:
```yaml
server:
  port: 8080
```

## Testing Scenarios

See `MIGRATION_SCENARIOS.md` for 14 detailed test scenarios covering:
- WAVE1 scenarios (6 cases)
- WAVE2 scenarios (5 cases)
- Edge cases (3 cases)

Use `sample-requests.http` with REST Client or IntelliJ HTTP Client for quick testing.

## API Specification

### Endpoint
```
POST /api/features/check
```

### Headers
```
customerId: {customer-id}
Content-Type: application/json
```

### Request Body
```json
{
  "features": ["feature1", "feature2", "feature3", "feature4"]
}
```

### Response (Success - 200)
```json
{
  "customerId": "CUST001",
  "features": [
    {
      "feature": "feature1",
      "enabled": false,
      "reason": "WAVE1: Customer with savings/cd only is being migrated to BankB"
    },
    {
      "feature": "feature2",
      "enabled": false,
      "reason": "WAVE1: Customer with savings/cd only is being migrated to BankB"
    }
  ]
}
```

### Response (Error - 400)
```json
{
  "timestamp": "2025-10-31T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "customerId header is required"
}
```

### Response (Error - 503)
```json
{
  "timestamp": "2025-10-31T10:30:00",
  "status": 503,
  "error": "Migration API Error",
  "message": "Failed to retrieve migration data for customer: CUST001"
}
```

## Modifying Business Rules (Excel)

To change feature suppression logic:

1. Open `src/main/resources/rules/migration-rules.csv` in Excel (or use `.xlsx` version)
2. Find the rule row you want to modify (rows 10-16)
3. Update conditions (columns B-E) or actions (column F)
4. To add a new rule: Insert a new row after the last rule
5. Save the file
6. Restart the application (rules are loaded at startup)

### Excel Rule Structure
- **Rows 1-9:** Configuration and headers (don't modify)
- **Rows 10+:** Actual business rules (safe to modify/add)
- **Empty cells:** Mean "any value" for that condition
- **Column B:** Migration wave (WAVE1, WAVE2, NOT_APPLICABLE)
- **Columns C-E:** Boolean conditions (true/false)
- **Column F:** Action to take

See `EXCEL_RULES_TEMPLATE.md` for complete documentation with examples.

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

## Troubleshooting

### Issue: Migration API not reachable
**Solution:** Check `MIGRATION_API_BASE_URL` environment variable or `application.yml`

### Issue: Rules not firing
**Solution:** Check logs for Drools rule compilation errors. Look for "Successfully loaded migration rules decision table" in logs.

### Issue: Validation errors
**Solution:** Ensure `customerId` header is provided and `features` array is not empty

### Issue: Wrong feature decisions
**Solution:** Open the Excel/CSV file and review rule conditions. Remember empty cells mean "any value". Check row order - rules are evaluated top to bottom.

### Issue: Excel file not loading
**Solution:** Ensure file name is exactly `migration-rules.xlsx` or `migration-rules.csv` in `src/main/resources/rules/` directory.

## Next Steps

1. **Integration:** Update `MIGRATION_API_BASE_URL` to point to actual migration API
2. **Testing:** Use scenarios from `MIGRATION_SCENARIOS.md` to validate
3. **Monitoring:** Enable actuator endpoints for health checks
4. **Deployment:** Package as Docker container or deploy to Kubernetes
5. **Customization:** Modify rules in Excel file (`migration-rules.xlsx` or `.csv`) as business requirements change

## Support

For questions or issues, refer to:
- `README.md` - Full documentation
- `MIGRATION_SCENARIOS.md` - Test scenarios and decision matrix
- `sample-requests.http` - API examples

## Dependencies

- **Java 17+** - Required for Spring Boot 3.2.0
- **Maven 3.8+** - Build tool
- **Spring Boot 3.2.0** - Application framework
- **Drools 8.44.0** - Rules engine
- **Lombok** - Reduces boilerplate code

## Production Considerations

1. **API Timeout:** Adjust `migration.api.timeout` based on network latency
2. **Logging:** Set appropriate log levels in `application.yml`
3. **Security:** Add authentication/authorization as needed
4. **Rate Limiting:** Consider adding rate limiting for the endpoint
5. **Caching:** Cache migration API responses if data doesn't change frequently
6. **Monitoring:** Add metrics and alerting for production use
7. **Error Handling:** Current implementation returns appropriate HTTP status codes

