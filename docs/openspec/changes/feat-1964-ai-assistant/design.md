# Design — AI assistant (Spring AI 2.0 + local oMLX)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Facts measured on `main` @ `10e3f3a06` (2026-10-28):

- **Backend**: Spring Boot 4.1.2, Java 26, PostgreSQL 16 (`postgres:16.15`, no pgvector), Flyway up to
  `V52`. Hexagonal per ADR-030 with ArchUnit ratchets. JWT HttpOnly cookie + double-submit CSRF; roles
  `ROLE_ADMIN` / `ROLE_NOTARY` / `ROLE_USER` from `users.type`; single tenant per installation
  (ADR-029); `@AuditPersonalDataRead` for personal-data reads. OpenTelemetry starter + OTLP + Prometheus.
  OpenAPI generated and committed, oasdiff breaking check.
- **Case data** is already exposed as inbound ports: `QueryManagementsUseCase#getCaseSummary`
  (`GET /api/v1/managements/{id}/case-summary`), `QueryWorkflowTraceUseCase`, current status, documents,
  payments, upcoming expirations.
- **Frontend**: Next.js 16; `app/dashboard/layout.tsx` is the main window; BFF proxy
  `app/api/v1/[...path]`; `core/` + `adapters/http` + `composition/` (ADR-030); browser OTel (#1739).
- **local-ai/** is the AI SDLC engine (foreman/worker/harness). It must not be imported by product code
  (`local-ai/MODULE.md`, ADR-026). It runs **oMLX** on the Mac host, an OpenAI-compatible server at
  `http://localhost:8000/v1` with streaming chat completions, tool calling, `/v1/embeddings`,
  `/v1/rerank` and `/v1/models` ([oMLX](https://github.com/jundot/omlx)). Presets in
  `local-ai/models.yaml`: `qwen3-coder` (Qwen3-Coder-30B-A3B 4-bit, ~17 GB, 32K), `gpt-oss` (20B MXFP4,
  ~12 GB, 64K, needs the harmony patch), `ternary-bonsai` (Ternary-Bonsai-2-27B 2-bit, ~8.6 GB, 64K,
  tool calls). Reference machine: MacBook Pro M5 / 24 GB with a 20 GB Metal ceiling. No embedding model
  is configured.
- **Spring AI**: current stable 2.0.2; "Spring AI 2.0.x supports Spring Boot 4.0.x and 4.1.x"
  ([Getting Started](https://docs.spring.io/spring-ai/reference/getting-started.html)). 2.1.0 is at RC1.

## Goals / Non-Goals

**Goals:**

- Answer questions about the case on screen and about procedure, grounded in authorized data, with
  citations, streamed in the main window.
- Keep every byte of case data inside the installation.
- Reuse the existing use cases and authorization; add no second path to the data.
- Keep Spring AI replaceable behind ports.

**Non-Goals:**

- Cloud models, autonomous writes, legal advice, fine-tuning, voice/vision, Swing client.

## Decisions

1. **Ports and adapters.** `AskAssistantUseCase` (in) → `AskAssistantService` → `ChatModelPort` and
   `KnowledgeRetrievalPort` (out). Spring AI lives only in `adapter.out.ai`; the domain types are
   `AssistantQuestion`, `AssistantAnswerChunk` (token / tool / citation / proposal / done / error),
   `CaseContext`, `Citation`. *Why over calling `ChatClient` from the controller:* ADR-030 rule R2,
   testability with a fake port, and Spring AI 2.x is still moving (2.1 RC).
2. **OpenAI-compatible starter, not the Ollama starter.** oMLX speaks the OpenAI API;
   `spring-ai-starter-model-openai` with `spring.ai.openai.base-url` and `extra-body` (`top_k`,
   `repetition_penalty`) covers oMLX, Ollama's OpenAI endpoint, vLLM and llama.cpp with one adapter
   ([OpenAI Chat](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)). The Ollama
   starter ([Ollama Chat](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)) stays a
   fallback if a host needs Ollama-native features. The spike checks whether the base URL takes `/v1`.
3. **Configuration, not code, links to local-ai.** Sampling values are copied from
   `local-ai/models.yaml` into `application.yml` defaults; nothing under `local-ai/` is imported or
   packaged (ADR-026). The backend container reaches the host via `host.docker.internal:8000`.
4. **Tools over inbound ports, read-only first.** Each tool is a `@Tool` method in
   `adapter.out.ai.tools` that calls one inbound port. The user id and request id travel in
   `ToolContext`, which is never sent to the model
   ([Tools](https://docs.spring.io/spring-ai/reference/api/tools.html)). A `NotaireToolAdvisor`
   (a `ToolCallingAdvisor` subclass) checks the role each tool declares, propagates the
   `SecurityContext` to the tool thread, audits the call, and in phase 3 turns write tools into
   `returnDirect` proposals. `spring.ai.tools.resolution.fallback.enabled=false` (default) and tighter
   limits (`max-calls-per-tool-default=5`, `max-total-tool-calls=15`). *Why not expose REST endpoints as
   MCP tools:* one more network hop and a second authorization surface.
5. **Context assembly is deterministic.** The client sends only `{contextType, contextId}`. The service
   loads the case summary through the inbound port as the user, masks DNI/CUIT unless needed, and wraps
   it, like every tool result and retrieved chunk, as untrusted data in the prompt template.
6. **Memory.** `MessageChatMemoryAdvisor` outside the tool loop (default order) with
   `JdbcChatMemoryRepository` on PostgreSQL; the conversation id is `user:conversation`. JDBC does not
   persist tool messages, which is acceptable outside the loop
   ([Chat Memory](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)). Flyway owns the
   schema (Spring AI schema initialization off).
7. **RAG in phase 2.** `RetrievalAugmentationAdvisor` with `PgVectorStore`
   ([RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html),
   [PGvector](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)); requires the
   `vector`, `hstore` and `uuid-ossp` extensions, so the PostgreSQL image changes (D3). Embeddings via
   oMLX `/v1/embeddings`. Corpus A: business docs and glossary; corpus B: case documents with
   `managementId` metadata, filtered to the current case. Phase 1 uses no vector store.
8. **Streaming API.** Spring MVC `SseEmitter` (or `Flux<ServerSentEvent>`) fed by
   `ChatClient.stream()`; one active stream per user; client disconnect cancels the subscription. The
   BFF must pass `text/event-stream` through without buffering (`X-Accel-Buffering: no`).
9. **Observability.** Keep Spring AI's content flags off: `spring.ai.chat.observations.log-prompt`,
   `log-completion`, `spring.ai.chat.client.observations.log-prompt`/`log-completion`,
   `spring.ai.tools.observations.include-content`, `spring.ai.vectorstore.observations.log-query-response`
   ([Observability](https://docs.spring.io/spring-ai/reference/observability/index.html)).
10. **Evals.** Spring AI `RelevancyEvaluator` / `FactCheckingEvaluator`
    ([Testing](https://docs.spring.io/spring-ai/reference/api/testing.html)) plus deterministic checks
    (expected tools called, citations present, no other case leaked), run locally against oMLX.

### Owner decisions (Gate 1)

| Id | Decision | Proposed default |
|----|----------|------------------|
| D1 | Default chat model, and an embedding model for the oMLX presets | Ternary Bonsai 27B 2-bit (fits next to the stack); Qwen3-Coder-30B-A3B as the quality alternative; embedding model chosen in the spike |
| D2 | Supported non-Mac hosts | Ollama documented as the alternative (same adapter) |
| D3 | pgvector in phase 2 or lexical retrieval | `pgvector/pgvector:pg16` image + Flyway extension migration |
| D4 | Conversation retention; conversation text in audit | 30 days; no text in audit |
| D5 | Write actions in phase 3 and their roles | Registration draft and status transition proposals, `ROLE_NOTARY`/`ROLE_ADMIN` |
| D6 | Milestone | v0.4 for phases 0–1; v0.5 phase 2; v0.6 phase 3 |

## Riesgos / Trade-offs

- [oMLX is Apple-Silicon only] → OpenAI-compatible adapter works with Ollama/vLLM/llama.cpp; D2.
- [24 GB machine cannot hold Qwen3-Coder plus the stack] → Bonsai default; model is configuration.
- [Local 2-bit/MoE models emit malformed tool calls] → spike in phase 0, small strict tool set, tool
  limits, eval gate before any model change.
- [Hallucinated legal facts] → grounding + citations, refusal rules, disclaimer, fact-checking evals.
- [Prompt injection via deeds/documents] → untrusted-data fencing, allow-listed read tools, server-side
  RBAC per tool, confirmation for any write, red-team evals.
- [Spring AI 2.x churn (2.1 RC, chat memory moving to spring-ai-session)] → ports isolate it; BOM pin.
- [SSE buffered by the BFF or a proxy] → explicit streaming tests at BFF and E2E level.
- [Dependency convergence with our security overrides] → spike runs `mvn dependency:tree` and the
  security scans before any feature code.
- Not mitigable here: answer latency depends on the host GPU; stated in the user manual.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Assistant disabled by default | integration + E2E | `AssistantStatusIntegrationTest`, `testing/e2e/tests/assistant.spec.ts` |
| Model server down | integration + E2E | `AssistantUnavailableIntegrationTest`, `assistant.spec.ts` |
| Remote endpoint refused | unit | `AssistantEndpointGuardTest` |
| Ask about the management on screen | integration (stub model) + E2E | `AssistantChatSseIntegrationTest`, `assistant.spec.ts` |
| User stops the answer | unit (frontend) + integration | `assistantStream.test.ts`, `AssistantChatSseIntegrationTest` |
| Data the REST API denies is denied to the assistant | unit + integration | `NotaireToolAdvisorTest` (tool × role matrix), `AssistantToolAuthorizationIntegrationTest` |
| Prompt injection inside a document | unit + eval | `AssistantPromptTemplateTest`, `testing/assistant-evals/injection/*.yaml` |
| Proposed action needs confirmation | unit + E2E (phase 3) | `NotaireToolAdvisorTest`, `assistant-proposal.spec.ts` |
| Interaction is audited without content | integration | `AssistantAuditIntegrationTest`, `AssistantObservationContentTest` |
| Conversation belongs to its owner | integration | `AssistantConversationOwnershipIntegrationTest` |
| One trace per question | integration | `AssistantTracePropagationIntegrationTest` |

- New unit tests: `AskAssistantServiceTest` (fake ports), `CaseContextAssemblerTest` (masking),
  `AssistantEndpointGuardTest`, `NotaireToolAdvisorTest`, `AssistantPromptTemplateTest`; frontend
  Vitest for `core/assistant` state and the SSE parser.
- New integration tests: `@SpringBootTest` + Testcontainers PostgreSQL + a stub OpenAI-compatible
  server (MockWebServer replaying recorded `/v1/chat/completions` SSE and tool-call payloads); ArchUnit
  rules for the `assistant` context.
- Eval prompts: ~40 Spanish cases in `testing/assistant-evals/` (case Q&A, procedure, find, refusal,
  injection, leakage), run by `testing/assistant-evals/run-evals.sh` against oMLX; thresholds ≥ 85 %
  relevancy, 100 % on injection/leakage; scores recorded in the PR of any model or prompt change.
- Coverage impact: new code at ≥ 80 %; JaCoCo and Vitest ratchets held.

## Regression Strategy

- Existing tests affected: `HexagonArchitectureTest` (new context must pass all rules, baseline does not
  grow), `SecurityAndCorsConfig` tests (new path rules), OpenAPI staleness test, BFF route tests.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh` (new `assistant/` collection, stub model)
- Legacy paths at risk: none; the assistant only reads through hexagonal inbound ports.

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: `assistant.spec.ts` (phase 1),
  `assistant-proposal.spec.ts` (phase 3).
- Golden path covered: open the panel on a management page, context chip, streamed answer from the stub
  model, citation link opens the record.
- Edge / error paths covered: Stop, unavailable state, disabled flag hides the panel, 429 rate limit,
  keyboard-only use and `aria-live` announcements.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd testing/e2e && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes (phase 1: chat memory + audit tables; phase 2: `vector` extension +
  `vector_store`).
- Deployment order / coupling: migration and code ship together; the flag stays off until the host has
  oMLX (or Ollama) with the chosen model loaded.
- Configuration or `.env` keys to add: `NOTAIRE_ASSISTANT_ENABLED`, `NOTAIRE_ASSISTANT_BASE_URL`,
  `NOTAIRE_ASSISTANT_API_KEY`, `NOTAIRE_ASSISTANT_CHAT_MODEL`, `NOTAIRE_ASSISTANT_EMBEDDING_MODEL`,
  `NOTAIRE_ASSISTANT_ALLOW_REMOTE`, `NOTAIRE_ASSISTANT_RETENTION_DAYS`.
- Feature flag: yes (`NOTAIRE_ASSISTANT_ENABLED`, default `false`).
- Smoke test after deploy (Gate 5): `/actuator/health` includes `assistant`; `GET
  /api/v1/assistant/status`; one question on a seeded management returns a cited answer.

## Rollback Strategy

- Revert safe: yes. Setting the flag to `false` removes the feature at runtime without a deploy.
- Database rollback: forward-fix only; the new tables are additive and unused when the flag is off.
- Data written under the new behavior after revert: conversations and audit rows remain, purged by the
  retention job or a manual delete.
- Blast radius if rollback is delayed: limited to the assistant panel; no existing endpoint changes.

## Migration Plan

1. Phase 0 (this PR): intent, CU88, ADR-031 Proposed; Owner Gate 1 and D1–D6.
2. Spike: BOM convergence on Boot 4.1.2 / Java 26; tool-call reliability of Bonsai, Qwen3 and gpt-oss
   through `ChatClient` against oMLX; base URL `/v1` handling; embedding model.
3. Phase 1: read-only assistant (v0.4). 4. Phase 2: RAG (v0.5). 5. Phase 3: guided actions (v0.6).

## Open Questions

- Which embedding model oMLX should serve (spike output, feeds D1).
