# Notaire Presentations

## 🎬 SDD Framework Guide

**Archivo:** `sdd-framework-guide.html`

Presentación sobre Spec-Driven Development, comparando OpenSpec vs GitHub SpecKit,
con dos demos basadas en cambios **reales y ya mergeados** en Notaire.

### 📊 Contenido (23 diapositivas)

1. Título
2. **Fundamentos de SDD** — qué es, sin SDD vs con SDD
3. **Problemas que resuelve** — alineación, trazabilidad, consistencia, context tax
4. **OpenSpec vs Spec Kit** — filosofías
5. **Tabla comparativa** — características detalladas
6-7. **Caso A — Brownfield con OpenSpec** — flujo real (`/opsx:explore → propose → apply → verify → archive`, diagrama Mermaid) + delta specs y ventajas en legacy
8-9. **Caso B — Greenfield con Spec Kit** — flujo (diagrama Mermaid) + estructura de artefactos
9.3. **Por qué SDD necesita ajustes en empresa/legacy** — Jira, GitHub, Confluence, ambientes dev, compliance
9.5. **¿Qué es Notaire?** — contexto técnico simplificado (diagrama Mermaid basado en SAD §3.2 Technical Context) antes de entrar a las demos
10-12. **DEMO 1 — OpenSpec real**: Issue #879 → PR #882 (fix) → PR #884 (archive Gate 5) — fix de `Inmueble.valuacionFiscal` (slide 12 incluye links a issue, branch, proposal/design/tasks/spec, commit, test y ambas PRs)
13-15. **DEMO 2 — Spec Kit real**: Issue #865 (CU-43) → PR #871 (feat) → PR #874 (archive Gate 5) — Reingresar documentación
15.5. **Demo 2 — Artefactos con links**: issue, branch, spec/plan/tasks/traceability, commit, test E2E y ambas PRs
16. **Cierre** — cuándo usar cada framework
17. **Adopción en equipos** — Pilotaje → Estándares → Extensión
18. **Cierre final**
19. **Anexo A — Testing con SDD** — el spec como motor de calidad verificable; diagrama Mermaid del workflow OpenSpec adaptado (Gherkin/BDD, TDD, Cucumber, contract testing con Pact/Testcontainers)
20. **Anexo B — Ciberseguridad con SDD** — el spec como cadena de control y evidencia; diagrama Mermaid del workflow OpenSpec adaptado (threat modeling STRIDE, SAST/SCA/SBOM, OWASP ZAP, policy-as-code con OPA)

### 🎯 Demos grounded en artefactos reales del repo

- **OpenSpec**: `openspec/changes/archive/2026-08-31-fix-inmueble-valuacion-fiscal-type/`
- **Spec Kit**: `speckit/specs/archive/003-cu43-reingresar-documentacion/`

Todos los números de Issue/PR y resultados de tests citados en la presentación
provienen de esos artefactos y del historial de commits, no son ejemplos inventados.

### 🎨 Características

- **Tema Notaire (Apple design tokens)** — dark/light mode:
  - Primary Blue (`#0A84FF` / `#0071E3`) — identidad Spec Kit
  - Success Green (`#34C759`) — identidad OpenSpec
  - Warning Orange (`#FF9500`) — Gates / Quality
  - Error Red (`#FF453A`) — breaking changes / riesgo
- **Diagramas Mermaid** embebidos (flujos OpenSpec y Spec Kit, contexto técnico de Notaire, y workflows OpenSpec adaptados a Testing/Ciberseguridad, renderizados en vivo)
- **Motion** con GSAP (entrada de slides, cursor glow, transiciones 200-400ms)
- **Interactividad:** ← → navegación con teclado, botones, contador de diapositivas, F pantalla completa, T cambia tema

### 🚀 Cómo Usar

```bash
open docs/presentations/sdd-framework-guide.html
```

O con servidor local (recomendado para que Mermaid/fonts carguen sin restricciones CORS):

```bash
python -m http.server 8000
# http://localhost:8000/docs/presentations/sdd-framework-guide.html
```

**Navegación:** `← →` teclado · click Anterior/Siguiente · `F` pantalla completa · `T` tema

### 🔗 Referencias

- OpenSpec demo real: `openspec/changes/archive/2026-08-31-fix-inmueble-valuacion-fiscal-type/`
- Spec Kit demo real: `speckit/specs/archive/003-cu43-reingresar-documentacion/`
- Constitución: `CONSTITUTION.md` §5 (SDLC Workflow), §6 (Quality Gates)

---

**Última actualización:** 2026-08-31 | **Branch:** `docs/redesign-sdd-presentation`
