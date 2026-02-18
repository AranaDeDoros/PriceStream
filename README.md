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
-   [Decoupled dashboard API that also works as API gateway (FastAPI) ](https://github.com/AranaDeDoros/PriceStreamDashboard)
  - Dashboard frontend to visualize runs data (React).

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
