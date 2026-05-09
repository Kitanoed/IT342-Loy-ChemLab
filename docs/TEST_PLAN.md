# Software Test Plan

## 1. Purpose
Validate the refactored ChemLab vertical-slice implementation and confirm all implemented backend, web, and mobile features function correctly after the restructuring.

## 2. Scope
In scope:
- Backend auth, user, inventory, bulk jobs, audit logs, and PubChem lookup APIs
- Web authentication, route guards, dashboard, and inventory routes
- Mobile auth storage and login/logout persistence
- Regression validation after vertical-slice refactor

Out of scope:
- External service uptime guarantees (PubChem availability)
- Production data migrations or data backfills
- Performance, load, and security penetration testing

## 3. Test Strategy
- Automated unit and slice tests cover auth service/controller behavior and route guards.
- Automated mobile unit tests cover token persistence and logout behavior.
- Manual regression exercises inventory and PubChem flows not covered by automated tests.

## 4. Functional Requirements Coverage
| Requirement | Coverage Type | Evidence |
| --- | --- | --- |
| User can register | Automated | Auth service/controller tests |
| User can log in | Automated | Auth service/controller tests |
| Authenticated user can retrieve profile | Manual | API validation during regression |
| Auth routes are protected | Automated | Web route guard tests |
| Public routes remain accessible | Automated | Web route guard tests |
| Auth state persists on device | Automated | Mobile TokenManager test |
| Inventory list/search works | Manual | API + web inventory route check |
| Inventory detail view works | Manual | API + web item route check |
| Inventory create/update works | Manual | API validation for POST/PUT/PATCH |
| Bulk job create/validate/execute works | Manual | API validation for bulk endpoints |
| Inventory audit logs retrievable | Manual | API validation for audit endpoints |
| PubChem lookup works | Manual | API validation for lookup endpoint |

## 5. Test Cases
| ID | Scenario | Expected Result |
| --- | --- | --- |
| TC-AUTH-001 | Register with valid data | 201 Created with JWT tokens |
| TC-AUTH-002 | Login with wrong password | 401 Unauthorized |
| TC-AUTH-003 | Access protected route unauthenticated | Redirect or 401/403 response |
| TC-AUTH-004 | Fetch current user profile | 200 OK with user data |
| TC-INV-001 | Search inventory | 200 OK with paged results |
| TC-INV-002 | View inventory item | 200 OK with item data |
| TC-INV-003 | Create inventory item | 200 OK and item persisted |
| TC-INV-004 | Update inventory item (PUT) | 200 OK and item updated |
| TC-INV-005 | Update inventory item (PATCH) | 200 OK and audit record written |
| TC-INV-006 | Create bulk job | 200 OK with job ID |
| TC-INV-007 | Validate bulk job | 200 OK with validation summary |
| TC-INV-008 | Execute bulk job | 200 OK with execution summary |
| TC-INV-009 | Fetch audit logs | 200 OK with audit entries |
| TC-PUB-001 | PubChem lookup | 200 OK with PubChem details or 404 |
| TC-MOB-001 | Save tokens after login | Tokens stored locally |
| TC-MOB-002 | Logout clears storage | Tokens removed |

## 6. Test Scripts / Test Steps
### Auth API (TC-AUTH-001/002/004)
1. POST /api/auth/register with valid payload.
2. Assert 201 response and JWT tokens.
3. POST /api/auth/login with invalid password.
4. Assert 401 response.
5. GET /api/user/me with valid JWT.
6. Assert 200 response and user data.

### Inventory API (TC-INV-001 to TC-INV-009)
1. GET /api/inventory with paging and optional filters.
2. GET /api/inventory/{itemId} for a known item.
3. POST /api/inventory with valid payload.
4. PUT /api/inventory/{itemId} with updated payload.
5. PATCH /api/inventory/{itemId} with quantity/status update.
6. POST /api/inventory/bulk-jobs with row inputs.
7. POST /api/inventory/bulk-jobs/{jobId}/validate.
8. POST /api/inventory/bulk-jobs/{jobId}/execute.
9. GET /api/inventory/{itemId}/audit-logs and /api/audit-logs.

### PubChem API (TC-PUB-001)
1. GET /api/pubchem/lookup?name={chemicalName}.
2. Assert 200 response with CID and formula or 404 for unknown chemical.

### Web Route Guards (TC-AUTH-003)
1. Open protected route without auth.
2. Assert redirect to login or blocked access.
3. Open public route without auth.
4. Assert page renders normally.

### Mobile Auth Storage (TC-MOB-001/002)
1. Persist tokens after login.
2. Assert tokens present in storage.
3. Logout and clear tokens.
4. Assert tokens removed.

## 7. Automated Test Cases
- Backend: AuthServiceTest, AuthControllerTest
- Web: ProtectedRoute.test, PublicRoute.test
- Mobile: TokenManagerTest

## 8. Execution Plan
1. Run backend tests.
2. Run web tests and build.
3. Run mobile unit tests.
4. Execute manual regression for inventory and PubChem.

## 9. Entry and Exit Criteria
Entry criteria:
- Refactor changes complete.
- Environment configuration available.

Exit criteria:
- All automated tests in scope pass.
- Manual regression steps completed and documented.
- No unresolved regression defects.

## 10. Risks
- External PubChem availability can affect lookup tests.
- Production-only settings can mask test misconfigurations.
- Mobile instrumentation coverage is limited without device infrastructure.