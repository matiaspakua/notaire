# Engineering documentation — availability

October 2026 cut, after the case-process schema. The question is whether the
engineering documents the method names are present, accurate, and reachable
from `docs/`. The business cut is
[EVALUACION-DOCUMENTACION.md](../100-business/EVALUACION-DOCUMENTACION.md).
The method is [INGENIERIA.md](INGENIERIA.md).

## 1. Available

| ID | Document | Role |
|----|----------|------|
| `DOC-ING` | [INGENIERIA.md](INGENIERIA.md) | Artifact IDs, traceability, process boxes, pipeline |
| `DOC-CICLO` | [CICLO-DE-VIDA.md](../200-architecture/203-design/CICLO-DE-VIDA.md) | State machines, C4, known divergences |
| `DOC-GESTION` | [GESTION.md](../200-architecture/203-design/GESTION.md) | One case walkthrough, one sequence per step |
| `DOC-SAD` | [sad.md](../200-architecture/201-SAD/sad.md) | arc42, sections 1–12 |
| `DOC-MTP` | [TEST-PLAN.md](303-testing/TEST-PLAN.md) | Master test plan |
| `SR-` | [THREAT-MODEL-AUTH-AUDIT.md](../200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md) | Login and audit threats |
| — | [DEPLOYMENT-PLAN.md](DEPLOYMENT-PLAN.md), [monitoring](../200-architecture/207-monitoring/README.md) | Operate, and return findings to intake |

Requirements CU01–CU87, the SAD, and the ADRs were already in the tree. They
are not rewritten here.

## 2. What this cut corrected

`DOC-GESTION` §3.4 stopped at entry and then withdrew. The service does not.
Entry requires a verified testimony (`POST /api/v1/testimonies/{id}/verify`,
CU08). Withdrawal requires a registered movement
(`POST /api/v1/testimony-movements/{idTestimony}/register-inscription`, CU11).
Those calls are now in §3.4. Inscription does not move the management.

Two sequences were missing even though the services exist:

| Step | Sequence | Does not do |
|------|----------|-------------|
| Re-enter a withdrawn testimony (CU44) | `DOC-GESTION` §3.7 | Move the management, or overwrite the previous movement |
| Re-enter a document (CU43) | `DOC-GESTION` §3.8 | Move the management, or complete documentation |

`DOC-GESTION` is now a row in the identification table of `DOC-ING`.

## 3. Gaps that are not a missing document

| Gap | Why it stays |
|-----|----------------|
| Deed statuses Anulada, No Paso, Inscripta | Vocabulary. No dedicated service, so no sequence |
| Registration draft in `Observado` | The service has no return from `Observado` to `Presentado para inscripción` or `Inscripto`. Stored statuses are `Generada`, `Presentado para inscripción`, `Observado`, and `Inscripto`. `DOC-GESTION` §3.6 says so |
| Budget status order | Not guarded by edges. `DOC-CICLO` §3.7 |
| Data dictionary columns | Finding F3, issue #1222. It needs a generator |
| Repository license | Issue #1226 |
| Each use-case file's HTTP paths | The actor narrative stays in the use case. The live calls are in `DOC-GESTION` |

Issue #921 stays open for the dictionary and the license.
