# CU02 – Iniciar Gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU02 – Iniciar Gestión |
| **Actores** | Recepcionista/Gestor, Cliente |
| **Propósito** | Inicia una nueva gestión en la escribanía. |
| **Descripción** | Un Cliente se acerca a la escribanía y solicita iniciar una gestión en base a uno o varios presupuestos. El Gestor/Recepcionista busca los presupuestos por su número o nombre y apellido o tipo y número de identificación del Cliente. El sistema muestra los trámites asociados a cada presupuesto. El Gestor/Recepcionista procede al inicio de una gestión, indicando fecha de inicio de la misma, el número de gestión, un detalle de encabezado y confirma los trámites a realizar. Finalmente, selecciona un escribano para dicha gestión, generando una lista de documentos, certificados necesarios para cada trámite (ver CU03), indica / selecciona un número de la nueva gestión, las observaciones adicionales, y si van a haber otros clientes involucrados en la gestión. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #3 (Gestionar Trámites), RF #10 (Iniciar trámites), RF #11 (Verificar presupuestos), RF #12 (Verificar clientes), RF #13 (Registrar inicio de gestión de trámites), RF #104 (Administrar carpetas de trámite), RF #105 (Generar carpeta de trámite), RF #106 (Estados de carpeta), RF #115 (Asignar suplente a una gestión), RF #119 (Diferencias entre protocolo principal y auxiliar); CU60 |
| **GitHub ID** | #155 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | Un Cliente se acerca a la escribanía y solicita iniciar una gestión en base a uno o varios presupuestos. |  |
| 2 | El Gestor/Recepcionista solicita el número de presupuesto o nombre y apellido o tipo y número de identificación del Cliente. |  |
| 3 | El cliente brinda los datos correspondientes. |  |
| 4 | El Gestor/Recepcionista procede a la búsqueda del presupuesto, ingresando los datos en el sistema. |  |
| 5 |  | El sistema busca el presupuesto solicitado. Se muestra una lista con la descripción del/los presupuesto, asociados al cliente. |
| 6 | El Gestor/Recepcionista, selecciona uno de los presupuestos y procede al inicio de la Gestión de dicho presupuesto. |  |
| 7 |  | Solicita al Gestor/Recepcionista fecha de inicio de la gestión, y trámites que deben ser confirmados, para la gestión. |
| 8 | El Gestor/ Recepcionista ingresa los datos solicitados. |  |
| 9 |  | Registra, el cliente, fecha de inicio de la gestión, y trámites que deben ser confirmados. |
| 10 |  | Solicita que se indique un escribano para ser asociado a la gestión y se muestra una lista de Escribanos para su selección. |
| 11 | El Gestor/Recepcionista selecciona un escribano. |  |
| 12 |  | Asocia el Escribano a la Gestión. |
| 13 |  | Solicita la confirmación final del trámite. |
| 14 | El Gestor/Recepcionista confirma la Gestión, indicando o seleccionando un número de gestión y un detalle para el encabezado. |  |
| 15 |  | Se registra la nueva Gestión. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 14.1 | El cliente indica que van a haber otros clientes involucrados en la gestión. El Gestor/Recepcionista solicita que se indiquen los nombres de los clientes involucrados para ser registrados en la gestión. El cliente brinda los datos solicitados. | Se buscan y asocian los clientes indicados a la nueva gestión. Vuelve al paso 15. |
| 5.1 | El número de presupuesto indicado no existe. | El sistema gestiona la excepción y notifica al usuario. |
| 5.2 | El presupuesto indicado ya se encuentra registrado en una gestión. | El sistema gestiona la excepción y notifica al usuario. |
| 9.1 | La persona no está dada de alta como cliente. | El sistema gestiona la excepción y notifica al usuario. |
| 15.1 | El número de gestión indicado por el usuario no es válido o ya se encuentra registrado. Se solicita ingresar un nuevo número de gestión. | El sistema gestiona la excepción y notifica al usuario. |
