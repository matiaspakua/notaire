# CU03 – Lista documentos y certificados necesarios

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU03 – Lista documentos y certificados necesarios |
| **Actores** | Persona/Cliente, Recepcionista/Gestor |
| **Propósito** | Genera una lista de los certificados y documentos necesarios para un trámite. |
| **Descripción** | Una Persona se acerca a la escribanía y solicita los documentos necesarios para realizar un determinado trámite. El Recepcionista consulta al sistema los documentos y/o certificados necesarios para el trámite, de una lista de trámites disponibles. El sistema presenta la información solicitada indicando: nombre, si vence o no, días de validez y quién hace entrega (Cliente/Entidad Externa)* de cada documento o certificado. Finalmente, se proporciona la información a la Persona. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #14 (Administrar certificados y documentos), RF #15 (Determinar los documentos necesarios para cada trámite), RF #16 (Generar solicitudes de certificados y documentos), RF #17 (Imprimir solicitudes) |
| **GitHub ID** | #156 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Una Persona se acerca a la escribanía y solicita los documentos necesarios para realizar un determinado trámite. |  |
| 2 | El Recepcionista solicita a la Persona que le indique qué trámite desea realizar. |  |
| 3 | La Persona indica el trámite que desea realizar. |  |
| 4 | El Recepcionista solicita una lista de trámites disponibles. |  |
| 5 |  | Busca y presenta una lista de trámites disponibles. |
| 6 | El Recepcionista selecciona un trámite de la lista. |  |
| 7 |  | Presenta la información solicitada indicando por cada documento correspondiente: (Nombre,; Si vence o no,; Días de validez; Quién hace entrega de cada documento o certificado.) |
| 8 | El Recepcionista solicita la impresión de la información obtenida. |  |
| 9 |  | Realiza la impresión de la información solicitada. |
| 10 | El Recepcionista hace entrega de la lista de documentos y/o certificados necesarios para el trámite, a la Persona. |  |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 7.1 | No se encuentra la información solicitada. | El sistema gestiona la excepción y notifica al usuario. |
