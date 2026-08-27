# CU83 – Definir Workflow de Estados y Transiciones

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU83 – Definir Workflow de Estados y Transiciones |
| **Actores** | Administrador, Escribano |
| **Propósito** | Permite definir y editar, sin necesidad de despliegue de código, el flujo de estados (grafo dirigido de nodos y transiciones) que sigue una gestión según su tipo de trámite. |
| **Descripción** | El sistema modela el ciclo de vida de una gestión como datos versionados: una `WorkflowDefinition` asociable a uno o más `TipoDeTramite`, compuesta de `WorkflowNode` (cada uno vinculado a un `EstadoDeGestion` existente, tipado `INITIAL`/`INTERMEDIATE`/`FINAL`) y `WorkflowTransition` (aristas dirigidas entre nodos, con condición opcional). Permite crear, editar y eliminar definiciones, nodos y transiciones vía API REST, validar la consistencia de un workflow (por ejemplo, que exista un nodo `FINAL` alcanzable) y consultar la traza de ejecución de una gestión sobre su workflow asignado. Estas transiciones ya no son de solo lectura: el Gestor/Escribano puede aplicarlas sobre una gestión real desde la pantalla de gestiones (acción "Cambiar estado"), y el sistema valida cada cambio de estado — incluido el archivado (CU16) — contra el grafo definido aquí antes de aplicarlo, registrando el resultado en la bitácora (CU13). Ver [ADR-014](../../200-architecture/202-ADR/ADR-014-workflow-engine.md) para la decisión de arquitectura y [FRONTEND-WORKFLOW-TRACKER.md](../../200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md) para el visualizador. |
| **Tipo** | Secundario / Administración |
| **Referencias Cruzadas** | RF #46 (Registro de auditoría, vía WorkflowTraceService); CU13, CU16, CU30, CU35, CU67 |
| **GitHub ID** | #451, #453, #454, #455, #833 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Administrador crea una nueva `WorkflowDefinition` y la asocia a uno o más `TipoDeTramite`. |  |
| 2 |  | El sistema registra la definición y la habilita para edición del grafo. |
| 3 | El Administrador agrega `WorkflowNode`s, cada uno vinculado a un `EstadoDeGestion` existente, marcando el nodo inicial y el/los nodo(s) final(es). |  |
| 4 | El Administrador agrega `WorkflowTransition`s dirigidas entre nodos, opcionalmente con una condición y descripción. |  |
| 5 | El Administrador solicita validar el workflow. |  |
| 6 |  | El sistema verifica la consistencia del grafo (nodo inicial único, nodo `FINAL` alcanzable desde todo nodo) vía `WorkflowValidationService` y reporta errores si los hay. |
| 7 |  | El sistema persiste la definición validada, disponible para su uso por las gestiones del tipo de trámite asociado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 6a | El grafo no tiene un nodo `FINAL` alcanzable desde algún nodo | El sistema rechaza la validación e indica el/los nodo(s) sin salida válida. |
| 6b | Se intenta crear una transición hacia un nodo inexistente o de otra `WorkflowDefinition` | El sistema rechaza la operación con un error de validación. |
