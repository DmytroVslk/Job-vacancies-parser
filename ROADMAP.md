# Java Job Aggregator Roadmap

## Project Goal

Turn the current simple Java job search web app into a professional, portfolio-ready Java web application.

The project starts as a Java 21 web application that searches Java developer jobs through external job APIs. The user opens a local web page, enters a location and optionally a position, and the frontend sends a request to:

```text
/search?location=...&position=...
```

The backend returns JSON with matching job postings.

## Long-Term Vision

Grow the project from a simple job search aggregator into a Java Job Search Assistant that can:

- Search jobs
- Filter jobs
- Rank jobs by relevance
- Remove duplicates
- Save jobs
- Track applications
- Help manage the job search process

## Stage 1: Clean Backend Foundation

Goal: make the backend clean, secure, understandable, and easier to maintain.

- Move API credentials out of the code.
- Add a simple config layer such as `AppConfig`.
- Keep environment variable reading outside provider logic.
- Separate HTTP handling, business logic, provider logic, parsing, and response formatting.
- Keep `WebServer` focused on requests and responses.
- Introduce `JobSearchService` for search business logic.
- Move toward clearer provider naming, such as `JobProvider` and `AdzunaJobProvider`.
- Replace manual JSON building with `org.json` or a dedicated response mapper.
- Return standard API responses:

```json
{
  "success": true,
  "count": 24,
  "jobs": [],
  "warnings": []
}
```

```json
{
  "success": false,
  "message": "Unable to fetch jobs right now."
}
```

- Improve error handling for API errors, network errors, invalid input, and empty results.
- Log technical details on the server side without exposing stack traces to the frontend.
- Reconsider old MVC/View classes and remove or refactor anything that no longer fits the web app.

## Stage 2: Better Search Engine

Goal: make search results more useful, relevant, and closer to a real aggregator.

- Improve Java job filtering using title, description, category, and available tags.
- Improve relevance scoring for titles such as `Java Developer`, `Java Engineer`, `Java Backend Developer`, `Junior Java Developer`, and `Entry-Level Java Developer`.
- Consider position keyword, seniority, remote status, and description in scoring.
- Add sorting by relevance first, then later by date, salary, or source.
- Add duplicate detection using title, company, and location.
- Add a provider/source field to `JobPosting`, for example `source: "Adzuna"`.
- Prepare for multiple providers through a common `JobProvider` interface.
- Add a second provider when the architecture is ready.
- Handle partial provider failure by returning available jobs with warnings.

## Stage 3: Better Frontend

Goal: make the frontend feel like a small finished web service.

- Validate the search form.
- Keep position optional.
- Disable the search button while loading.
- Add loading, error, and empty results states.
- Improve job cards with title, company, location, salary if available, source, and job link.
- Add provider/source badges.
- Add filters for remote/hybrid/onsite, seniority, internship, salary, posted date, and source.
- Add sorting options such as relevance, newest, salary, and company.
- Add pagination or `Load More`.
- Make the UI responsive on desktop and mobile.

## Stage 4: Persistence Features

Goal: turn the app from a simple search tool into a useful job search assistant.

- Add a database, starting with H2 or SQLite and later possibly PostgreSQL.
- Add saved jobs.
- Add search history.
- Add an application tracker with statuses such as Saved, Applied, Interview, Offer, Rejected, and Archived.
- Add notes for saved jobs or applications.
- Add application and interview dates.
- Add user preferences such as preferred location, remote-only mode, seniority focus, and preferred keywords.
- Optionally add user accounts later with registration, login, logout, password hashing, and user-specific data.

## Stage 5: Professional Finish

Goal: make the project ready for a portfolio, GitHub, resume, or interview.

- Add automated tests for URL building, API response parsing, Java job filtering, relevance scoring, duplicate detection, JSON response mapping, and service logic.
- Improve README documentation with setup, environment variables, API examples, architecture, provider extension guide, and roadmap.
- Add logging for search requests, provider errors, returned job counts, unexpected exceptions, and API failures.
- Add security polish: no secrets in GitHub, input validation, encoded query parameters, no exposed stack traces, and request limits.
- Prepare for deployment on a Java-friendly platform such as Render, Railway, or Fly.io.
- Add production config, environment variables, build command, start command, and optional Dockerfile.
- Add a live demo if possible.
- Add screenshots of search, results, saved jobs, or tracker views.
- Add a simple architecture diagram:

```text
Frontend -> WebServer/Controller -> JobSearchService -> JobProviders -> External APIs
```

- Clean the GitHub repository with no secrets, clear structure, useful README, and optional issues/project board.

## Recommended Implementation Order

1. Clean Backend Foundation: config, API keys, service/provider structure, JSON response, error handling.
2. Better Search Engine: relevance scoring, duplicate detection, better filtering, provider source field, second provider.
3. Better Frontend: loading/error/empty states, job cards, filters, sorting, pagination or `Load More`.
4. Persistence Features: database, saved jobs, search history, application tracker.
5. Professional Finish: tests, documentation, logging, deployment, security polish, live demo.

## Portfolio Description

Built a Java 21 web application that aggregates Java developer job postings from external job APIs. Implemented a local HTTP backend using Java's built-in `HttpServer`, provider-based architecture, JSON parsing, relevance-based sorting, duplicate detection, job filtering, and a simple HTML/CSS/JavaScript frontend. The project is designed to grow into a job search assistant with saved jobs, application tracking, database persistence, tests, documentation, and deployment support.

## Core Product Flow

```text
Search jobs
  -> Filter jobs
  -> Rank jobs
  -> Remove duplicates
  -> Save jobs
  -> Track applications
  -> Manage job search
```
