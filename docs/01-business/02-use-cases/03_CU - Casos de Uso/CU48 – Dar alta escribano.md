# CU48 – Dar alta escribano

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU48 – Dar alta escribano |
| **Actores** | Escribano/Administrador |
| **Propósito** | Permite dar de alta un nuevo escribano. |
| **Descripción** | El Escribano/Administrador decide dar de alta un nuevo escribano, por lo tanto ingresa el nombre y apellido o tipo y número de identificación para buscar a la Persona a ser registrada como Escribano. El sistema muestra los datos de la Persona encontrada y solicita el registro del escribano. El Escribano/Administrador ingresa los datos solicitados, y registra el nuevo Escribano. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #113 (Administrar suplencias), RF #114 (Registrar suplentes del escribano); CU61 |
| **GitHub ID** | #201 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide dar de alta un nuevo escribano. |  |
| 2 |  | Solicita nombre y apellido o tipo y número de identificación de la persona. |
| 3 | Ingresa el nombre y apellido o tipo y número de identificación de la persona. |  |
| 4 |  | Busca la persona asociada a los datos ingresados. Muestra los datos de la persona encontrada. |
| 5 | Confirma, que es la persona indicada. |  |
| 6 |  | Solicita registro de escribano. |
| 7 | Ingresa los datos solicitados y confirma la operación. |  |
| 8 |  | Registra el nuevo escribano. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | La persona no esta registrada en el sistema. | El sistema gestiona la excepción y notifica al usuario. |
