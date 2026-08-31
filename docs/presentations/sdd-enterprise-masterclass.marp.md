---
marp: true
theme: gaia
_class: lead
paginate: true
backgroundColor: #07090e
color: #f8fafc
style: |
  section {
    font-family: 'Plus Jakarta Sans', system-ui, sans-serif;
    padding: 35px 55px;
    background-color: #07090e;
    color: #f8fafc;
  }
  h1 {
    color: #38bdf8;
    font-size: 2.3rem;
  }
  h2 {
    color: #818cf8;
    font-size: 1.6rem;
  }
  h3 {
    color: #34d399;
    font-size: 1.2rem;
  }
  a {
    color: #38bdf8;
    text-decoration: none;
    font-weight: bold;
  }
  code {
    font-family: 'JetBrains Mono', monospace;
    background: #111827;
    color: #38bdf8;
    padding: 2px 6px;
    border-radius: 4px;
  }
  pre {
    background: #080c16;
    border: 1px solid rgba(255,255,255,0.08);
    border-radius: 8px;
    padding: 12px;
  }
  footer {
    font-size: 0.75rem;
    color: #64748b;
  }
  table {
    font-size: 0.88rem;
  }
  th {
    background: #111827;
    color: #f8fafc;
  }
  td {
    background: #0e1320;
    border-bottom: 1px solid rgba(255,255,255,0.05);
  }
---

<!-- _class: lead -->
# Spec-Driven Development (SDD)

<h3 style="color:#34d399;font-size:1.2rem;margin-top:0;">Arquitectura, Gobernanza y Lecciones de Producción en Sistemas Complejos</h3>

**Matías Míguez** · *Software Engineering & Architecture*
*Notaire SDLC · 2026*

* **Repositorio:** [github.com/matiaspakua/notaire](https://github.com/matiaspakua/notaire)

---

## 01. El Ecosistema de la Charla

* **OpenSpec (`v1.8.0`):** Deltas efímeros en filesystem. Micro-PRs de 40 líneas auditables en 5 min. [`openspec/config.yaml`](https://github.com/matiaspakua/notaire/blob/main/openspec/config.yaml)
* **GitHub SpecKit (`v0.16.1`):** Dossier documental exhaustivo con clarificación socrática interactiva.
* **Gobernanza Notaire:** 10 Principios no negociables. Verificación live de tickets con `gh issue view`. [`CONSTITUTION.md`](https://github.com/matiaspakua/notaire/blob/main/CONSTITUTION.md)

*Acceso a código:* [PagoService.java](https://github.com/matiaspakua/notaire/blob/main/backend-api/src/main/java/com/licensis/notaire/service/PagoService.java) · [validate-sdlc-plan.sh](https://github.com/matiaspakua/notaire/blob/main/scripts/validate-sdlc-plan.sh)

---

## 02. ¿Qué es Spec-Driven Development?

> **Del Prompt Engineering caótico al Contrato Formal Ejecutable.**

* **El Problema del Prompt Libre:** Pedir código directamente genera soluciones inconsistentes, asunciones erróneas y código fantasma.
* **La Spec como Blueprint:** Se formaliza la intención de negocio en un contrato estructurado (objetivo, deltas, escenarios WHEN/THEN y tareas).
* **El Agente como Ejecutor Guiado:** El LLM ya no adivina el alcance: lee la spec, escribe el test que falla y produce el cambio exacto.

> **Definición Clave:** SDD es la disciplina de usar especificaciones formales y versionadas en Git como la única fuente de verdad que guía el ciclo de vida de los agentes de IA.

---

## 03. Los Tres Pilares de una Spec Real

1. **Ejecutable:** El agente consume la spec por CLI para derivar el plan técnico y los casos de prueba sin interpretación subjetiva.
2. **Verificable:** Cada requisito define escenarios `WHEN / THEN` vinculados 1:1 con métodos de test en JUnit / Playwright.
3. **Efímera en Tránsito:** El cambio vive en `changes/<id>` durante el PR y se fusiona atómicamente en `specs/` al mergear.

> **La Prueba Ácida:** "Si eliminás el archivo de especificación y el equipo sigue trabajando exactamente igual, entonces nunca fue una spec: era solo documentación decorativa."

---

## 04. Comparativa: OpenSpec vs. GitHub SpecKit

| Dimensión | OpenSpec (`v1.8.0`) | GitHub SpecKit (`v0.16.1`) |
| :--- | :--- | :--- |
| **Modelo Mental** | **Deltas en Filesystem** (`ADDED`, `MODIFIED`) | **Expediente Documental** por feature |
| **Impacto en PR** | **Micro-Deltas:** 40-60 líneas auditables en 5 min | **Dossier:** 200+ líneas de análisis y decisiones |
| **Estructura** | `changes/<id>` (efímero) ➔ `specs/` (vivo) | `specs/<id>/` carpeta estática |
| **Mejor Caso** | **Brownfield, refactoring, micro-PRs frecuentes** | **Greenfield, dominios regulados, ambigüedad alta** |

---

## 05. El Costo del Rigor: Evidencia Empírica

* **Métricas Reales (Scott Logic Benchmark):**
  * SpecKit Dossier: **2,577 líneas MD** para **689 líneas Java** (3.7x sobrecarga).
  * OpenSpec Delta: **180 líneas MD** para **650 líneas Java** (0.27x).
* **ROI Threshold:**
  * **Tareas < 2 días:** El overhead documental supera el ahorro. Usar OpenSpec o TDD iterativo.
  * **Tareas > 1 semana o Dominios Regulados:** El costo de error justifica el dossier formal.
* **Higiene de Contexto:** Dossiers masivos saturan la atención del LLM y aumentan duplicaciones.

---

## 06. El Problema Real: La Deriva Silenciosa

> **El código no miente. Todo lo demás queda desactualizado.**

```java
/** CU15 - Procesar pago: Valida que el monto no exceda el total. */
public Pago procesarPago(Integer idPresupuesto, Float monto...) {
    Float saldo = totalPresupuesto - totalPagado;
    log.info("Saldo pendiente: {}", saldo);
    // ❌ El código NUNCA valida contra 'monto'!
    return pagoRepository.save(entity);
}
```

* **Javadoc / Wiki:** Promete validación de sobrepago.
* **Código Java:** [PagoService.java](https://github.com/matiaspakua/notaire/blob/main/backend-api/src/main/java/com/licensis/notaire/service/PagoService.java#L30-L50) calcula, loguea y persiste (cobro de $800 sobre saldo de $700).
* **1,561 Tests Existentes:** 100% en verde (solo prueban happy path).

---

## 07. Cross-Check: Ajustes para SDLC Empresarial

| Dimensión | OpenSpec Vanilla (Off-the-shelf) | OpenSpec Enterprise (Notaire Tuned) |
| :--- | :--- | :--- |
| **Validación Tickets** | Acepta string libre (alucina `#9999`) | **Live Check:** [`validate-sdlc-plan.sh`](https://github.com/matiaspakua/notaire/blob/main/scripts/validate-sdlc-plan.sh) valida con `gh issue view` |
| **Inyección Reglas** | Prompts advisory en `CLAUDE.md` | **CLI Native:** Inyección en [`openspec/config.yaml`](https://github.com/matiaspakua/notaire/blob/main/openspec/config.yaml) |
| **Estructura Tareas** | Lista genérica | **12 Grupos Obligatorios:** TDD, Integration, Flyway, E2E, CI |
| **Descubrimiento** | `explore` crea cambios sin ticket | **Bridge Pattern:** `explore` ➔ `openspec-triage` ➔ `propose` |

---

## 08. El Flujo End-to-End: De la Idea al Merge

```text
[1. Explore] ───> [2. Triage] ───> [3. Propose] ───> [4. TDD/Apply] ───> [5. Preflight] ───> [6. Archive]
(0 archivos)     (Issue real)    (Delta specs)     (RED -> GREEN)      (CI & Review)      (Specs vivas)
                      │                                    │                  │                 │
                      ▼                                    ▼                  ▼                 ▼
                  [Gate 1.0]                           [Gate 1 & 2]       [Gate 3 & 4]       [Gate 5]
```

1. **Explore (OpenSpec):** Investigación segura con 0 archivos creados.
2. **Triage (Notaire):** Convierte análisis en Issue de negocio con `gh issue create`.
3. **Propose (OpenSpec):** Scaffolding del delta citando el schema `notaire-sdlc`.
4. **TDD / Apply (Gate 1 & 2):** Test visto fallar en JUnit antes de codificar la solución.
5. **Preflight (Gate 3 & 4):** `preflight.sh --fix` (Spotless + JaCoCo ratchet 80% + PR review).
6. **Archive (OpenSpec Gate 5):** Merge de delta a `openspec/specs/` y cierre del ticket.

---

## 09. La Solución Notaire: Tríada de Gobernanza

1. **Constitución Agnóstica ([`CONSTITUTION.md`](https://github.com/matiaspakua/notaire/blob/main/CONSTITUTION.md)):**
   * 10 Principios no negociables (P1: TDD obligatorio, P6: Flyway SSOT, P10: Adaptar, no reemplazar).
2. **Inyección por CLI ([`openspec/config.yaml`](https://github.com/matiaspakua/notaire/blob/main/openspec/config.yaml)):**
   * Configura el schema propietario `notaire-sdlc`. Inyecta el contexto a cualquier agente sin tocar vendor files.
3. **Puertas Mecánicas en Bash ([`validate-sdlc-plan.sh`](https://github.com/matiaspakua/notaire/blob/main/scripts/validate-sdlc-plan.sh)):**
   * Valida en vivo con `gh issue view` que el Issue existe y está `OPEN`.
   * Integrado en `scripts/preflight.sh` y en GitHub Actions (`pr-validation.yml`).

---

## 10. Los 5 Quality Gates y Preflight Local

| Quality Gate | Condición de Bloqueo (Hard Stop) | Herramienta |
| :--- | :--- | :--- |
| **Gate 1 (Plan)** | Issue abierto en GitHub + Spec + Acceptance Criteria. | [`validate-sdlc-plan.sh`](https://github.com/matiaspakua/notaire/blob/main/scripts/validate-sdlc-plan.sh) |
| **Gate 2 (TDD)** | Unit & Integration tests vistos fallar antes de codificar. | `mvn test -pl backend-api` |
| **Gate 3 (Pre-PR)** | Cobertura JaCoCo ratchet 80% + Spotless limpio. | [`preflight.sh --fix`](https://github.com/matiaspakua/notaire/blob/main/scripts/preflight.sh) |
| **Gate 4 (Merge)** | CI/CD 100% verde + Aprobación Code Owner. | `pr-validation.yml` |
| **Gate 5 (Done)** | Deploy exitoso + Smoke test verificado ➔ Cerrar Issue. | `cd.yml` + Smoke check |

---

## 11. Concurrencia y Deltas en Paralelo

1. **Aislamiento por Rama:** Cada cambio en `<type>/<issue>_<desc>` con carpeta en `openspec/changes/<name>`.
2. **Rebase Pre-Apply:** `git pull origin main --rebase` antes de `/opsx:apply` sincroniza `specs/` maestras.
3. **Fusión en Archive:** Al mergear, `/opsx:archive` fusiona secuencialmente el delta en `specs/`, evitando colisiones.

---

## 12. El Puente Explore ➔ Triage ➔ Issue ➔ Propose

Ver documentación completa en: [openspec/NOTAIRE-ADAPTATIONS.md](https://github.com/matiaspakua/notaire/blob/main/openspec/NOTAIRE-ADAPTATIONS.md)

```text
+----------------+      +-------------------+      +----------------+
|  opsx:explore  | ───> |  openspec-triage  | ───> |  opsx:propose  |
|  (Pensamiento) |      |  (Crea Real Issue)|      |  (Scaffold)    |
+----------------+      +-------------------+      +----------------+
                                                           │
                                                           ▼
                                               +------------------------+
                                               | validate-sdlc-plan.sh  |
                                               | (Gate 1 Mechanical)    |
                                               +------------------------+
```

---

## 13. SpecKit Adaptado: Misma Tríada, Otro Vendor

Segundo framework evaluado en paralelo, aislado en `speckit/` — sin tocar `openspec/`. Ver [`speckit/NOTAIRE-ADAPTATIONS.md`](https://github.com/matiaspakua/notaire/blob/main/speckit/NOTAIRE-ADAPTATIONS.md).

| Brecha detectada | Fix OpenSpec | Fix SpecKit |
| :--- | :--- | :--- |
| Sin campo Issue/Caso de Uso obligatorio | `proposal.md` (schema `notaire-sdlc`) | Header "Notaire Traceability" en `spec.md` |
| Sin verificación en vivo | `validate-sdlc-plan.sh` + `gh issue view` | `validate-speckit-plan.sh` (mismo código) |
| Plan sin secciones de release | `design.md` obligatorio | `plan.md` + 5 secciones SDLC agregadas |
| Sin ledger de trazabilidad | Artefacto `traceability` nativo del schema | `traceability-template.md` (no existía en SpecKit) |

**Asimetría real:** `openspec/config.yaml` es un punto de extensión nativo de la CLI (sobrevive a `openspec update`). `speckit/.specify/memory/constitution.md` es un archivo vendor que `specify upgrade` puede resetear — mitigación actual: reaplicar desde el historial de git.

---

## 14. Evidencia SpecKit: 3 Casos de Uso End-to-End

| Caso de Uso | Issue | PR | Estado |
| :--- | :--- | :--- | :--- |
| CU03 – Documentos necesarios por trámite | [#860](https://github.com/matiaspakua/notaire/issues/860) | [#861](https://github.com/matiaspakua/notaire/pull/861) | mergeado |
| CU10 – Movimientos doc. entidades externas | [#863](https://github.com/matiaspakua/notaire/issues/863) | [#864](https://github.com/matiaspakua/notaire/pull/864) | mergeado |
| CU43 – Reingresar documentación | [#865](https://github.com/matiaspakua/notaire/issues/865) | [#871](https://github.com/matiaspakua/notaire/pull/871) | mergeado |

* **Trampa real, no anticipada en el diseño inicial, y que se repitió tres veces:** el primer merge (CU03) cerró su Issue y rompió `validate-speckit-plan.sh` para *todo* push posterior, porque el script exigía Issues `OPEN` ([#866](https://github.com/matiaspakua/notaire/issues/866) → [PR #867](https://github.com/matiaspakua/notaire/pull/867)). El mismo fallo se repitió con CU10 porque archivar la carpeta era un paso manual, no forzado ([#868](https://github.com/matiaspakua/notaire/issues/868) → [PR #869](https://github.com/matiaspakua/notaire/pull/869)). Y volvió a pasar una tercera vez con CU43 ([#873](https://github.com/matiaspakua/notaire/issues/873)): el fix de #868 solo parchea la plantilla para *futuras* specs — la de CU43 ya existía antes del fix, así que no traía la tarea de archivado generada. Lección: un fix a nivel de plantilla no retroactiva artefactos ya en curso; el gate mecánico (el validador fallando fuerte en `main`) es lo que realmente atrapa esto, no la plantilla.
* **Bugs reales solo detectados en Playwright contra el stack real** (no en H2): columnas NOT NULL sin setear, válidas bajo `ddl-auto=create` pero no en PostgreSQL — motivó un test de regresión Postgres-only dedicado en CU43.

Comparación completa, con veredicto: [`speckit/EVALUATION.md`](https://github.com/matiaspakua/notaire/blob/main/speckit/EVALUATION.md)

---

## 15. Lecciones de Producción: PRs Recientes

1. **Mandato de Commits Atómicos ([PR #850](https://github.com/matiaspakua/notaire/pull/850)):** Un commit lógico por paso del plan (`Closes #issue`).
2. **Ciclos Multi-CU a Escala ([PR #852](https://github.com/matiaspakua/notaire/pull/852)):** Circuito post-firma abarcó **6 Casos de Uso** (CU06/07/08/11/12/44) y Flyway V17-V18 en un solo delta.
3. **Descubrimiento de Bugs de Contrato (400 vs 404):** Al formalizar escenarios para IDs inexistentes, TDD detectó errores en endpoints antes del merge.
4. **Disciplina de Archivado ([PR #854](https://github.com/matiaspakua/notaire/pull/854)):** `changes/` queda limpio tras cada PR y `openspec/specs/` se mantiene sincronizado con `main`.

---

## 16. Trampas Comunes en Adopción de SDD

1. **El Olvido de Archivar:** Mergear código sin correr `/opsx:archive` desincroniza `specs/`. Solución: Gate 5 en CI bloquea el cierre del Issue si `changes/` no está limpio.
2. **Aserciones Debilitadas:** El agente afloja el test para forzar el verde. Solución: Code review audita el delta de la spec contra los tests con AssertJ.
3. **Desfase de Spotless/Linter:** `mvn verify` pasa localmente pero CI falla en formateo. Solución: Pre-push hook ejecuta obligatoriamente [`preflight.sh --fix`](https://github.com/matiaspakua/notaire/blob/main/scripts/preflight.sh).

---

## 17. Simulador Interactivo del Ciclo SDD en Notaire

1. `/opsx:explore` ➔ Investigando CU15 en `PagoService.java`.
2. `/opsx:propose #848` ➔ Scaffolding de `proposal.md`, `design.md`, `specs/`, `tasks.md`.
3. `bash scripts/validate-sdlc-plan.sh` ➔ **Gate 1 PASSED (Issue #848 OPEN en GitHub)**.
4. `/opsx:apply` ➔ `PagoServiceTest` en rojo ➔ Código en verde (82.4% cobertura).
5. `/opsx:archive` ➔ Delta consolidado en `openspec/specs/pagos/`. Issue cerrado.

---

## 18. Demo E2E: CU15 Control de Sobrepago ([#848](https://github.com/matiaspakua/notaire/issues/848))

* **Caso de Uso:** `CU15 - Procesar Pago de Trámite`.
* **Escenario:** Presupuesto de $1,000 con $300 pagados. Saldo = $700.
* **Delta Spec ([`proposal.md`](https://github.com/matiaspakua/notaire/blob/main/openspec/changes/pago-limite-saldo-pendiente/proposal.md)):**

```markdown
## MODIFIED Requirements
### Requirement: Validación de Monto Máximo
El sistema SHALL rechazar cualquier pago cuyo monto supere el saldo pendiente.

#### Scenario: Rechazar sobrepago
- WHEN se recibe un pago con monto > saldo pendiente
- THEN el servicio arroja BusinessException(409 Conflict)
- AND ningún registro de pago es persistido
```

---

## 19. Siguientes Pasos: Memoria Compartida & Base de Conocimiento

```text
+--------------------+      +-----------------------+      +--------------------+
|  openspec/specs/   | ───> |  Vector Index & RAG   | ───> |  AI Agents & Devs  |
|  (Specs + ADRs)    |      |  (Embeddings Semánt.) |      |  (Natural Queries) |
+--------------------+      +-----------------------+      +--------------------+
                                       ▲                             │
                                       └────── Feedback Loop ────────┘
```

1. **Búsqueda Semántica:** Indexación vectorial de specs, ADRs y cambios archivados para consultar decisiones pasadas.
2. **Continuous Feedback Loop:** Los agentes reutilizan patrones consolidados y decisiones previas sin re-prompting manual.
3. **Onboarding Instantáneo:** Cualquier desarrollador o auditor consulta la base de conocimiento y obtiene la spec exacta y el commit de origen.

---

## 20. Setup e Instalación

### OpenSpec

```bash
npm install -g @fission-ai/openspec
openspec init
openspec schema fork spec-driven notaire-sdlc
```

### GitHub SpecKit

```bash
uv tool install specify-cli
mkdir -p speckit && cd speckit
specify init --here --integration claude --non-interactive --script sh
```

### Git Hooks & CI

```bash
bash scripts/install-git-hooks.sh # Instala pre-push con preflight.sh
```

Ver script: [install-git-hooks.sh](https://github.com/matiaspakua/notaire/blob/main/scripts/install-git-hooks.sh)

---

<!-- _class: lead -->
<h1 style="color:#38bdf8;font-size:2.3rem;">La spec no es documentación</h1>
<h2 style="color:#818cf8;font-size:1.6rem;">Es el test que todavía no escribiste</h2>

**Presentador:** Matías Míguez · **Repositorio:** [github.com/matiaspakua/notaire](https://github.com/matiaspakua/notaire)

¡Gracias! · *Q&A*
