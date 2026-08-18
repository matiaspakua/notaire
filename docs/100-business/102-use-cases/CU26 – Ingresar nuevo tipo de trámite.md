# CU26 – Ingresar nuevo tipo de trámite

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU26 – Ingresar nuevo tipo de trámite |
| **Actores** | Escribano |
| **Propósito** | Da de alta un nuevo tipo de trámite. |
| **Descripción** | El Escribano decide dar de alta un nuevo tipo de trámite. El sistema solicita: nombre del nuevo tipo de trámite, y demás datos, presentando además una lista de todos los documentos registrados posibles para el trámite. El Escribano indica los datos solicitados. Finalmente, guarda los cambios y queda registrado el nuevo tipo de trámite. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #56 (Ingresar nuevos trámites) |
| **GitHub ID** | #179 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide dar de alta un nuevo tipo de trámite. |  |
| 2 |  | Solicita: (Nombre del nuevo trámite; Si requiere inscripción o no; Si se archiva o no; Si se asocia con inmuebles; Observaciones; Muestra además, una lista de todos los documentos registrados posibles para el trámite.) |
| 3 | El Escribano ingresa los datos solicitados, confirmando el nuevo tipo de trámite. |  |
| 4 |  | Registra los datos ingresados, creando un nuevo tipo de trámite. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen tipos de documento para asociar al tipo de trámite. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | El trámite ingresado ya existe. | El sistema gestiona la excepción y notifica al usuario. |
