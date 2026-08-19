# CU01 – Preparar Presupuesto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU01 – Preparar Presupuesto |
| **Actores** | Recepcionista, Persona |
| **Propósito** | Permite generar un presupuesto a una persona, en base a la solicitud de un trámite. |
| **Descripción** | Una Persona se acerca a la escribanía para realizar un presupuesto. El Recepcionista pregunta a la Persona si desea que se le genere un presupuesto, para esto le solicita el nombre, apellido, tipo y número de identificación para buscarla en el sistema. El recepcionista solicita la generación de un presupuesto para la persona, seleccionando un tipo de trámite. Finalmente, el recepcionista ingresa los datos correspondientes, y confirma el presupuesto. El presupuesto es generado con un número asociado al mismo, y la fecha actual. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #4 (Preparar Presupuestos), RF #5 (Procesar solicitud de presupuestos), RF #7 (Imprimir presupuestos), RF #9 (Agregar ítems adicionales a los presupuestos); CU61, CU64 |
| **GitHub ID** | #154 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Una persona se acerca a la escribanía para realizar un presupuesto. |  |
| 2 |  | Solicita la búsqueda de la persona. |
| 3 | El Recepcionista, solicita a la persona los siguientes datos, para buscarla en el sistema: (Nombre y Apellido, o; Tipo y número de identificación) |  |
| 4 |  | Busca la persona y muestra los datos correspondientes, y los tipos de trámites disponibles. |
| 5 | El Recepcionista solicita la generación de un presupuesto para la persona, seleccionando un tipo de trámite de una lista de trámites disponibles. |  |
| 6 |  | Busca el tipo de trámite y muestra: (Nombre del trámite; Ítem asociados [Nombre del Ítem, Valor, Porcentaje (valores variables), Observaciones (valores variables)]) Y solicita en caso de que corresponda : (Nomenclatura catastral; Valuación fiscal; Domicilio; Tipo de inmueble; Observaciones) del inmueble asociado. |
| 7 | El recepcionista ingresa los datos correspondientes, y confirma la configuración del presupuesto. |  |
| 8 |  | Calcula el total del presupuesto y lo muestra. |
| 9 | El Recepcionista ingresa observaciones del presupuesto, si corresponde, y confirma la creación del mismo. |  |
| 10 |  | Crea el nuevo presupuesto para la persona indicada con la fecha actual, e indica el número generado para el mismo. Muestra la opción de imprimir el mismo. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | La persona no existe, se debe dar de alta a la persona. | El sistema gestiona la excepción y notifica al usuario. |
| 6.1 | No existen tipos de trámite disponibles. | El sistema gestiona la excepción y notifica al usuario. |
