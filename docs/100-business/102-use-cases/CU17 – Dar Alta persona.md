# CU17 – Dar Alta persona

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU17 – Dar Alta persona |
| **Actores** | Recepcionista, Persona |
| **Propósito** | Permite dar de alta una nueva persona en la escribanía. |
| **Descripción** | Una Persona se acerca a la escribanía para realizar algún trámite. El Recepcionista solicita a la Persona la información correspondiente para ser dada de alta en el sistema. La persona informa los datos solicitados, y el Recepcionista la registra. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #12 (Verificar clientes), RF #38 (Administrar clientes), RF #39 (Registrar nuevos clientes); CU61 |
| **GitHub ID** | #170 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El recepcionista debe, dar de alta una persona. |  |
| 2 |  | Solicita los siguientes datos, de los cuales los 4 primero son obligatorios: (Nombre; Apellido; Tipo de identificación.; número de identificación; Teléfono; Correo electrónico de la persona.) |
| 3 | El Recepcionista solicita que la Persona indiquen los datos solicitados. |  |
| 4 | La persona aporta los datos indicados. |  |
| 5 | El Recepcionista ingresa los datos y confirma los cambios. |  |
| 6 |  | Registra los datos ingresados, dando de alta una persona. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 6.1 | Ya existe otra persona registrada con el mismo número de identificación. | El sistema rechaza el alta (HTTP 409), muestra un mensaje indicando el duplicado, ofrece un acceso directo a la persona existente y conserva los datos ingresados en el formulario. |
| 6.2 | Alguno de los datos son incorrectos. | El sistema gestiona la excepción y notifica al usuario. |
