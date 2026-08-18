# CU61 – Buscar persona o cliente

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU61 – Buscar persona o cliente |
| **Actores** | Gestor/Recepcionista/Escribano, Persona/Cliente |
| **Propósito** | Permite buscar una persona o cliente. |
| **Descripción** | Un usuario necesita buscar una persona o cliente, por lo tanto indica el nombre y apellido o el tipo y número de identificación de la persona que desea buscar. El sistema busca y devuelve el o las personas (ya sean “personas” o “clientes”) que concuerdan con los datos indicados. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #38 (Administrar clientes), RF #41 (Buscar y ver detalle de clientes) |
| **GitHub ID** | #214 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un usuario necesita buscar una persona o cliente. |  |
| 2 |  | Solicita que se indique alguno de los siguientes datos: (el nombre y apellido; el tipo y número de identificación) |
| 3 | El usuario indica alguno de los datos solicitados. |  |
| 4 |  | Busca y devuelve el o las personas o clientes que concuerdan con los datos indicados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | No existen personas o clientes que concuerden con los datos indicados. | El sistema gestiona la excepción y notifica al usuario. |
