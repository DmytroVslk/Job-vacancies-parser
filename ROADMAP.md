# Java Job Aggregator — Global Development Roadmap

Important Project Scope Clarification
------------------------------------
The word "Java" in this project name refers to the technology used to build the application, not to a restriction to Java developer roles.

This project is a Java-based job search assistant designed to aggregate and rank job postings. The initial version focuses on IT and tech-related roles, while the provider-based architecture can be extended to other industries later.

Examples of supported searches may include:

    Software Engineer
    Java Developer
    Frontend Developer
    Data Analyst
    Data Engineer
    QA Engineer
    DevOps Engineer
    Cloud Engineer
    IT Project Manager
    Remote jobs
    Entry-level jobs
    Internship jobs

## Project Goal

Turn the current simple job search web app into a professional, portfolio-ready Java web application.

The project starts as a Java 21 web application focused on searching IT and tech-related job postings through external job APIs. The user opens a local web page, enters a location and optionally a position, and the frontend sends a request to:

```text
/search?location=...&position=...
```

The backend returns JSON with matching job postings.

## Long-Term Vision

Grow the project from a simple job search aggregator into a job search assistant built with Java that can:

- Search jobs
- Filter jobs
- Rank jobs by relevance
- Remove duplicates
- Save jobs
- Track applications
- Help manage the job search process

## Stage 1: Clean Backend Foundation

Goal: make the backend clean, secure, understandable, and easier to maintain.

1.1. Move API credentials out of the code.  
1.2. Add a simple config layer such as `AppConfig`.  
1.3. Keep environment variable reading outside provider logic.  
1.4. Separate HTTP handling, business logic, provider logic, parsing, and response formatting.  
1.5. Keep `WebServer` focused on requests and responses.  
1.6. Introduce `JobSearchService` for search business logic.  
1.7. Move toward clearer provider naming, such as `JobProvider` and `AdzunaJobProvider`.  
1.8. Replace manual JSON building with `org.json` or a dedicated response mapper.  
1.9. Return standard API responses:

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

1.10. Improve error handling for API errors, network errors, invalid input, and empty results.  
1.11. Log technical details on the server side without exposing stack traces to the frontend.  
1.12. Reconsider old MVC/View classes and remove or refactor anything that no longer fits the web app.

## Stage 2: Better Search Engine

Goal: make search results more useful, relevant, and closer to a real aggregator.

2.1. Improve job filtering using title, description, category, seniority, location, work type, and available tags.  
2.2. Improve relevance scoring for user-selected roles and keywords, such as `Software Engineer`, `Data Analyst`, `QA Engineer`, `DevOps Engineer`, and `Project Manager`.  
2.3. Consider position keywords, seniority, location, remote/hybrid/onsite status, employment type, and description in scoring.  
2.4. Add sorting by relevance first, then later by date, salary, or source.  
2.5. Add duplicate detection using title, company, and location.  
2.6. Add a provider/source field to `JobPosting`, for example `source: "Adzuna"`.  
2.7. Prepare for multiple providers through a common `JobProvider` interface.  
2.8. Add a second provider when the architecture is ready.  
2.9. Handle partial provider failure by returning available jobs with warnings. Done.

## Stage 3: Better Frontend

Goal: make the frontend feel like a small finished web service.

3.1. Validate the search form.  
3.2. Keep position optional.  
3.3. Disable the search button while loading.  
3.4. Add loading, error, and empty results states.  
3.5. Improve job cards with title, company, location, salary if available, source, and job link.  
3.6. Add provider/source badges.  
3.7. Add filters for remote/hybrid/onsite, seniority, internship, salary, posted date, and source.  
3.8. Add sorting options such as relevance, newest, salary, and company.  
3.9. Add pagination or `Load More`.  
3.10. Make the UI responsive on desktop and mobile.

## Stage 4: Persistence Features

Goal: turn the app from a simple search tool into a useful job search assistant.

4.1. Add a database, starting with H2 or SQLite and later possibly PostgreSQL.  
4.2. Add saved jobs.  
4.3. Add search history.  
4.4. Add an application tracker with statuses such as Saved, Applied, Interview, Offer, Rejected, and Archived.  
4.5. Add notes for saved jobs or applications.  
4.6. Add application and interview dates.  
4.7. Add user preferences such as preferred location, remote-only mode, seniority focus, and preferred keywords.  
4.8. Optionally add user accounts later with registration, login, logout, password hashing, and user-specific data.

## Stage 5: Professional Finish

Goal: make the project ready for a portfolio, GitHub, resume, or interview.

5.1. Add automated tests for URL building, API response parsing, job filtering across different roles, relevance scoring, duplicate detection, JSON response mapping, and service logic.  
5.2. Improve README documentation with setup, environment variables, API examples, architecture, provider extension guide, and roadmap.  
5.3. Add logging for search requests, provider errors, returned job counts, unexpected exceptions, and API failures.  
5.4. Add security polish: no secrets in GitHub, input validation, encoded query parameters, no exposed stack traces, and request limits.  
5.5. Prepare for deployment on a Java-friendly platform such as Render, Railway, or Fly.io.  
5.6. Add production config, environment variables, build command, start command, and optional Dockerfile.  
5.7. Add a live demo if possible.  
5.8. Add screenshots of search, results, saved jobs, or tracker views.  
5.9. Add a simple architecture diagram:

```text
Frontend -> WebServer/Controller -> JobSearchService -> JobProviders -> External APIs
```

5.10. Clean the GitHub repository with no secrets, clear structure, useful README, and optional issues/project board.

## Recommended Implementation Order

1. Clean Backend Foundation: config, API keys, service/provider structure, JSON response, error handling.
2. Better Search Engine: relevance scoring, duplicate detection, better filtering, provider source field, second provider.
3. Better Frontend: loading/error/empty states, job cards, filters, sorting, pagination or `Load More`.
4. Persistence Features: database, saved jobs, search history, application tracker.
5. Professional Finish: tests, documentation, logging, deployment, security polish, live demo.

## Portfolio Description

Built a Java 21 web application that aggregates and ranks IT and tech-related job postings from external job APIs. Implemented a local HTTP backend using Java's built-in `HttpServer`, provider-based architecture, JSON parsing, relevance-based sorting, duplicate detection, job filtering, and a simple HTML/CSS/JavaScript frontend. The provider-based design can later extend the search assistant to additional industries as it grows with saved jobs, application tracking, database persistence, tests, documentation, and deployment support.

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
