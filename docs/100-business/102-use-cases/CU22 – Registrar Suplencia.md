# CU22 – Registrar Suplencia

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU22 – Registrar Suplencia |
| **Actores** | Escribano |
| **Propósito** | Registra la licencia de un escribano. |
| **Descripción** | Un Escribano inicia una licencia, por lo tanto, solicita al sistema los escribanos habilitados para poder suplantarlo. Luego selecciona uno de los escribanos habilitados, e indica el período de la licencia a realizar. El Escribano confirma los datos indicados. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #43 (Administrar usuarios), RF #113 (Administrar suplencias), RF #114 (Registrar suplentes del escribano), RF #115 (Asignar suplente a una gestión); CU61 |
| **GitHub ID** | #175 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un Escribano inicia una licencia, por lo tanto, solicita al sistema los escribanos habilitados para poder suplantarlo. |  |
| 2 |  | Busca y presenta la lista de escribanos habilitados para realizar suplencias. |
| 3 | El Escribano selecciona a un suplente, de la lista presentada e indica el período de la licencia y algunas observaciones. |  |
| 4 |  | Se registra la nueva suplencia para el período indicado. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | No existen escribanos registrados. | El sistema gestiona la excepción y notifica al usuario. |
| 4.1 | El periodo indicado no es válido. | El sistema gestiona la excepción y notifica al usuario. |

## Efecto en la asignación de gestiones (RF #115)

Mientras la suplencia registrada está vigente (fecha de la gestión dentro
del período `fechaInicio`–`fechaFin`), toda gestión nueva o editada que se
intente asignar al escribano suplantado (CU02) se redirige automáticamente
al escribano suplente. El sistema deja constancia de la redirección en las
observaciones de la gestión y lo notifica al usuario al guardar (CU48/CU51
— el suplente debe tener registro de escribano vigente).
