# ADR-031: Local AI assistant through Spring AI behind hexagonal ports

**Status:** Proposed — awaiting Owner decision on issue #1964 (Gate 1)  
**Date:** 2026-10-28  
**Deciders:** Owner (product), Engineering  
**Related:** ADR-019 (secrets), ADR-026 (module separation), ADR-029 (single tenant, roles), ADR-030 (hexagonal), CU88, [OpenSpec change](../../openspec/changes/feat-1964-ai-assistant/proposal.md)

## Context

The product wants an assistant in the main window that explains the case on screen and answers procedure
questions (CU88). Case data is personal and notarial; each installation is one notary's tenant (ADR-029)
and the data must not leave it. The repo already runs a local OpenAI-compatible model server, oMLX, for
the AI SDLC (`local-ai/`, ADR-026: not importable by product code). Spring AI 2.0.x is the Spring
portfolio's AI client and supports Spring Boot 4.0.x and 4.1.x; the backend is on Boot 4.1.2.

## Decision (proposed)

1. Add an `assistant` bounded context. The application layer depends only on its own ports
   (`AskAssistantUseCase`, `ChatModelPort`, `KnowledgeRetrievalPort`); Spring AI 2.0 (`ChatClient`,
   advisors, tools, chat memory, vector store) lives only in `adapter.out.ai`.
2. Talk to the model through the OpenAI-compatible API (`spring-ai-starter-model-openai` with a
   configurable base URL). oMLX on the host is the default; Ollama, vLLM or llama.cpp serve other hosts.
   The product never imports `local-ai/`; it shares only the server, over HTTP, by configuration.
3. The model endpoint must be local or on a private network; a remote host needs an explicit Owner
   override.
4. Tools delegate only to existing inbound ports and run with the requesting user's authorization,
   checked per tool; no tool changes state without user confirmation through the normal use case.
5. Prompt and completion content is never exported to logs, traces or metrics; interactions are audited
   without content by default.
6. The feature ships behind `NOTAIRE_ASSISTANT_ENABLED`, off by default.

## Consequences

- New runtime prerequisite per installation (a local model server and enough memory), documented in the
  deployment guide; the product keeps working when it is absent.
- Spring AI upgrades are contained in one adapter package.
- Phase 2 RAG with pgvector changes the PostgreSQL image (separate Owner decision D3).
- Model quality becomes a release concern: an eval suite gates model and prompt changes.

## Alternatives considered

- **Calling oMLX with a hand-written HTTP client**: less dependency weight, but re-implements tool
  calling, streaming, memory, RAG and observability that Spring AI provides.
- **LangChain4j**: comparable features; Spring AI chosen for Boot auto-configuration and Micrometer
  observations aligned with the existing OTel stack.
- **Cloud LLM API**: better models, but sends case data outside the tenant; rejected.
- **Spring AI Ollama starter**: oMLX is not Ollama; the OpenAI-compatible starter covers both.
