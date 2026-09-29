# PriceStream

## Overview

PriceStream is WIP a modular Scala backend service for concurrent price ingestion and historical price tracking
across multiple platforms. It exposes operational endpoints for the ingestion runs and metrics dashboard visualization.

The system combines:

-   REST API for tracking and querying data
-   Scheduled ingestion pipeline
-   Concurrent provider processing
-   Persistent ingestion run tracking
-   Historical price tracking (WIP)
-   [Decoupled dashboard API and API gateway (FastAPI) ](https://github.com/AranaDeDoros/PriceStreamDashboard)
  - [Dashboard frontend to visualize runs data (React)](https://github.com/AranaDeDoros/PriceStreamDashboardFrontend)

------------------------------------------------------------------------

## HTTP API

The HTTP server listens on `http://localhost:8080`. Routes are mounted in
two groups:

- `/tracking` for recording prices and retrieving product history
- `/api` for ingestion-run and platform data used by the dashboard

All successful responses are JSON, except `POST /tracking/track`, which
returns an empty response body.

---

### Tracking endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/tracking/track` | Creates the platform and product if needed, then records a price. |
| `GET` | `/tracking/history/{platform}/{externalId}` | Returns the recorded price history for one product. |
| `GET` | `/tracking/products` | Lists all tracked products. |
| `GET` | `/tracking/products?platform={platform}` | Lists products for a platform. The filter is case-insensitive. |

Record a price with a positive `price`. `name` and `url` are optional:

```bash
curl -i -X POST http://localhost:8080/tracking/track \
  -H "Content-Type: application/json" \
  -d '{
    "platform": "dummyjson",
    "externalId": "1",
  }'
```

This returns a TrackPriceResponse with `201 Created` as status. 

---

Retrieve a product's history:

```bash
curl http://localhost:8080/tracking/history/dummyjson/1
```

Example response:

```json
[
  {
    "price": 199.99,
    "recordedAt": "2026-09-27T12:00:00Z"
  }
]
```

An unknown platform or product returns `404 Not Found`. Product-list responses
have this shape (the `platform` value is the platform's internal ID):

```json
[
  {
    "platform": "1",
    "externalId": "1",
    "name": "Example product",
    "url": "https://example.com/products/1"
  }
]
```
---

### Ingestion and platform endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/runs` | Lists all ingestion runs. |
| `GET` | `/api/runs/{id}` | Returns an ingestion run by UUID. |
| `GET` | `/api/runs/platform/{platform}` | Lists ingestion runs for a platform. |
| `GET` | `/api/status/{status}` | Lists ingestion runs with a status. Valid values are `Running`, `Completed`, and `Failed`. |
| `GET` | `/api/platforms` | Lists configured platforms. |

For example:

```bash
curl http://localhost:8080/api/runs
curl http://localhost:8080/api/runs/11111111-1111-1111-1111-111111111111
curl http://localhost:8080/api/runs/platform/dummyjson
curl http://localhost:8080/api/status/Completed
curl http://localhost:8080/api/platforms
```

An ingestion-run response has the following shape. `finishedAt` and `error`
are `null` while applicable data is unavailable:

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "startedAt": "2026-09-27T12:00:00Z",
  "finishedAt": "2026-09-27T12:01:00Z",
  "status": "Completed",
  "error": null,
  "products_processed": 42
}
```

`/api/platforms` returns objects such as:

```json
{
  "id": 1,
  "name": "dummyjson",
  "baseUrl": "https://dummyjson.com"
}
```

The list endpoints return arrays of these objects. A missing run, or a
platform/status query that finds no runs or platforms, returns `404 Not Found`.

------------------------------------------------------------------------

## Architecture

### Core Stack

**Backend (Scala)**
- Cats Effect
- FS2
- Http4s
- Doobie (PostgreSQL)
- Circe

**Dashboard** 
- FastAPI 
- PostgreSQL
- React
- JWT-based authentication

------------------------------------------------------------------------

## Key Design Decisions

### 1. Explicit Resource Lifecycle Management

All infrastructure components are managed using `Resource`:

-   HTTP server
-   Database transactor
-   HTTP client
-   Background ingestion scheduler

This guarantees safe startup and graceful shutdown.

------------------------------------------------------------------------

### 2. Background Ingestion Scheduler

The ingestion pipeline is implemented as an FS2 stream:

-   Initial ingestion at startup
-   Recurring ingestion every 10 minutes (`Stream.awakeEvery`)
-   Executed in a dedicated fiber
-   Gracefully cancelled on application shutdown

------------------------------------------------------------------------

### 3. Concurrent Provider Processing

Each ingestion cycle processes providers in parallel:

``` scala
providers.parTraverse_(ingestFromProvider)
```

This leverages structured concurrency via **Cats Effect fibers**.

------------------------------------------------------------------------

### 4. Failure Handling and Observability

Each ingestion run is persisted with state tracking:

-   Running
-   Completed
-   Failed

Failures are captured using `.attempt` to prevent scheduler crashes and
ensure the system continues operating.

------------------------------------------------------------------------

### 5. Service Isolation

The backend ingestion service is logically isolated from the dashboard.\
The dashboard consumes only exposed REST endpoints secured with JWT
authentication.

This allows independent evolution of:

-   Ingestion logic
-   API contracts
-   Visualization layer

------------------------------------------------------------------------

## System Flow

1.  Application starts
2.  Resources are initialized (DB, client, server, scheduler)
3.  Initial ingestion runs
4.  Scheduler triggers ingestion every 10 minutes
5.  Dashboard consumes exposed endpoints for visualization

------------------------------------------------------------------------

## Future Improvements(?)

-   Bounded concurrency (`parTraverseN`)
-   Rate limiting / throttling
-   Retry with exponential backoff
-   Metrics and observability (Prometheus)
-   Message broker integration (Kafka / RabbitMQ)
-   Circuit breaker for external providers

------------------------------------------------------------------------

## Why make this?

To
-   Explore structured concurrency in Scala
-   Apply effect-based architecture in real backend systems
-   Model clean separation between domain and infrastructure
-   Implement safe background processing with lifecycle control

------------------------------------------------------------------------
