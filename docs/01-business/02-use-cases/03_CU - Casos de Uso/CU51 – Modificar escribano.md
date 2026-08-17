# CU51 – Modificar escribano

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU51 – Modificar escribano |
| **Actores** | Escribano/Administrador |
| **Propósito** | Permite modificar un escribano. |
| **Descripción** | El Escribano/Administrador decide modificar los datos de un escribano, por lo tanto ingresa el nombre y apellido o tipo y número de identificación para realizar la búsqueda. El sistema muestra los datos del Escribano encontrado y el Escribano/Administrador modifica los datos necesarios, guardando los cambios realizados |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #113 (Administrar suplencias), RF #114 (Registrar suplentes del escribano); CU61 |
| **GitHub ID** | #204 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide modificar un escribano. |  |
| 2 |  | Solicita nombre y apellido o tipo y número de identificación de la persona. |
| 3 | Ingresa el nombre y apellido o tipo y número de identificación de la persona. |  |
| 4 |  | Busca la persona asociada a los datos ingresados, permitiendo modificar el número de registro del escribano. |
| 5 | Modificas el número de registro del escribano y confirma la operación. |  |
| 6 |  | Registra los cambios realizados. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | La persona no esta registrada en el sistema. | El sistema gestiona la excepción y notifica al usuario. |
