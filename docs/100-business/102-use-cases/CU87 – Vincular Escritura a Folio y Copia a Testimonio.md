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
| **GitHub ID** | _pendiente — se completa al crear el issue de implementación_ |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano completa el detalle de una escritura, seleccionando los folios que ocupa de la lista de folios disponibles (CU05, paso 5). | |
| 2 | | Registra el vínculo escritura↔folio(s) y actualiza el estado de esos folios a "Ocupado". |
| 3 | El Escribano genera un testimonio o una copia de una escritura ya firmada (CU07). | |
| 4 | | Registra el vínculo copia/testimonio↔escritura de origen. |
| 5 | El Escribano o Gestor consulta el protocolo de una escritura, un folio o un cuaderno. | Muestra la cadena completa: cuaderno → folio(s) → escritura → copias/testimonios emitidos. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 1.1 | El folio seleccionado ya está vinculado a otra escritura | El sistema rechaza la selección y notifica que el folio no está disponible. |
| 1.2 | La escritura ocupa más de un folio | El sistema permite seleccionar varios folios consecutivos y vincula todos a la misma escritura. |
| 4.1 | Se genera una copia de un testimonio ya inscripto (CU11) | El sistema conserva el vínculo original escritura→testimonio→copia, sin duplicar el registro. |
