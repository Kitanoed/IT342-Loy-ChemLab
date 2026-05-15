# IT342-Loy-ChemLab

ChemLab is a multi-platform laboratory inventory and request system with a Spring Boot backend, React web frontend, and Android mobile app. The codebase is organized using Vertical Slice Architecture for feature-level modules.

## Project Structure
- backend: Spring Boot API and domain logic
- web: React + Vite frontend
- mobile: Android (Kotlin) client
- docs: Test plan and regression report

## Completed Application Architecture (with HTTPS REST Paths)
```mermaid
flowchart LR
  Student([Student])
  Staff([Lab Staff / Instructor])
  Admin([Admin])

  Web["Web App (React + Vite)"]
  Mobile["Mobile App (Android Kotlin)"]
  API["Spring Boot API (HTTPS)"]
  DB[("Database")]
  PubChem[("PubChem API")]

  subgraph AuthEndpoints["Auth HTTPS REST"]
    A1["POST /api/auth/register"]
    A2["POST /api/auth/login"]
    A3["POST /api/auth/logout"]
    A4["GET /api/user/me"]
  end

  subgraph InventoryEndpoints["Inventory HTTPS REST"]
    I1["GET /api/inventory"]
    I2["GET /api/inventory/{itemId}"]
    I3["POST /api/inventory"]
    I4["PUT /api/inventory/{itemId}"]
    I5["PATCH /api/inventory/{itemId}"]
    I6["POST /api/inventory/bulk-jobs"]
    I7["POST /api/inventory/bulk-jobs/{jobId}/validate"]
    I8["POST /api/inventory/bulk-jobs/{jobId}/execute"]
    I9["GET /api/inventory/bulk-jobs/{jobId}"]
    I10["GET /api/inventory/bulk-jobs/{jobId}/errors"]
    I11["GET /api/inventory/{itemId}/audit-logs"]
    I12["GET /api/audit-logs"]
  end

  subgraph PubChemEndpoints["PubChem HTTPS REST"]
    P1["GET /api/pubchem/lookup?name={chemicalName}"]
    P2["GET /functions/v1/pubchem-lookup?name={chemicalName}"]
  end

  Student --> Web
  Staff --> Web
  Admin --> Web
  Student --> Mobile
  Staff --> Mobile
  Admin --> Mobile

  Web --> API
  Mobile --> API

  API --> AuthEndpoints
  API --> InventoryEndpoints
  API --> PubChemEndpoints

  API --> DB
  PubChemEndpoints --> PubChem
```

## Backend (Spring Boot)
### Environment
Create a backend/.env file with:
- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET

### Run
```powershell
cd backend
./mvnw.cmd spring-boot:run
```

### Test
```powershell
cd backend
./mvnw.cmd test
```

## Web (React + Vite)
### Install
```powershell
cd web
npm install
```

### Run
```powershell
cd web
npm run dev
```

### Test/Build
```powershell
cd web
npm run test
npm run build
```

## Mobile (Android)
### Test
```powershell
cd mobile
./gradlew.bat test
```

## Documentation
- Test Plan: docs/TEST_PLAN.md
- Regression Report: docs/REGRESSION_REPORT.md
