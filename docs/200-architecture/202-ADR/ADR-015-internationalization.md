# ADR-015: Internacionalización del Frontend con next-intl

**Status:** Accepted
**Date:** 2026-05-23
**Deciders:** Matías Miguez
**Related:** ADR-005 (Modern Frontend Migration), ADR-011 (Centralized Design System)

## Context

Notaire es un sistema para escribanías argentinas, con contenido, formularios y mensajes en español. El plan de producto contempla que el sistema pueda ofrecerse en otras jurisdicciones de habla no hispana, y en cualquier caso las cadenas de texto de la interfaz estaban hardcodeadas en los componentes React, mezclando presentación con contenido y dificultando cualquier cambio de copy sin tocar código.

## Decision

Adoptar **next-intl** como librería de internacionalización del frontend Next.js 16 (App Router), con:

- **Catálogos de mensajes** en `frontend/messages/{locale}.json` — actualmente `es.json` (locale por defecto) y `en.json`.
- **Resolución de locale** centralizada en `frontend/src/i18n/request.ts`, integrada con el App Router de Next.js (`next-intl` provee el plugin de configuración server-side).
- **Server actions** de cambio de idioma en `frontend/src/i18n/actions.ts`.
- Cobertura de test dedicada (`frontend/src/tests/unit/i18n.test.ts`) que verifica que ambos catálogos existen y están sincronizados en claves.

### Por qué next-intl (y no react-i18next, ni next-i18next)

| Criterio | next-intl (elegido) | react-i18next | next-i18next |
|----------|----------------------|----------------|----------------|
| Soporte App Router (Next.js 13+) | Nativo, diseñado para Server Components | Requiere wrappers manuales para RSC | Diseñado para Pages Router, soporte limitado en App Router |
| Tipado TypeScript de claves de mensaje | Sí, vía inferencia de tipos sobre el JSON | Parcial | Parcial |
| Tamaño / dependencias | Ligera, sin dependencia de `i18next` como runtime | Requiere el ecosistema completo de `i18next` | Requiere `i18next` + `react-i18next` |
| Mantenimiento activo para Next.js 16 | Sí | Sí (agnóstico de framework) | Menor — próximo a stale para App Router |

## Consequences

### Positivos

- Los textos de la interfaz están centralizados en `frontend/messages/*.json`, separados del código de los componentes — alineado con el principio de "no hardcodear valores" de `.claude/rules/ui-ux-design.md`.
- Server Components pueden resolver mensajes sin JavaScript adicional en el cliente, preservando el presupuesto de rendimiento de `<2s` interactivo establecido en el SAD.
- El test `i18n.test.ts` previene que un catálogo quede desincronizado (clave presente en `es.json` pero ausente en `en.json`), evitando textos en blanco en producción.

### Negativos

- Todo nuevo componente debe usar las claves de mensaje en lugar de strings literales — requiere disciplina de code review; no hay lint automatizado que lo fuerce todavía.
- Solo `es` (por defecto) y `en` están cubiertos; añadir una jurisdicción con otro idioma requiere un catálogo completo antes de habilitarla.

## Related ADRs

- ADR-005: Modern Frontend Migration (Next.js)
- ADR-011: Centralized Design System
