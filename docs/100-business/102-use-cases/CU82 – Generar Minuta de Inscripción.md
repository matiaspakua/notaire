# CU82 – Generar Minuta de Inscripción

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU82 – Generar Minuta de Inscripción |
| **Actores** | Escribano, Gestor |
| **Propósito** | Permitir la generación oficial del certificado "Minuta de Inscripción" para escrituras y trámites sobre bienes inmuebles, y realizar el seguimiento de su presentación, observaciones, devolución e inscripción definitiva ante el Registro de la Propiedad Inmueble. |
| **Descripción** | Cuando una escritura involucra la constitución, transmisión o modificación de derechos reales sobre inmuebles (ej. compraventa, donación, hipoteca), se debe emitir la minuta de inscripción conteniendo los datos catastrales (nomenclatura, matrícula, valuación fiscal, linderos) y los datos de las partes. El sistema permite generar este documento, imprimirlo y llevar el registro de todo el circuito registral. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #117 / RF-91 (#116 Impresión de minuta de inscripción), RF #118 (Seguimiento de presentación para inscripción); CU05, CU07, CU11, CU44, CU56, CU69 |
| **GitHub ID** | #116 (printed draft); generate/track shipped under #839 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Gestor solicita generar la minuta de inscripción para una gestión de inmuebles con escritura aprobada. | Recupera los datos de la escritura, datos de las partes (transmitente y adquirente) y datos catastrales del inmueble (CU69). |
| 2 | El Gestor verifica los datos registrales (matrícula, tomo/folio/finca, nomenclatura catastral, valuación fiscal, precio de la operación). | Valida la completitud de los campos requeridos por la autoridad registral. |
| 3 | El Gestor confirma la generación de la Minuta de Inscripción. | Genera el documento oficial de minuta de inscripción con número identificador y habilita su impresión en formulario normalizado. |
| 4 | El Gestor presenta el testimonio junto con la minuta de inscripción ante el Registro de la Propiedad. | Registra la fecha de presentación y el número de entrada registral asignado, cambiando el estado a "Presentado para inscripción". |
| 5 | El organismo registral procesa el trámite y devuelve el testimonio inscripto. | El Gestor registra la fecha de recepción, número de inscripción definitivo y observaciones del registro (CU56). |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | Faltan datos catastrales o certificados previos requeridos | El sistema alerta sobre los campos faltantes (ej. certificado de dominio o inhibición) e impide generar la minuta hasta completar la información. |
| 5.1 | El organismo registral formula observaciones o rechaza provisoriamente la inscripción | El Gestor registra las observaciones formuladas por el registro, cambia el estado a "Observado" y programa la fecha de subsanación y reingreso del testimonio (CU44). |

## Printed draft (RF-91 / #116)

`GET /api/v1/reports/registration-draft/{idRegistrationDraft}` returns a PDF
for an existing draft (unknown id is 404). The PDF shows recorded fields:
draft number, status, generation date, operation price, deed number/date,
parties, cadastral designation, registration number, volume/folio, boundaries,
fiscal appraisal, address, and registry notes. Missing values are `N/A`.
Cadastral completeness is enforced when generating the draft
(`POST /api/v1/registration-drafts`), not again on print. The system does
not invent official-form geometry, a signature block, or grantor/grantee
role labels. Filename: `minuta_inscripcion_{id}.pdf`.

