# CU87 – Vincular Escritura a Folio y Copia a Testimonio

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU87 – Vincular Escritura a Folio y Copia a Testimonio |
| **Actores** | Escribano |
| **Propósito** | Registrar el vínculo formal entre una escritura y el folio (o folios) que ocupa dentro del protocolo, y entre cada copia/testimonio y la escritura de la que proviene, para poder armar el protocolo notarial desde el sistema. |
| **Descripción** | Hoy folios (CU28, CU33, CU63), cuadernos (CU80) y escrituras (CU05, CU06) se administran cada uno por su lado; ninguna pantalla registra que "esta escritura vive en tal folio" ni que "esta copia salió de tal testimonio" (CU07). Este caso de uso cubre esa acción de ensamblaje: al preparar o firmar una escritura, el Escribano indica el/los folio(s) que ocupa (de los ya disponibles), y al generar un testimonio o una copia, el sistema registra de qué escritura y testimonio provienen. El resultado queda disponible para la carátula del cuaderno (CU80) y para cualquier consulta del protocolo. |
| **Tipo** | Primario / Protocolos |
| **Referencias Cruzadas** | RF #94 (Administrar folios), RF #96 (Control de numeración correlativa de folios), RF #121 (Control de numeración de escrituras); CU05, CU06, CU07, CU28, CU80 |
| **GitHub ID** | #838 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano, al dar de alta o editar un folio, selecciona la escritura (`estado = "Firmada"`, sin folio vinculado) que ocupa ese folio. | |
| 2 | | Registra el vínculo folio↔escritura y actualiza el estado del folio a "Utilizado". |
| 3 | El Escribano genera un testimonio o una copia de una escritura ya firmada (CU07). | |
| 4 | | Registra el vínculo copia/testimonio↔escritura de origen. |
| 5 | El Escribano o Gestor consulta el listado de folios o de escrituras. | Muestra, para cada folio, la escritura vinculada, y para cada escritura, el folio que ocupa. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 1.1 | El folio seleccionado ya está `Utilizado`, vinculado a otra escritura | El sistema rechaza la operación (409) y notifica que el folio no está disponible. |
| 1.2 | Una misma escritura ocupa más de un folio | El sistema lo permite: cada folio se vincula individualmente a la escritura por su `escrituraId`, sin límite de folios por escritura. |
| 1.3 | Se reintenta guardar un folio ya vinculado a la misma escritura (sin cambiar el vínculo) | El sistema lo permite (operación idempotente), sin exigir desvincular primero. |
| 4.1 | Se intenta generar una copia de un testimonio que ya tiene un movimiento inscripto (CU11) | El sistema rechaza el alta de la copia (409) para no emitir copias de un testimonio ya inscripto. |
