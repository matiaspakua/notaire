# CU81 – Gestión de Trámites en Protocolo Auxiliar

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU81 – Gestión de Trámites en Protocolo Auxiliar |
| **Actores** | Escribano, Recepcionista/Gestor, Cliente |
| **Propósito** | Permitir la gestión integral del flujo simplificado de 5 pasos del Protocolo Auxiliar (sin generación de carpetas) para trámites como certificaciones de firmas, actas de manifestaciones, de domicilio, autorizaciones, poderes y constataciones. |
| **Descripción** | A diferencia del Protocolo Principal (que requiere apertura de carpeta física, múltiples certificados e inscripción registral), los trámites del Protocolo Auxiliar siguen un circuito ágil: solicitud del trámite, verificación/alta del cliente, redacción de escritura en folio de protocolo auxiliar, firma inmediata por las partes y el escribano, y entrega directa del testimonio. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #111 (Protocolo auxiliar), RF #112 (Flujo de trámite protocolo auxiliar), RF #119 (Diferencias entre protocolo principal y auxiliar); CU02, CU05, CU06, CU07, CU36, CU68 |
| **GitHub ID** | #312 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un cliente se presenta en la escribanía solicitando un trámite de protocolo auxiliar (ej. certificación de firma, autorización de viaje, poder especial). | Solicita los datos de identificación del cliente y el tipo de trámite auxiliar solicitado. |
| 2 | El recepcionista ingresa los datos del cliente (nombre, apellido, DNI/CUIT). | Busca al cliente o permite su registro rápido si es nuevo (CU17). |
| 3 | El Escribano inicia la confección de la escritura en Protocolo Auxiliar. | Presenta los folios de Protocolo Auxiliar disponibles y asigna el siguiente número de escritura correlativo correspondiente al Protocolo Auxiliar. |
| 4 | El Escribano redacta el cuerpo del acta/documento y lo asocia a los folios auxiliares seleccionados. | Valida la integridad del texto y la correlatividad del folio de protocolo auxiliar. |
| 5 | El cliente y el Escribano firman la escritura auxiliar en soporte papel. | El Escribano registra en el sistema que la escritura ha sido firmada por todas las partes intervinientes. |
| 6 | El Escribano solicita la generación del testimonio en hoja notarial especial. | Genera el testimonio numerado y registra la expedición del documento. |
| 7 | El Escribano hace entrega del testimonio al cliente y registra la entrega en el sistema. | Actualiza el estado del trámite a "Finalizado / Entregado" sin generar carpeta física de archivo. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | El cliente no está registrado | Se ejecuta el alta rápida de persona/cliente (CU17/CU18) y se continúa con el trámite. |
| 3.1 | No hay folios de Protocolo Auxiliar disponibles | El sistema informa la falta de folios auxiliares y requiere la carga de un nuevo lote (CU28/CU36). |
| 5.1 | La escritura no llega a firmarse | El Escribano marca los folios utilizados con la leyenda "no pasó" (CU33/RF #98), conservando la correlatividad numérica. |
