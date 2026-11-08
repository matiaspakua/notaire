# AI assistant in the main window (Spring AI 2.0 + local oMLX model)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1964 |
| Use Case | CU88 – Asistente IA de la gestión |
| Branch | `feat/1964_ai_assistant` |
| Gate 1 status | pending — owner-pending: Owner approval of this specification and decisions D1–D6 (design.md) |

## Objetivo

Notary staff spend time stitching a case (*gestión*) together from many screens (case summary, workflow,
documents, payments, expirations) and looking up procedure. An assistant in the main window that answers
questions about the case on screen and about the Notaire business rules, grounded in the user's own
authorized data and running on a **local** model, shortens that work without any data leaving the
installation (RNF-12, ADR-029).

## What Changes

- **New bounded context `assistant`** in `backend-api` (ADR-030 layout):
  - inbound port `application.port.in.assistant.AskAssistantUseCase` (streaming answer) and
    `ManageAssistantConversationsUseCase`;
  - outbound ports `application.port.out.assistant.ChatModelPort` and `KnowledgeRetrievalPort`;
  - use case `application.usecase.assistant.AskAssistantService` (context assembly, guards, audit);
  - outbound adapter `adapter.out.ai` on **Spring AI 2.0.2** (`spring-ai-bom`):
    `spring-ai-starter-model-openai` against the local OpenAI-compatible server (oMLX by default),
    `ChatClient` with `ToolCallingAdvisor`, `MessageChatMemoryAdvisor` (JDBC), and in phase 2
    `RetrievalAugmentationAdvisor` + `PgVectorStore`;
  - read-only **tools** (`@Tool`) that delegate only to existing inbound ports (case summary, workflow
    trace, required documents, payments, upcoming expirations, glossary), executed in the user's
    security context with per-tool role checks and audit;
  - inbound adapter `adapter.in.web.assistant.AssistantController`: REST + **SSE** streaming.
- **Frontend**: assistant side panel in `app/dashboard/layout.tsx` with case-context awareness
  (`core/assistant`, `adapters/http/assistant`, `composition`), streamed answers, citations, Stop,
  unavailable state; the BFF `app/api/v1/[...path]` streams `text/event-stream` unbuffered.
- **Configuration**: `NOTAIRE_ASSISTANT_*` keys, feature flag **off by default**; startup guard that
  refuses a non-local model endpoint.
- **Observability**: Spring AI `gen_ai.client.operation` / `spring.ai.tool` observations through the
  existing OTLP bridge, content never exported; Grafana "Assistant" dashboard and availability alert.
- **Evals**: curated Spanish eval prompts with Spring AI evaluators, run locally against oMLX as the
  release gate for model or prompt changes.
- No **BREAKING** change: all OpenAPI changes are additive.

Phases (issue #1964): 0 intent + spike (this PR) → 1 read-only assistant → 2 RAG (pgvector) →
3 guided actions with confirmation.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The assistant answers only with data the same user could read through the REST API | CU88, ADR-029, CU78 | New |
| Case data never leaves the installation: the model endpoint must be local unless the Owner allows otherwise | CU88, RNF-12, ADR-029 | New |
| The assistant never changes state on its own; any action is a proposal the user confirms through the normal use case | CU88 | New |
| Every assistant interaction is audited (who, context, tools, model, tokens) without storing prompt text by default | CU88, CU78, #1635 | New |
| Facts about a case are cited back to the record they came from | CU88 | New |
| The assistant is disabled by default and administered by `ROLE_ADMIN` | CU88, CU77 | New |

## Capabilities

### New Capabilities

- `assistant-chat`: conversational assistant over the user's authorized case data and business
  knowledge, local model provider, tool authorization, streaming API, privacy, audit and observability.

### Modified Capabilities

- None in phase 0. Phase 1 adds the BFF streaming requirement to the existing frontend BFF behaviour
  only if a spec for it exists at that time.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes (phase 1+) | `assistant` context, Spring AI adapter, SSE controller, tools, audit, health indicator |
| `frontend` | yes (phase 1+) | assistant panel, `core/assistant`, HTTP adapter, BFF streaming |
| `frontend-swing` | no | legacy client gets no assistant |
| `infra` / observability | yes | Grafana dashboard, alert rule; phase 2 PostgreSQL image with pgvector |
| CI/CD (`.github/workflows`) | yes (small) | stub-model integration tests run in the existing jobs; no GPU or real model in CI |

### Surface area

- Entities: `assistant_conversation`, `assistant_message` (Spring AI JDBC chat memory schema, owned by
  Flyway), `assistant_interaction` audit records; phase 2 `vector_store`.
- Endpoints (additive): `POST/GET /api/v1/assistant/conversations`,
  `DELETE /api/v1/assistant/conversations/{id}`, `POST /api/v1/assistant/conversations/{id}/messages`
  (SSE or JSON), `GET /api/v1/assistant/status`.
- Database (Flyway `V53`+): chat memory and audit tables in phase 1; `CREATE EXTENSION vector` and
  `vector_store` in phase 2 (Owner decision D3).
- Configuration / `.env`: `NOTAIRE_ASSISTANT_ENABLED`, `_BASE_URL`, `_API_KEY`, `_CHAT_MODEL`,
  `_EMBEDDING_MODEL`, `_ALLOW_REMOTE`, `_RETENTION_DAYS` (added to `.env.example`, no secrets).
- Dependencies: `org.springframework.ai:spring-ai-bom:2.0.2`, `spring-ai-starter-model-openai`,
  `spring-ai-starter-model-chat-memory-repository-jdbc`; phase 2 `spring-ai-starter-vector-store-pgvector`.
  Spring AI 2.0.x supports Spring Boot 4.0.x and 4.1.x; the repo is on Boot 4.1.2.

### Architecture review

Follows ADR-030: Spring AI types stay in `adapter.out.ai`; the application layer sees only its own
ports and domain types; tools call inbound ports, never repositories. Follows ADR-026: the product does
not import `local-ai/`; both only talk to the same oMLX server over HTTP. A new external runtime
dependency (local LLM server) and a new data flow are architectural, so
[ADR-031](../../../200-architecture/202-ADR/ADR-031-local-ai-assistant.md) (Proposed) is recorded in
this PR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU88 – Asistente IA de la gestión.md` | New use case (created in this PR) |
| `docs/200-architecture/202-ADR/ADR-031-local-ai-assistant.md` + ADR index | New ADR, Proposed (this PR); Accepted at Gate 1 |
| `docs/200-architecture/201-SAD/sad.md` | Assistant context and the local model runtime (phase 1) |
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | STRIDE rows for the assistant: injection, data leakage, tool abuse (phase 1) |
| User manual | "Asistente" section (phase 1) |
| `.env.example`, deployment docs | `NOTAIRE_ASSISTANT_*` keys, oMLX / Ollama host setup (phase 1) |
| `CHANGELOG.md` | n/a for phase 0 (no user-visible change); Features entry in phase 1 |

## Out of Scope

- Cloud LLM providers or external API keys (only by an explicit later Owner decision).
- Importing or packaging `local-ai/` (ADR-026); installing oMLX is a host prerequisite.
- Autonomous write actions; legal advice; fine-tuning on notary data; voice or vision.
- The legacy Swing client.
