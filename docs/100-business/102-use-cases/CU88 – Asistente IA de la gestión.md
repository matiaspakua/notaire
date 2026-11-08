# CU88 – Asistente IA de la gestión

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU88 – Asistente IA de la gestión |
| **Actores** | Escribano, Gestor (usuario), Administrador |
| **Propósito** | Ayudar al usuario a entender la gestión que tiene en pantalla y a resolver dudas de procedimiento y de reglas del sistema, con respuestas basadas en sus propios datos autorizados y generadas por un modelo de IA local, sin que los datos salgan de la instalación. |
| **Descripción** | Hoy, para saber en qué estado está una gestión y qué falta, el usuario recorre el resumen de la gestión (CU07, CU11, CU12, CU70), el workflow (CU83), los documentos, los pagos y los vencimientos. El asistente es un panel lateral en la ventana principal: el usuario pregunta en lenguaje natural, el sistema consulta esos mismos casos de uso con los permisos del usuario y responde con citas a los registros de origen. El asistente no modifica datos por su cuenta: toda acción que sugiera es una propuesta que el usuario confirma y que ejecuta el caso de uso normal. |
| **Tipo** | Secundario / Asistencia |
| **Referencias Cruzadas** | RNF-12 (seguridad y privacidad); CU02, CU07, CU11, CU12, CU70, CU77, CU78, CU83; ADR-029, ADR-030, ADR-031 |
| **GitHub ID** | #1964 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El usuario abre el asistente desde la ventana principal (botón del encabezado o `Ctrl/⌘+J`). | Muestra el panel. Si el usuario está en una gestión u otro registro, muestra el contexto ("Gestión #123 · Compraventa") y preguntas sugeridas. |
| 2 | El usuario escribe una pregunta sobre la gestión o sobre el procedimiento. | |
| 3 | | Carga el contexto de la gestión con los permisos del usuario, consulta los casos de uso necesarios (resumen, workflow, documentos, pagos, vencimientos, glosario) y genera la respuesta con el modelo local. |
| 4 | | Muestra la respuesta a medida que se genera, con citas que enlazan a las pantallas de los registros consultados, y registra la interacción en la auditoría. |
| 5 | El usuario sigue la conversación, abre una cita o detiene la respuesta. | Mantiene el historial de la conversación del usuario. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 1.1 | El asistente está deshabilitado por el Administrador | El panel no se muestra. |
| 3.1 | El modelo local no está disponible | El sistema informa "asistente no disponible"; el resto del sistema funciona normalmente. |
| 3.2 | La pregunta requiere datos que el usuario no puede ver | El sistema no los consulta y responde que el usuario no tiene permiso. |
| 3.3 | Un documento consultado contiene instrucciones dirigidas al asistente | El sistema las trata como datos y no las ejecuta. |
| 4.1 | El asistente sugiere una acción (por ejemplo, preparar una minuta de inscripción) | El sistema muestra una propuesta; solo se ejecuta si el usuario la confirma, a través del caso de uso correspondiente (CU82). |
| 5.1 | El usuario elimina una conversación | El sistema la borra; las conversaciones se eliminan también al vencer el período de retención. |

## Reglas

- El asistente solo usa datos que el mismo usuario podría ver en el sistema.
- Los datos de las gestiones no salen de la instalación: el modelo de IA corre en el equipo local o en la red privada de la escribanía.
- El asistente explica y orienta; no brinda asesoramiento legal ni reemplaza la decisión del Escribano.
