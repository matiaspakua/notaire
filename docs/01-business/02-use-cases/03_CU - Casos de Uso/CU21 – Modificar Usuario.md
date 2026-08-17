# CU21 – Modificar Usuario

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU21 – Modificar Usuario |
| **Actores** | Escribano |
| **Propósito** | Permite modificar alguno de los datos de un Usuario. |
| **Descripción** | El Escribano decide modificar alguno de los datos de un Usuario, por lo tanto, solicita al sistema que le presente una lista de todos los Usuarios registrados. El sistema presenta la lista solicitada y el Escribano selecciona un Usuario. El sistema presenta los datos del Usuario seleccionado. El Escribano modifica alguno de los datos y guarda los cambios realizados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #47 (Permitir modificar datos de usuarios), RF #82 (Acceso de usuarios), RF #83 (Cifrado de contraseña); CU61 |
| **GitHub ID** | #174 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar alguno de los datos de un Usuario. |  |
| 2 |  | Muestra una lista de Usuarios disponibles. |
| 3 | El Escribano selecciona un Usuario de la lista. |  |
| 4 |  | Presenta los datos del Usuario seleccionado: nombre, apellido, nombre de usuario, tipo de usuario y estado. |
| 5 | El Escribano modifica alguno de los datos y guarda los cambios realizados. |  |
| 6 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 3.1 | El Usuario no existe en la lista. | El sistema gestiona la excepción y notifica al usuario. |
| 6.1 | Alguno de los datos indicados no es válido. | El sistema gestiona la excepción y notifica al usuario. |
