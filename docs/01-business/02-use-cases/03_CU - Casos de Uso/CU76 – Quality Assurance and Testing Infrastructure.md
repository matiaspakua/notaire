# CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas (Quality Assurance and Testing Infrastructure)

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas |
| **Actores** | Equipo de QA, Equipo de Desarrollo, Pipeline CI/CD |
| **Propósito** | Proveer una infraestructura integral de pruebas automatizadas (unitarias, integración y E2E Playwright) y validación de estándares de interfaz gráfica para asegurar la calidad y estabilidad de todas las funcionalidades notariales. |
| **Descripción** | Define las prácticas de prueba, estándares visuales de formularios secuenciales y control de calidad requeridos para validar cada caso de uso y requerimiento del sistema. |
| **Tipo** | Soporte / Calidad |
| **Referencias Cruzadas** | RF #74 (Aspecto visual), RF #75 (Diseño de ventanas), RF #76 (Diseño de campos y combos), RF #77 (Especificación de campos a completar), RF #78 (Uso de colores en la GUI), RF #79 (Seguimiento del trabajo sobre ventanas), RF #80 (Identificación de sesión), RF #86 (Java VM), RF #87 (Sistema operativo), RF #90 (Metodología de desarrollo), RF #91 (Modelo de desarrollo), RF #92 (Lenguaje de programación) |
| **GitHub ID** | #276, #295, #296 |

## Alcance de Calidad e Interfaz

- Pruebas unitarias de servicios y lógica de dominio con JUnit 5 y Mockito.
- Pruebas de integración con base de datos real en contenedores (Testcontainers/PostgreSQL).
- Pruebas End-to-End (E2E) con Playwright para validar flujos de usuario completos.
- Validación de accesibilidad, navegación por teclado (Tab) y formularios secuenciales claros.
- Monitoreo continuo de cobertura de código con JaCoCo (meta ≥ 80%).

## Ciclo de Verificación de Calidad

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El desarrollador implementa pruebas unitarias e integración siguiendo TDD. | Ejecuta la suite de pruebas locales (`mvn test`, `mvn verify`). |
| 2 | Se realiza un cambio en la interfaz gráfica de usuario. | Se ejecutan las pruebas E2E con Playwright simulando la interacción en formularios. |
| 3 | El pipeline de CI/CD procesa la integración del código. | Valida Checkstyle, SpotBugs, cobertura JaCoCo (≥ 80%) y suite completa de tests. |
| 4 | El sistema valida la consistencia visual y de sesión. | Verifica que el nombre del usuario y el estado del trámite se visualicen en todo momento en pantalla. |

## Criterios de Aceptación

- [x] Cobertura de código superior al 80% verificada por JaCoCo.
- [x] Pruebas E2E de Playwright implementadas para los flujos críticos de negocio.
- [x] Interfaz gráfica validada con navegación secuencial por teclado y combos predefinidos.
- [x] Verificación de identificación permanente de sesión de usuario en pantalla.
