# CU41 – Modificar Cliente

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU41 – Modificar Cliente |
| **Actores** | Escribano/Recepcionista, Cliente |
| **Propósito** | Permite modificar los datos de un Cliente. |
| **Descripción** | El Escribano/Recepcionista solicita al Cliente su nombre y apellido o tipo y número de identificación, para buscarlo en el sistema. El sistema muestra los datos pertenecientes al Cliente, y éste indica que alguno es incorrecto o ha cambiado. El Escribano/Recepcionista procede a modificar los datos incorrectos y guarda los cambios. |
| **Tipo** | Primario. |
| **Referencias Cruzadas** | RF #38 (Administrar clientes), RF #40 (Modificación de clientes); CU61 |
| **GitHub ID** | #194 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Recepcionista solicita al Cliente su nombre y apellido o tipo y número de identificación. |  |
| 2 | El cliente aporta los datos solicitados. | Busca el Cliente, asociado a los datos ingresados y muestra los datos pertenecientes al Cliente. (Nombre; Apellido; Tipo y número de identificación; Teléfono; correo electrónico; Nacionalidad; Fecha de nacimiento estado civil; Cuit/Cuil; En caso de ser casado/divorciado, número de nupcias; Sexo; Ocupación; Domicilio) |
| 3 | EL cliente indica que algunos de los datos son incorrectos o han cambiado. |  |
| 4 | El Escribano/Recepcionista procede a modificar los datos indicados y confirma los cambios. |  |
| 5 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | La persona no se encuentra cargada como Cliente. | El sistema gestiona la excepción y notifica al usuario. |
| 5.1 | Alguno de los datos son incorrectos. | El sistema gestiona la excepción y notifica al usuario. |
