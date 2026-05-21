# AGENTS.md

## Project Overview

This project is a Java web application that aggregates Java developer job postings from the Adzuna API and presents them in a browser-based UI. It uses a built-in HTTP server and follows the MVC pattern with a Strategy pattern for data providers.

### Key Features
- Search for Java developer jobs by location
- Results filtered and sorted by relevance
- REST-like search endpoint (`/search?location=...`)
- Clean, sortable web UI
- Automatic browser launch on server start

### Architecture
- **MVC**: Separation of concerns between Model, View, Controller
- **Strategy**: Easily add new job provider APIs by implementing the `Strategy` interface
- **Entry Points**: `WebServer.java` (main), `Aggregator.java` (console)

### Build & Run
- Requires **Java 21** and **Maven**
- Build: `mvn compile`
- Run: `mvn exec:java -Dexec.mainClass=main.WebServer`

---

## Shortcomings & Issues

### 1. Hardcoded API Provider
- Only Adzuna is implemented; no abstraction for multiple providers in use.
- Adding new providers requires manual code changes and configuration.

### 2. No API Key Management
- Adzuna API credentials are not documented or managed securely.
- No environment variable or config file support for secrets.

### 3. Minimal Error Handling
- Limited error handling for network/API failures, invalid input, or empty results.
- No user feedback for backend errors.

### 4. No Automated Tests
- No unit or integration tests are present.
- No test instructions or coverage.

### 5. Outdated Frontend Assets
- Some assets are duplicated in `bin/` and `src/`.
- No build process for frontend (JS/CSS), possible drift between versions.

### 6. Documentation Gaps
- No CONTRIBUTING, architecture, or API authentication docs.
- No instructions for adding new providers or extending the UI.

### 7. Java 21 Requirement
- Requires a recent JDK (21), which may not be available by default on all systems.

---

## Recommendations for Agents
- Always build with Maven and run with the specified main class.
- Check for API credentials before running.
- Prefer editing files in `src/`, not `bin/` or `target/`.
- Link to [README.md](README.md) for usage and architecture details.
- If adding a provider, implement the `Strategy` interface and update `Model.java`.
- If updating the UI, keep `src/main/resources/view/` as the source of truth.

---

For more details, see [README.md](README.md).
