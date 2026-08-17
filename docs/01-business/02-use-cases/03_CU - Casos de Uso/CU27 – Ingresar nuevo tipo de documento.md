# CU27 – Ingresar nuevo tipo de documento

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU27 – Ingresar nuevo tipo de documento |
| **Actores** | Escribano |
| **Propósito** | Da de alta un nuevo tipo de documento. |
| **Descripción** | El Escribano decide dar de alta un nuevo tipo de documento. El sistema solicita: nombre del tipo de documento, y otros datos necesarios. El Escribano ingresa los datos solicitados y decide guardar los cambios, con lo cual queda registrado el nuevo tipo de documento. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #57 (Ingresar nuevos documentos) |
| **GitHub ID** | #180 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide dar de alta un nuevo tipo de documento. |  |
| 2 |  | Solicita: (Nombre del documento; Si posee vencimiento o no; Cantidad de días de validez; Quién debe entregar el mismo (Cliente/Entidad Externa)) |
| 3 | El Escribano indica los datos solicitados y confirma los mismos. |  |
| 4 |  | Registra un nuevo documento en el sistema con los datos indicados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | El documento ya existe. | El sistema gestiona la excepción y notifica al usuario. |
