# CU69 – Gestión de Inmuebles

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU69 – Gestión de Inmuebles |
| **Actores** | Escribano, Gestor |
| **Propósito** | Permite gestionar la información de los inmuebles asociados a las escrituras y trámites. |
| **Descripción** | El sistema permite dar de alta, modificar, consultar y eliminar información técnica y catastral de inmuebles (propiedades). Esta información es fundamental para los trámites que requieren inscripción o verificación de deuda catastral. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #117 (Generar minuta de inscripción); CU02, CU11, CU56 |
| **GitHub ID** | #292 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano selecciona la opción de gestión de inmuebles. |  |
| 2 |  | El sistema muestra la interfaz de gestión de inmuebles y solicita los datos. |
| 3 | El Escribano ingresa los datos del inmueble: (Nomenclatura Catastral; Partida Inmobiliaria; Valuación Fiscal; Domicilio (Calle, Número, Localidad); Tipo de Inmueble (Urbano, Rural)) |  |
| 4 |  | El sistema valida los datos ingresados. |
| 5 | El Escribano confirma el registro. |  |
| 6 |  | El sistema guarda la información y confirma el éxito de la operación. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | El inmueble ya existe con esa nomenclatura catastral. | El sistema gestiona la excepción y notifica al usuario. |
