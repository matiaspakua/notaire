  
*Sistema de*  
*Gestión Notarial*

*![][image1]*  
Especificación de Requerimientos de Software (SRS)  
***Indice***

[I.	Sistema a desarrollar	3](#sistema-a-desarrollar)  
[1.1	Objetivos del sistema.	3](#heading=h.wtm7h4fqev14)  
[1.2	Requerimientos Funcionales.	3](#heading=h.5ccxdd3dcq4i)  
[1.3	Requerimientos de Rendimiento.	7](#heading=h.vauami9tcn1k)  
[3.1	Requerimientos de Interfaz	7](#heading=h.8kya0q2n93o6)  
[5.1	Requerimientos de Seguridad y Privacidad.	8](#heading=h.kbix86ou2ntu)  
[2.1	Requerimientos de software para el sistema.	8](#heading=h.kysmcmuvnw7t)  
[1.1	Requerimientos de Hardware para el sistema.	8](#heading=h.rvt4f8ais91)  
[1.2	Requerimientos para el desarrollo.	8](#heading=h.qt21jm2sindo)

1. **Sistema a desarrollar**

Se debe desarrollar un producto de software que permita administrar y llevar el control de los trámites de una escribanía.

1. ***Objetivos del sistema.***

* Ayudar a realizar el seguimiento y control del proceso de los trámites.  
* Tener un registro de los clientes y sus trámites asociados.  
* Tener un registro de las escrituras realizadas en los protocolos.  
* Facilitar el acceso a la información de los trámites y clientes.  
* Facilitar la confección de las declaraciones juradas.

  2. ***Requerimientos Funcionales.***

1. **Gestionar Trámites**  
   1. Preparar Presupuestos  
      1. Procesar solicitud de presupuestos: Tomar datos esenciales de la persona que solicita un presupuesto, así como: nombre, apellido, tipo y número de identificación, teléfono y correo electrónico. Generar un número de presupuesto para permitir ser identificado posteriormente, en caso de que el cliente decida aceptarlo y comenzar un trámite.  
      2. Editar plantillas de presupuestos: Permitir a los usuarios personalizar la plantilla, detallando en las mismas los conceptos a ser presupuestados, los valores de los mismos y el total del presupuesto.  Se detallan gastos fijos y variables (impuestos, sellos, etc.).  
      3. Imprimir presupuestos: Provee la capacidad de imprimir los presupuestos existentes en papel.  
      4. Modificar presupuestos: Permitir al usuario modificar los conceptos y valores de los presupuestos.   
      5. Agregar ítems adicionales a los presupuestos ya realizados.  
   2. **Iniciar trámites.**  
      1. Verificar Prepuestos. Para iniciar cualquier trámite, debe existir si o si un presupuesto, ya que el mismo es asociado al trámite. Si no existe uno, se deberá crearlo en el momento en que se desee iniciar cualquier tipo de trámite.  
      2. Verificar Clientes: Para iniciar cualquier tipo de trámite, la persona interesada deberá estar registrada como cliente de la escribanía, si no es así, se deberá dar de alta a la persona como cliente. Ver RF 2\.  
      3. Registrar Inicio de gestión de trámites: se deberá registra la fecha en que se inició un determinado trámite. El tipo de trámite y cómo está compuesto, o sea, qué documentos y certificados son necesarios para poder llevarlo a cabo, y el escribano a cargo de realizar la gestión.   
   3. **Administrar  Certificados y Documentos**  
      1. Determinar los documentos necesarios para cada trámite. Identificar aquellos que posean vencimientos o impuestos a pagar y presentar estos datos al usuario.  
      2. Generar solicitudes de certificados y documentos.  
      3. Imprimir solicitudes.  
      4. Informar cuáles y cuándo fueron preparados los documentos, indicando la gestión a la cual pertenece y el cliente.  
      5. Informar cuales y cuando fueron presentados/recibidos los documentos (seguimiento): permitir realizar el seguimiento de los documentos a través de la presentación de los mismos, deudas, impuestos y vencimientos de los documentos necesarios para las gestiones. Se deberá informar a los usuarios de estos eventos mediante algún tipo de aviso automático. Los avisos deberán ser disparados por aquellos documentos próximos a vencer, a fin de evitar demoras en los trámites. Se deberá poder informar si fueron canceladas las deudas y liberados los documentos, y en caso contrario, listar cuales documentos están pendientes de pago y/o liberación.  
   4. **Abonar trámite.**  
      1. Registrar quien abona el trámite (comprador, vendedor, etc.): permitir identificar al cliente y al trámite a ser abonado, la fecha en que fue abonado y el saldo restante. Todo trámite se debe asociar a un presupuesto. Por lo tanto, el costo y saldo del trámite se calcula en base al mismo.  
      2. Los presupuestos se pueden abonar por completo o pueden ser abonados en cuotas, las cuales no son fijas, esto quiere decir que los clientes pueden pagar el costo del trámite en cualquier cantidad de cuotas, sin importar el monto. Se debe advertir de cualquier deuda que presente una gestión, al momento de finalizar la misma.  
      3. Para cada pago realizado se deberá generar e imprimir un recibo común en donde se detalle: el cliente que abona el trámite, la fecha de pago, el concepto que se esta abonando y el total abonado.   
   5. **Consultar estado e historial de los trámites.**  
      1. Saber el estado de un trámite en un determinado momento.  
      2. Informar el historial de un trámite: permitir visualizar la cronología del trámite, indicando todos los cambios de estados, las fechas en que se realizaron y las observaciones para cada etapa de la gestión del trámite. Este registro deberá informar todos los documentos / certificados solicitados para un determinado trámite. El registro incluirá: fecha de solicitud, fecha de entrega, documentos/certificados pendientes, si están liberados, etc.  
   6. **Generar Escrituras**  
      1. Preparar escrituras: Permitir registrar en la gestión de los trámites, nuevas escrituras. Se deberá registrar:  
* número de escritura  
* fecha  
* números de folio utilizados (de una lista de folios disponibles)  
* trámites involucrados  
* cuerpo de escritura  
* rol de cada cliente involucrado  
    
  2. Firmar Escrituras. Todas las escrituras para poder ser “aprobadas” deben estar firmadas por el escribano y por el o los clientes intervinientes.   
     3. Informar que escritura/s conforman un trámite (Número de escritura, folios que la componen, fecha de la escritura y registro al cual pertenece).  
  7. **Administrar Inscripciones**  
     1. Generar y registrar testimonios de escrituras. Se deberá registrar los folios utilizados, la fecha en que se creo el testimonio, el número de testimonio, copias generadas, cuando se presentó para inscribir si corresponde, y se deberá indicar si fueron liberadas todas las deudas.  
     2. Registrar los testimonios inscriptos, indicando la fecha de inscripción, si tuvo observaciones, el número de inscripción, cuando fue recibido en la escribanía y en los casos donde se debió reenviar para inscribir causa de observaciones o algún otro evento, registrar la fecha de reingreso.  
     3. Registrar cuando y por quién fue retirado un testimonio.  
  8. **Archivar Trámites.**  
     1. Una vez inscripto, el legajo del trámite debe ser archivado informando si quedaron deudas sin cancelar. Asignar un número de archivo y registrar los siguientes datos: fecha de archivo, número de bibliorato, número de carpeta y observaciones.  
2. **Administrar Clientes**  
   1. Registrar nuevos clientes de la escribanía con los siguientes datos:  
      * Nombre y Apellido completos.  
      * Nacionalidad  
      * Tipo y Número de identificación  
      * CUIT / CUIL  
      * Estado civil / Número de nupcias.  
      * Ocupación.  
      * Domicilio  
      * Teléfono / Correo Electrónico  
   2. Modificación de clientes. Permitir a los usuarios modificar cualquier dato de los clientes.  
   3. Buscar y ver detalle de clientes. Permitir la posibilidad de buscar cliente y ver todos los datos personales del mismo. Ver trámites y carpetas asociados a ese cliente. Ver si posee trámites inconclusos o deudas.  
3. **Administrar Usuarios**  
   1. **Crear nuevos usuarios.**  
      1. Definir nuevos usuarios del sistema y determinar el conjunto de funciones a las cuales ese usuario tiene acceso (permisos).  
      2. Ver registro de eventos en el sistema. El cual incluye ver el estado de los usuarios, cuando ingresaron al sistema, que cambios realizados y sobre que elementos.  
      3. Permitir modificar los dados de los usuarios, así como cambiar las funciones a las cuales tienen acceso (permisos).  
4. **Generar Índices.**  
   1. Ver índices de trámites. Los índices se generan para cada protocolo. Se deberá indicar el año, registro de escribano, número de escritura, el nombre del trámite, fecha de escritura, clientes (partes involucradas), número del primer folio, tipo de protocolo.  
   2. Permitir editar e imprimir los índices.  
5. **Generar las Declaraciones Juradas (DDJJ)**  
   1. Generar DDJJ a partir de las escrituras realizadas en el mes. Se genera una DDJJ por cada Protocolo y por cada Registro de escribano. Se debe indicar en el encabezado: Mes y Año, Tipo de protocolo, Registro y titular del mismo/suplentes. En la descripción: número de escritura, número de folio en el que comienza la escritura, día del mes en que se otorgó, tipo de trámite, personas involucradas, impuestos a pagar. Si se trata del Protocolo Principal, y existen trámites relacionados con inmuebles, además hay que detallar: Nomenclatura Catastral, Valuación Fiscal y Precio de la misma.  
   2. Imprimir declaraciones juradas.  
6. **Administrar Tablas.**  
   1. Ingresar Nuevos Trámites. Proveer la capacidad de definir nuevos trámites.  
   2. Ingresar Nuevos Documentos. Proveer la capacidad de definir nuevos documentos.  
   3. Ingresar Nuevos Folios. Proveer la capacidad de cargar en los sistemas nuevos folios.  
   4. Ingresar Nuevos Estados. Proveer la capacidad de definir nuevos estados par ale gestión de los trámites.  
   5. Ingresar Nuevos Conceptos. Proveer la capacidad de definir nuevos conceptos, los cuales se incluyen en los presupuestos.  
7. **Administrar las plantillas**  
   1. Plantilla de Trámites.  
      1. Crear nuevas plantillas de trámites. De acuerdo a un trámite, se deberá poder seleccionar los documentos/certificados necesarios para ese trámite, según una lista predefinida de documentos disponibles.  
      2. Modificar plantillas de trámites. Según plantillas existentes, se deberá poder acceder a cualquiera de ella y modifica la composición de documentos/certificados asociados.  
      3. Eliminar Plantilla de trámites. Según plantillas existentes, se deberá poder eliminar/deshabilitar para su uso, determinadas plantilla para trámites.  
   2. Plantillas de Presupuestos.  
      1. Crear nuevas plantillas de Presupuestos. De acuerdo a un trámite, se deberá poder seleccionar los conceptos necesarios para esa plantilla de Presupuestos, según una lista predefinida de conceptos disponibles.  
      2. Modificar plantillas de Presupuestos. Según plantillas existentes, se deberá poder acceder a cualquiera de ella y modifica la composición de conceptos asociados.  
   3. Eliminar Plantilla de Presupuestos. Según plantillas existentes, se deberá poder eliminar/deshabilitar para su uso, determinadas plantilla para Presupuestos.

   3. ***Requerimientos de Rendimiento.***

1. El sistema no deberá consumir mas de 300MB de memoria RAM, debido a que superado este límite se puede provocar que la computadora de ralentice.  
2. No se deberá utilizar más del 50% del uso del procesador, por el mismo motivo que el punto anterior.  
3. Ninguna operación puede demorar más de 10 segundos en completarse, puesto que un tiempo de demora así puede provocar desagrado a los usuarios.

   1. ***Requerimientos de Interfaz***

1. El software deberá posee una interfaz grafica capaz de visualizar todos los formularios de manera secuencial, en cuanto a como se deben completar los campos que los componen. La navegación de los campos se deberá poder realizar, haciendo un clic con el puntero del ratón o a través de la tecla de tabulación.  
2. Todos los campos en donde existan valores predefinidos, se deberán presentar como listas desplegables o cajas de verificación según sea el caso.  
3. Los nombres de los campos a completar, deberán ser lo suficientemente significativos, de manera tal que no se generen confusiones o ambigüedades.  
4. No se deberán utilizar colores que dificulten la correcta visualización de los datos.  
5. En cada pantalla, se deberá identificar el proceso que se esta realizando, y el estado actual del mismo. El usuario deberá poder visualizar en donde se encuentra en todo momento.  
6. En todo momento se debe conocer el nombre del usuario que está trabajando en la sesión actual.

   1. ***Requerimientos de Seguridad y Privacidad.***

1. El acceso al sistema se deberá realizar mediante el ingreso de un nombre de usuario y contraseña.  
   1. Los datos de usuario y contraseña deberán estas cifrados.  
2. Se deberán tener en cuenta todas las políticas de seguridad requeridas para cualquier tipo de aplicación estándar. Esto incluye, la encriptación en la transferencia de datos en la comunicación cliente-servidor y la validación de acceso de los usuarios.

   1. ***Requerimientos de software para el sistema.***

1. Debido a que el producto de software será una aplicación Java, se deberá tener instalado el entorno de ejecución de Java, JRE 1.7 o superior.  
2. Sistema operativo independiente.

   1. ***Requerimientos de Hardware para el sistema.***

No aplicable.

2. ***Requerimientos para el desarrollo.***

1. Metodología de desarrollo: Se utilizará la metodología del Paradigma de Orientación a Objetos.  
2. Modelo de Desarrollo: A definir.  
3. Lenguaje de programación. Se utilizará el lenguaje de programación Java en su última versión estable, debido a que es uno de los lenguajes más utilizados para el desarrollo de aplicación Orientada a Objetos. Además, es un lenguaje de uso libre que no requiere el pago de ningún tipo de licencias de uso.  
4. Motor de Base de datos: Se utilizara el motor de base de datos libre MySQL en su última versión estable, debido a la gran compatibilidad y cantidad de información disponible de su uso junto con el lenguaje Java.  
5. El sistema deberá soportar múltiples usuarios conectados simultáneamente.

