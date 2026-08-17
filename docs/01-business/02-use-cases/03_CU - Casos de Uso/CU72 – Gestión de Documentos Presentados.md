# CU72 – Gestión de Documentos Presentados

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU72 – Gestión de Documentos Presentados |
| **Actores** | Escribano, Gestor |
| **Propósito** | Permite registrar y realizar el seguimiento de los documentos físicos presentados por los clientes para una gestión. |
| **Descripción** | El sistema registra la recepción de documentos (DNI, Títulos, Planos, etc.) necesarios para llevar adelante una escritura o trámite, permitiendo saber en todo momento qué documentos tiene la escribanía en su poder. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #14 (Administrar certificados y documentos), RF #18 (Informar preparación de documentos), RF #19 (Informar seguimiento de documentos); CU04, CU43 |
| **GitHub ID** | #163 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor selecciona una gestión abierta. |  |
| 2 |  | El sistema muestra la lista de documentos requeridos y presentados para esa gestión. |
| 3 | El Gestor registra la recepción de un documento físico. |  |
| 4 |  | El sistema solicita la fecha de recepción y observaciones. |
| 5 | El Gestor confirma la recepción. |  |
| 6 |  | El sistema actualiza el estado del documento a "Presentado". |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| - | Flujo estándar sin desvíos | La operación se completa según el curso normal de eventos. |
