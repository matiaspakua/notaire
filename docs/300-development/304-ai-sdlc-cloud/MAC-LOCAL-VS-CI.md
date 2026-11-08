# Mac local gates vs CI ownership

> For hex / R2 work under epic #1693. Source evidence: Mac validation on tip
> after #1846 (see Project-store `docs/mac-local-validation-1693.md`).
> Process index: [`FLEET-LESSONS-HEX-1693.md`](FLEET-LESSONS-HEX-1693.md).

When a self-hosted Mac (Docker OK) just re-ran the left column on clean
`origin/main`, agents may **lean on** that evidence for hex/R2-only PRs.
CI still owns the right column — do not skip those jobs or merge without them
when they are required for the PR path set.

**Mac green does not replace CI green.** A green Mac ArchUnit / unit / FE / local
security run is evidence for those columns only. It is **not** merge authority
and does **not** mean Bruno full, Playwright, CodeQL, DAST, or image build are
safe to skip. Always run `bash workspace/sdlc/check-heavy-ci.sh <pr>` on the
PR head before merge.

## Safe to lean on Mac (when freshly green on tip)

| Area | Typical command / note |
|------|------------------------|
| ArchUnit hex ratchet R1–R6 | `mvn -pl backend-api -Dtest=HexagonArchitectureTest test` |
| Use-case slice cycles R7 | `mvn -pl backend-api -Dtest=UseCaseSliceCycleArchitectureTest test` |
| Backend unit + coverage/`verify` | `mvn -pl backend-api test` then `verify -DskipTests` after tests |
| `pg-integration` (Testcontainers) | `mvn -pl backend-api test -Ppg-integration` (needs Docker) |
| Spotless / Checkstyle | `mvn -pl backend-api spotless:check` (via preflight) |
| FE typecheck + eslint (hex import bans) + Vitest + build | `npm run typecheck` / `lint` / `test -- --run` / `next build` |
| FE suppressions ratchet | `eslint-suppressions.json` is `{}` / **count 0** when FE hex Done |
| Security local profile | `python3 security/run_gates.py --profile local` (DAST skipped by design) |

## Still let CI own

| Area | Why |
|------|-----|
| CodeQL | Cloud/GitHub Advanced Security; not mirrored by Mac local profile |
| Full Bruno API suite | CI / `preflight.sh --full` / `run_pipeline.sh` territory |
| Playwright E2E | CI shards + fresh stack; Mac may skip for hex-critical focus |
| DAST / ZAP | Needs `run_pipeline` / deployed stack |
| Docker image build / smoke | CI CD path |
| `gh`-dependent Process Checks | Local `gh issue view` can **flake** (“Issue #N was not found”); re-run validator — not a product regression |

## Practical split for #1693 R2 PRs

1. Mac (or Cloud with Docker) proves ArchUnit shrink + unit/pg + FE lint/tests before push.
2. Steward still waits for `bash workspace/sdlc/check-heavy-ci.sh <pr>` on the PR head.
3. Do not open a “Mac failed preflight once on `gh`” product bug — re-run
   `validate-sdlc-plan.sh` (retries are built in after #1858; hardened further in #1907).
4. After package moves to `adapter.out.persistence.*`, Mac/Cloud must also refresh
   english-locals / `docs/tests` path refs and INVENTORY before Process Checks
   (`FLEET-LESSONS-HEX-1693.md` — R2 path hygiene).
