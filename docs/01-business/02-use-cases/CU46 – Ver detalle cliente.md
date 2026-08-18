# CU46 – Ver detalle cliente

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU46 – Ver detalle cliente |
| **Actores** | Recepcionista/Gestor |
| **Propósito** | Permite visualizar toda la información de un cliente. |
| **Descripción** | El recepcionista necesita ver la información de un determinado cliente. El sistema solicita que se indique el nombre y apellido, o tipo y número de identificación para buscar al cliente deseado. El recepcionista ingresa los datos solicitados y se presenta toda la información asociada al cliente encontrado. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #38 (Administrar clientes), RF #41 (Buscar y ver detalle de clientes); CU61 |
| **GitHub ID** | #199 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El recepcionista necesita ver la información de un determinado cliente. |  |
| 2 |  | Solicita que se indique el nombre y apellido, o tipo y número de identificación para buscar al cliente deseado. |
| 3 | El recepcionista ingresa los datos solicitados. |  |
| 4 |  | Busca y presenta la información asociada al cliente encontrado: (Nombre; Apellido; Nacionalidad; fecha de nacimiento; estado civil / número de nupcias; ocupación; domicilio; teléfono / e-mail; tipo y número de identificación) |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existe el cliente indicado. | El sistema gestiona la excepción y notifica al usuario. |
