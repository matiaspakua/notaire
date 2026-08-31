# Notaire Presentations

## 🎬 SDD Framework Guide

**Archivo:** `sdd-framework-guide.html`

Presentación completa sobre Spec-Driven Development, comparando OpenSpec vs GitHub SpecKit con demos prácticas de ambas herramientas.

### 📊 Contenido (20 diapositivas)

1. **Fundamentos de SDD** - Definición, por qué importa con IA
2. **Problemas Resueltos** - Sin SDD vs Con SDD
3. **Frameworks Overview** - SpecKit vs OpenSpec (filosofías)
4. **Tabla Comparativa** - Características detalladas
5. **SpecKit Greenfield** - Flujo recomendado, artefactos
6. **OpenSpec Brownfield** - Flujo, ventajas en legacy
7. **DEMO OpenSpec - Propose** - Creación de delta spec
8. **DEMO OpenSpec - Apply** - Validación y gates
9. **DEMO OpenSpec - Archive** - Cierre e integración con PR
10. **DEMO SpecKit - Planning** - Spec exhaustiva e interactiva
11. **DEMO SpecKit - Implementation** - Ciclo TDD y review
12. **Conclusiones** - Cuándo usar cada herramienta
13. **Adopción en Equipos** - Pilotaje → Estándares → Extensión
14-15. **ANEXO: Spec Memory** - El faltante crítico en SDD
16. **Cierre Final** - Resumen y Q&A

### 🎨 Características

- **Tema minimalista profesional** basado en Notaire design system
- **Colores:**
  - Primary Blue (`#3b82f6`) - Acciones principales
  - Success Green (`#10b981`) - OpenSpec
  - Accent Orange (`#f59e0b`) - Highlights
  - Dark background (`#111827`) - Contraste

- **Elementos visuales:**
  - Feature boxes con bordes coloreados
  - Comparison tables con gradientes
  - Process flow diagrams
  - Metrics cards con valores destacados
  - Code blocks con sintaxis resaltada

- **Interactividad:**
  - ← → Navegación con teclado (arrow keys)
  - Botones Previous/Next
  - Contador de diapositivas
  - Transiciones suaves (0.6s)
  - Hover effects en botones

### 🚀 Cómo Usar

**Opción 1: Abrir en navegador**
```bash
open docs/presentations/sdd-framework-guide.html
```

**Opción 2: Servidor local**
```bash
python -m http.server 8000
# Luego abrir: http://localhost:8000/docs/presentations/sdd-framework-guide.html
```

**Navegación:**
- Teclado: `← →` (arrow keys)
- Ratón: Click "Anterior" / "Siguiente"
- Slide counter en esquina inferior izquierda

### 📝 Demás Presentaciones

- **openspec-flow-animation.html** - Animación del flujo OpenSpec
- **sdd-enterprise-masterclass.html** - Masterclass SDD (versión anterior)
- **sdd-enterprise-masterclass.marp.md** - Markdown source (Marp format)

### 🎓 Notas para Presentadores

**Estructura de flujo:**
1. Minutos 0-3: Introducción a SDD (diapositivas 1-3)
2. Minutos 3-8: Frameworks overview (diapositivas 4-7)
3. Minutos 8-20: DEMO OpenSpec end-to-end (diapositivas 8-9)
4. Minutos 20-28: DEMO SpecKit greenfield (diapositivas 10-11)
5. Minutos 28-35: Conclusiones y adopción (diapositivas 12-13)
6. Minutos 35-40: ANEXO Spec Memory (diapositivas 14-15)
7. Minutos 40-45: Q&A (diapositiva 16)

**Tiempo total:** ~45 minutos con Q&A

### 📱 Responsive Design

- **Desktop (1200px+):** Layout completo, 2-column sections
- **Tablet (768-1024px):** Ajustes de padding y font-size
- **Mobile (< 640px):** Single column, texto más grande

### 🔗 Referencias

- Especificación técnica: `docs/300-development/303-testing/TEST-PLAN.md`
- Análisis OpenSpec: `openspec/NOTAIRE-ADAPTATIONS.md`
- Análisis SpecKit: `speckit/NOTAIRE-ADAPTATIONS.md`
- Constitución: `CONSTITUTION.md` §7 (Testing Rules)

---

**Última actualización:** 2026-08-31 | **Branch:** `docs/redesign-sdd-presentation`
