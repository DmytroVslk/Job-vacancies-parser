# Java Job Aggregator

A Java-based job search assistant designed to aggregate and rank job postings from the [Adzuna](https://www.adzuna.com/) jobs API. The initial version focuses on IT and tech-related roles such as software engineering, QA, data, DevOps, and internships, while its provider-based architecture can be extended to other industries later.

## Current Scope and Future Expansion

The current product mode returns IT and tech-related vacancies only. The search pipeline is not hardcoded to a single role or provider: providers supply postings, classifiers derive searchable attributes, and the service applies the active scope. A future version can support additional industries by extending the scope rules and user-facing filters rather than replacing the search architecture.

## Features

- Search IT and tech jobs by city or region (e.g. `Dallas`, `New York`, `Remote`)
- Optionally narrow results by job title or keyword, such as Java Developer, Data Analyst, QA Engineer, or DevOps
- Filter the backend search by category, seniority, work type, or a derived tag
- Relevance-first sorting - title, description, location, and optional preferences determine result order
- Duplicate removal based on title, company, and location when those values are available
- Provider source included for each posting, currently `Adzuna`
- Up to 250 results fetched per search (5 pages × 50 results)
- Clean web UI with results rendered in a sortable table
- Automatic browser launch on server start

## How It Works

1. The user enters a location and optionally a position in the web UI and submits a search.
2. The browser sends a `GET /search?location=<city>&position=<position>` request to the local server; API clients may also send optional filter criteria.
3. `AdzunaJobProvider` queries the Adzuna Jobs API with `where=<city>`; it includes `what=<position>` for a selected role, or Adzuna's `it-jobs` category for the broad `All IT / Tech Roles` search, and identifies returned postings with `source: "Adzuna"`.
4. `JobSearchService` identifies seniority and remote/hybrid/onsite work type, derives tags, keeps only postings matching the current IT/tech scope, filters position searches against title, description, and category, then sorts by relevance using role text, optional preferences, and an exact-city location bonus.
5. Duplicate detection removes repeated postings with the same title, company, and location after sorting, retaining the highest-ranked occurrence.
6. Jackson serializes response objects to JSON; the UI renders it as a table.

## Architecture

The project follows the **MVC** pattern with a simple `JobProvider` interface for data sources.

```
src/main/java/
├── main/
│   ├── WebServer.java      — HTTP server (default port 8080), routes: / and /search
│   └── AppConfig.java      — reads .env and environment configuration
├── service/
│   ├── JobSearchService.java — search business logic
│   ├── JobSearchCriteria.java — optional position/category/seniority/work-type/tag filters
│   ├── JobSortOption.java — supported result ordering modes, currently relevance
│   ├── JobRelevanceScorer.java — calculates relevance score for result ordering
│   ├── JobDuplicateDetector.java — removes repeated title/company/location results
│   ├── JobSeniorityClassifier.java — derives internship/junior/mid/senior/lead level
│   ├── JobWorkTypeClassifier.java — derives remote/hybrid/onsite work type
│   ├── JobTechScopeClassifier.java — identifies IT/tech-related postings
│   └── JobTagClassifier.java — collects derived searchable characteristics
├── model/
│   ├── AdzunaJobProvider.java — Adzuna API integration and response parsing
│   ├── JobProvider.java       — interface for job providers
│   └── ProviderException.java — provider error type for safe API errors
├── response/
│   ├── ErrorResponse.java     — standard error JSON returned by API endpoints
│   ├── JobSearchResponse.java — standard success JSON returned by /search
│   └── JobSearchResult.java   — job item shape inside /search responses
└── vo/
    └── JobPosting.java     — job data object (title, company, city, url, website, source, description, category, seniority, workType, employmentType, employmentSchedule, techRelated, tags)
```

## Getting Started

**Prerequisites:** Java 21, Maven

Create an Adzuna application, copy the example config, and fill in your real credentials:

```bash
cp .env.example .env
```

Then edit `.env`:

```text
ADZUNA_APP_ID=your_real_app_id
ADZUNA_APP_KEY=your_real_app_key
SERVER_PORT=8080
ADZUNA_COUNTRY=us
```

Real environment variables can still override values from `.env` when needed.

```bash
# Build
mvn compile

# Run the web server
mvn exec:java -Dexec.mainClass=main.WebServer
```

The server starts at `http://localhost:8080` by default and opens in your default browser automatically.

## Search API

```
GET /search?location={city}&position={job_title_or_keyword}&category={category}&seniority={level}&preferredSeniority={level}&workType={type}&preferredWorkType={type}&preferredEmploymentType={type}&preferredEmploymentSchedule={schedule}&tag={tag}&sort={mode}
```

| Parameter  | Description                          | Example       |
|------------|--------------------------------------|---------------|
| `location` | City or region to search jobs in     | `Dallas`      |
| `position` | Optional job title or keyword; omitted to search all jobs in the location | `data analyst` |
| `category` | Optional provider category text | `IT Jobs` |
| `seniority` | Optional derived level: `internship`, `junior`, `mid`, `senior`, or `lead` | `junior` |
| `preferredSeniority` | Optional preferred level used to rank matching jobs without filtering other levels out | `junior` |
| `workType` | Optional derived work type: `remote`, `hybrid`, or `onsite` | `remote` |
| `preferredWorkType` | Optional preferred work type used to rank matching jobs without filtering other types out | `remote` |
| `preferredEmploymentType` | Optional contract type preference from provider data: `permanent` or `contract` | `contract` |
| `preferredEmploymentSchedule` | Optional schedule preference from provider data: `full-time` or `part-time` | `full-time` |
| `tag` | Optional derived characteristic, currently values such as `tech`, seniority, or work type | `remote` |
| `sort` | Optional result order; currently supports `relevance` and defaults to it | `relevance` |

The current version returns only postings identified as IT or tech-related. Omitting `position` queries Adzuna's broad IT category in the selected location instead of silently forcing one particular role such as Java Developer. Searches for specific roles such as QA or Data Engineer still use their role keywords.

The current web form sends `location` and `position`; results are still sorted by relevance because that is the default mode. The additional API filters and explicit `sort=relevance` parameter are available in the backend now and can be exposed as frontend controls in a later UI stage.

Returns a standard JSON response object:

```json
{
  "success": true,
  "count": 1,
  "jobs": [
    {
      "title": "Java Developer",
      "company": "Acme Corp",
      "location": "Dallas",
      "url": "https://...",
      "website": "adzuna.com",
      "source": "Adzuna"
    }
  ],
  "warnings": []
}
```

Frontend code should read jobs from `response.jobs`.

Error responses use the same standard shape:

```json
{
  "success": false,
  "message": "Location is required."
}
```

## Dependencies

| Library        | Purpose                        |
|----------------|-------------------------------|
| `org.json`     | JSON parsing of Adzuna API responses |
| `jackson-databind` | JSON serialization of backend API responses |
