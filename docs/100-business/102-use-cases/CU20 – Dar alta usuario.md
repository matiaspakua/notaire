# CU20 – Dar alta usuario

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU20 – Dar alta usuario |
| **Actores** | Escribano |
| **Propósito** | Permite dar de alta un nuevo usuario. |
| **Descripción** | El Escribano decide dar de alta un nuevo usuario, por lo tanto ingresa el nombre y apellido o tipo y número de identificación para buscar a la Persona a ser registrada como Usuario. El sistema muestra los datos de la Persona encontrada y solicita los necesarios para asignarle un usuario. El Escribano ingresa los datos solicitados, y registra el nuevo Usuario. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #44 (Crear nuevos usuarios), RF #45 (Definir nuevos usuarios), RF #82 (Acceso de usuarios), RF #83 (Cifrado de contraseña); CU61 |
| **GitHub ID** | #173 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano decide dar de alta un nuevo usuario. |  |
| 2 |  | Solicita nombre y apellido o tipo y número de identificación de la persona. |
| 3 | Ingresa el nombre y apellido o tipo y número de identificación de la persona. |  |
| 4 |  | Busca la persona asociada a los datos ingresados. Muestra los datos de la persona encontrada. |
| 5 | Confirma, que es la persona indicada. |  |
| 6 |  | Solicita nombre de usuario, contraseña, tipo de usuario y estado (habilitado / deshabilitado). |
| 7 | Ingresa los datos solicitados y confirma la operación. |  |
| 8 |  | Registra el nuevo usuario. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 5.1 | Concurrir a CU 17 Sigue paso 3 | La persona no está registrada en el sistema. |
