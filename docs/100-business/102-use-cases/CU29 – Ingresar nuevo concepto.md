# CU29 – Ingresar nuevo concepto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU29 – Ingresar nuevo concepto |
| **Actores** | Escribano |
| **Propósito** | Da de alta un nuevo concepto. |
| **Descripción** | El Escribano da de alta un nuevo concepto. El sistema solicita los datos necesarios. El Escribano ingresa los datos solicitados y decide guardar los cambios. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #55 (Administrar tablas base), RF #60 (Ingresar nuevos conceptos) |
| **GitHub ID** | #182 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide dar de alta un nuevo tipo de concepto. |  |
| 2 |  | Solicita: (Nombre del concepto; Valor; Porcentaje; Si es valor fijo o variable) |
| 3 | Ingresa los datos solicitados y confirma los cambios. |  |
| 4 |  | Registra los datos ingresados, registrando un nuevo concepto. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | El concepto ya existe. | El sistema gestiona la excepción y notifica al usuario. |
