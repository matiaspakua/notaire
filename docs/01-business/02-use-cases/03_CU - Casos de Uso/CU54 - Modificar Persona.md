# CU54 – Modificar Persona

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU54 – Modificar Persona |
| **Actores** | Escribano/Recepcionista, Cliente |
| **Propósito** | Permite modificar los datos de una persona. |
| **Descripción** | El Escribano/Recepcionista solicita al Cliente su nombre y apellido o tipo y número de identificación, para buscarlo en el sistema. El sistema muestra los datos pertenecientes a la persona, y éste indica que alguno es incorrecto. El Escribano/Recepcionista procede a modificar los datos incorrectos. EL Escribano/Recepcionista guarda los cambios realizados. |
| **Tipo** | Primario. |
| **Referencias Cruzadas** | RF #38 (Administrar clientes), RF #40 (Modificación de clientes); CU61 |
| **GitHub ID** | #207 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Recepcionista solicita al Cliente su nombre y apellido o tipo y número de identificación. |  |
| 2 |  | Busca la Persona, asociada a los datos ingresados y muestra los datos pertenecientes a la persona. |
| 3 | La persona indica que algunos de los datos son incorrectos o han cambiado. |  |
| 4 | El Escribano/Recepcionista procede a modificar los datos incorrectos, ya sea: (Nombre; Apellido; Tipo y número de identificación; Teléfono; Correo electrónico) y confirma los cambios. |  |
| 5 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | La persona no se encuentra cargada. | El sistema gestiona la excepción y notifica al usuario. |
| 5.1 | Alguno de los datos son incorrectos. | El sistema gestiona la excepción y notifica al usuario. |
