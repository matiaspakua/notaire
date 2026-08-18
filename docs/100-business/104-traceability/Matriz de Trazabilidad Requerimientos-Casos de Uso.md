# Matriz de Trazabilidad Requerimiento <-> Caso de Uso

Generada automáticamente a partir del campo **Referencias Cruzadas** (`RF #N`) presente en
los 82 archivos `CU*.md` (`docs/100-business/102-use-cases/`) y del catálogo
`docs/100-business/101-requirements/requerimientos.csv`.

**Cobertura verificada:** los 119 requisitos del catálogo (#3-#121) están referenciados por al menos un
caso de uso, y todas las referencias `RF #N` dentro de los casos de uso apuntan a requisitos existentes
(0 huecos, 0 referencias colgantes).

> Para regenerar esta tabla tras modificar un CU o el catálogo de requisitos, volver a ejecutar el
> script de extracción descripto en `docs/300-development/` (traceability tooling) o reconstruir
> manualmente siguiendo el mismo criterio: parsear `**Referencias Cruzadas**` en cada `CU*.md`.

## Requisito -> Casos de Uso

| Requisito | Título | Casos de Uso |
|---|---|---|
| #3 | Gestionar Trámites | CU02, CU03, CU04, CU05, CU06, CU07, CU08, CU09, CU10, CU11, CU12, CU13, CU14, CU15, CU16, CU42, CU43, CU44, CU52, CU53, CU56 |
| #4 | Preparar Presupuestos | CU01, CU45, CU60 |
| #5 | Procesar solicitud de presupuestos | CU01, CU60 |
| #6 | Editar plantillas de presupuestos | CU39, CU49, CU55 |
| #7 | Imprimir presupuestos | CU01, CU45, CU60 |
| #8 | Modificar presupuestos | CU45 |
| #9 | Agregar ítems adicionales a los presupuestos | CU01, CU45, CU71 |
| #10 | Iniciar trámites | CU02 |
| #11 | Verificar presupuestos | CU02 |
| #12 | Verificar clientes | CU02, CU17, CU18 |
| #13 | Registrar inicio de gestión de trámites | CU02, CU53 |
| #14 | Administrar certificados y documentos | CU03, CU04, CU09, CU10, CU42, CU43, CU72 |
| #15 | Determinar los documentos necesarios para cada trámite | CU03 |
| #16 | Generar solicitudes de certificados y documentos | CU03 |
| #17 | Imprimir solicitudes | CU03 |
| #18 | Informar preparación de documentos | CU04, CU10, CU72 |
| #19 | Informar seguimiento de documentos | CU09, CU10, CU42, CU43, CU72 |
| #20 | Abonar trámite | CU15, CU47 |
| #21 | Registrar quién abona el trámite | CU15 |
| #22 | Abonar presupuestos en cuotas | CU15, CU47 |
| #23 | Generar e imprimir recibos de pagos | CU15 |
| #24 | Consultar estado e historial de los trámites | CU13, CU14 |
| #25 | Saber el estado de un trámite en un momento determinado | CU14 |
| #26 | Informar el historial de un trámite | CU13 |
| #27 | Generar escrituras | CU05, CU06, CU52, CU62 |
| #28 | Preparar escrituras | CU05, CU62 |
| #29 | Firmar escrituras | CU06 |
| #30 | Informar qué escritura(s) conforman un trámite | CU05, CU52, CU62 |
| #31 | Modificar escritura | CU52 |
| #32 | Administrar inscripciones | CU07, CU08, CU11, CU12, CU44, CU56, CU70 |
| #33 | Generar y registrar testimonios de escrituras | CU07, CU08, CU70 |
| #34 | Registrar testimonios inscriptos | CU11, CU44, CU56 |
| #35 | Registrar retiro de testimonio | CU12 |
| #36 | Archivar trámites | CU16 |
| #37 | Archivar trámite | CU16 |
| #38 | Administrar clientes | CU17, CU18, CU19, CU41, CU46, CU54, CU61 |
| #39 | Registrar nuevos clientes | CU17, CU18 |
| #40 | Modificación de clientes | CU41, CU54 |
| #41 | Buscar y ver detalle de clientes | CU46, CU61 |
| #42 | Buscar gestiones de cliente | CU19 |
| #43 | Administrar usuarios | CU20, CU21, CU22, CU23, CU48, CU51, CU59, CU73 |
| #44 | Crear nuevos usuarios | CU20 |
| #45 | Definir nuevos usuarios | CU20 |
| #46 | Registro de auditoría | CU23, CU73 |
| #47 | Permitir modificar datos de usuarios | CU21 |
| #48 | Generar índices | CU24 |
| #49 | Ver índices de trámites | CU24 |
| #50 | Permitir editar e imprimir los índices | CU24 |
| #51 | Generar declaraciones juradas | CU25, CU50 |
| #52 | Generar DDJJ a partir de escrituras realizadas en el mes | CU25 |
| #53 | Generar DDJJ para Rentas | CU50 |
| #54 | Imprimir declaraciones juradas | CU25, CU50 |
| #55 | Administrar tablas base | CU26, CU27, CU28, CU29, CU30, CU31, CU32, CU33, CU34, CU35, CU36, CU37, CU38, CU40, CU57, CU58, CU63, CU64, CU65, CU66, CU67, CU68 |
| #56 | Ingresar nuevos trámites | CU26, CU31, CU57, CU64 |
| #57 | Ingresar nuevos documentos | CU27, CU32, CU38, CU65 |
| #58 | Ingresar nuevos folios | CU28, CU33, CU63 |
| #59 | Ingresar nuevos estados | CU30, CU35, CU67 |
| #60 | Ingresar nuevos conceptos | CU29, CU34, CU37, CU66 |
| #61 | Administrar plantillas | CU39, CU49, CU55, CU79 |
| #62 | Plantillas de trámites | CU79 |
| #63 | Crear nuevas plantillas de trámites | CU79 |
| #64 | Modificar plantillas de trámites | CU79 |
| #65 | Eliminar plantillas de trámites | CU79 |
| #66 | Plantillas de presupuestos | CU39, CU49, CU55 |
| #67 | Crear nuevas plantillas de presupuestos | CU39 |
| #68 | Modificar plantillas de presupuestos | CU55 |
| #69 | Eliminar plantillas de presupuestos | CU49 |
| #70 | Uso de memoria RAM | CU74 |
| #71 | Uso de CPU | CU74 |
| #72 | Tiempo de respuesta | CU74 |
| #73 | Múltiples usuarios | CU74 |
| #74 | Aspecto visual | CU76 |
| #75 | Diseño de ventanas | CU76 |
| #76 | Diseño de campos y combos | CU76 |
| #77 | Especificación de campos a completar | CU76 |
| #78 | Uso de colores en la GUI | CU76 |
| #79 | Seguimiento del trabajo sobre ventanas | CU76 |
| #80 | Identificación de sesión | CU76 |
| #81 | Seguridad y privacidad | CU78 |
| #82 | Acceso de usuarios | CU20, CU21, CU78 |
| #83 | Cifrado de contraseña | CU20, CU21, CU78 |
| #84 | Transporte de información por red | CU78 |
| #85 | Acceso a la base de datos | CU75, CU78 |
| #86 | Java VM | CU75, CU76 |
| #87 | Sistema operativo | CU75, CU76 |
| #88 | PC de escritorio | CU74, CU77 |
| #89 | Notebook | CU74, CU77 |
| #90 | Metodología de desarrollo | CU76 |
| #91 | Modelo de desarrollo | CU76 |
| #92 | Lenguaje de programación | CU75, CU76 |
| #93 | Motor de base de datos | CU75 |
| #94 | Administrar folios | CU28, CU33, CU36, CU40, CU58, CU63, CU68 |
| #95 | Cargar folios del Colegio Notarial | CU28, CU63 |
| #96 | Control de numeración correlativa de folios | CU28, CU33, CU63 |
| #97 | Manejo de folios dañados (errose) | CU33 |
| #98 | Manejo de folios no usados (no pasó) | CU33 |
| #99 | Seguimiento de disponibilidad de folios | CU63 |
| #100 | Administrar cuadernos | CU80 |
| #101 | Generar cuadernos | CU80 |
| #102 | Numerar cuadernos | CU80 |
| #103 | Generar carátula de cuaderno | CU80 |
| #104 | Administrar carpetas de trámite | CU02, CU16, CU53 |
| #105 | Generar carpeta de trámite | CU02 |
| #106 | Estados de carpeta | CU02, CU16, CU53 |
| #107 | Estados y transiciones del trámite | CU30, CU35, CU67 |
| #108 | Definir estados del trámite | CU30, CU35, CU67 |
| #109 | Definir transiciones válidas de estado | CU30, CU35 |
| #110 | Registrar historial de cambios de estado | CU13, CU53 |
| #111 | Protocolo auxiliar | CU81 |
| #112 | Flujo de trámite protocolo auxiliar | CU81 |
| #113 | Administrar suplencias | CU22, CU48, CU51, CU59 |
| #114 | Registrar suplentes del escribano | CU22, CU48, CU51 |
| #115 | Asignar suplente a una gestión | CU02, CU22, CU53 |
| #116 | Historial de suplencias | CU59 |
| #117 | Generar minuta de inscripción | CU69, CU82 |
| #118 | Seguimiento de presentación para inscripción | CU11, CU44, CU56, CU82 |
| #119 | Diferencias entre protocolo principal y auxiliar | CU02, CU05, CU36, CU40, CU58, CU68, CU81 |
| #120 | Impresión de testimonios | CU07, CU08, CU70 |
| #121 | Control de numeración de escrituras | CU05, CU06 |

## Caso de Uso -> Requisitos

| Caso de Uso | Requisitos |
|---|---|
| CU01 – Preparar Presupuesto | #4, #5, #7, #9 |
| CU02 – Iniciar Gestión | #3, #10, #11, #12, #13, #104, #105, #106, #115, #119 |
| CU03 – Lista documentos y certificados necesarios | #3, #14, #15, #16, #17 |
| CU04 – Registrar documentación cliente | #3, #14, #18 |
| CU05 – Preparar escritura | #3, #27, #28, #30, #119, #121 |
| CU06 – Firmar escritura (Esta Junto a Preparar Escritura) | #3, #27, #29, #121 |
| CU07 – Generar testimonio | #3, #32, #33, #120 |
| CU08 – Verificar Testimonio | #3, #32, #33, #120 |
| CU09 – Registrar deudas documentos de Cliente | #3, #14, #19 |
| CU10 – Registrar movimientos documentación de entidades externas | #3, #14, #18, #19 |
| CU11 – Ingresar para inscripción | #3, #32, #34, #118 |
| CU12 – Retirar testimonio | #3, #32, #35 |
| CU13 – Ver historial de gestión | #3, #24, #26, #110 |
| CU14 – Consultar estado gestión | #3, #24, #25 |
| CU15 – Procesar pago | #3, #20, #21, #22, #23 |
| CU16 – Archivar Gestión | #3, #36, #37, #104, #106 |
| CU17 – Dar Alta persona | #12, #38, #39 |
| CU18 – Dar Alta Cliente | #12, #38, #39 |
| CU19 – Buscar gestiones de un Cliente | #38, #42 |
| CU20 – Dar alta usuario | #43, #44, #45, #82, #83 |
| CU21 – Modificar Usuario | #43, #47, #82, #83 |
| CU22 – Registrar Suplencia | #43, #113, #114, #115 |
| CU23 – Ver registro de actividades de usuario | #43, #46 |
| CU24 – Generar libro de índices | #48, #49, #50 |
| CU25 – Generar Declaración Jurada del mes | #51, #52, #54 |
| CU26 – Ingresar nuevo tipo de trámite | #55, #56 |
| CU27 – Ingresar nuevo tipo de documento | #55, #57 |
| CU28 – Ingresar nuevos folios | #55, #58, #94, #95, #96 |
| CU29 – Ingresar nuevo concepto | #55, #60 |
| CU30 – Ingresar nuevo estado de Gestión | #55, #59, #107, #108, #109 |
| CU31 – Modificar tipo de trámite | #55, #56 |
| CU32 – Modificar tipo de documento | #55, #57 |
| CU33 – Modificar folio | #55, #58, #94, #96, #97, #98 |
| CU34 – Modificar concepto | #55, #60 |
| CU35 – Modificar estado de Gestión | #55, #59, #107, #108, #109 |
| CU36 – Ingresar tipos de folio | #55, #94, #119 |
| CU37 – Eliminar concepto | #55, #60 |
| CU38 – Eliminar tipo de documento | #55, #57 |
| CU39 – Crear Plantilla Presupuesto | #6, #61, #66, #67 |
| CU40 – Modificar tipo de folio | #55, #94, #119 |
| CU41 - Modificar Cliente | #38, #40 |
| CU42 – Informar próximos vencimientos | #3, #14, #19 |
| CU43 – Reingresar documentación | #3, #14, #19 |
| CU44 – Reingresar testimonio | #3, #32, #34, #118 |
| CU45 – Modificar presupuesto | #4, #7, #8, #9 |
| CU46 – Ver detalle cliente | #38, #41 |
| CU47 – Consultar Pago | #20, #22 |
| CU48 – Dar alta escribano | #43, #113, #114 |
| CU49 – Eliminar Plantilla Presupuesto | #6, #61, #66, #69 |
| CU50 – Generar Declaración Jurada de Rentas | #51, #53, #54 |
| CU51 – Modificar escribano | #43, #113, #114 |
| CU52 - Modificar Escritura | #3, #27, #30, #31 |
| CU53 - Modificar Gestión | #3, #13, #104, #106, #110, #115 |
| CU54 - Modificar Persona | #38, #40 |
| CU55 – Modificar Plantilla Presupuesto | #6, #61, #66, #68 |
| CU56 – Registrar inscripcion | #3, #32, #34, #118 |
| CU57 – Eliminar tipo de trámite | #55, #56 |
| CU58 – Eliminar tipo de folio | #55, #94, #119 |
| CU59 - Consultar Suplencias | #43, #113, #116 |
| CU60 – Buscar Presupuesto | #4, #5, #7 |
| CU61 – Buscar persona o cliente | #38, #41 |
| CU62 – Buscar Escritura | #27, #28, #30 |
| CU63 – Buscar Folios | #55, #58, #94, #95, #96, #99 |
| CU64 – Buscar Tipo de tramite | #55, #56 |
| CU65 – Buscar Tipos de documentos | #55, #57 |
| CU66 – Buscar Conceptos | #55, #60 |
| CU67 – Buscar Estados de Gestión | #55, #59, #107, #108 |
| CU68 – Buscar tipos de folios. | #55, #94, #119 |
| CU69 – Gestión de Inmuebles | #117 |
| CU70 – Gestión de Copias | #32, #33, #120 |
| CU71 – Gestión de Items | #9 |
| CU72 – Gestión de Documentos Presentados | #14, #18, #19 |
| CU73 – Registro de Auditoría | #43, #46 |
| CU74 – Performance and Caching Strategy | #70, #71, #72, #73, #88, #89 |
| CU75 – Database Management and Migrations | #85, #86, #87, #92, #93 |
| CU76 – Quality Assurance and Testing Infrastructure | #74, #75, #76, #77, #78, #79, #80, #86, #87, #90, #91, #92 |
| CU77 – Operations Monitoring and Incident Management | #88, #89 |
| CU78 – Security and Compliance | #81, #82, #83, #84, #85 |
| CU79 – Administrar Plantillas de Trámite | #61, #62, #63, #64, #65 |
| CU80 – Administrar Cuadernos de Folios | #100, #101, #102, #103 |
| CU81 – Gestión de Trámites en Protocolo Auxiliar | #111, #112, #119 |
| CU82 – Generar Minuta de Inscripción | #117, #118 |
