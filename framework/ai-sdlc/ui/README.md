# Kit UI static mount

Placeholder for the factory static UI served by `aisdlc serve` at `/ui/`
(#2253 Phase A.2). Do not import Notaire `frontend/src/theme`.

Phase B/C ship kit-neutral backoffice HTML via `aisdlc backoffice --html|--serve`
(stdlib adapters; inline tokens). Phase D (#2242) ships the separate **runtime**
surface via `aisdlc runtime --html|--serve` (Projects / Start run / Monitor)
over Phase A.4 Project/Run ports — still no Node build. Optional ES modules /
`tokens.css` may land here later without changing the OpenAPI SoR.
