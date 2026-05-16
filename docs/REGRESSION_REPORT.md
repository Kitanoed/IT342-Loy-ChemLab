# Regression Test Report

## Project Information
- Project: ChemLab multi-platform laboratory inventory and request system
- Date: 2026-05-09
- Scope: Backend, web, and mobile after vertical-slice refactor

## Refactoring Summary
- Moved inventory and PubChem controllers/services and DTOs into feature slices.
- Consolidated backend auth/user into feature slices and preserved centralized security.
- Kept shared infrastructure (entities, repositories, security, config) in root packages.

## Updated Project Structure
- Backend auth: backend/src/main/java/edu/cit/loy/chemlab/features/auth
- Backend user: backend/src/main/java/edu/cit/loy/chemlab/features/user
- Backend inventory: backend/src/main/java/edu/cit/loy/chemlab/features/inventory
- Backend PubChem: backend/src/main/java/edu/cit/loy/chemlab/features/pubchem
- Web auth: web/src/features/auth
- Web inventory: web/src/features/inventory
- Mobile auth: mobile/app/src/main/java/com/example/chemlab/features/auth

## Test Plan Documentation
- Software Test Plan: docs/TEST_PLAN.md

## Automated Test Evidence
- Backend: mvnw.cmd test (pass)
- Web: npm run build (pass)
- Mobile: gradlew.bat test (pass)

## Regression Test Results
| Area | Result | Notes |
| --- | --- | --- |
| Backend auth APIs | Pass | Register/login tests passed |
| Backend inventory APIs | Pass | Manual API checks for inventory and bulk jobs |
| Backend PubChem API | Pass | Manual lookup validation |
| Web route guards | Pass | Protected/public route tests passed |
| Web inventory routes | Pass | Manual navigation check |
| Mobile auth storage | Pass | Token manager unit test passed |

## Issues Found
- None during this regression cycle.

## Fixes Applied
- Completed feature-slice move for inventory and PubChem modules.

## Residual Risk
- Web tests focus on route guards rather than full UI flows.
- Mobile coverage is limited to local unit tests; instrumentation remains future work.