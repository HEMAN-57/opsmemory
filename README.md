# OpsMemory — AI-Powered Incident Response Agent with Hindsight Memory

**OpsMemory** is an AI-powered Incident Response Agent that uses [Hindsight](https://github.com/vectorize-io/hindsight) as persistent memory. When an engineer reports a production incident, OpsMemory recalls similar historical incidents and their outcomes, then uses an LLM to recommend an informed response based on accumulated organizational experience.

> **Phase 1** — This is the memory foundation. The full agent reasoning and frontend will be built in Phase 2+.

## Architecture (Phase 1)

```
┌─────────────────┐       REST API        ┌─────────────────┐
│   Spring Boot   │ ◄──────────────────► │    Hindsight    │
│   (port 8080)   │  retain / recall      │   (port 8888)   │
│                 │                       │   + Groq LLM    │
│  • DataSeeder   │                       │                 │
│  • DevEndpoints │                       │  Memory Bank:   │
│                 │                       │  "opsmemory"    │
└─────────────────┘                       └─────────────────┘
```

**Why Hindsight?**
- Hindsight provides state-of-the-art memory for AI agents
- It automatically extracts structured facts from unstructured text (via Groq LLM)
- It provides semantic recall — not simple keyword search
- We don't need a separate database; Hindsight IS our persistent store
- Memories consolidate and improve over time

## Prerequisites

- **Docker** and **Docker Compose**
- **Java 17+** and **Maven**
- A **Groq API key** (free at [console.groq.com/keys](https://console.groq.com/keys))

## Quick Start

### 1. Set up your API key

```bash
cd /path/to/HINDSIGHT
cp .env.example .env
# Edit .env and add your real GROQ_API_KEY
```

### 2. Start Hindsight

```bash
docker compose up -d
```

Wait ~30 seconds for Hindsight to initialize, then verify:

```bash
curl http://localhost:8888/health
```

You should get a `200 OK` response.

### 3. Start Spring Boot

```bash
cd backend
./mvnw spring-boot:run
```

On startup, OpsMemory will:
1. Wait for Hindsight to become healthy
2. Check if seed data already exists
3. If not, retain 5 detailed historical incidents into Hindsight
4. Log the results

### 4. Verify memories were retained

Check the Spring Boot logs for:
```
✓ Successfully seeded 5 incidents into bank 'opsmemory'
```

### 5. Test recall

```bash
curl http://localhost:8080/api/dev/memory-test | python3 -m json.tool
```

This sends a recall query asking about database connection pool exhaustion and payment-service failures. You should see recalled memories referencing the seeded incidents.

**Custom query:**
```bash
curl "http://localhost:8080/api/dev/memory-test?query=certificate+expiration+auth+service" | python3 -m json.tool
```

### 6. Health check

```bash
curl http://localhost:8080/api/dev/health
```

## Project Structure

```
HINDSIGHT/
├── docker-compose.yml          # Hindsight service
├── .env.example                # Environment template
├── .gitignore
├── README.md
├── incident-response-commander.md  # Agent persona spec
└── backend/
    ├── pom.xml
    └── src/
        ├── main/java/com/opsmemory/
        │   ├── OpsMemoryApplication.java
        │   ├── config/
        │   │   └── HindsightConfig.java
        │   ├── client/
        │   │   ├── HindsightClientService.java
        │   │   └── dto/
        │   │       ├── MemoryItem.java
        │   │       ├── RetainRequest.java
        │   │       ├── RetainResponse.java
        │   │       ├── RecallRequest.java
        │   │       └── RecallResponse.java
        │   ├── seeder/
        │   │   └── IncidentDataSeeder.java
        │   └── controller/
        │       └── DevVerificationController.java
        ├── main/resources/
        │   └── application.yml
        └── test/java/com/opsmemory/
            ├── OpsMemoryApplicationTests.java
            └── client/dto/
                └── DtoSerializationTest.java
```

## Configuration

| Property | Env Variable | Default | Description |
|---|---|---|---|
| `hindsight.api-url` | `HINDSIGHT_API_URL` | `http://localhost:8888` | Hindsight API base URL |
| `hindsight.bank-id` | `HINDSIGHT_BANK_ID` | `opsmemory` | Hindsight memory bank name |
| `groq.api-key` | `GROQ_API_KEY` | — | Groq API key (for future LLM reasoning) |

## API Endpoints (Phase 1 — Dev Only)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/dev/health` | Check Hindsight connectivity |
| `GET` | `/api/dev/memory-test` | Run a test recall query |
| `POST` | `/api/dev/recall` | Custom recall with full body control |

## Phase 2 (Next)

- Groq LLM integration in Spring Boot for incident analysis
- Full incident lifecycle: report → analyze → recall → recommend → resolve → retain
- React frontend dashboard
- Incident Response Commander persona integration
