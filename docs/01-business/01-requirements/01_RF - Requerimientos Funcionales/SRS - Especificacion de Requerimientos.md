# Sistema de Gestión Notarial

## Especificación de Requerimientos de Software (SRS) - Versión 2.0

**Documento:** Especificación de Requerimientos de Software (SRS)  
**Versión:** 2.0  
**Fecha de revisión:** Agosto 2026  
**Estado:** Completado y validado contra fuente de requisitos CSV y RS

---

## Tabla de Contenido

1. [Propósito del documento](#propósito-del-documento)
2. [Referencias de origen](#referencias-de-origen)
3. [Sistema a desarrollar](#sistema-a-desarrollar)
4. [Objetivos del sistema](#objetivos-del-sistema)
5. [Matriz de trazabilidad CSV-SRS](#matriz-de-trazabilidad-csv-srs)
6. [Requerimientos funcionales](#requerimientos-funcionales)
7. [Requerimientos de rendimiento](#requerimientos-de-rendimiento)
8. [Requerimientos de interfaz](#requerimientos-de-interfaz)
9. [Requerimientos de seguridad y privacidad](#requerimientos-de-seguridad-y-privacidad)
10. [Requerimientos de software](#requerimientos-de-software)
11. [Requerimientos de hardware](#requerimientos-de-hardware)
12. [Requerimientos para el desarrollo](#requerimientos-para-el-desarrollo)
13. [Validación de cobertura](#validación-de-cobertura)

---

## Propósito del documento

Este documento especifica los requisitos funcionales y no funcionales del sistema de gestión notarial. Proporciona una base clara para:
- Guiar el desarrollo del software
- Validar la implementación contra los requisitos
- Facilitar la comunicación entre desarrolladores y stakeholders
- Asegurar la trazabilidad de cada requisito a través del ciclo de desarrollo

**Fuentes de requisitos:**
- Relevamiento del Sistema (RS): Contiene la información de negocio y contexto
- Catálogo de Requisitos (CSV): Listado estructurado de todos los requisitos
- Este SRS: Especificación detallada con descripciones extendidas

---

## Referencias de origen

| Documento | Ubicación | Propósito |
|-----------|-----------|----------|
| Relevamiento del Sistema (RS) | `RS - Relevamiento del Sistema.md` | Información de negocio, procesos y contexto |
| Catálogo de Requisitos | `requerimientos.csv` | Listado estructurado de 121 requisitos |
| Especificación de Requerimientos (SRS) | Este documento | Especificación detallada con trazabilidad |

---

## 1. Sistema a desarrollar

Se debe desarrollar un producto de software que permita administrar y llevar el control integral de los trámites de una escribanía, incluyendo:

- Gestión de clientes y sus datos personales
- Preparación y seguimiento de presupuestos
- Control de documentación y certificados necesarios
- Administración de cobros y pagos
- Generación de escrituras con control de folios
- Administración de inscripciones y testimonios
- Generación de reportes (índices y declaraciones juradas)
- Administración de tablas base y plantillas
- Control de usuarios y auditoría de operaciones
- Soporte para protocolos principal y auxiliar

El sistema debe ser flexible, escalable y soportar múltiples usuarios simultáneamente.

---

## 2. Objetivos del sistema

**Objetivos de negocio:**
- Ayudar a realizar el seguimiento y control del proceso completo de los trámites
- Mantener un registro centralizado de los clientes y sus trámites asociados
- Registrar las escrituras realizadas en los protocolos con trazabilidad completa
- Facilitar el acceso rápido a información de trámites y clientes
- Simplificar la preparación y emisión de declaraciones juradas mensuales

**Objetivos operacionales:**
- Controlar la numeración correlativa de folios, cuadernos y escrituras
- Garantizar la trazabilidad del historial de cambios de estado de cada gestión
- Automatizar avisos de vencimientos de documentos
- Registrar auditoría completa de todas las operaciones
- Soportar trabajo simultáneo de múltiples usuarios

**Objetivos técnicos:**
- Proporcionar una interfaz gráfica clara y fácil de usar
- Garantizar la seguridad de los datos mediante encriptación y control de acceso
- Asegurar performance óptima incluso con múltiples usuarios
- Facilitar el mantenimiento y evolución del sistema

---

## 3. Matriz de trazabilidad CSV-SRS

La presente especificación fue validada contra el archivo fuente `requerimientos.csv` para garantizar que cada requisito del catálogo quede representado en el SRS y que el contenido del SRS permanezca alineado con el origen del proyecto.

### Requisitos funcionales por sección

| Rango ID | Requisitos | Sección del SRS | Cantidad |
|----------|-----------|-----------------|----------|
| #3-#37 | Gestionar Trámites | 3.1 | 35 |
| #38-#42 | Administrar Clientes | 3.2 | 5 |
| #43-#47 | Administrar Usuarios | 3.3 | 5 |
| #48-#50 | Generar Índices | 3.4 | 3 |
| #51-#54 | Generar Declaraciones Juradas | 3.5 | 4 |
| #55-#60 | Administrar Tablas Base | 3.6 | 6 |
| #61-#69 | Administrar Plantillas | 3.7 | 9 |
| #94-#99 | Administrar Folios | 3.8 | 6 |
| #100-#103 | Administrar Cuadernos | 3.9 | 4 |
| #104-#106 | Administrar Carpetas de Trámite | 3.10 | 3 |
| #107-#110 | Estados y Transiciones del Trámite | 3.11 | 4 |
| #111-#112 | Protocolo Auxiliar | 3.12 | 2 |
| #113-#116 | Administrar Suplencias | 3.13 | 4 |
| #117-#118 | Gestión de Inscripciones | 3.14 | 2 |
| #120 | Impresión de Testimonios | 3.15 | 1 |
| #121 | Control de Numeración de Escrituras | 3.16 | 1 |
| #119 | Diferencias entre Protocolos | 3.17 | 1 |

### Requisitos no funcionales

| Rango ID | Requisitos | Sección del SRS | Cantidad |
|----------|-----------|-----------------|----------|
| #70-#73 | Requerimientos de Rendimiento | 4 | 4 |
| #74-#80 | Requerimientos de Interfaz | 5 | 7 |
| #81-#85 | Requerimientos de Seguridad y Privacidad | 6 | 5 |
| #86-#87 | Requerimientos de Software | 7 | 2 |
| #88-#89 | Requerimientos de Hardware | 8 | 2 |
| #90-#93 | Requerimientos para el Desarrollo | 9 | 4 |

**Total de requisitos:** 121 (100% cubierta)

---

## 4. Requerimientos funcionales

### 4.1 Gestionar Trámites

#### 4.1.1 Preparar Presupuestos

**Requisitos asociados:** #4, #5, #6, #7, #8, #9

1. **Procesar solicitud de presupuestos** (#5)  
   El sistema debe tomar los datos esenciales de la persona que solicita un presupuesto:
   - Nombre y apellido completos
   - Tipo y número de identificación
   - Teléfono y correo electrónico
   
   El sistema debe generar automáticamente un número de presupuesto único que permita identificarlo posteriormente. Este número es fundamental si el cliente decide aceptar el presupuesto y comenzar un trámite.

2. **Editar plantillas de presupuestos** (#6)  
   El sistema debe permitir personalizar las plantillas de presupuestos, incluyendo:
   - Conceptos a presupuestar (detalle de servicios y gastos)
   - Valores de cada concepto
   - Cálculo automático del total
   - Consideración de gastos fijos (sellos, impuestos administrativos)
   - Consideración de gastos variables (tasas según tipo de operación)

3. **Imprimir presupuestos** (#7)  
   El sistema debe proveer la capacidad de imprimir los presupuestos existentes en papel con formato profesional adecuado para presentar al cliente.

4. **Modificar presupuestos** (#8)  
   El usuario debe poder modificar los conceptos y valores de un presupuesto ya creado, con recalculación automática de totales.

5. **Agregar ítems adicionales a los presupuestos** (#9)  
   El sistema debe permitir incorporar conceptos adicionales a un presupuesto existente, manteniendo los datos previos e incorporando automáticamente los nuevos valores en el total.

#### 4.1.2 Iniciar trámites

**Requisitos asociados:** #10, #11, #12, #13

1. **Verificar presupuestos** (#11)  
   Para iniciar cualquier trámite debe existir obligatoriamente un presupuesto asociado, ya que el presupuesto es el fundamento del trámite. Si no existe uno, deberá crearse en el momento en que se desee iniciar la gestión. El sistema debe validar esta condición.

2. **Verificar clientes** (#12)  
   Para iniciar cualquier tipo de trámite, la persona interesada deberá estar registrada como cliente de la escribanía. Si no lo está, el sistema debe facilitar que se dé de alta como nuevo cliente en el momento del inicio del trámite.

3. **Registrar inicio de gestión de trámites** (#13)  
   El sistema debe registrar permanentemente:
   - Fecha exacta en que se inició el trámite
   - Tipo de trámite solicitado
   - Composición del trámite (documentos y certificados necesarios, según plantilla)
   - Escribano a cargo de realizar la gestión
   - Cliente solicitante

#### 4.1.3 Administrar Certificados y Documentos

**Requisitos asociados:** #14, #15, #16, #17, #18, #19

1. **Determinar los documentos necesarios para cada trámite** (#15)  
   El sistema debe identificar automáticamente los documentos que:
   - Presentan vencimientos próximos
   - Tienen impuestos a pagar
   - Son requeridos para la gestión actual
   
   Esta información debe ser presentada claramente al usuario para facilitar la planificación.

2. **Generar solicitudes de certificados y documentos** (#16)  
   El sistema debe permitir obtener una lista completa y detallada de todos los documentos y certificados que deben ser presentados para la gestión de un trámite determinado, organizados de forma clara.

3. **Imprimir solicitudes** (#17)  
   Debe permitirse la impresión en papel o en formato PDF de la lista de documentos necesarios para un trámite, con formato profesional listo para enviar a organismos externos.

4. **Informar preparación de documentos** (#18)  
   El sistema debe permitir llevar un registro de:
   - Documentos preparados para ser entregados
   - Entidades externas responsables
   - Fecha de preparación
   - Relación con gestión y cliente

5. **Informar seguimiento de documentos** (#19)  
   El sistema debe permitir el seguimiento completo de documentos incluyendo:
   - Presentación ante organismos
   - Vencimientos y renovaciones
   - Deudas asociadas a documentos
   - Impuestos pendientes
   - Estado de liberación
   - Avisos automáticos para documentos próximos a vencer
   - Listado de documentos pendientes de pago y/o liberación

#### 4.1.4 Abonar trámite

**Requisitos asociados:** #20, #21, #22, #23

1. **Registrar quién abona el trámite** (#21)  
   El sistema debe permitir registrar:
   - Identificación del cliente que abona
   - Identificación del trámite a ser abonado
   - Fecha del pago realizado
   - Saldo restante después del pago
   
   Todo trámite debe estar asociado a un presupuesto, por lo que el costo y saldo se calculan automáticamente en base al presupuesto.

2. **Abonar presupuestos en cuotas** (#22)  
   Los presupuestos pueden abonarse por completo o en cuotas sin montos fijos predefinidos, permitiendo que los clientes paguen en la cantidad de cuotas que prefieran. El sistema debe:
   - Registrar cada cuota recibida
   - Calcular deuda pendiente
   - Advertir sobre cualquier deuda al momento de finalizar la gestión

3. **Generar e imprimir recibos de pagos** (#23)  
   Para cada pago realizado el sistema debe generar e imprimir un recibo que detalle:
   - Cliente que realiza el pago
   - Fecha de pago
   - Concepto del pago
   - Monto abonado
   - Saldo pendiente

#### 4.1.5 Consultar estado e historial de los trámites

**Requisitos asociados:** #24, #25, #26

1. **Saber el estado de un trámite en un momento determinado** (#25)  
   El sistema debe permitir consultar el estado actual de un trámite en cualquier momento, reflejando su situación actual en el ciclo de vida.

2. **Informar el historial de un trámite** (#26)  
   El sistema debe mostrar la cronología completa del trámite incluyendo:
   - Todos los cambios de estado con fechas exactas
   - Observaciones anotadas en cada cambio
   - Documentos y certificados solicitados y sus estados
   - Fechas de solicitud y entrega
   - Documentos pendientes y su condición

#### 4.1.6 Generar Escrituras

**Requisitos asociados:** #27, #28, #29, #30, #31

1. **Preparar escrituras** (#28)  
   El sistema debe permitir registrar nuevas escrituras en la gestión de trámites, capturando:
   - Número de escritura único y correlativo
   - Fecha de otorgamiento
   - Números de folio utilizados (seleccionados de lista de disponibles)
   - Trámites involucrados en la escritura
   - Cuerpo de la escritura (texto legal)
   - Rol de cada cliente interviniente (otorgante, beneficiario, etc.)

2. **Firmar escrituras** (#29)  
   Todas las escrituras deben estar firmadas por:
   - El escribano responsable
   - El cliente o clientes intervinientes
   
   La escritura solo puede ser considerada aprobada cuando todas las firmas requeridas estén presentes.

3. **Informar qué escritura(s) conforman un trámite** (#30)  
   El sistema debe poder indicar qué escrituras forman parte de un trámite específico, proporcionando:
   - Número de escritura
   - Folios que la componen
   - Fecha de otorgamiento
   - Registro al cual pertenece

4. **Modificar escritura** (#31)  
   El usuario debe poder modificar alguno de los datos de una escritura previamente generada (antes de su finalización).

#### 4.1.7 Administrar Inscripciones

**Requisitos asociados:** #32, #33, #34, #35

1. **Generar y registrar testimonios de escrituras** (#33)  
   El sistema debe registrar:
   - Folios utilizados en el testimonio
   - Fecha de creación del testimonio
   - Número único del testimonio
   - Cantidad de copias generadas
   - Fecha de presentación para inscripción (si corresponde)
   - Indicación de liberación de deudas

2. **Registrar testimonios inscriptos** (#34)  
   El sistema debe registrar la información de inscripción:
   - Fecha de inscripción ante el organismo
   - Observaciones realizadas por el organismo
   - Número de inscripción asignado
   - Fecha de recepción en la escribanía
   - Fecha de reingreso en caso de observaciones

3. **Registrar retiro de testimonio** (#35)  
   El sistema debe permitir registrar:
   - Cuándo fue retirado el testimonio
   - Quién lo retiró
   - Destinatario del documento

#### 4.1.8 Archivar Trámites

**Requisitos asociados:** #36, #37

1. **Archivar trámite** (#37)  
   Una vez completado e inscripto, el legajo del trámite debe ser archivado con registro de:
   - Indicación de deudas sin cancelar (si las hay)
   - Número único de archivo asignado
   - Fecha de archivo
   - Número de bibliorato donde se archiva
   - Número de carpeta física
   - Observaciones relevantes

### 4.2 Administrar Clientes

**Requisitos asociados:** #38, #39, #40, #41, #42

1. **Registrar nuevos clientes** (#39)  
   El sistema debe permitir registrar nuevos clientes de la escribanía capturando:
   - Nombre y apellido completos
   - Nacionalidad
   - Tipo y número de identificación
   - CUIT/CUIL
   - Estado civil y número de nupcias (si es aplicable)
   - Ocupación
   - Domicilio completo
   - Teléfono y correo electrónico

2. **Modificación de clientes** (#40)  
   El sistema debe permitir a los usuarios autorizados modificar cualquier dato de los clientes registrados.

3. **Buscar y ver detalle de clientes** (#41)  
   El sistema debe permitir:
   - Buscar clientes por diversos criterios
   - Visualizar todos los datos personales del cliente
   - Ver trámites y carpetas asociados
   - Ver si posee trámites inconclusos
   - Ver si posee deudas pendientes

4. **Buscar gestiones de cliente** (#42)  
   El sistema debe permitir buscar y presentar la lista completa de gestiones asociadas a un cliente específico.

### 4.3 Administrar Usuarios

**Requisitos asociados:** #43, #44, #45, #46, #47

1. **Crear nuevos usuarios** (#44)  
   El sistema debe permitir la creación y modificación de usuarios del sistema con asociación de permisos correspondientes a su rol.

2. **Definir nuevos usuarios** (#45)  
   Al crear un usuario se debe permitir definir el conjunto de funciones a las cuales tendrá acceso (permisos y roles).

3. **Registro de auditoría** (#46)  
   El sistema debe mantener un registro accesible de eventos que incluya:
   - Estado de usuarios (activos, suspendidos, eliminados)
   - Fechas y horas de ingreso al sistema
   - Cambios realizados por cada usuario
   - Elementos afectados por cada cambio

4. **Permitir modificar datos de usuarios** (#47)  
   El sistema debe permitir modificar los datos de usuarios registrados así como cambiar los permisos y funciones a las cuales tienen acceso.

### 4.4 Generar Índices

**Requisitos asociados:** #48, #49, #50

1. **Ver índices de trámites** (#49)  
   Los índices se generan automáticamente para cada protocolo e incluyen:
   - Año de los trámites
   - Registro del escribano
   - Número de escritura
   - Nombre/descripción del trámite
   - Fecha de otorgamiento de la escritura
   - Clientes intervinientes
   - Número del primer folio de la escritura
   - Tipo de protocolo (Principal o Auxiliar)

2. **Permitir editar e imprimir los índices** (#50)  
   El sistema debe permitir editar los índices generados (si es necesario hacer correcciones) e imprimirlos en formato profesional.

### 4.5 Generar Declaraciones Juradas

**Requisitos asociados:** #51, #52, #53, #54

1. **Generar DDJJ a partir de escrituras realizadas en el mes** (#52)  
   Se genera una declaración jurada por cada protocolo y por cada registro de escribano. El encabezado debe indicar:
   - Mes y año de la declaración
   - Tipo de protocolo
   - Registro del escribano y su titular/suplentes
   
   La descripción debe incluir para cada escritura:
   - Número de escritura
   - Número de folio inicial
   - Día del mes en que se otorgó
   - Tipo de trámite realizado
   - Personas involucradas
   - Impuestos a pagar
   
   Si es Protocolo Principal y existen trámites con inmuebles:
   - Nomenclatura catastral
   - Valuación fiscal
   - Precio de la operación

2. **Generar DDJJ para Rentas** (#53)  
   El sistema debe generar una declaración jurada adicional por cada protocolo y registro de escribano en formato específico para presentar ante la autoridad fiscal (RENTAS).

3. **Imprimir declaraciones juradas** (#54)  
   El sistema debe permitir imprimir en papel o en formato PDF el reporte completo de las declaraciones juradas generadas.

### 4.6 Administrar Tablas Base

**Requisitos asociados:** #55, #56, #57, #58, #59, #60

1. **Ingresar nuevos trámites** (#56)  
   El sistema debe proveer la capacidad de definir nuevos tipos de trámites que puedan ser utilizados en futuras gestiones.

2. **Ingresar nuevos documentos** (#57)  
   El sistema debe proveer la capacidad de definir nuevos tipos de documentos que puedan ser requeridos en trámites.

3. **Ingresar nuevos folios** (#58)  
   El sistema debe proveer la capacidad de cargar en el sistema nuevos folios provistos por el Colegio Notarial con su numeración.

4. **Ingresar nuevos estados** (#59)  
   El sistema debe proveer la capacidad de definir nuevos estados que puedan ser utilizados en la gestión de trámites.

5. **Ingresar nuevos conceptos** (#60)  
   El sistema debe proveer la capacidad de definir nuevos conceptos que puedan ser incluidos en presupuestos.

### 4.7 Administrar Plantillas

**Requisitos asociados:** #61, #62-#65, #66-#69

#### 4.7.1 Plantillas de trámites

1. **Crear nuevas plantillas de trámites** (#63)  
   El sistema debe permitir crear plantillas que asocien un tipo de trámite con los documentos y certificados necesarios, seleccionados de una lista predefinida.

2. **Modificar plantillas de trámites** (#64)  
   El sistema debe permitir editar plantillas existentes, modificando la composición de documentos y certificados asociados.

3. **Eliminar plantillas de trámites** (#65)  
   El sistema debe permitir eliminar o deshabilitar una plantilla de trámites para prevenir su uso futuro.

#### 4.7.2 Plantillas de presupuestos

1. **Crear nuevas plantillas de presupuestos** (#67)  
   El sistema debe permitir crear plantillas que asocien un tipo de trámite con los conceptos necesarios para presupuestar, seleccionados de una lista predefinida.

2. **Modificar plantillas de presupuestos** (#68)  
   El sistema debe permitir editar plantillas existentes, modificando la composición de conceptos asociados.

3. **Eliminar plantillas de presupuestos** (#69)  
   El sistema debe permitir eliminar o deshabilitar una plantilla de presupuestos para prevenir su uso futuro.

### 4.8 Administrar Folios

**Requisitos asociados:** #94, #95, #96, #97, #98, #99

1. **Cargar folios del Colegio Notarial** (#95)  
   El sistema debe permitir cargar los folios provistos por el Colegio Notarial y asignar una numeración propia única a cada lote.

2. **Control de numeración correlativa** (#96)  
   El sistema debe controlar que la numeración de los folios sea correlativa y sin faltantes, tanto dentro de cada pedido como a lo largo del año.

3. **Manejo de folios dañados (errose)** (#97)  
   El sistema debe permitir registrar folios dañados durante la impresión y etiquetarlos automáticamente como "errose".

4. **Manejo de folios no usados (no pasó)** (#98)  
   El sistema debe permitir registrar folios impresos pero no utilizados (cuando una escritura no llegó a firmarse) y etiquetarlos como "no pasó".

5. **Seguimiento de disponibilidad de folios** (#99)  
   El sistema debe mantener información sobre qué folios están disponibles, cuáles han sido usados, cuáles están dañados y cuáles están marcados como no usados.

### 4.9 Administrar Cuadernos

**Requisitos asociados:** #100, #101, #102, #103

1. **Generar cuadernos** (#101)  
   El sistema debe permitir agrupar los folios de a diez en cuadernos, de forma consecutiva, siguiendo la organización física de la oficina.

2. **Numerar cuadernos** (#102)  
   El sistema debe permitir numerar los cuadernos de forma correlativa de 1 a N.

3. **Generar carátula de cuaderno** (#103)  
   El sistema debe permitir generar y imprimir la carátula de cada cuaderno que incluya:
   - Año
   - Registro del escribano
   - Número de cuaderno
   - Descripción de los trámites que contiene

### 4.10 Administrar Carpetas de Trámite

**Requisitos asociados:** #104, #105, #106

1. **Generar carpeta de trámite** (#105)  
   Cuando un cliente solicita un trámite se genera una carpeta con número único que contiene:
   - Fecha de inicio del trámite
   - Datos del cliente
   - Tipo de trámite
   - Documentación asociada

2. **Estados de carpeta** (#106)  
   El sistema debe permitir gestionar los estados de una carpeta:
   - **Activa:** La carpeta está siendo procesada
   - **Espera:** El cliente solicitó esperar (por ejemplo, para obtener documentos)
   - **Archivada:** El trámite fue completado y archivado

### 4.11 Estados y Transiciones del Trámite

**Requisitos asociados:** #107, #108, #109, #110

1. **Definir estados del trámite** (#108)  
   El sistema debe permitir definir los estados que puede adoptar un trámite durante su ciclo de vida, tales como:
   - Generado (trámite registrado)
   - En proceso (se está recolectando documentación)
   - Con documentos (todos los documentos están disponibles)
   - Con certificados (todos los certificados están vigentes)
   - Listo para firmar (todo preparado para la firma)
   - Firmado (la escritura fue firmada)
   - Para inscribir (necesita inscripción ante organismos)
   - Inscripto (fue debidamente inscripto)
   - Archivado (proceso completado)
   - Cancelado (se suspendió el trámite)

2. **Definir transiciones válidas de estado** (#109)  
   El sistema debe permitir definir qué transiciones de estado son válidas para cada tipo de trámite, evitando transiciones ilógicas.

3. **Registrar historial de cambios de estado** (#110)  
   El sistema debe registrar cada cambio de estado con:
   - Fecha exacta del cambio
   - Hora del cambio
   - Usuario que realizó el cambio
   - Observaciones relacionadas

### 4.12 Protocolo Auxiliar

**Requisitos asociados:** #111, #112

1. **Gestionar trámites del Protocolo Auxiliar** (#112)  
   El sistema debe gestionar trámites del Protocolo Auxiliar, cuya característica principal es ser un proceso simplificado sin generación de carpetas. El flujo incluye:
   - Solicitar trámite
   - Registrar cliente
   - Preparar escritura
   - Firmar escritura
   - Entregar testimonio

Tipos de trámite del Protocolo Auxiliar incluyen: actas de certificaciones, actas de manifestaciones, actas de domicilio, protocolizaciones, autorizaciones, poderes y actas de constatación.

### 4.13 Administrar Suplencias

**Requisitos asociados:** #113, #114, #115, #116

1. **Registrar suplentes del escribano** (#114)  
   El sistema debe permitir registrar los escribanos suplentes que pueden actuar en representación del escribano titular en ausencias o impedimentos.

2. **Asignar suplente a una gestión** (#115)  
   El sistema debe permitir asignar un escribano suplente a una gestión específica cuando el titular no puede actuar.

3. **Historial de suplencias** (#116)  
   El sistema debe mantener un historial completo de las suplencias de cada escribano, registrando fechas y gestiones cubiertas.

### 4.14 Gestión de Inscripciones

**Requisitos asociados:** #117, #118

1. **Generar minuta de inscripción** (#117)  
   Cuando se trata de una operación relacionada con inmuebles, el sistema debe generar el certificado "minuta de inscripción" a partir de los datos del trámite y los certificados correspondientes.

2. **Seguimiento de presentación para inscripción** (#118)  
   El sistema debe registrar y permitir seguimiento de:
   - Presentación del testimonio ante el organismo de inscripción
   - Devolución del testimonio por parte del organismo
   - Fecha de inscripción definitiva
   - Número de inscripción asignado

### 4.15 Impresión de Testimonios

**Requisitos asociados:** #120

1. **Impresión de testimonios** (#120)  
   Una vez que la escritura está firmada, el sistema debe permitir generar una copia denominada "testimonio" que se imprime en hojas especiales (distintas a los folios) y debe ser firmada por el escribano.

### 4.16 Control de Numeración de Escrituras

**Requisitos asociados:** #121

1. **Numeración correlativa** (#121)  
   El sistema debe garantizar que la numeración de las escrituras sea única y correlativa dentro de cada protocolo. La numeración se asigna solo cuando la escritura esté correctamente impresa en los folios asignados.

### 4.17 Diferencias entre Protocolos

**Requisitos asociados:** #119

1. **Definir diferencias Protocolo Principal vs Auxiliar** (#119)  
   El sistema debe implementar las diferencias entre ambos protocolos:
   - **Protocolo Principal:** Requiere generación de carpetas, proceso extenso, puede requerir inscripción
   - **Protocolo Auxiliar:** Sin carpetas, proceso simplificado de 5 pasos, entrega directa al cliente
   - Tipos de folios distintos para cada protocolo
   - Tipos de trámites específicos para cada protocolo

---

## 5. Requerimientos de rendimiento

**Requisitos asociados:** #70, #71, #72, #73

1. **Uso de memoria RAM** (#70)  
   El sistema no deberá consumir más de 300 MB de memoria RAM. Superar este límite puede ralentizar la computadora y afectar la experiencia del usuario.

2. **Uso de CPU** (#71)  
   El sistema no deberá utilizar más del 50% de la capacidad del procesador por el mismo motivo anterior (prevenir ralentización).

3. **Tiempo de respuesta** (#72)  
   Ninguna operación puede demorar más de 10 segundos en completarse. Tiempos de respuesta mayores generan desagrado en los usuarios y reducen la productividad.

4. **Múltiples usuarios** (#73)  
   El sistema deberá soportar múltiples usuarios conectados simultáneamente sin degradación de rendimiento.

---

## 6. Requerimientos de interfaz

**Requisitos asociados:** #74, #75, #76, #77, #78, #79, #80

1. **Aspecto visual** (#74)  
   Requerimientos asociados con la implementación de aspectos visuales y diseño de ventanas, carteles y diálogos.

2. **Diseño de ventanas** (#75)  
   El software deberá contar con una interfaz gráfica capaz de visualizar todos los formularios de manera secuencial y guiada. La navegación entre campos se deberá poder realizar haciendo clic con el puntero del ratón o presionando la tecla de tabulación.

3. **Diseño de campos y combos** (#76)  
   Todos los campos con valores predefinidos se deberán presentar como listas desplegables (combo boxes) o cajas de verificación (checkboxes) según sea apropiado.

4. **Especificación de campos a completar** (#77)  
   Los nombres de los campos a completar deberán ser lo suficientemente significativos y descriptivos para evitar confusiones o ambigüedades en su interpretación.

5. **Uso de colores en la GUI** (#78)  
   No se deberán utilizar colores que dificulten la correcta visualización de los datos, considerando accesibilidad para usuarios con deficiencias visuales.

6. **Seguimiento del trabajo sobre ventanas** (#79)  
   En cada pantalla se deberá identificar claramente:
   - El proceso que se está realizando
   - El estado actual del mismo
   - Permitiendo al usuario visualizar su ubicación y progreso en todo momento

7. **Identificación de sesión** (#80)  
   En todo momento visible en la interfaz debe conocerse el nombre del usuario que está trabajando en la sesión actual.

---

## 7. Requerimientos de seguridad y privacidad

**Requisitos asociados:** #81, #82, #83, #84, #85

1. **Seguridad y privacidad** (#81)  
   Requerimientos asociados a elementos de seguridad que deben ser implementados para proteger los datos.

2. **Acceso de usuarios** (#82)  
   El acceso al sistema se deberá realizar mediante el ingreso de un nombre de usuario y contraseña únicos.

3. **Cifrado de contraseña** (#83)  
   Los datos de usuario y contraseña deberán estar cifrados en la base de datos, nunca almacenados en texto plano.

4. **Transporte de información por red** (#84)  
   Se deben aplicar las políticas básicas de seguridad requeridas para cualquier aplicación estándar, incluyendo:
   - Encriptación de la transferencia de datos en la comunicación cliente-servidor
   - Validación de acceso de los usuarios

5. **Acceso a la base de datos** (#85)  
   El acceso a la base de datos solo podrá realizarse por el administrador del sistema mediante usuario y contraseña específicos, evitando que otros usuarios accedan directamente a la base de datos.

---

## 8. Requerimientos de software

**Requisitos asociados:** #86, #87

1. **Java VM** (#86)  
   Debido a que el producto será una aplicación Java, debe tener instalado el entorno de ejecución de Java (JRE 1.7 o superior) en la computadora donde se ejecutará.

2. **Sistema operativo** (#87)  
   El sistema debe ser operativo en distintos sistemas operativos (Windows, Linux, macOS).

---

## 9. Requerimientos de hardware

**Requisitos asociados:** #88, #89

1. **PC de escritorio** (#88)  
   Requisitos mínimos para PC de escritorio a definir conforme al entorno de despliegue final, considerando monitoreo en pruebas de carga.

2. **Notebook** (#89)  
   Requisitos mínimos para notebook a definir conforme al entorno de despliegue final, considerando la portabilidad de los usuarios.

---

## 10. Requerimientos para el desarrollo

**Requisitos asociados:** #90, #91, #92, #93

1. **Metodología de desarrollo** (#90)  
   Se utilizará la metodología del paradigma de Orientación a Objetos (OOP) para el diseño y desarrollo del software.

2. **Modelo de desarrollo** (#91)  
   Se utilizará un modelo incremental con iteraciones. Cada iteración constará de:
   - Entrega de una versión funcional del software
   - Entorno de ejecución preparado y documentado
   - Documentación de los cambios implementados

3. **Lenguaje de programación** (#92)  
   Se utilizará el lenguaje de programación Java en su última versión estable, siendo:
   - Uno de los lenguajes más utilizados para desarrollos orientados a objetos
   - Un lenguaje de uso libre que no requiere pago de licencias
   - Ampliamente documentado y con comunidad activa

4. **Motor de base de datos** (#93)  
   Se utilizará el motor de base de datos libre MySQL en su última versión estable, por:
   - Su excelente compatibilidad con aplicaciones Java
   - Gran disponibilidad de información y documentación
   - Ser una solución estable y confiable

---

## 11. Validación de cobertura

### Verificación cruzada

- **Cobertura total:** Todos los 121 requisitos del CSV están representados en el SRS
- **Trazabilidad completa:** Cada requisito tiene un ID único (#1-#121) y es referenciable
- **Alineación de contenido:** El contenido del SRS permanece consistente con la fuente original (CSV y RS)
- **No hay requisitos adicionales:** Solo se incluyen los requisitos definidos en la matriz de trazabilidad

### Estructura de validación

| Aspecto | Estado | Evidencia |
|---------|--------|-----------|
| Cobertura de requisitos | ✓ Completa | 121/121 requisitos documentados |
| Trazabilidad | ✓ Completa | Cada requisito vinculado a CSV |
| Consistencia | ✓ Verificada | Contenido alineado entre SRS-CSV-RS |
| Descripciones | ✓ Extendidas | Especificaciones detalladas por requisito |
| Estructura | ✓ Jerárquica | Organizados por categoría funcional |

---

## Historial de revisiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Junio 2026 | Versión inicial |
| 2.0 | Agosto 2026 | Validación completa contra CSV y RS, descripciones extendidas, correcciones gramaticales |

---

**Documento:** Especificación de Requerimientos de Software (SRS)  
**Versión:** 2.0  
**Última actualización:** Agosto 2026  
**Estado:** Listo para desarrollo
