# Java Job Aggregator

A web application that aggregates Java developer job postings from the [Adzuna](https://www.adzuna.com/) jobs API and displays them in a clean browser interface. It features a built-in HTTP server, a REST-like search endpoint, and relevance-based result sorting.

## Features

- Search Java developer jobs by city or region (e.g. `Dallas`, `New York`, `Remote`)
- Results filtered to Java-specific titles using whole-word matching (`\bjava\b`)
- Relevance scoring — titles like `Java Developer` and `Java Engineer` appear first
- Up to 250 results fetched per search (5 pages × 50 results)
- Clean web UI with results rendered in a sortable table
- Automatic browser launch on server start

## How It Works

1. The user enters a location in the web UI and submits a search.
2. The browser sends a `GET /search?location=<city>` request to the local server.
3. `AdzunaStrategy` queries the Adzuna Jobs API with `what=java+developer&where=<city>`.
4. Results are filtered (only titles containing the word *java*) and sorted by relevance score.
5. The server returns JSON; the UI renders it as a table.

## Architecture

The project follows the **MVC** pattern with a **Strategy** pattern for data sources.

```
src/main/java/
├── main/
│   ├── WebServer.java      — HTTP server (default port 8080), routes: / and /search
│   ├── Aggregator.java     — alternative console entry point
│   └── Controller.java     — bridges View and Model
├── model/
│   ├── AdzunaStrategy.java — Adzuna API integration, filtering, scoring
│   ├── Model.java          — aggregates results from one or more Providers
│   ├── Provider.java       — wraps a Strategy, supports runtime strategy swap
│   └── Strategy.java       — interface for job-fetching strategies
├── view/
│   ├── HtmlView.java       — console/HTML view implementation
│   └── View.java           — View interface
└── vo/
    └── JobPosting.java     — job data object (title, company, city, url, website)
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
GET /search?location={city}
```

| Parameter  | Description                          | Example       |
|------------|--------------------------------------|---------------|
| `location` | City or region to search jobs in     | `Dallas`      |

Returns a JSON array of job objects:

```json
[
  {
    "title": "Java Developer",
    "company": "Acme Corp",
    "location": "Dallas",
    "url": "https://...",
    "website": "adzuna.com"
  }
]
```

## Dependencies

| Library        | Purpose                        |
|----------------|-------------------------------|
| `org.json`     | JSON parsing of API responses |
| `org.jsoup`    | HTML parsing (reserved)       |
