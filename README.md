# OpsMemory — AI Incident Response Agent with Persistent Hindsight Memory

**OpsMemory** is an AI-powered incident response agent that turns production incidents into reusable organizational knowledge.

When an engineer reports a production incident, OpsMemory uses **Hindsight persistent memory** to recall similar historical incidents, previous root causes, resolutions, and outcomes. It then combines that organizational memory with the current incident and uses an LLM to generate an informed incident analysis and recommended response.

After the incident is resolved, the verified resolution is retained in Hindsight so that future incidents can benefit from what the organization has already learned.

> **Core idea:** Every production incident should make the next incident easier to solve.

---

## 🚨 The Problem

Production incidents are rarely completely new.

Engineering teams repeatedly encounter problems such as:

* Database connection-pool exhaustion
* Payment-service failures
* API timeouts
* Deployment regressions
* Authentication failures
* Certificate expiration
* Infrastructure saturation

The problem is that the knowledge from previous incidents is often scattered across postmortems, tickets, Slack conversations, runbooks, and individual memory.

When a similar incident happens again, engineers may spend valuable time rediscovering what the organization has already learned.

**OpsMemory turns those previous incidents into persistent, searchable organizational memory.**

---

## 💡 The Solution

OpsMemory creates a continuous learning loop:

```text
                 NEW INCIDENT
                      │
                      ▼
              ┌───────────────┐
              │   OpsMemory   │
              │    Agent      │
              └───────┬───────┘
                      │
                      ▼
              HINDSIGHT RECALL
                      │
                      ▼
        Similar historical incidents
        Previous root causes
        Previous resolutions
        Previous outcomes
                      │
                      ▼
                GROQ LLM
                      │
                      ▼
             Incident Analysis
             Likely Root Cause
             Recommended Actions
                      │
                      ▼
              HUMAN VERIFICATION
                      │
                      ▼
               INCIDENT RESOLVED
                      │
                      ▼
             HINDSIGHT RETENTION
                      │
                      ▼
          NEW ORGANIZATIONAL MEMORY
                      │
                      └──────► Future incidents
```

The important part is the feedback loop:

**Recall → Reason → Resolve → Retain → Recall again**

---

# 🧠 Why Hindsight?

Hindsight is the central memory layer of OpsMemory.

Instead of treating every incident as an isolated prompt, OpsMemory can retrieve knowledge accumulated from previous incidents.

Hindsight provides the persistent-memory capabilities required for this workflow:

* **Persistent memory** across interactions
* **Semantic recall** of relevant historical knowledge
* **Retention** of newly verified information
* Memory that can be reused by future agent interactions

OpsMemory therefore does not simply ask an LLM:

> "What should I do about this incident?"

It asks:

> "What happened when we experienced something similar before, what worked, and what should we do now?"

That distinction is the foundation of the product.

---

# 🤖 How the Agent Works

### 1. Engineer reports an incident

Example:

```text
Payment service is timing out and checkout requests are failing intermittently.
Customers are experiencing slow or failed payments during checkout.
```

### 2. OpsMemory recalls historical knowledge

Hindsight searches the persistent memory bank for relevant incidents.

For a payment-service timeout, it may recall previous incidents involving:

* Database connection-pool exhaustion
* Long-running transactions
* Payment-service failures
* Database saturation
* Previous mitigation strategies

### 3. Groq analyzes the incident

The LLM receives the current incident together with relevant historical memory and produces:

* Severity
* Likely root cause
* Immediate actions
* Recommended investigation steps
* Prevention measures

### 4. Engineer verifies the result

The AI produces a **likely** diagnosis, not an unquestionable fact.

The engineer confirms what actually happened and records the verified:

* Root cause
* Resolution
* Outcome

### 5. Resolution becomes organizational memory

The verified resolution is retained in Hindsight.

The next similar incident can therefore benefit from the newly captured knowledge.

---

# 🔁 Human-in-the-Loop Design

OpsMemory deliberately keeps the final resolution capture human-approved.

The AI can generate a likely root cause and recommended remediation automatically, but an AI hypothesis should not automatically become organizational truth.

The workflow is therefore:

```text
AI hypothesis
      ↓
Engineer verification
      ↓
Verified resolution
      ↓
Persistent organizational memory
```

This prevents incorrect AI-generated assumptions from being permanently stored and subsequently influencing future incident responses.

The architecture can later be extended toward greater automation by connecting the agent to logs, metrics, traces, deployment events, and incident-management systems.

---

# 🏗️ Architecture

```text
┌──────────────────────────────┐
│        React + Vite          │
│       Incident Console       │
│                              │
│  • Incident reporting        │
│  • AI analysis               │
│  • Hindsight recall display  │
│  • Resolution + retention    │
└──────────────┬───────────────┘
               │ HTTPS
               ▼
┌──────────────────────────────┐
│       Spring Boot API        │
│                              │
│   Incident Agent Service     │
│                              │
│  ┌────────────┐ ┌─────────┐ │
│  │ Hindsight  │ │  Groq   │ │
│  │ Client     │ │ Client  │ │
│  └─────┬──────┘ └────┬────┘ │
└────────┼──────────────┼──────┘
         │              │
         ▼              ▼
┌────────────────┐ ┌──────────────┐
│    Hindsight   │ │  Groq LLM    │
│ Persistent     │ │  Reasoning   │
│ Memory Bank    │ │              │
└────────────────┘ └──────────────┘
```

---

# 🛠️ Tech Stack

### Frontend

* React
* Vite
* JavaScript
* Lucide React

### Backend

* Java 17
* Spring Boot
* Spring WebFlux
* Maven

### AI

* Groq
* `openai/gpt-oss-120b`

### Memory

* Hindsight

### Deployment

* Render Static Site
* Render Web Service
* GitHub

---

# 📁 Project Structure

```text
HINDSIGHT/
├── README.md
├── docker-compose.yml
├── .env.example
├── .gitignore
├── incident-response-commander.md
│
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   │
│   └── src/main/
│       ├── java/com/opsmemory/
│       │   ├── OpsMemoryApplication.java
│       │
│       │   ├── api/dto/
│       │   │   ├── AnalysisDetails.java
│       │   │   ├── IncidentAnalyzeRequest.java
│       │   │   ├── IncidentAnalyzeResponse.java
│       │   │   ├── IncidentResolveRequest.java
│       │   │   └── IncidentResolveResponse.java
│       │
│       │   ├── client/
│       │   │   ├── GroqClientService.java
│       │   │   ├── HindsightClientService.java
│       │   │   └── dto/
│       │   │
│       │   ├── config/
│       │   │   ├── GroqConfig.java
│       │   │   └── HindsightConfig.java
│       │
│       │   ├── controller/
│       │   │   ├── DevVerificationController.java
│       │   │   ├── IncidentController.java
│       │   │   └── RootController.java
│       │
│       │   ├── seeder/
│       │   │   └── IncidentDataSeeder.java
│       │   │
│       │   └── service/
│       │       └── IncidentAgentService.java
│       │
│       └── resources/
│           └── application.yml
│
└── frontend/
    ├── package.json
    ├── index.html
    ├── vite.config.js
    │
    └── src/
        ├── App.jsx
        ├── App.css
        ├── index.css
        └── main.jsx
```

---

# 🔌 API

## Analyze Incident

```http
POST /api/incidents/analyze
```

Analyzes the current incident using historical Hindsight memory and LLM reasoning.

Example request:

```json
{
  "incident": "Payment service is timing out and checkout requests are failing",
  "severity": "HIGH",
  "symptoms": "Checkout requests are timing out intermittently"
}
```

---

## Resolve and Retain

```http
POST /api/incidents/resolve
```

Records the verified incident resolution and retains it in Hindsight.

Example request:

```json
{
  "incident": "Payment service is timing out and checkout requests are failing",
  "rootCause": "Database connection-pool exhaustion caused by long-running checkout transactions",
  "resolution": "Terminated stuck transactions, restarted payment-service, increased connection pool limits, and added saturation alerts",
  "outcome": "Payment service recovered and checkout requests returned to normal"
}
```

---

## Incident History

```http
GET /api/incidents/history
```

Returns retained incident memories from the Hindsight memory bank.

---

## Development Verification

```http
GET /api/dev/health
```

Checks Hindsight connectivity.

```http
GET /api/dev/memory-test
```

Runs a test recall query.

```http
POST /api/dev/recall
```

Performs a custom Hindsight recall.

---

## Root Status

```http
GET /
```

Returns:

```json
{
  "service": "OpsMemory",
  "status": "UP",
  "message": "AI Incident Command Center API"
}
```

---

# ⚙️ Configuration

| Property            | Environment Variable | Description                    |
| ------------------- | -------------------- | ------------------------------ |
| `hindsight.api-url` | `HINDSIGHT_API_URL`  | Hindsight API base URL         |
| `hindsight.bank-id` | `HINDSIGHT_BANK_ID`  | Hindsight memory bank          |
| `hindsight.api-key` | `HINDSIGHT_API_KEY`  | Hindsight Cloud authentication |
| `groq.api-key`      | `GROQ_API_KEY`       | Groq API authentication        |
| `groq.model`        | `GROQ_MODEL`         | LLM model used for reasoning   |

### Security

API keys are server-side configuration and must never be placed in the React frontend.

The frontend only requires the public backend URL:

```text
VITE_API_BASE_URL=https://opsmemory.onrender.com
```

Vite exposes `VITE_*` variables to client-side code during the build, so secrets must not use that prefix.

---

# 🚀 Local Development

## Prerequisites

* Java 17+
* Docker
* Docker Compose
* Node.js / npm
* Groq API key
* Hindsight instance or Hindsight Cloud credentials

---

## Start the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

## Start the Frontend

```bash
cd frontend
npm ci
npm run dev
```

The Vite development server will provide the local frontend URL.

For local development, configure:

```text
VITE_API_BASE_URL=http://localhost:8080
```

For the deployed frontend, configure:

```text
VITE_API_BASE_URL=https://opsmemory.onrender.com
```

Vite loads mode-specific environment files during development and production builds.

---

# ☁️ Production Deployment

OpsMemory is deployed as two services on Render.

### Frontend

```text
Render Static Site
Root Directory: frontend
Build Command: npm ci && npm run build
Publish Directory: dist
```

### Backend

```text
Render Web Service
Root Directory: backend
Runtime: Docker
Branch: production
```

The frontend communicates with the production Spring Boot API using:

```text
https://opsmemory.onrender.com
```

---

# 🌐 Live Demo

### Application

https://opsmemory-frontend.onrender.com

### Backend API

https://opsmemory.onrender.com

### Source Code

https://github.com/HEMAN-57/opsmemory

---

# 🎯 Hackathon Demo Flow

The complete product can be demonstrated in under a minute:

```text
1. Report a production incident
              ↓
2. OpsMemory recalls similar incidents
              ↓
3. AI analyzes current + historical context
              ↓
4. Agent recommends response actions
              ↓
5. Engineer verifies the actual resolution
              ↓
6. Resolution is retained in Hindsight
              ↓
7. Future incidents can recall the new knowledge
```

### Example

**Incident:**

> Payment service is timing out and checkout requests are failing intermittently.

**Historical memory:**

Previous payment incidents involving connection-pool exhaustion and long-running transactions.

**Agent reasoning:**

Likely database connection-pool exhaustion caused by long-running checkout transactions.

**Resolution:**

Terminate stuck transactions, restart the payment service, increase pool limits, and add saturation alerts.

**Learning:**

The verified resolution becomes persistent organizational memory.

---

# 🔮 Future Extensions

OpsMemory's current MVP establishes the persistent-memory foundation. Future versions can extend the agent with:

* Automatic log and metric ingestion
* Distributed tracing integration
* Deployment-event correlation
* Automated incident detection
* Integration with PagerDuty / incident-management systems
* Slack or Microsoft Teams incident channels
* Runbook retrieval
* Automated low-risk remediation
* Human approval workflows for high-risk actions
* Automatic postmortem generation
* Incident pattern detection across services
* Memory confidence and verification mechanisms

The long-term goal is an incident-response system that becomes increasingly useful because **the organization itself becomes part of the agent's memory**.

---

# 🏆 What Makes OpsMemory Different?

Traditional AI incident assistants can generate recommendations from the current prompt.

OpsMemory adds another dimension:

```text
Current Incident
      +
Organizational Memory
      ↓
Context-Aware Response
      ↓
Verified Resolution
      ↓
New Organizational Memory
```

The agent doesn't just answer incidents.

**It learns from them.**
