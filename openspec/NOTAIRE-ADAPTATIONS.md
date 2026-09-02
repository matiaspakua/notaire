# OpenSpec en Notaire — Guía de instalación, adaptaciones y quality gates

> **Autoridad**: `CONSTITUTION.md` en la raíz del repositorio es el documento de
> mayor autoridad. Este archivo es el registro canónico de cómo **OpenSpec** fue
> instalado, adaptado y conectado a esa Constitución en el proyecto Notaire.
> Consolida y reemplaza `docs/300-development/OPENSPEC-CONSTITUTION-BRIDGE.md`
> (archivado en `docs/000-archive/`), per Constitución P7/§8 (nunca duplicar
> documentación permanente). Tracked under #783 (adopción original) y #870
> (consolidación).
>
> Este documento cubre **únicamente OpenSpec**. El proyecto también evalúa
> **SpecKit** como segundo framework spec-driven bajo `speckit/` — ver
> `speckit/NOTAIRE-ADAPTATIONS.md` para su propia historia de adaptación. Los
> dos se mantienen deliberadamente separados: directorios distintos, scripts de
> validación distintos, sin código compartido ni referencias cruzadas más allá
> de esta oración.

---

## Tabla de contenidos

1. [¿Qué es OpenSpec?](#1-qué-es-openspec)
2. [Instalación](#2-instalación)
3. [Configuración inicial del proyecto](#3-configuración-inicial-del-proyecto)
4. [Adaptaciones Notaire — paso a paso](#4-adaptaciones-notaire--paso-a-paso)
   - 4.1 [Fork del schema: `notaire-sdlc`](#41-fork-del-schema-notaire-sdlc)
   - 4.2 [Contexto de la Constitución en `config.yaml`](#42-contexto-de-la-constitución-en-configyaml)
   - 4.3 [El gap Explore → Issue → Propose](#43-el-gap-explore--issue--propose)
   - 4.4 [Skill project-owned: `openspec-triage`](#44-skill-project-owned-openspec-triage)
   - 4.5 [Gate mecánico: `validate-sdlc-plan.sh`](#45-gate-mecánico-validate-sdlc-plansh)
   - 4.6 [Archivado: `openspec-archive-change`](#46-archivado-openspec-archive-change)
5. [Flujo completo — secuencia canónica](#5-flujo-completo--secuencia-canónica)
6. [Quality Gates por etapa SDLC](#6-quality-gates-por-etapa-sdlc)
   - 6.1 [Gate 1 — Especificación (no empezar sin esto)](#61-gate-1--especificación-no-empezar-sin-esto)
   - 6.2 [Gate 2 — TDD (tests primero, fallando)](#62-gate-2--tdd-tests-primero-fallando)
   - 6.3 [Gate 3 — PR-ready (suite verde + docs)](#63-gate-3--pr-ready-suite-verde--docs)
   - 6.4 [Gate 4 — Merge (CI verde + review)](#64-gate-4--merge-ci-verde--review)
   - 6.5 [Gate 5 — Done (deploy + smoke test + cierre)](#65-gate-5--done-deploy--smoke-test--cierre)
7. [Mapa de herramientas por etapa](#7-mapa-de-herramientas-por-etapa)
8. [Skills y commands disponibles](#8-skills-y-commands-disponibles)
  - 8.1 [Composición de skills para el SDLC](#81-composición-de-skills-para-el-sdlc)
  - 8.2 [Contratos de artefactos y evidencias](#82-contratos-de-artefactos-y-evidencias)
9. [Evidencia: changes que recorrieron el flujo completo](#9-evidencia-changes-que-recorrieron-el-flujo-completo)
10. [Verificación del gate mecánico](#10-verificación-del-gate-mecánico)
11. [Cómo adaptar OpenSpec en un proyecto nuevo](#11-cómo-adaptar-openspec-en-un-proyecto-nuevo)
12. [Referencias](#12-referencias)

---

## 1. ¿Qué es OpenSpec?

OpenSpec es una CLI de especificación schema-driven. Su núcleo: un **schema**
(`openspec/schemas/<nombre>/schema.yaml`) declara un conjunto de **artifacts**
(proposal, spec deltas, design, tasks, traceability), cada uno con un template
Markdown y un bloque `instruction:` que la CLI inyecta en el contexto del agente
vía `openspec instructions`. El comando `openspec new change <nombre>` genera el
directorio `openspec/changes/<nombre>/` a partir de esos templates; `openspec
archive <nombre>` mueve un change completado a `openspec/changes/archive/` y
sincroniza sus deltas aceptados en `openspec/specs/<capability>/spec.md`.

Lo que OpenSpec no hace por defecto: verificar que el Issue existe en GitHub,
exigir diseño técnico en cada cambio, ni conectar con los gates de CI de un
proyecto específico. Esas capas las agrega Notaire sobre OpenSpec.

### Comandos CLI de referencia rápida

```bash
# Instalar / actualizar
npm install -g openspec

# Ciclo de vida de un change
openspec new change "<nombre-en-kebab-case>"      # scaffoldea los artifacts
openspec status --change "<nombre>"               # orden de construcción de artifacts
openspec instructions <artifact> --change "<nombre>"  # instrucciones para el agente
openspec validate "<nombre>" --strict             # chequeos estructurales
openspec list                                     # changes activos
openspec archive "<nombre>"                       # archivar un change completado

# Checks de integridad
openspec doctor                                   # salud de la instalación
openspec context --json                           # root resuelto por la CLI
openspec schemas --json                           # schemas disponibles
```

---

## 2. Instalación

### Prerrequisitos

| Herramienta | Versión mínima | Uso |
|-------------|----------------|-----|
| Node.js | 18+ | Runtime de la CLI |
| npm | 9+ | Instalación de la CLI |
| `gh` CLI | 2.x | Verificación live de Issues (Gate 1) |
| Git | cualquiera | Branches, commits, pre-push hook |
| Bash | 3.2+ | Scripts de validación |
| Java 21 + Maven 3.9 | (proyecto) | Backend quality gates |
| Node.js 22 + npm 10 | (proyecto) | Frontend quality gates |

### Pasos de instalación

```bash
# 1. Instalar la CLI globalmente
npm install -g openspec

# 2. Verificar la instalación
openspec --version
openspec doctor

# 3. Autenticar gh CLI (necesario para el gate de Issue live)
gh auth login

# 4. Instalar git hooks del proyecto (pre-push ejecuta preflight.sh)
bash scripts/install-git-hooks.sh

# 5. Verificar que el schema del proyecto está reconocido
openspec schemas --json
# debe incluir "notaire-sdlc"
```

### Verificar que todo está conectado

```bash
# Debe listar los changes activos bajo openspec/changes/
openspec list

# Debe validar todos los changes activos
bash scripts/validate-sdlc-plan.sh

# Debe ejecutar todos los gates locales
bash scripts/preflight.sh --list
```

---

## 3. Configuración inicial del proyecto

La configuración de OpenSpec en Notaire vive en **dos archivos project-owned**:

```
openspec/
├── config.yaml                        # contexto de la Constitución + reglas + operations
└── schemas/
    └── notaire-sdlc/
        ├── schema.yaml                # fork del schema spec-driven con adaptaciones
        └── templates/
            ├── proposal.md            # template con todas las secciones obligatorias
            ├── traceability.md        # ledger Issue → ... → Release
            ├── spec.md                # delta spec con Scenarios como criterios
            ├── design.md              # diseño técnico + estrategias testing/deploy
            └── tasks.md               # 12 grupos SDLC + Definition of Done
```

Ninguno de estos archivos es generado por `openspec update` — son project-owned
y sobreviven actualizaciones de la CLI. Los skills vendor (`openspec-propose`,
`openspec-apply-change`, `openspec-archive-change`, `openspec-explore`,
`openspec-sync-specs`, `openspec-update-change`) en `.claude/skills/` sí son
reemplazados por `openspec update`; por eso ninguna adaptación del proyecto vive
dentro de ellos.

---

## 4. Adaptaciones Notaire — paso a paso

### 4.1 Fork del schema: `notaire-sdlc`

**Archivo**: `openspec/schemas/notaire-sdlc/schema.yaml`

El schema upstream es `spec-driven` (empaquetado con openspec 1.8.0). Notaire lo
forkea en un schema propio para poder agregar la Constitución sin tocar el
código vendor. El fork mantiene intactas todas las primitivas de parsing de
OpenSpec:

- `## ADDED/MODIFIED/REMOVED/RENAMED Requirements`
- `### Requirement:` (tres hashtags)
- `#### Scenario:` con exactamente cuatro hashtags
- `- [ ]` checkboxes de tareas

Lo que el fork **agrega** sobre el upstream:

| Adición | Por qué |
|---------|---------|
| `CONSTITUTION.md` como contexto obligatorio en cada artifact | Todo agente recibe la Constitución via `openspec instructions`, sin importar la herramienta |
| Secciones mandatorias en `proposal.md` | Impact Analysis, Documentation Impact, Reglas de negocio — exigidas por §5 paso 3-4 |
| Artifact `traceability` | Implementa P4 (trazabilidad Issue→Release), no existe en el schema upstream |
| `design.md` obligatorio (no condicional) | La Constitución §6/§7 exige estrategias de testing/regresión/Playwright/deploy/rollback en cada cambio |
| Schema `notaire-sdlc` en `config.yaml` | Hace que `openspec new change` use este schema por defecto |

Para actualizar openspec sin perder el fork:
```bash
openspec update          # actualiza skills vendor
# Luego revisar manualmente:
diff <(openspec schema which spec-driven --json) openspec/schemas/notaire-sdlc/schema.yaml
```

### 4.2 Contexto de la Constitución en `config.yaml`

**Archivo**: `openspec/config.yaml`

Este es el mecanismo por el cual **cualquier agente** — Claude Code, OpenCode,
GitHub Copilot, Codex, Cursor — recibe la Constitución y las reglas del proyecto.
La CLI inyecta `context:` en cada llamada a `openspec instructions <artifact>`.

```yaml
schema: notaire-sdlc
githubCopilot:
  cloudAgent: true

context: |
  This repository is governed by CONSTITUTION.md at its root...
  [puntero a la Constitución + no-negociables, nunca una copia]

rules:
  proposal:    [...]   # secciones obligatorias, Issue real, nunca fabricado
  traceability:[...]   # no pre-llenar, solo evidencia real
  specs:       [...]   # Scenarios con 4 hashtags, happy+edge+error paths
  design:      [...]   # todas las secciones, Testing Strategy completa
  tasks:       [...]   # 12 grupos obligatorios + DoD

operations:
  apply:
    guidance:  [...]   # verificar Gate 1 primero, TDD, preflight.sh antes de push
  archive:
    guidance:  [...]   # solo después de Gate 5: deploy + smoke + Issue cerrado
```

El `context:` es un **puntero** a `CONSTITUTION.md` más sus no-negociables, no
una copia. La documentación permanente tiene un único dueño (P7/§8). Los `rules:`
por artifact y el `operations:` por comando exigen explícitamente el Issue real,
los 12 grupos de tareas, y que traceability.md nunca se pre-llene.

### 4.3 El gap Explore → Issue → Propose

**El problema que había**: `opsx:explore` produce un informe de hallazgos/gaps
pero nunca scaffoldea un change (es una postura de pensamiento). `opsx:propose`
scaffoldea un change y espera un Issue number en el header de `proposal.md`. El
único check que existía validaba el **formato** del Issue reference (`#[0-9]+`),
no que el Issue fuera **real**. Nada impedía que un agente escribiera `#999999`
en un proposal y lo pasara a Gate 1.

**La solución**: tres piezas que trabajan juntas:

```
opsx:explore          openspec-triage          opsx:propose
(thinking, produce  ──▶  (informe → Issues   ──▶  (scaffoldea change,
 informe; nunca           reales, estimados,        proposal.md cita
 scaffoldea change)       priorizados, con CU)      Issue REAL)
                                   │
                                   ▼
                     scripts/validate-sdlc-plan.sh
                     resuelve el Issue live via
                     `gh issue view` — un Issue
                     fabricado o cerrado falla Gate 1
```

Todo hallazgo en `openspec/explore*.md` debe tener un Issue de GitHub real y
abierto antes de que `/opsx:propose` lo convierta en un change. Esta regla está
en `openspec/config.yaml`'s `context:` y es reforzada mecánicamente por el
script (ver §4.5).

### 4.4 Skill project-owned: `openspec-triage`

**Archivo**: `.claude/skills/openspec-triage/SKILL.md`

- `metadata.author: notaire-project` — no es tocado por `openspec update`
- El eslabón faltante entre `opsx:explore` y `opsx:propose`
- Toma un informe de exploración y produce una lista de candidatos estimados,
  priorizados y con CU-XX asociado
- Solo crea Issues de GitHub con confirmación explícita del usuario (`gh issue create`)
- Su única salida es un número de Issue real — lo único que puede pasarse a `opsx:propose`

**Flujo interno del skill**:
1. Leer el informe fuente completo
2. Extraer un candidato por feature/gap: título, CU-XX, descripción, acceptance
   criteria, notas técnicas
3. Estimar tamaño (S/M/L) y prioridad (`priority:high/medium/low`)
4. Escribir la lista candidata en `openspec/<source>_issues.md`
5. Mostrar al usuario y **detenerse** — nunca crear Issues sin confirmación
6. Con confirmación, `gh issue create` uno a uno, registrar el número resultante
7. Reportar cuáles tienen Issue real y están listos para `opsx:propose`

**Guardrails**: nunca inventar un número de Issue; nunca scaffoldear un change
(`openspec new change`) desde este skill; nunca saltear el check de Use Case.

### 4.5 Gate mecánico: `validate-sdlc-plan.sh`

**Archivo**: `scripts/validate-sdlc-plan.sh`

Este es el gate mecánico que convierte las reglas advisory del schema en un check
que falla con exit code ≠ 0. Es bash puro — funciona para cualquier agente, CI,
o humano, sin hooks tool-specific.

```bash
# Uso
bash scripts/validate-sdlc-plan.sh                 # todo los changes activos
bash scripts/validate-sdlc-plan.sh <change-name>   # un change específico
bash scripts/validate-sdlc-plan.sh --list          # mapeo checks ↔ §Constitución
```

**Qué verifica**:

| Check | Archivo | Qué falla |
|-------|---------|-----------|
| Issue + Use Case en header | `proposal.md` | §4/Gate 1 — número faltante o placeholder |
| Issue **existe y está OPEN** en GitHub | `proposal.md` | Explore→Issue→Propose — resuelve live via `gh issue view` |
| Secciones obligatorias | `proposal.md` | Objetivo, What Changes, Reglas de negocio, Impact Analysis, Documentation Impact |
| Chain completa | `traceability.md` | Issue→Spec→Tasks→Commits→PR→Merge→Release |
| No pre-llenada | `traceability.md` | Detecta SHAs/PRs ficticios en la cadena |
| Al menos un delta spec | `specs/**/*.md` | Salvo `skip_specs: true` en `.openspec.yaml` |
| Scenarios bien formados | `specs/**/*.md` | `#### Scenario:` con 4 hashtags |
| Secciones de diseño | `design.md` | Testing/Regression/Playwright/Deployment/Rollback |
| 12 grupos SDLC | `tasks.md` | Gate 1-5, grupos 1-12 + Definition of Done |

**El check de Issue live** (el más importante):

```bash
# Cuando gh está disponible y autenticado:
# ✗ - Issue #999999 was not found on GitHub (Gate 1 fails)
# ✓ - Issue #780 verified live on GitHub (OPEN)

# Sin gh disponible:
# - (advisory) gh CLI not available/authenticated — Issue not live-verified
#   (nota visible, nunca un silent pass)
```

**Integración**: ejecutado por `scripts/preflight.sh` (local, pre-push) y por
`.github/workflows/pr-validation.yml` (CI, bloqueante).

### 4.6 Archivado: `openspec-archive-change`

**Skill**: `.claude/skills/openspec-archive-change/SKILL.md` (vendor, `metadata.author: openspec`)

El mecanismo de archivado no requirió adaptación — se usa tal como viene.
`openspec/config.yaml`'s `operations.archive.guidance` pina la secuencia para
que un agente no pueda archivar temprano:

```yaml
operations:
  archive:
    guidance:
      - "Do not archive before Gate 5 - deployed, smoke test passed, Issue closed."
      - "Confirm traceability.md is complete from Issue through Release..."
      - "Confirm the permanent documentation named in Documentation Impact was updated..."
      - "Summarize what moved into openspec/specs/ and what remains outstanding."
```

**Resultado del archivo**:
- Change movido a `openspec/changes/archive/YYYY-MM-DD-<name>/`
- Deltas aceptados sincronizados en `openspec/specs/<capability>/spec.md`
- `traceability.md` completa con evidencia real (SHAs, PR, CI run, smoke test)

---

## 5. Flujo completo — secuencia canónica

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     NOTAIRE SDLC — FLUJO COMPLETO                           │
└─────────────────────────────────────────────────────────────────────────────┘

  opsx:explore                                           GATE 1
  ─────────────                                          ──────
  Producir hallazgos/gaps                     ◄── openspec-triage → Issue real
  en openspec/explore*.md                         (nunca opsx:propose sin Issue)
          │
          ▼
  openspec-triage (SKILL, project-owned)
  Informe → candidatos estimados/priorizados
  gh issue create (con confirmación)
  Issue # real registrado en el informe
          │
          ▼
  opsx:propose (SKILL, vendor)
  openspec new change "<name>"
  Scaffoldea: proposal.md, traceability.md,
  specs/, design.md, tasks.md
          │
          ▼
  bash scripts/validate-sdlc-plan.sh ◄── Gate mecánico:
  • Issue existe y está OPEN               Issue fabricado → FALLA
  • Secciones obligatorias                 Issue cerrado  → FALLA
  • Traceability no pre-llenada            Sin diseño     → FALLA
  • 12 grupos SDLC presentes               Sin specs      → FALLA
  • Scenarios bien formados
          │
          ▼ Gate 1 pasa
  ─────────────────────────────────────────────────────
  git checkout main && git pull
  git checkout -b <type>/<issue-number>_<description>  ◄── rama desde main
  gh issue edit <n> --add-label "in-progress"
  ─────────────────────────────────────────────────────
          │
          ▼
  GATE 2 — TDD
  Escribir tests (unit + integration) → observarlos FALLAR
  mvn test -pl backend-api -Dtest=<NewTestClass>  # debe FALLAR aquí
          │
          ▼
  opsx:apply (SKILL, vendor)
  Implementar (make tests pass)
  Refactor (KIS, SRP, eliminar código muerto/duplicado)
          │
          ▼
  GATE 3 — PR-ready
  mvn test -pl backend-api                  # unit + integration
  mvn jacoco:check -pl backend-api          # coverage ratchet ≥ 70% línea / 25% branch
  mvn verify -pl backend-api                # Checkstyle + SpotBugs
  cd frontend && npx playwright test        # E2E (si hay cambios UI)
  Actualizar documentación permanente       # docs/, README.md, CHANGELOG.md
  bash scripts/preflight.sh --fix           # mirrors todos los CI gates
  bash scripts/run_pipeline.sh              # gate final pre-PR (HTML dashboard)
          │
          ▼
  git push -u origin <branch>
  gh pr create --title "[#<issue>] type(scope): description"
          │
          ▼
  GATE 4 — CI verde + review
  ci.yml + pr-validation.yml + frontend-ci.yml + playwright-e2e.yml
  Code review (code owner merging = aprobación en repo solo-maintainer)
  Merge via PR (nunca push directo a main)
          │
          ▼
  GATE 5 — Done
  cd.yml → imagen Docker → GHCR
  Smoke test en el entorno destino
  gh issue close <n> --comment "Closes #<n> via PR #<pr>"
  openspec archive <change-name>            # sync specs, traceability completa
```

---

## 6. Quality Gates por etapa SDLC

### 6.1 Gate 1 — Especificación (no empezar sin esto)

**Cuándo**: Antes de crear la rama o escribir cualquier código.

**Condiciones** (todas deben cumplirse):

- [ ] Issue de GitHub existe, tiene etiqueta, y está vinculado a un Caso de Uso (`CU-XX` / `RF-XX` / `RNF-XX`)
- [ ] Especificación escrita (proposal, specs, design, tasks con `openspec new change`)
- [ ] Acceptance Criteria definidos como `#### Scenario:` en el delta spec
- [ ] Impact Analysis y módulos afectados confirmados en `proposal.md`
- [ ] ADR si el cambio es arquitectónico (`docs/200-architecture/202-ADR/`)

**Scripts y herramientas que lo enforcan**:

```bash
# Validación mecánica local (y en CI)
bash scripts/validate-sdlc-plan.sh <change-name>

# Verificación del Issue live
gh issue view <number>   # usado internamente por validate-sdlc-plan.sh

# Validación estructural del schema
openspec validate <change-name> --strict
```

**CI jobs que lo bloquean**:
- `pr-validation.yml` → job `sdlc-plan-validation` → `bash scripts/validate-sdlc-plan.sh` (BLOCKING)

**Regla de trazabilidad** (P4, §4): todo hallazgo en `openspec/explore*.md`
debe tener un Issue real antes de que `opsx:propose` lo convierta en un change.
Ver `.claude/skills/openspec-triage/SKILL.md` para el proceso.

---

### 6.2 Gate 2 — TDD (tests primero, fallando)

**Cuándo**: Después de crear la rama, antes de escribir código de implementación.

**Condiciones**:

- [ ] Tests unitarios escritos (y **observados fallando**)
- [ ] Tests de integración escritos donde aplique
- [ ] Cada `#### Scenario:` del delta spec tiene al menos un test que lo cubre

**Comandos obligatorios**:

```bash
# Backend — deben FALLAR en este punto
mvn test -pl backend-api -Dtest=<NuevoTestClass>

# Frontend — deben FALLAR en este punto
cd frontend && npx vitest run <nuevo-test>
```

**Herramientas**:

| Nivel | Framework | Ubicación | Cobertura objetivo |
|-------|-----------|-----------|-------------------|
| Unit (backend) | JUnit 5 + Mockito | `src/test/java/.../unit/` | 70-85% línea |
| Integration (backend) | Testcontainers / H2 | `src/test/java/.../integration/` | 60-75% |
| Component (frontend) | Vitest + React Testing Library | `frontend/src/**/*.test.tsx` | 60-75% línea |
| API Contract | Bruno | `backend-api/api-test/` | 80-100% endpoints |
| E2E | Playwright | `frontend/tests/e2e/TS-nnnn-*.spec.ts` | 1+ por Caso de Uso |

**Convenciones**:
- Nombres: `shouldXxxYyy` con `@DisplayName` (backend); `describe/it` (frontend)
- Nomenclatura E2E: `TS-nnnn-<workflow-name>.spec.ts` con trazabilidad a CU
- DTOs: `DtoEntityName` (e.g. `DtoUsuario`)
- Assertions: AssertJ (backend); React Testing Library (frontend)

---

### 6.3 Gate 3 — PR-ready (suite verde + docs)

**Cuándo**: Antes de abrir el Pull Request. El gate completo se ejecuta con
`bash scripts/run_pipeline.sh`, que levanta el stack Docker por sí mismo.

**Condiciones** (ninguna puede fallar):

- [ ] Suite completa verde: unit + integration + regresión + E2E
- [ ] Coverage ≥ ratchet floor (JaCoCo: 70% línea / 25% branch; Vitest: 75% línea)
- [ ] Playwright verde para cambios UI
- [ ] Checkstyle sin violaciones
- [ ] Spotless formateado
- [ ] ESLint sin errores
- [ ] Documentación permanente actualizada y consistente
- [ ] `bash scripts/run_pipeline.sh` pasado (genera HTML dashboard en `reports/pipeline/<timestamp>/`)

**Comandos completos**:

```bash
# Backend: calidad completa
mvn test -pl backend-api                          # unit + integration
mvn jacoco:check -pl backend-api                  # coverage gate (ratchet floor)
mvn verify -pl backend-api                        # Checkstyle + SpotBugs + cobertura
mvn checkstyle:check -pl backend-api              # style solo
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false  # static analysis

# Frontend: calidad completa
cd frontend && npm run build                      # TypeScript compile + Next.js build
cd frontend && npx eslint src                     # ESLint
cd frontend && npx vitest run --coverage          # Vitest + coverage

# E2E (requiere stack corriendo)
cd frontend && npx playwright test                # todos los tests
cd frontend && npx playwright test --reporter=html  # con reporte HTML

# API tests
bash testing/scripts/test.sh                      # Bruno / HTTP suite

# Formato Java (Spotless — NO está en mvn verify, requiere llamada explícita)
mvn spotless:check -pl backend-api                # verificar
mvn spotless:apply -pl backend-api                # corregir

# Gate local completo (mirrors CI)
bash scripts/preflight.sh                         # todo excepto suites server-backed
bash scripts/preflight.sh --fix                   # auto-fix lo que es fixable
bash scripts/preflight.sh --fast                  # solo format/lint/compile/typecheck
bash scripts/preflight.sh --full                  # + Playwright E2E + Bruno + Docker

# Gate pre-PR con dashboard HTML
bash scripts/run_pipeline.sh                      # OBLIGATORIO antes de abrir PR
```

**CI jobs que lo bloquean** (todos deben ser verdes):

| Workflow | Jobs clave | Qué verifica |
|----------|-----------|--------------|
| `ci.yml` | build, unit-tests, integration-tests, coverage, security | Compile, JUnit, Testcontainers, JaCoCo, Trivy |
| `pr-validation.yml` | branch-naming, sdlc-plan, spotless, checkstyle, dep-analysis | Gate 1, formato, Spotless (BLOCKING) |
| `frontend-ci.yml` | typecheck, eslint, vitest, build | TypeScript, ESLint, Vitest ≥75%, Next.js build |
| `playwright-e2e.yml` | playwright, bruno-api | E2E Playwright, Bruno API suite |

**Herramientas de calidad detalladas**:

| Herramienta | Config | Ratchet/Target | Dónde se enforza |
|------------|--------|----------------|-----------------|
| JaCoCo | `pom.xml` | 70% línea / 25% branch (floor); 80%/80% (target) | `mvn verify`, `ci.yml` job `coverage` |
| Checkstyle | `backend-api/checkstyle.xml` | 0 violaciones (warn en CI) | `mvn checkstyle:check`, `pr-validation.yml` |
| SpotBugs | `backend-api/spotbugs-exclude.xml` | report-only | `ci.yml` job `code-quality` |
| Spotless | `pom.xml` (unbound del lifecycle) | 0 errores (BLOCKING) | `mvn spotless:check`, `pr-validation.yml` Code Lint |
| ESLint | `frontend/eslint.config.mjs` | 0 errores | `frontend-ci.yml` |
| Vitest | `frontend/vitest.config.ts` | 75% línea | `frontend-ci.yml` |
| Trivy | built-in | HIGH+CRITICAL reportados | `ci.yml` job `security`, `cd.yml` |
| markdownlint | ratchet vs `origin/main` | solo archivos .md del branch | `run_pipeline.sh` (local) |

> **Nota crítica**: Spotless está **deliberadamente desvinculado** de `mvn verify`
> (`backend-api/pom.xml`, ver issue #705). Un branch puede pasar `mvn verify`
> localmente y fallar "Code Lint" en CI. Por esto `scripts/preflight.sh` invoca
> Spotless explícitamente — `mvn verify` solo no predice CI.

---

### 6.4 Gate 4 — Merge (CI verde + review)

**Cuándo**: Antes del merge del Pull Request.

**Condiciones**:

- [ ] CI/CD completamente verde (todos los workflows requeridos)
- [ ] Code review aprobado (en este repo: code owner mergeando el PR = aprobación)
- [ ] Sin conflictos de merge
- [ ] Actualizaciones de documentación completas
- [ ] `traceability.md` actualizado con PR number y CI run URL

**Convenciones de PR**:

```
Título: [#<issue>] type(scope): description (≤70 chars)
Body: .github/PULL_REQUEST_TEMPLATE.md (referencia Issue + Use Case)
```

**Convenciones de commits** (Conventional Commits):

```
feat(scope): description
fix(scope): description
...
Closes #<issue-number>
```

---

### 6.5 Gate 5 — Done (deploy + smoke test + cierre)

**Cuándo**: Después del merge, antes de cerrar el Issue.

**Condiciones**:

- [ ] Deploy ejecutado (CD pipeline `cd.yml` publicó imagen a GHCR)
- [ ] Smoke test pasado en el entorno destino (endpoint health + flujo clave del cambio)
- [ ] Issue cerrado con referencia al PR
- [ ] `openspec archive <change-name>` ejecutado con `traceability.md` completa

**Comandos**:

```bash
# Verificar deploy
gh run list --workflow=cd.yml --limit=1

# Smoke test manual
curl http://localhost:8080/actuator/health
# + verificar el flujo específico del change

# Cerrar Issue
gh issue close <number> --comment "Implemented in PR #<pr>. Closes #<number>."

# Archivar el change (sync specs + traceability completa)
openspec archive <change-name>
```

---

## 7. Mapa de herramientas por etapa

```
ETAPA                 HERRAMIENTA / ARCHIVO                          GATE
─────────────────────────────────────────────────────────────────────────
Issue + Use Case      GitHub Issues; gh CLI; .github/ISSUE_TEMPLATE/  Gate 1
                      issue.md

Exploration           opsx:explore                                    (pre-Gate 1)
                      .claude/skills/openspec-explore/SKILL.md
                      .claude/commands/opsx/explore.md
                      → produce openspec/explore*.md

Triage               openspec-triage (project-owned)                  (pre-Gate 1)
                      .claude/skills/openspec-triage/SKILL.md
                      → produce openspec/*_issues.md + Issues reales

Specification         openspec new change                             Gate 1
                      openspec/schemas/notaire-sdlc/schema.yaml
                      openspec/config.yaml (context + rules)
                      openspec/changes/<name>/ (5 artifacts)

Plan validation       bash scripts/validate-sdlc-plan.sh              Gate 1
                      .github/workflows/pr-validation.yml
                      (BLOCKING: Issue live, secciones, 12 grupos)

Traceability          traceability.md por change                      P4
                      openspec archive → openspec/specs/

Branch + commits      Git; branch <type>/<issue-number>_<desc>        §9

TDD                   JUnit 5 + Mockito (unit)                        Gate 2
                      Testcontainers (integration)
                      Vitest (frontend components)
                      mvn test -pl backend-api -Dtest=<Class>

Implementation        opsx:apply                                      Gate 2→3
                      .claude/skills/openspec-apply-change/SKILL.md
                      .claude/commands/opsx/apply.md

Coverage              JaCoCo (mvn jacoco:check)                       Gate 3
                      70% línea / 25% branch (floor); 80% (target)
                      Vitest --coverage (75% línea frontend)

Code style            Checkstyle (checkstyle.xml)                     Gate 3
                      Spotless (mvn spotless:check — unbound)
                      ESLint (eslint.config.mjs)

Static analysis       SpotBugs (spotbugs-exclude.xml)                 Gate 3
                      Trivy (filesystem + Docker image)

E2E                   Playwright (frontend/tests/e2e/)                Gate 3
                      Bruno (backend-api/api-test/)
                      npx playwright test

Local preflight       bash scripts/preflight.sh [--fix/--fast/--full] Gate 3
                      (pre-push hook instalado por install-git-hooks.sh)

Pre-PR gate           bash scripts/run_pipeline.sh                    Gate 3
                      → reports/pipeline/<timestamp>/index.html

CI/CD                 ci.yml (build, test, coverage, security)        Gate 4
                      pr-validation.yml (lint, sdlc, branch naming)
                      frontend-ci.yml (ts, eslint, vitest, build)
                      playwright-e2e.yml (E2E + Bruno)
                      cd.yml (Docker → GHCR)

Security              Trivy (ci.yml security job)                     Gate 4
                      SARIF → GitHub Security tab

Agent rules           AGENTS.md, CLAUDE.md                            §10
                      .claude/rules/ai-agent-workflow.md
                      .claude/rules/code-quality.md
                      .claude/rules/general.md
                      .claude/rules/programming.md
                      .claude/rules/ui-ux-design.md
                      .claude/rules/database-migrations.md
                      .claude/rules/refactoring.md

Agent context (todos) openspec/config.yaml (context + rules)          §10
                      → inyectado por CLI en openspec instructions
                      → alcanza Claude Code, OpenCode, Copilot,
                        Codex, Cursor por el mismo canal

Deploy                cd.yml → GHCR                                   Gate 5
                      (trigger: push main o tag v* o manual dispatch)

Archive               openspec archive <name>                         Gate 5
                      .claude/skills/openspec-archive-change/SKILL.md
                      .claude/commands/opsx/archive.md
```

---

## 8. Skills y commands disponibles

### Skills vendor (generados por `openspec update`, no modificar)

| Skill | Archivo | Cuándo usarlo |
|-------|---------|---------------|
| `openspec-explore` | `.claude/skills/openspec-explore/SKILL.md` | Explorar el codebase/negocio sin scaffoldear nada |
| `openspec-propose` | `.claude/skills/openspec-propose/SKILL.md` | Scaffoldear un change desde un Issue real |
| `openspec-apply-change` | `.claude/skills/openspec-apply-change/SKILL.md` | Implementar tasks de un change |
| `openspec-archive-change` | `.claude/skills/openspec-archive-change/SKILL.md` | Archivar tras Gate 5 |
| `openspec-sync-specs` | `.claude/skills/openspec-sync-specs/SKILL.md` | Sincronizar deltas a specs principales |
| `openspec-update-change` | `.claude/skills/openspec-update-change/SKILL.md` | Actualizar artifacts de un change |

### Skill project-owned (no tocado por `openspec update`)

| Skill | Archivo | Cuándo usarlo |
|-------|---------|---------------|
| `openspec-triage` | `.claude/skills/openspec-triage/SKILL.md` | Después de `opsx:explore`, antes de `opsx:propose` |

### Commands slash (Claude Code)

```
.claude/commands/opsx/
├── explore.md     # /opsx:explore
├── propose.md     # /opsx:propose
├── apply.md       # /opsx:apply
├── update.md      # /opsx:update
├── sync.md        # /opsx:sync
└── archive.md     # /opsx:archive
```

### Entry points por agente (§10 de la Constitución)

| Agente | Entry point |
|--------|------------|
| **Claude Code** | `CLAUDE.md` + `.claude/rules/ai-agent-workflow.md` |
| **OpenCode** | `opencode.json` (carga `CLAUDE.md` y `.claude/rules/*`) |
| **GitHub Copilot** | `.github/agents/openspec.agent.md`, `.github/prompts/opsx-*` |
| **Cualquier agente** | `AGENTS.md` en la raíz; `.claude/skills/openspec-*` |
| **Cualquier agente via CLI** | `openspec instructions <artifact> --change <name>` |

### 8.1 Composición de skills para el SDLC

Las skills genéricas derivadas del TPF están instaladas en `.claude/skills/`.
Son provider-neutral y describen intención, entradas, salidas, validación y
riesgos. Las skills específicas de Notaire conservan la autoridad sobre
comandos, paquetes, convenciones y herramientas locales. No se reemplazan entre
sí: se componen por responsabilidad.

| Momento | Skill transversal | Skill Notaire complementaria | Salida esperada |
|---|---|---|---|
| Estrategia | `delivery-maturity-roadmap` | `product-owner`, `analyst` | baseline, outcome, roadmap y backlog priorizado |
| Descubrimiento | `devsecops-traceability` | `analyst`, `openspec-explore` | Issue, CU/RF/RNF, impacto y evidencia origen |
| Triage | `devsecops-traceability` | `openspec-triage` | candidatos estimados y Issues reales |
| Propuesta | `devsecops-traceability` | `openspec-propose` | proposal y ledger inicial sin evidencia inventada |
| Arquitectura | `architecture-decision-design` | `java-architect`, `plantuml`, `design-doc-mermaid` | design, ADR, C4/UML y decisiones revisables |
| Seguridad | `secure-threat-modeling` | `backend`, `devops`, `programming` | threat model, riesgo, privacidad, `SR-*` y verificadores |
| QA | `qa-automation-strategy` | `testing`, `maven-build`, `api-rest` | MTP, matriz de escenarios, fixtures, vectores y evidencias |
| API | `api-contract-testing` | `api-rest`, `backend` | OpenAPI/Swagger, contrato, compatibilidad y pruebas Bruno |
| Implementación | `devsecops-traceability` | `openspec-apply-change`, `java`, `frontend-design` | TDD observado, código, tests y ledger actualizado |
| Integración | `ci-cd-quality-gates` | `devops`, `maven-build` | gates, reportes, artifact digest/provenance y promoción |
| Operación | `operations-observability-readiness` | `devops` | SLO, telemetry, dashboards, alertas, runbook y rollback |
| Cierre | `devsecops-traceability` | `openspec-archive-change` | trazabilidad completa, docs permanentes y change archivado |

#### Secuencia operativa recomendada

```text
1. delivery-maturity-roadmap
  -> define outcome, restricciones, métricas y prioridad
2. analyst + product-owner + openspec-explore
  -> refina necesidad, CU, requisitos y escenarios
3. devsecops-traceability + openspec-triage
  -> convierte hallazgos en Issues reales vinculados a CU
4. openspec-propose + validate-sdlc-plan.sh
  -> crea y valida proposal, traceability, specs, design y tasks
5. architecture-decision-design + secure-threat-modeling
  -> decide arquitectura, modela amenazas y define SR/controles
6. qa-automation-strategy + api-contract-testing
  -> diseña pruebas, contrato, fixtures y evidencia antes del código
7. openspec-apply-change + skills de implementación locales
  -> TDD rojo, implementación mínima, refactor y ledger incremental
8. ci-cd-quality-gates
  -> preflight, suite completa, seguridad, artifact, promoción y rollback
9. operations-observability-readiness
  -> readiness, deploy verification, smoke test, SLO y soporte
10. devsecops-traceability + openspec-archive-change
  -> cierra evidencia, actualiza docs, sincroniza specs y archiva
```

Una skill se activa por intención, no por nombre de herramienta. Por ejemplo,
un cambio de endpoint activa `api-contract-testing` aunque la implementación
use REST, gRPC o AsyncAPI; la skill local `api-rest` aporta después los detalles
de Bruno y del repositorio.

### 8.2 Contratos de artefactos y evidencias

Cada change debe conservar una cadena única y verificable. OpenSpec registra el
cambio temporal; `docs/` registra conocimiento permanente; GitHub/CI registra
el estado de entrega. Ningún artifact debe copiar la Constitución ni repetir
una documentación permanente.

| Artifact | Responsable primario | Debe contener | Se valida en |
|---|---|---|---|
| Issue | Product/FA | CU/RF/RNF, alcance, AC, prioridad, owner | Gate 1 |
| `proposal.md` | Analyst/agent | objetivo, cambios, reglas, impacto, docs, out-of-scope | Gate 1 |
| `specs/**/spec.md` | Product/FA/QA | comportamiento normativo y escenarios happy/edge/error | Gate 1 |
| `design.md` | Architecture/Dev/Sec/QA/Ops | decisiones, riesgos, testing, deploy, rollback | Gate 1 |
| `traceability.md` | Change owner | Issue→CU→spec→test→code→docs→PR→deploy | Gates 1-5 |
| Test plan/cases | QA + Dev | `TC/TS-*`, fixtures, vectores, expected/evidence | Gate 2 |
| Threat model | Security + Architecture | activos, límites, STRIDE/LINDDUN, risk, `SR-*` | Gates 1-3 |
| API contract | API owner | schemas, auth/authz, errors, compatibility, examples | Gates 1-3 |
| Pipeline evidence | DevOps/CI | source, tests, scans, artifact digest, approvals | Gates 3-4 |
| Runbook/SLO | Operations | signal, threshold, action, owner, escalation, recovery | Gates 3-5 |
| Release/smoke evidence | Release owner | version, target, health, key flow, timestamp, result | Gate 5 |

#### Reglas de calidad de evidencia

1. Usar IDs estables y conservar los IDs existentes; nunca usar números de línea
  como identificadores de trazabilidad.
2. Diferenciar `planned`, `verified`, `missing` y `conflicting`; un plan no es
  evidencia de ejecución.
3. Registrar owner, fecha, entorno, herramienta/versión y ubicación de evidencia
  cuando el dato pueda cambiar o no sea reproducible.
4. Redactar secretos, tokens, datos personales, financieros, biométricos y
  registros de producción; definir retención y control de acceso.
5. Un test verde prueba el escenario ejecutado, no la ausencia de defectos; un
  scan sin hallazgos no prueba seguridad total.
6. Una excepción debe indicar aprobador, alcance, motivo, fecha de expiración y
  acción compensatoria. Nunca convertir una excepción temporal en una regla.

#### Cómo seleccionar el nivel de rigor

Aplicar el flujo completo a cambios de negocio, API, datos, seguridad,
arquitectura, infraestructura o producción. Para cambios puramente editoriales
se puede omitir la especificación funcional, pero debe quedar registrado el
motivo y mantenerse la revisión documental. Nunca omitir el análisis de impacto,
la trazabilidad ni la validación apropiada al riesgo.

#### Mantenimiento y evolución

- Las skills genéricas se actualizan en su origen y se sincronizan como una
  unidad; las adaptaciones de Notaire viven en esta guía, la Constitución y el
  catálogo `.claude/skills/README.md`.
- Antes de actualizar una skill, ejecutar sus escenarios de `evals/evals.json` y
  comparar comportamiento; mantener el conjunto de pruebas cuando se cambie el
  alcance.
- Antes de actualizar OpenSpec, preservar el fork `notaire-sdlc`, ejecutar
  `openspec validate --strict` y `bash scripts/validate-sdlc-plan.sh` sobre un
  fixture representativo.
- Cuando una herramienta cambie, actualizar el mapa de tooling y la skill local
  correspondiente; las prácticas y contratos no deben quedar atados a una sola
  herramienta.

---

## 9. Evidencia: changes que recorrieron el flujo completo

Al momento de esta revisión, `openspec/changes/archive/` tiene los siguientes
changes completados con el flujo Explore → Issue → Propose → Apply → Archive,
cada uno con `traceability.md` con SHAs reales, PR mergeado y CI pasado:

| Change | Issue | PR | Fecha |
|--------|-------|----|-------|
| `persist-metodo-pago-on-pago` | #792 | #828 | 2026-08-20 |
| `resolve-presupuesto-tramite-cardinality` | — | — | 2026-08-20 |
| `verify-debt-on-gestion-archive` | #819 | #826 | 2026-08-20 |
| `payment-financial-tracking` | #820 | #845 | 2026-08-26 |
| `escritura-post-firma-legal-cycle` | #832 | #852 | 2026-08-27 |
| `gestion-workflow-y-bitacora` | #833 | #855 | 2026-08-28 |
| `fix-inmueble-valuacion-fiscal-type` | — | — | 2026-08-31 |
| `persona-validacion-duplicados` | #835 | — | 2026-09-02 |
| `protocolo-auxiliar-tramites` | #839 | #907 | 2026-09-02 |
| `escritura-folio-picker-form` | #892 | — | 2026-09-02 |

Varios changes más están activos bajo `openspec/changes/` en cualquier momento.
`openspec/explore.md` es el registro permanente de trazabilidad hallazgo↔Issue
para el dominio de negocio — toda exploración debe resolverse en un Issue real,
sin excepciones.

---

## 10. Verificación del gate mecánico

Probado directamente contra `scripts/validate-sdlc-plan.sh` usando un fixture
temporal en `openspec/changes/` (nunca commiteado):

| Caso | Issue reference | Resultado |
|------|-----------------|-----------|
| Negativo | `#999999` (no existe) | ✗ `proposal: Issue #999999 was not found on GitHub` — Gate 1 falla |
| Positivo | `#780` (real, abierto) | ✓ `proposal: Issue #780 verified live on GitHub (OPEN)` |
| Degradado | `gh` no disponible | `- proposal: gh CLI not available/authenticated — Issue not live-verified` (advisory, no falla) |

---

## 11. Cómo adaptar OpenSpec en un proyecto nuevo

Esta sección resume, de forma genérica, los pasos que Notaire siguió. Útil como
guía para aplicar el mismo patrón en otro proyecto.

### Paso 1: Instalar y verificar

```bash
npm install -g openspec
openspec doctor
gh auth login    # para el gate de Issue live
```

### Paso 2: Inicializar el directorio openspec/

```bash
openspec init    # crea openspec/ con config.yaml y schema por defecto
```

### Paso 3: Crear el schema propio del proyecto

```bash
cp -r "$(openspec schema which spec-driven --path)" openspec/schemas/<mi-proyecto>/
# Editar openspec/schemas/<mi-proyecto>/schema.yaml:
#   name: <mi-proyecto>
#   Agregar artifacts necesarios (ej. traceability)
#   Hacer design.md obligatorio
#   Ajustar templates
```

Apuntar `openspec/config.yaml` al nuevo schema:
```yaml
schema: <mi-proyecto>
```

### Paso 4: Inyectar el proceso de gobierno en config.yaml

```yaml
context: |
  [Pointer a tu Constitución/proceso + no-negociables]

rules:
  proposal:    [...]   # secciones obligatorias, Issue real
  traceability:[...]   # no pre-llenar
  specs:       [...]   # Scenarios completos
  design:      [...]   # Testing/Deployment/Rollback
  tasks:       [...]   # grupos SDLC + DoD

operations:
  apply:    { guidance: [...] }
  archive:  { guidance: [...] }
```

### Paso 5: Crear el gate mecánico (bash)

Adaptar `scripts/validate-sdlc-plan.sh` para verificar:
- Issue reference presente en `proposal.md`
- Issue existe y está abierto (`gh issue view <number>`)
- Secciones obligatorias del proceso
- Grupos de tareas obligatorios

Integrar en:
- Git pre-push hook (`scripts/install-git-hooks.sh`)
- CI (workflow de PR validation, BLOCKING)

### Paso 6: Crear el skill de triage (project-owned)

Crear `.claude/skills/<mi-triage>/SKILL.md` con `metadata.author: <mi-proyecto>`.
El skill toma un informe de exploración y produce Issues reales antes de
que cualquier change sea scaffoldeado.

### Paso 7: Conectar con los quality gates de CI/CD existentes

Agregar en `scripts/preflight.sh` (o equivalente):
```bash
# SDLC plan validation
bash scripts/validate-sdlc-plan.sh

# ... resto de gates del proyecto (tests, lint, coverage, etc.)
```

### Principios guía (de la Constitución P10)

- **Adaptar, no reemplazar**: los skills vendor y el engine de OpenSpec se
  instalan/actualizan sin tocar; las adaptaciones viven en archivos project-owned.
- **Una única fuente de verdad**: la Constitución/proceso de gobierno está en un
  solo archivo; `config.yaml` apunta a él, nunca lo copia.
- **Agente-agnóstico**: `config.yaml` usa el extension point nativo de OpenSpec
  — funciona para Claude Code, Copilot, Codex, Cursor y cualquier herramienta
  futura que lea `openspec instructions`.
- **Gates mecánicos, no advisory**: las reglas del schema son consejos; el script
  bash es el check que realmente falla el CI.

---

## 12. Referencias

| Documento | Qué cubre |
|-----------|-----------|
| `CONSTITUTION.md` | §3 Definition of Done; §4 Conventions; §5 SDLC Workflow; §6 Quality Gates; §7 Testing; §8 Documentation; §9 Git; §10 AI Agents; §11 Release; §13 Tooling Map |
| `AGENTS.md` | Agents disponibles, workflow mandatorio, quick reference |
| `CLAUDE.md` | Entry point para Claude Code |
| `.claude/rules/ai-agent-workflow.md` | Workflow paso a paso para agentes |
| `.claude/rules/code-quality.md` | JaCoCo, Checkstyle, SpotBugs, Trivy — configuración y umbrales |
| `.claude/rules/ui-ux-design.md` | Design system, tokens, FormContainer |
| `.claude/rules/database-migrations.md` | Flyway — Convenciones, V{n}__description.sql |
| `docs/300-development/303-testing/TEST-PLAN.md` | Targets de cobertura, patrones de fixture por nivel |
| `docs/300-development/303-testing/TESTING-PATTERNS.md` | Código concreto: AAA, Test Data Builders, RTL |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Nomenclatura TS-nnnn, trazabilidad CU |
| `docs/300-development/CI-PREFLIGHT.md` | Mapeo local ↔ CI de cada gate |
| `openspec/explore.md` | Registro permanente hallazgo↔Issue del dominio de negocio |
| `openspec/schemas/notaire-sdlc/schema.yaml` | Schema completo con instructions por artifact |
| `openspec/config.yaml` | Contexto, reglas por artifact, guidance por operación |
| `scripts/validate-sdlc-plan.sh` | Gate mecánico — `--list` mapea cada check a §Constitución |
| `scripts/preflight.sh` | Gate local completo — `--list` mapea local ↔ CI job |
| `scripts/run_pipeline.sh` | Gate pre-PR con dashboard HTML |
| `.github/workflows/pr-validation.yml` | CI — SDLC validation, Spotless, branch naming |
| `.github/workflows/ci.yml` | CI — build, tests, coverage, security |
| `.github/workflows/frontend-ci.yml` | CI — TypeScript, ESLint, Vitest, Next.js |
| `.github/workflows/playwright-e2e.yml` | CI — E2E Playwright + Bruno API |
| `.github/workflows/cd.yml` | CD — Docker image → GHCR |

---

*Última revisión: 2026-09-02. Este documento es la única fuente canónica de
cómo OpenSpec fue instalado y adaptado en Notaire. Cambios a este documento
requieren PR, como cualquier otro cambio al proceso (Constitución §12).*
