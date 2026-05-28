# AGENTS.md

## Project Overview

This project is a Java web application that aggregates and ranks job postings from external APIs and presents them in a browser-based UI. The current product scope focuses on IT and tech-related roles, while the provider-based design is intended to support additional sources and later additional industries. It uses a built-in HTTP server and follows the MVC pattern with provider adapters behind a common interface.

### Key Features
- Search IT and tech job postings by location, optionally narrowed by role keywords
- Backend filtering by category, seniority, work type, and tags
- Relevance sorting and duplicate removal across provider results
- Provider `source` included for returned postings, currently `Adzuna` or `Jooble`
- Partial provider failure handling returns successful provider results with warnings
- REST-like search endpoint (`/search?location=...&position=...`)
- Clean, sortable web UI
- Automatic browser launch on server start

### Architecture
- **MVC**: Separation of concerns between Model, View, Controller
- **Providers**: Add job API adapters by implementing the `JobProvider` interface
- **Entry Point**: `WebServer.java`

### Build & Run
- Requires **Java 21** and **Maven**
- Build: `mvn compile`
- Run: `mvn exec:java -Dexec.mainClass=main.WebServer`

---

## Shortcomings & Issues

### 1. Limited External Providers Configured
- Adzuna and Jooble are currently connected as real external providers.
- Additional providers can be added through the common `JobProvider` contract when needed.

### 2. Provider Failure Handling Is Partial
- If one provider fails while another succeeds, the API returns available jobs with warnings.
- If every configured provider fails, the API returns a safe error response.

### 3. No Automated Tests
- No unit or integration tests are present.
- No test instructions or coverage.

### 4. Outdated Frontend Assets
- Some assets are duplicated in `bin/` and `src/`.
- No build process for frontend (JS/CSS), possible drift between versions.

### 5. Documentation Gaps
- No CONTRIBUTING document or dedicated architecture document.
- README covers setup and provider extension, but documentation can grow as new providers and frontend features are added.

### 6. Java 21 Requirement
- Requires a recent JDK (21), which may not be available by default on all systems.

---

## Recommendations for Agents
- Always build with Maven and run with the specified main class.
- Check for Adzuna and optional Jooble API credentials in environment variables or local `.env` before running live searches.
- Prefer editing files in `src/`, not `bin/` or `target/`.
- Link to [README.md](README.md) for usage and architecture details.
- If adding a provider, implement `JobProvider`, map API responses to `JobPosting`, provide its `source` name, and register it in the application setup.
- If updating the UI, keep `src/main/resources/view/` as the source of truth.

---

For more details, see [README.md](README.md).
