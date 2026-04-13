

*Sistema de*

*Gestión Notarial*

![][image1]  
Diccionario de Datos

***Universidad FASTA***

*San Carlos de Bariloche*

*Carrera: Licenciatura en Sistemas*

*Cátedra: Seminario de Informática I – Cuarto año*

*Profesor: Alejandro Nikolic*

*Entrega: Año 2012*

*Alumnos:*

* *Juan Carlos Ramos*  
* *L. Matías Miguez*  
* *Estefanía Klein*

***Indice***

I.	Detalle del Diccionario de Datos	[5](#detalle-del-diccionario-de-datos)

1\.	conceptos	[5](#heading=h.7bgaae3ug31y)

2\.	copias	[5](#copias)

3\.	documentos\_presentados	[5](#documentos_presentados)

4\.	escrituras	[6](#heading)

5\.	estados\_de\_gestion	[7](#estados_de_gestion)

6\.	folios	[7](#folios)

7\.	folios\_copias	[7](#folios_copias)

8\.	gestiones\_de\_escrituras	[7](#gestiones_de_escrituras)

9\.	historial	[8](#historial)

10\.	inmuebles	[8](#inmuebles)

11\.	items	[8](#items)

12\.	movimientos\_testimonio	[9](#movimientos_testimonio)

13\.	pagos	[9](#pagos)

14\.	personas	[9](#personas)

15\.	plantilla\_presupuestos	[10](#plantilla_presupuestos)

16\.	plantilla\_tramites	[10](#plantilla_tramites)

17\.	presupuestos	[10](#presupuestos)

18\.	registro\_auditoria	[11](#registro_auditoria)

19\.	suplencias	[11](#suplencias)

20\.	testimonios	[11](#testimonios)

21\.	tipos\_de\_documento	[12](#tipos_de_documento)

22\.	tipos\_de\_folio	[12](#tipos_de_folio)

23\.	tipos\_de\_tramite	[12](#tipos_de_tramite)

24\.	tipos\_identificacion	[13](#tipos_identificacion)

25\.	tramites	[13](#tramites)

26\.	tramites\_personas	[13](#tramites_personas)

27\.	usuarios	[14](#usuarios)

1. **Detalle del Diccionario de Datos**

   Este documento provee una descripción del modelo de datos del sistema Notaire.

1. ***conceptos***

   Representa los conceptos (ítems) que pueden ser aplicados a un presupuesto.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_concepto | INT(11) | X | X | X |   | Representa la clave primaria para Conceptos |
| nombre | TEXT |   | X |   |   | Indica el nombre del tipo de concepto |
| valor | FLOAT |   | X |   | '0' | Indica el valor del concepto, es cero (0) por default. |
| porcentaje | INT(11) |   | X |   | '0' | Indica un porcentaje aplicable sobre un valor en particular, es cero (0) por default. |
| habilitado | TINYINT(1) |   | X |   |   | Indica si este concepto se puede usar o no. |
| concepto\_fijo | TINYINT(1) |   | X |   |   | Indica se el valor del concepto es un valor fijo o calculado en base al porcentaje. |

2. ***copias***

   Representa las copias generadas de un Testimonio.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_copia | INT(11) | X | X | X |   | Representa la clave primaria para Copias (de testimonios) |
| numero | INT(11) |   | X |   |   | Indica la cantidad de copias que se han generado de un testimonio |
| fecha\_impresion | DATE |   | X |   |   | Indica la fecha de impresión de la copia |
| fecha\_retiro | DATE |   |   |   | NULL | Indica la fecha en la cual el cliente ha retirado una copia |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones de la copia, por ejemplo, si fue retirada por otra persona que no sea el cliente. |
| fk\_id\_testimonio | INT(11) |   |   |   | NULL | Clave foránea al testimonio al cual pertenece la copia. |
| fk\_id\_persona | INT(11) |   |   |   | NULL | Clave foránea al cliente al cual pertenece la copia. |

3. ***documentos\_presentados***

   Representa los documentos presentados por el Cliente y las Entidades Externas para una Gestión en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_documento\_presentado | INT(11) | X | X | X |   | Indica la clave primaria para Documentos Presentados |
| nombre | TEXT |   | X |   |   | Indica el nombre del tipo de documento (corresponde con los valores de Tipos de Documentos) |
| numero\_carton | INT(11) |   |   |   | NULL | Indica el número de cartón con el cual fue ingresado |
| fecha\_ingreso | DATE |   |   |   | NULL | Indica la fecha de ingreso a alguna entidad externa |
| fecha\_salida | DATE |   |   |   | NULL | Indica la fecha en que fue devuelto por la entidad externa |
| preparado | TINYINT(1) |   | X |   |   | Indica si el documento fue preparado o no |
| vence | TINYINT(1) |   | X |   |   | Indica si el documento vence o no |
| fecha\_vencimiento | DATE |   |   |   | NULL | Si posee vencimiento, indica la fecha del mismo. |
| dias\_vencimiento | INT(11) |   |   |   | NULL | Si posee vencimiento, indica la cantidad de días de validéz. |
| importe\_a\_pagar | FLOAT |   |   |   | NULL | Indica si para el documento, se debe pagar algún importe. |
| fecha\_pago | DATE |   |   |   | NULL | Si posee importe a pagar, indica la fecha de pago del mismo. |
| liberado | TINYINT(1) |   | X |   |   | Indica si el documento está liberado o no. |
| fecha\_liberado | DATE |   |   |   | NULL | Indica la fecha de liberación del documento. |
| observado | TINYINT(1) |   | X |   |   | Indica, si al ser devuelto por la entidad externa, fue observado o no. |
| observaciones | TEXT |   |   |   | NULL | Si fue observado, indica las observaciones del mismo. |
| entregado | TINYINT(1) |   |   |   | '0' | Indica si el documento ha sido entregado o no |
| reingresado | TINYINT(1) |   | X |   |   | Indica si el documento fue reingresado para inscripción. |
| quien\_entrega | TEXT |   | X |   |   | Indica que tipo de entidad hace entrega del documento: el cliente, o una entidad externa |
| fk\_id\_tramite | INT(11) |   | X |   |   | Clave foránea compuesta, hacia gestiones de escrituras |
| fk\_id\_tipo\_documento | INT(11) |   |   |   | NULL |   |

   

4. ***escrituras***

   Representa las escrituras generadas para una gestión en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_escritura | INT(11) | X | X | X |   | indica la clave primaria para Escrituras (clave NO semántica) |
| numero | INT(11) |   | X |   |   | Clave semántica que representa el número de escritura. |
| fecha\_escrituracion | DATE |   | X |   |   | indica la fecha en que se realizó la escritura |
| cuerpo | TEXT |   | X |   |   | Detalle completo de la escritura |
| estado | TEXT |   | X |   |   | Estado actual de la escritura: Firmada / No Pasó |
| matricula\_inscripcion | TEXT |   |   |   | NULL | Indica el número de matricula de inscripción (si corresponde para el tipo de trámite) |
| fecha\_inscripcion | DATE |   |   |   | NULL | Indica la fecha en que se realizó la inscripción (si corresponde) |
| observaciones | TEXT |   |   |   | NULL | Observaciones para la escritura |

5. ***estados\_de\_gestion***

   Representas los estados que puede adquirir una gestión.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_estado\_gestion | INT(11) | X | X | X |   | Representa la clave primaria para los estados de gestión |
| nombre | TEXT |   | X |   |   | Representa el nombre de un estado posible de gestión |
| observaciones | TEXT |   |   |   | NULL | Indica obsevaciones que describen el estado de gestión |

6. ***folios***

   Representa los folios utilizados para escrituras y testimonios.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_folio | INT(11) | X | X | X |   | Indica la clave primaria para folios (clave NO semántica) |
| numero | INT(11) |   | X |   |   | Indica el número de folios (clave semántica) |
| anio | INT(11) |   | X |   |   | Indica el año al cual corresponde el folio |
| fk\_id\_escritura | INT(11) |   |   |   | NULL | Clave foránea hacia escrituras. |
| fk\_id\_tipo\_folio | INT(11) |   | X |   |   | Clave foránea hacia copias |
| fk\_id\_persona\_escribano | INT(11) |   | X |   |   | Clave foránea hacia el Escribano responsable del folio |
| estado | TEXT |   | X |   |   | Indica el estado actual del folio |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones en el folio |

7. ***folios\_copias***

   Representa los folios utilizados para una copia en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| fk\_id\_folio | INT(11) | X | X |   |   |   |
| fk\_id\_copia | INT(11) | X | X |   |   |   |

8. ***gestiones\_de\_escrituras***

   Representa las gestiones para un conjunto de trámites en particular.

   

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_gestion | INT(11) | X | X | X |   | Representa parte de la clave primaria para ésta relación asociativa |
| numero | INT(11) |   | X |   |   | Representa el número (clave semantica) de la gestión |
| fecha\_inicio | DATE |   | X |   |   | Indica la fecha de inicio de la gestión. |
| encabezado | TEXT |   | X |   |   | Indica el titulo/encabezado de la gestión |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones para la gestión. |
| numero\_archivo | INT(11) |   |   |   | NULL | Indica el número de carpeta en la cual se archiva la gestión |
| numero\_bibliorato | INT(11) |   |   |   | NULL | Indica el número de bibliorato en el cual se archiva la gestión |
| fk\_id\_persona\_escribano | INT(11) |   | X |   |   | Representa el escribano asociado a la gestión |
| fk\_id\_estado\_de\_gestion | INT(11) |   |   |   | NULL | Indica el estado actual de la gestión. |

9. ***historial***

   Representa el historial de una gestión en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_historial | INT(11) | X | X | X |   | Indica la clave primaria para historial de gestión |
| fk\_id\_gestion | INT(11) |   | X |   |   | Clave foránea compuesta hacia gestiones de escrituras |
| fk\_id\_estado\_gestion | INT(11) |   | X |   |   | Clave foránea hacia estados de gestión |
| fecha | DATE |   | X |   |   | Indica la fecha en que se cambio el estado de la gestión de escrituras. |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones en el cambio de estado. |

10. ***inmuebles***

    Representa los inmuebles que pueden estar asociados a un trámite en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_inmueble | INT(11) | X | X | X |   |   |
| nomenclatura\_catastral | TEXT |   | X |   |   | Indica el identificador único del inmueble a nivel municipal. |
| valuacion\_fiscal | TEXT |   |   |   | NULL | Indica el valor legal del inmueble. |
| domicilio | TEXT |   | X |   |   | Representa el domicilio (la dirección física del inmueble) |
| tipo\_inmueble | TEXT |   | X |   |   | Casa, Terreno, Depto, etc. |
| observaciones | TEXT |   |   |   | NULL | Observaciones que describen el inmueble. |

11. ***items***

    Representa los ítems de un presupuesto.

    

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_item | INT(11) | X | X | X |   | Indica la clave primaria para Items |
| nombre | TEXT |   | X |   |   | Indica el nombre del ítem. |
| valor | FLOAT |   | X |   | '0' | Indica el valor de ítem. |
| porcentaje | INT(11) |   |   |   | NULL | Indica un valor de procentaje (0-100%) |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones. |
| fk\_id\_presupuesto | INT(11) |   | X |   |   | Clave foránea hacia el presupuesto al cual pertenecen los ítems. |
| concepto\_fijo | TINYINT(1) |   | X |   |   | Indica si el item posee un valor fijo o es calculado en base al porcentaje. |

12. ***movimientos\_testimonio***

    Representa los movimientos de un testimonio en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_movimiento\_testimonio | INT(11) | X | X | X |   | Indica la clave primaria para movimiento de testimonios |
| fecha\_ingreso | DATE |   | X |   |   | Indica la fecha de ingreso (en la entidad externa) |
| fecha\_salida | DATE |   |   |   | NULL | Indica la fecha de salida (de la entidad externa) |
| fecha\_inscripcion | DATE |   |   |   | NULL | indica la fecha de inscripción |
| inscripta | TINYINT(1) |   | X |   |   | Indica si fue inscripta o no |
| numero\_carton | INT(11) |   | X |   |   | Indica el número de carton |
| observaciones | TEXT |   |   |   | NULL | Indica las observaciones |
| fk\_id\_testimonio | INT(11) |   | X |   |   | Clave foránea hacia Testimonios |

13. ***pagos***

    Representa los pagos realizados por un Cliente para un presupuesto en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_pago | INT(11) | X | X | X |   | Indica la clave primaria (y número) de pago |
| monto | FLOAT |   | X |   |   | Indica el monto del pago. |
| fecha | DATE |   | X |   |   | Indica la fecha en que se realizó el pago. |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones del pago realizado. |
| fk\_id\_presupuesto | INT(11) |   | X |   |   | Clave foránea hacia el presupuesto, al cual corresponde el pago realizado. |

14. ***personas***

    Representa las personas que realizan trámites en la escribanía.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_persona | INT(11) | X | X | X |   | Representa la clave primaria para la tabla Personas |
| nombre | TEXT |   | X |   |   | Indica el nombre de la persona/cliente |
| apellido | TEXT |   | X |   |   | Indica el apellido de la persona/cliente |
| nacionalidad | TEXT |   |   |   | NULL | Indica la nacionalidad de la persona/cliente |
| fk\_id\_tipo\_identificacion | INT(11) |   | X |   |   | Referencia hacia el tipo de identificación de la persona. |
| numero\_identificacion | VARCHAR(20) |   | X |   |   | Indica el número de identificación |
| cuit | TEXT |   |   |   | NULL | Indica el número de CUIT/CUIL de la persona |
| sexo | VARCHAR(11) |   |   |   | NULL | Indica el sexo de la persona/cliente |
| fecha\_nacimiento | DATE |   |   |   | NULL | Indica la fecha de nacimiento de la persona/cliente |
| estado\_civil | TEXT |   |   |   | NULL | Indica el estado civil de la persona/cliente |
| numero\_nupcias | INT(11) |   |   |   | NULL | Si estuvo casado, indica cuantas nupcias contrajo. |
| ocupacion | TEXT |   |   |   | NULL | Indica la ocupación de la persona/cliente |
| domicilio | TEXT |   |   |   | NULL | Indica el domicilio de la persona/cliente |
| telefono | TEXT |   |   |   | NULL | Indica el número de teléfono de la persona/cliente |
| e\_mail | TEXT |   |   |   | NULL | Indica el e-mail de la persona/cliente |
| registro\_escribano | INT(11) |   |   |   | NULL | Indica si la persona es un escribano. |
| es\_cliente | TINYINT(1) |   | X |   |   | Indica si la persona es cliente o no |

15. ***plantilla\_presupuestos***

    Representa los conceptos a cobrar para un presupuesto de un tipo de trámite en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| fk\_id\_tipo\_tramite | INT(11) | X | X |   |   | Clave primaria compuesta para plantilla de presupuestos |
| fk\_id\_concepto | INT(11) | X | X |   |   | Clave primaria compuesta para plantilla de presupuestos |
| observaciones | TEXT |   |   |   | NULL | Observaciones para plantilla de presupuestos |

16. ***plantilla\_tramites***

    Representa los documentos necesarios para un tipo de trámite en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| fk\_id\_tipo\_tramite | INT(11) | X | X |   |   | Clave primaria compuesta para plantilla de trámites |
| fk\_id\_tipo\_documento | INT(11) | X | X |   |   | Clave primaria compuesta para plantilla de trámites |
| observaciones | TEXT |   |   |   | NULL | Observaciones para plantilla de trámites |

17. ***presupuestos***

    Representa los presupuestos realizados para un trámite en particular.

    

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_presupuesto | INT(11) | X | X | X |   | Indica la clave primaria (y número) del presupuesto. |
| fecha | DATE |   | X |   |   | Indica la fecha en que fue creado el presupuesto |
| total | FLOAT |   | X |   |   | Indica el monto total del presupuesto, calculado en base a los ítems. |
| saldo | FLOAT |   | X |   | '0' | Indica el saldo restante a pagar del presupuesto. |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones sobre el presupuesto. |
| fk\_id\_tramite | INT(11) |   | X |   |   | Clave foránea compuesta hacia gestiones de escritura. Inicialmente puede ser nula. |
| fk\_id\_persona | INT(11) |   | X |   |   | Clave foránea compuesta hacia gestiones de escritura. Inicialmente puede ser nula. |

18. ***registro\_auditoria***

    Representa el registro de las acciones realizadas por los usuarios del sistema.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_registro\_auditoria | INT(11) | X | X | X |   | Indica la clave primaria para Registro de Auditoria |
| fk\_id\_usuario | INT(11) |   | X |   |   | Clave foránea hacia usuario. |
| modulo | TEXT |   | X |   |   | Nombre del modulo donde se está realizando algúna acción |
| detalle\_operacion | TEXT |   | X |   |   | Detalle de alguna operación realizada. |
| fecha | DATETIME |   | X |   |   | Indica la fecha en la cual se realizó la operación. |

19. ***suplencias***

    Representa las suplencias realizadas entre escribanos.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_suplencia | INT(11) | X | X | X |   | Indica la clave primaria para suplencias. |
| fk\_id\_suplantado | INT(11) |   | X |   |   | Clave foránea hacia el escribano que se toma licencia (suplantado) |
| fk\_id\_suplente | INT(11) |   | X |   |   | Clave foránea hacia el escribano que va a realizar la suplencia (suplente) |
| fecha\_inicio | DATE |   | X |   |   | Indica la fecha de inicio de la suplencia. |
| fecha\_fin | DATE |   | X |   |   | Indica la fecha de finalización de la suplencia. |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones en la suplencia |

20. ***testimonios***

    Representa los testimonios generados para una escritura en particular.

    

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_testimonio | INT(11) | X | X | X |   | Representa la clave primaria para Testimonios |
| numero | INT(11) |   | X |   |   | Indica el número del testimonio |
| observado | TINYINT(1) |   | X |   |   | Indica si fue observado o no |
| observaciones | TEXT |   |   |   | NULL | Si fue observado, indica las observaciones realizadas al testimonio |
| fk\_id\_escritura | INT(11) |   | X |   |   | Referencia hacia la escrtura a la cual pertenece el testimonio |

21. ***tipos\_de\_documento***

    Representa los tipos de documento necesarios para utilizar en el sistema.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_tipo\_documento | INT(11) | X | X | X |   | clave primaria de tipos de documentos |
| nombre | TEXT |   | X |   |   | El nombre del tipo de documento |
| vence | TINYINT(1) |   | X |   |   | indica si posee vencimiento o no. |
| dias\_vencimiento | INT(11) |   |   |   | NULL | En el caso de poseer vencimiento, indica la cantidad de días antes de vencer. |
| quien\_entrega | TEXT |   | X |   |   | Indica quién tiene que entregar el documentos: el cliente, o una entidad externa. |
| habilitado | TINYINT(1) |   | X |   |   | Indica si el tipo de documento esta habilitado para ser utilizado en un plantilla de trámite o no. |

22. ***tipos\_de\_folio***

    Representa los tipos de folio necesarios para utilizar en el sistema.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_tipo\_folio | INT(11) | X | X | X |   | Indica la clave primaria para tipos de folios |
| nombre | TEXT |   | X |   |   | Indica el nombre del tipo de folio |
| observaciones | TEXT |   |   |   | NULL | Indica observaciones para el tipo de folio |
| habilitado | TINYINT(1) |   | X |   |   | Indica si el tipo de folio esta habilitado para ser utilizado cuando se registran ingreso de tandas de folios o no. |

23. ***tipos\_de\_tramite***

    Representa los tipos de trámite necesarios para utilizar en el sistema.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_tipo\_tramite | INT(11) | X | X | X |   | Indica la clave primaria del tipo de trámite |
| nombre | TEXT |   | X |   |   | Indica el nombre del tipo de trámite |
| se\_archiva | TINYINT(1) |   | X |   |   | Indica si el tipo de trámite requiere ser archivado o no. Si se archiva, se debe registrar el número de carpeta y bibliorato, caso contrario, se registra con cero (0) |
| se\_inscribe | TINYINT(1) |   | X |   |   | Indica si el tipo de trámite debe ser enviado a inscribir, una vez realizada la escritura. |
| asocia\_inmuebles | TINYINT(1) |   | X |   |   | Indica si tipo de trámite actual se le debe asociar un inmueble o no. |
| observaciones | TEXT |   |   |   | NULL | Observaciones que describe el tipo de trámite. |
| habilitado | TINYINT(1) |   | X |   |   | Indica si el tipo de trámite esta habilitado para poder ser utilizado para iniciar gestiones o no. |

24. ***tipos\_identificacion***

    Representa los tipos de identificación de una persona.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_tipo\_identificacion | INT(11) | X | X | X |   | Clave primaria para tipo de identificación. |
| nombre | TEXT |   | X |   |   | Nombre del tipo de identificación. |

25. ***tramites***

    Representa los trámites particulares a realizarse en una gestión.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_tramite | INT(11) | X | X | X |   | Clave primaria para la tabla tramites. |
| observaciones | TEXT |   |   |   | NULL | Obsevaciones para el tramite actual. |
| fk\_id\_tipo\_tramite | INT(11) |   | X |   |   | Referencia hacia el tipo de tramite que se está realizando. |
| fk\_id\_gestion | INT(11) |   |   |   | NULL | Referencia hacia la gestión a la cual esta asociado el tramite actual. |
| fk\_id\_escritura | INT(11) |   |   |   | NULL | Referencia hacia la escritura generada en base al tramite de gestion actual, |
| fk\_id\_presupuesto | INT(11) |   |   |   | NULL | Referencia hacia el presupuesto al que pertenece el tramite de gestion. |
| fk\_id\_inmueble | INT(11) |   |   |   | NULL | Referencia (si es que aplica) hacia el inmueble asociado al tramite de gestion actual. |

26. ***tramites\_personas***

    Representa la persona que realizó un trámite en particular.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| fk\_id\_tramite | INT(11) | X | X |   |   | Referncia hacia un tramite en curso. |
| fk\_id\_persona\_cliente | INT(11) | X | X |   |   | Referencia hacia una persona involucrada en un tramite en curso. |
| observaciones | TEXT |   |   |   | NULL | Detalle sobre el registro actual. |

27. ***usuarios***

    Representa los usuarios del sistema.

| Column name | DataType | PK | Not Null | AI | Default | Comment |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| version | INT(11) |   | X |   |   |   |
| id\_usuario | INT(11) | X | X | X |   | Representa la clave primaria de usuario |
| nombre | TEXT |   | X |   |   | Indica el nombre de usuario para ingresar al sistema |
| contrasenia | TEXT |   | X |   |   | Indica la contraseña de usuario para ingresar al sistema |
| estado | TINYINT(1) |   | X |   |   | Indica si está activo o inactivo |
| tipo | TEXT |   | X |   |   | Representa el tipo de usuario, y por ende los permisos asociados. |
| fk\_id\_persona | INT(11) |   | X |   |   | Clave foránea a los datos del usuario real. |

    

    

