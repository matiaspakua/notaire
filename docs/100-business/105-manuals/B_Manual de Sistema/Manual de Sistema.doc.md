

*Sistema de*  
*Gestión Notarial*

![][image1]

Manual de Sistema

***Universidad FASTA***

*San Carlos de Bariloche*

*Carrera: Licenciatura en Sistemas*

*Cátedra: Seminario de Informática I – Cuarto año*

*Profesor: Alejandro Nikolic*

*Entrega: Año 2012*

*Alumnos:*

* *Juan Carlos Ramos*  
* *Luis Matías Miguez*  
* *Estefanía Klein*  
  


***Indice***

[Indice	3](#indice)  
[I.	Introducción	5](#heading)  
[1\.	Objetivo del sistema	5](#se-explicará-la-metodología-de-desarrollo-seleccionada,-las-herramientas-utilizadas,-los-elementos-de-análisis,-diseño-uml,-y-finalmente-los-modelos-de-implementación-concretos.)  
[2\.	Metodología de desarrollo	5](#administración-básica-del-sistema)  
[3\.	Herramientas de desarrollo	5](#patrón-singleton)  
[II.	Diagrama de Arquitectura general del sistema	6](#diagrama-de-arquitectura-general-del-sistema)  
[4\.	Arquitectura Cliente/Servidor	6](#diagrama-de-arquitectura-general-del-sistema)  
[5\.	Cliente	7](#heading-1)  
[6\.	GUI	7](#la-aplicación-de-cliente,-se-encuentra-implementada-como-un-modelo-de-capas,-definido-en-paquetes.-a-continuación-se-describen-cada-una-de-las-capas-\(tires\)-que-componen-el-sistema.)  
[7\.	Módulos / Pantallas (frames)	7](#la-implementación-utilizando-un-contenedor-principal-y-frames-permite-que-la-aplicación-cliente-sea-de-tipo-mdi-\(multiple-document-interface\),-en-la-cual-los-usuarios-pueden-utilizar-varios-módulos-simultáneamente.)  
[8\.	Negocio	8](#heading-2)  
[9\.	Persistencia	9](#heading-3)  
[10\.	Servicios	10](#heading-4)  
[III.	Interfaces entre Paquetes	12](#heading-5)  
[11\.	Interfaz GUI / Negocio (Vertical)	12](#cada-uno-de-los-paquetes-de-la-aplicación-tiene-una-interfaz-de-comunicación-homogénea-bien-definida.-a-continuación-se-explica-cada-interfaz:)  
[12\.	Interfaz Negocio / Persistencia (Vertical)	12](#la-comunicación-entre-la-capa-de-negocio-\(controllernegocio\)-y-cada-uno-de-los-elementos-de-la-capa-de-presentación-\(gui,-franes\)-se-realiza-utilizando-objetos-tipo-dto-\(data-transfer-object\).-cada-clase-de-negocio-tiene-su-correspondiente-dto-y-además-existen-dto-especiales-que-transportan-valores-que-no-representan-objetos-de-negocio,-como-por-ejemplo,-dtoflag,-cuya-responsabilidad-es-indicar-si-una-acción-fue-realizada-o-no-\(boolean\).)  
[13\.	Interfaz GUI-Negocio-Persistencia / Servicios (Horizontal)	12](#la-comunicación-entre-la-capa-de-negocio-y-la-capa-de-persistencia-se-realiza-utilizando-los-objetos-de-negocio-como-parámetros-para-clases-tipo-jpa-\(java-persistence-api\),-donde,-para-cada-clase-de-negocio-existe-su-correspondiente-jpa.-un-jpa-es-responsable-de-insertar,-modificar,-eliminar-y-buscar-instancias-de-objetos-de-negocio-persistidos-en-la-bbdd.)  
[IV.	Diagrama de casos de uso (actores y escenarios)	13](#heading-6)  
[14\.	Actores del Sistema	13](#relacion-de-\<externds\>:-cuando-un-caso-de-uso-“extiende”-la-funcionalidad-del-caso-de-uso-base.-tanto-el-caso-de-uso-“extendido”-como-el-“base”-pueden-ser-independientes-entre-sí.)  
[15\.	Diagramas de Casos de uso (por paquete)	13](#heading-7)  
[16\.	Presupuestos	14](#cada-uno-se-los-siguientes-diagramas-hace-referencias-a-un-módulo-del-sistema.-cada-módulo-contiene-varios-escenarios-de-casos-de-uso,-junto-con-los-actores-que-intervienen-en-el-mismo.)  
[17\.	Clientes	15](#heading-8)  
[18\.	Gestiones	15](#heading-9)  
[19\.	Gestión	16](#dentro-del-módulo-“gestiones”-existen-varios-sub-módulos.-a-continuación-se-muestran-cada-uno-por-separado.)  
[20\.	Documentación	17](#heading-10)  
[21\.	Escrituras	18](#heading-11)  
[22\.	Inscripciones	19](#heading-12)  
[23\.	Testimonios	20](#heading-13)  
[24\.	Protocolos	20](#heading-14)  
[25\.	Folios	22](#heading-15)  
[26\.	Pagos	23](#heading-16)  
[27\.	Administración	23](#heading-17)  
[28\.	Conceptos	24](#dentro-del-modulo-de-administración-se-encuentra-los-principales-sub-módulos-abm-de-la-aplicación.)  
[29\.	Documentos	25](#heading-18)  
[30\.	Escribanos	26](#heading-19)  
[31\.	Estados de Gestión	27](#heading-20)  
[32\.	Plantillas de Presupuestos	28](#heading-21)  
[33\.	Tipos de Folios	29](#heading-22)  
[34\.	Tramites	30](#heading-23)  
[35\.	Usuarios	31](#heading-24)  
[V.	Diagrama Entidad Relación / Compensación	32](#heading-25)  
[36\.	Diagrama Entidad-Relación	32](#diagrama-entidad-relación)  
[37\.	Diagrama de Compensación	32](#para-la-correcta-visualización-del-diagrama-ver:-diagrama-entidad-relación-en-el-archivo-del-proyecto-enterprise-architect:-notaire.ea)  
[VI.	Diagrama de Clases y Paquetes	33](#heading-26)  
[VII.	Estructura de las clases	34](#heading-27)  
[38\.	Interfaz serializable	34](#interfaz-serializable)  
[39\.	Annotations	34](#todas-las-clases-del-paquete-de-negocio-deben-implementar-la-interfaz-serializable.-este-es-un-requerimiento-del-framework-de-persistencia-hibernate.-esta-características,-junto-con-las-annotations-son-los-2-\(dos\)-elementos-básicos-que-requiere-el-framework-para-poder-realizar-el-mapeo-objeto-relacional.)  
[40\.	Clave no semántica	35](#heading-28)  
[41\.	Version	35](#debido-a-la-decisión-de-utilizar-claves-no-semánticas,-todas-las-clases-de-negocio-definen-un-atributo-idclasex,-el-cual-corresponde-a-la-clave-no-semántica-del-modelo-relación.)  
[42\.	DTO (Data Dransfer Object)	35](#caso-contrario,-no-se-puede-escribir-el-registro-debido-a-que-ha-sido-modificado-por-otra-aplicación.)  
[43\.	Setter/Setter DTO	35](#todos-los-dto’s-implementan-la-interfaz-“dtovalido”,-el-cual-implementa-un-método-que-debe-ser-redefinido-para-cada-dto-y-cuyo-propósito-es-el-de-validar-el-estado-del-dto-antes-de-ser-utilizado.)  
[44\.	Registro de Auditoria. Redefinición toString()	35](#debido-a-la-estrecha-relación-clase-dto,-cada-clase-define-dos-métodos-especiales-para-crear-un-dto-con-el-estado-actual-del-objeto-“getdto\(\)”;-y-un-segundo-método-que-permite-asignar-a-una-clase-valores-de-atributos-\(estado\)-en-base-a-un-dto,-“setdto\(\)”.)  
[45\.	JPA (Java Persistente Api)	35](#y-el-conjunto-de-datos-que-se-ha-modificado,-junto-con-los-valores-correspondientes.-la-redefinición-del-método-tostring\(\)-define-este-último-punto,-indicando-qué-valores-de-la-clase-de-negocio-se-registrar-cuando-son-modificados.)

1. **Introducción**

El presente documento tiene por objetivo explicar la estructura del sistema Notaire, y sus componentes, desde el punto de vista del desarrollador.  
Se explicará la metodología de desarrollo seleccionada, las herramientas utilizadas, los elementos de análisis, diseño UML, y finalmente los modelos de implementación concretos.

1. ***Objetivo del sistema***

Notaire consiste en un sistema de gestión de trámites notariales. Es una herramienta para mejorar y agilizar, el seguimiento y realización de los trámites pertenecientes a las gestiones de una escribanía.  
Para lograr su objetivo cuenta con las siguientes funciones:

* Administración de Clientes  
* Administración de Presupuestos  
* Administración de Gestiones  
* Administración de Protocolos  
* Administración básica del sistema

2. ***Metodología de desarrollo***

Se utilizó la metodología Orientada a Objetos para el análisis, diseño e implementación del sistema, de la cual se aplicaron patrones de diseño, y buenas prácticas de la ingeniería del software para resolver la construcción del sistema.  
Los patrones y aspectos de diseño implementados fueron:

* Desarrollo en capas (multi-tier)  
* Patrón Expert  
* Patrón Controller  
* Patrón Facade  
* Patrón Singleton

3. ***Herramientas de desarrollo***

* Lenguaje de programación

El lenguaje de programación utilizado fue Java, en su Versión 7 (JDK 1.7).

* IDE

El entorno de desarrollo utilizado fue Netbeans en su Versión 7.2

* Control de Versiones

Para el control de versiones se utilizó la plataforma Java.net como repositorio de código (servidor SVN) y el cliente Tortoise en su Versión 1.7.x.

* Framework de persistencia

El framework de persistencia utilizado fue Hibernate (ORM) en su Versión 3, junto con el Driver de conexión mysql-connector-java-5.1.14.

* Generador de Reportes

El generador de reportes utilizado fue Jaspers en su Versión 4.1.3, junto con la herramienta visual iReports.

* Base de datos

El motor de base de datos utilizado fue MySql en su versión 5.5.x, integrado con la herramienta phpMyAdmin en su versión 3.5.x y el servidor HTTP apache en su versión 2.2.x (todos estos componentes, integrados dentro de la herramienta WAMP).

* Documentación y diagramas

Para la documentación y los distintos diagramas se utilizaron las siguientes herramientas:

* Para documentos, planillas y diagramas genéricos: Microsoft Office (Word, Excel, Visio, Project) / LibreOffice 3.6  
  * Para diagramas UML y ERD: Enterprise Architect en su Versión 7.5  
  * Para imágenes e iconos: Corel Draw.  
2. **Diagrama de Arquitectura general del sistema**

4. ***Arquitectura Cliente/Servidor***

El sistema Notaire está desarrollado bajo un modelo de arquitectura Cliente-Servidor. El servidor consta del motor de base de datos MySql (donde está implementado el modelo de datos) y el cliente, el cual es un cliente “pesado” o “rico” (rich client), lo que significa que toda la lógica de negocio-transacciones  
seguridad-persistencia se encuentra implementada en la aplicación cliente.  
La comunicación entre el cliente y el servidor se realiza por medio de una red TCP/IP, no encriptada.  
El siguiente diagrama muestra la arquitectura general del sistema:

![][image2]

5. ***Cliente***

La aplicación de cliente, se encuentra implementada como un modelo de capas, definido en paquetes. A continuación se describen cada una de las capas (tires) que componen el sistema.

6. ***GUI***

El paquete GUI esta implementado con un JDesktopPane (contenedor principal), una barra de menú, una barra de herramientas (lateral) y un área de trabajo. En el área de trabajo, se crean y muestran, los distintos módulos o pantallas con sus respectivas funciones. Cada módulo se encuentra implementado utilizando un JinternalFrame, y estos frames son visibles sólo dentro del área de trabajo del contenedor principal.  
La implementación utilizando un contenedor principal y frames permite que la aplicación cliente sea de tipo MDI (multiple document interface), en la cual los usuarios pueden utilizar varios módulos simultáneamente.

7. ***Módulos / Pantallas (frames)***

Existen un conjunto bien definidos de tipos de módulos y pantallas, a saber:

- Tipo Alta-Baja-Modificación

Estos tipos de frames están compuestos por campos donde se ingresan o muestran datos, y permiten al usuario modificarlos de algún modo. En los frames de tipo Alta, se ingresan nuevos datos en la BBDD; en los frames de tipo Modificación, se buscan y modifican datos existentes en la BBDD; y finalmente en los frames de tipo Baja se eliminan o deshabilitan elementos de datos existentes en la BBDD.

- Tipo reportes

Estos tipos de frames muestra datos que han sido extraídos desde la BBDD y cuyo destino son reportes, como por ejemplo: lista de documentos para tipos de trámites, lista de documentos próximos a vencer, etc. Generalmente estos frames están implementados de manera tal que se muestra una grilla con los datos solicitados.

- Tipo Búsquedas

Estos tipos de frames son utilizados par ingresar un determinado conjunto de datos y realizar búsquedas con datos existentes en la BBDD. Todos los módulos de búsquedas estén implementados como una secuencia de dos pasos: el primero consisten en el ingreso de datos “claves” para realizar la búsqueda; el segundo paso consisten en el resultado de la búsqueda por parte de la aplicación en base a los datos ingresados. Generalmente el resultados de la busca devuelve un conjunto de datos, por lo cual, se utilizan grillas para mostrar dicho resultado.

- Tipo confirmaciones, advertencias y Errores

Estos tipos de frames son muy simples y únicamente son utilizados como carteles para proveer algún tipo de información, a saber: Confirmaciones, de que alguna acción ha sido realizada con éxito; Advertencia, que alguna tipo de acción requiere intervención del usuario (por ejemplo, datos mal ingresados); y Errores, que alguna acción no se ha podido realizar correctamente.

![][image3]

8. ***Negocio***

El paquete de Negocio está constituido por una clase principal denominada ControllerNegocio, que implementa el patrón Facade, Singleton y Controller. Se trata de un punto único de acceso al subsitema que compone el paquete de Negocio (Patrón Facade), es único y no admite múltiples instancias (Patrón Singleton), además es quien conoce la lógica del negocio para administra todos y cada uno de los distintos procesos que representan los Casos de Uso (Patrón Controller).  
Cada clase del paquete de Negocio tiene la responsabilidad de representar a una entidad del modelo de negocio de la escribanía. Esto requiere definir todos los atributos y métodos de acceso necesarios para representar, consulta y manipular su conjunto de datos adecuadamente.

![][image4]

9. ***Persistencia***

El paquete de persistencia esta compuesto por un conjunto de clases, todas las cuales, interactúan directamente con el Framework de persistencia utilizado, en este caso, Hibernate. Cada Clase se corresponde con su parte del paquete de negocio, definiendo un mapeo 1-a-1.  
Cada clase del paquete de persistencia tiene la responsabilidad de definir los métodos necesarios para poder: persistir, modificar, eliminar y buscar los elementos de datos correspondientes a la clase de negocio a la cual se corresponde.  
Todos los métodos de las clases de persistencia están definidos dentro del marco de una “transacción”, lo cual indica que cada método es un proceso que cumple con las propiedades ACID:

* Atomicidad (Atomicity): es la propiedad que asegura que la operación se ha realizado o no, y por lo tanto ante un fallo del sistema no puede quedar a medias.  
  * Consistencia (Consistency): es la propiedad que asegura que sólo se empieza aquello que se puede acabar. Por lo tanto, se ejecutan aquellas operaciones que no van a romper la reglas y directrices de integridad de la base de datos.  
  * Aislamiento (Isolation): es la propiedad que asegura que una operación no puede afectar a otras. Esto asegura que la realización de dos transacciones sobre el mismo conjunto de datos nunca generará ningún tipo de error.  
  * Permanencia (Durability): es la propiedad que asegura que una vez realizada la operación, ésta persistirá y no se podrá deshacer aunque falle el sistema.

![][image5]

10. ***Servicios***

El paquete de servicios esta compuesto por clases tipo controller-singleton para realizar tareas generales. Existen 3 (tres) clases definidas en este paquete:  
AdministradorValidaciones: tiene por responsabilidad la validación de campos de ingreso de datos y módulos. Contiene un conjunto de métodos cada uno con una tarea bien definida, por ejemplo: validar campo vació, validar campo alfa-numérico, validar campo solo texto, etc.  
AdministradorSeguridad: tiene por responsabilidad la validación de usuarios para login y la  encriptación de campos, por ejemplo: contraseñas.  
AdministradorSesion: tiene por responsabilidad registrar e identificar, para la sesión actual, el usuario registrado.

![][image6]

3. **Interfaces entre Paquetes**

Cada uno de los paquetes de la aplicación tiene una interfaz de comunicación homogénea bien definida. A continuación se explica cada interfaz:

11. ***Interfaz GUI / Negocio (Vertical)***

La comunicación entre la capa de negocio (controllerNegocio) y cada uno de los elementos de la capa de presentación (GUI, franes) se realiza utilizando objetos tipo DTO (data transfer object).  Cada clase de negocio tiene su correspondiente DTO y además existen DTO especiales que transportan valores que no representan objetos de negocio, como por ejemplo, DtoFlag, cuya responsabilidad es indicar si una acción fue realizada o no (boolean).

12. ***Interfaz Negocio / Persistencia (Vertical)***

La comunicación entre la capa de negocio y la capa de persistencia se realiza utilizando los objetos de negocio como parámetros para clases tipo JPA (Java Persistence Api), donde, para cada clase de negocio existe su correspondiente JPA. Un JPA es responsable de Insertar, Modificar, Eliminar y Buscar instancias de objetos de negocio persistidos en la BBDD.

13. ***Interfaz GUI-Negocio-Persistencia / Servicios (Horizontal)***

Todas las capas de la aplicación pueden comunicarse con una capa vertical denominada Capa de Servicios, la cual es responsable de proveer un conjunto de clases que realizan tareas genéricas y/o muy particulares, dependiendo de la necesidad de cada una de las capas horizontales. La esencia de la capa de servicios es agrupar ciertas tareas y/o funciones que son independientes entre sí y no pertenecen a ninguna de las capas horizontales. Esto quiere decir, que la capa de servicio provee soporte, mientras que las capas horizontales representan al negocio y las funciones asociadas.

4. **Diagrama de casos de uso (actores y escenarios)**

Los siguientes diagramas muestran los distintos escenarios de Casos de Uso del sistema junto con los actores involucrados en cada uno y los tipos de relaciones que los asocian.  
En los diagramas de Casos de Uso se pueden distinguir las siguientes relaciones:

* Asociación Directa: Cuando un Actor hace uso directo de un Caso de Uso, o sea, de la funcionalidad del mismo.  
  * Relación de \<include\>: Cuando un Caso de Uso “incluye” dentro de su escenario (llamado), la funcionalidad de otro caso de uso. Por ejemplo, el Caso de Uso “Crear Presupuesto”, incluye la funcionalidad del Caso de Uso “Buscar Persona/Cliente”.  
  * Relacion de \<externds\>: Cuando un Caso de Uso “extiende” la funcionalidad del caso de uso base. Tanto el caso de uso “extendido” como el “base” pueden ser independientes entre sí.

14. ***Actores del Sistema***

El siguiente diagrama muestra los distintos actores que participan en los casos de uso del sistema.

![][image7]

15. ***Diagramas de Casos de uso (por paquete)***

Cada uno se los siguientes diagramas hace referencias a un módulo del sistema. Cada módulo contiene varios escenarios de casos de uso, junto con los actores que intervienen en el mismo.

16. ***Presupuestos***

![][image8]

17. ***Clientes***

![][image9]

18. ***Gestiones***

Dentro del módulo “Gestiones” existen varios sub-módulos. A continuación se muestran cada uno por separado.

19. ***Gestión***

![][image10]

20. ***Documentación***

![][image11]

21. ***Escrituras***

![][image12]

22. ***Inscripciones***

![][image13]

23. ***Testimonios***

![][image14]

24. ***Protocolos***

Dentro del módulo “Protocolos” esta el sub-módulo Folios.  
![][image15]

25. ***Folios***

![][image16]

26. ***Pagos***

![][image17]

27. ***Administración***

Dentro del modulo de administración se encuentra los principales sub-módulos ABM de la aplicación.

28. ***Conceptos***

![][image18]

29. ***Documentos***

![][image19]

30. ***Escribanos***

![][image20]

31. ***Estados de Gestión***

![][image21]

32. ***Plantillas de Presupuestos***

![][image22]

33. ***Tipos de Folios***

![][image23]

34. ***Tramites***

![][image24]

35. ***Usuarios***

![][image25]

5. **Diagrama Entidad Relación / Compensación**

36. ***Diagrama Entidad-Relación***

El diagrama de Entidad-Relación detalla las distintas entidades del modelo de negocio, junto con sus atributos, y sus relaciones. En el ERD no solo se modelan los elementos concretos, sino también los  procesos (trámites, gestiones, seguimiento del estado, etc.), que se llevan a cabo en la escribanía.

Para la correcta visualización del diagrama ver: **Diagrama Entidad-Relación** en el archivo del proyecto Enterprise Architect: Notaire.ea

37. ***Diagrama de Compensación***

Describe a todas las entidades del ERD (junto con sus relaciones) y los mecanismos que se aplican cuando se realizan algunas de las siguientes operaciones:

* **Insertar**: Que sucede con la entidad referente cuando se inserta una nueva tupla en la entidad referenciada.  
* **Modificar**: Que sucede con la entidad referenciada cuando se modifica la entidad referente,  
* **Borrar**: Que sucede con la entidad referenciada cuando se borra la entidad referente,

Los mecanismo de compensación que se aplican, son los siguiente:

* **Impedir**: No se puede realizar ninguna operación.  
* **Cascada**: Se realiza la operación en cascada.  
* **NULL**: Se asigna nulo.

Para la correcta visualización del diagrama ver: **Diagrama Entidad-Relación** en el archivo del proyecto Enterprise Architect: Notaire.ea. Ver el elemento en la esquina superior izquierda.

6. **Diagrama de Clases y Paquetes**

El diagrama de clases y paquetes es la representación estática de cada uno de los elementos que componen el modelo de negocio de la escribanía, junto con los atributos (datos) que representa a cada entidad; los métodos (operaciones) accesibles para el conjunto de atributos; y las relaciones entre las distintas clases.

Para la correcta visualización del diagrama ver: **Diagrama de Clases** en el archivo del proyecto Enterprise Architect: Notaire.ea

En el proyecto EA, dentro de Diagrama de Clases se pueden distinguir 5 (cinco) paquetes principales, los cuales definen las 5 principales capas (tiers) de la aplicación:

* Gui  
* Dto  
* Negocio  
* Jpa  
* Servicios

7. **Estructura de las clases**

38. ***Interfaz serializable***

Todas las clases del paquete de negocio deben implementar la Interfaz **Serializable**. Este es un requerimiento del Framework de persistencia Hibernate. Esta características, junto con las Annotations son los 2 (dos) elementos básicos que requiere el Framework para poder realizar el mapeo Objeto-relacional.

39. ***Annotations***

Las anotaciones proveen información sobre un programa, sin ser parte del programa en sí. No tienen efecto directo en las operaciones del código en el cual se aplican. Algunos de los usos más comunes de las anotaciones son:

* Proveer información para el compilador: Las anotaciones pueden ser usadas por el compilador para detectar errores o suprimir advertencias.  
* Procesamiento en tiempo-de-compilación / tiempo-de-desarrollo: Herramientas de SW pueden procesar la información de las anotaciones para generar código, archivos XML, etc.  
* Procesamiento en tiempo de ejecución: Algunas anotaciones proveen la capacidad de ser examinadas durante la ejecución de un programa.

Las anotaciones pueden ser aplicadas en la declaración de clases, atributos, métodos y cualquier elemento de un programa.  
El framework de persistencia Hibernate requiere de las anotaciones para identificar las entidades (clases) a ser persistidas, junto con el conjunto de atributos (campos de las tablas) de cada entidad. Cada clase de negocio se mapea directamente como una entidad en el modelo relacional y describe en el área de definición de atributos las anotaciones correspondiente a cada uno, para poder ser mapeado correctamente.  
El siguiente es un ejemplo de anotaciones para la clase “Persona”:

@Entity  
@Table(name \= "personas")  
@XmlRootElement  
@NamedQueries(  
{  
    @NamedQuery(name \= "Persona.findAll", query \= "SELECT p FROM Persona p"),     
})  
public class Persona implements Serializable  
{  
    @Basic(optional \= false)  
    @Column(name \= "version")  
    @Version  
    private int version;  
    @OneToMany(cascade \= CascadeType.ALL, mappedBy \= "persona", fetch \= FetchType.LAZY)  
    private List\<TramitesPersonas\> tramitesPersonasList;  
    private static final long serialVersionUID \= 1L;  
    @Id  
    @GeneratedValue(strategy \= GenerationType.IDENTITY)  
    @Basic(optional \= false)  
    @Column(name \= "id\_persona")  
    private Integer idPersona;  
    @Basic(optional \= false)  
    @Lob  
    @Column(name \= "nombre")  
    private String nombre;  
    @Basic(optional \= false)  
    @Lob  
    @Column(name \= "apellido")  
    private String apellido;  
    .  
    .  
    .  
}

40. ***Clave no semántica***

Una clave semántica, es un atributo que define unívocamente a una entidad, y que posee un significado en el modelo de negocio.  
Todas las tablas del modelo relacional implementan claves “no semánticas”, o sea, no representativas del modelo de negocio. Esta decisión fue tomada como solución al problema de tener en varios casos claves semánticas compuestas y complejas de entender. Además, cada entidad posee su correspondiente clave semántica, la cual posee un significado en el modelo de negocio.  
Debido a la decisión de utilizar claves no semánticas, todas las clases de negocio definen un atributo **idClaseX**, el cual corresponde a la clave no semántica del modelo relación.

41. ***Version***

El control de concurrencia de objetos fue implementado utilizando una característica del framework de persistencia Hibernate, el cual define  y administra, para cada tabla del modelo relacional, un atributo denominado “**version**”.  
El proceso de control de concurrencia es el siguiente:

1. Cuando un objeto es creado y persistido por primera vez, hibernate asigna el valor 1 al atributo **version**.  
2. Cuando una aplicación  A y otra aplicación B, hacen lectura de un mismo registro, deben asegurarse de leer el valor del atributo versión.  
3. Independientemente de cual de las dos aplicaciones escriban el registro, deben controlar que el valor de versión existente en la BBDD (antes de la lectura) coincide con el valor actual (después de la lectura).   
4. Si es así, significa que el registro no ha cambiado y que la operación puede concretarse con éxito.  
5. Caso contrario, no se puede escribir el registro debido a que ha sido modificado por otra aplicación.

42. ***DTO (Data Dransfer Object)***

Los DTO son clases que se utilizan para transportar datos entre capas (tiers) de la aplicación. Cada clase de negocio define a un DTO, pero además existen DTO’s especiales, para propósitos muy particulares. El motivo de definir DTO especiales, es el de mantener homogénea la interfaz de comunicación entre dos capas.  
Todos los DTO’s implementan la interfaz “**DtoValido**”, el cual implementa un método que debe ser redefinido para cada DTO y cuyo propósito es el de validar el estado del DTO antes de ser utilizado.

43. ***Setter/Setter DTO***

Debido a la estrecha relación clase-dto, cada clase define dos métodos especiales para crear un DTO con el estado actual del objeto “**getDto()**”; y un segundo método que permite asignar a una clase valores de atributos (estado) en base a un DTO, “**setDto()**”.

44. ***Registro de Auditoria. Redefinición toString()***

En todas las clases de negocio se redefinió el método toString(). El propósito es para funcionamiento del mecanismo del registro de auditoria.   
El mecanismo de registro de auditoria está implementado de la siguiente manera: cada vez que se realice algún cambio en los datos y éstos deban ser persistidos (creación, modificación y/o eliminación), se invoca a un método del ControllerNegocio, el cual tiene como responsabilidad indicar:

* Desde qué **módulo** se esta realizando el cambios**,**   
* **Quién** realiza el cambio (usuario logueado),   
* **Momento** en el que se realiza el cambio (fecha-hora),  
* y el conjunto de datos que se ha modificado, junto con los valores correspondientes. La redefinición del método toString() define este último punto, indicando qué valores de la clase de negocio se registrar cuando son modificados.

45. ***JPA (Java Persistente Api)***

Los JPA son clases cuya responsabilidad es la de intermediar entre la capa de negocio y el Framework de   
Persistencia. Un JPA es responsable de Insertar, Modificar, Eliminar y Buscar elementos en la BBDD y construir los objetos de negocio correspondientes.  
Cada JPA tiene las siguientes características:

* **Interfaz Serializable**: Todos los JPA implementan la interfaz Serializable por requerimiento del Framework de persistencia Hibernate.  
* **Interfaz IPersistenciaJpa**: La interfaz IPersistencaJpa define el método: getNombreJpa(), el cual es utilizado por la clase ControllerJpa, cuya responsabilidad es mantener y administrar una lista de todos los JPA existentes.  
* **Métodos**: Cada JPA define los siguientes métodos:  
  * **create**: persiste objetos nuevos en la BBDD.  
  * **edit**: modifica objetos existentes en la BBDD.  
  * **destroy**: elimina objetos existentes en la BBDD.  
  * **find**/s: se utiliza para buscar objetos existentes en la BBDD.

[image1]: images/image1.png

[image2]: images/image2.png

[image3]: images/image3.png

[image4]: images/image4.png

[image5]: images/image5.png

[image6]: images/image6.png

[image7]: images/image7.png

[image8]: images/image8.png

[image9]: images/image9.png

[image10]: images/image10.png

[image11]: images/image11.png

[image12]: images/image12.png

[image13]: images/image13.png

[image14]: images/image14.png

[image15]: images/image15.png

[image16]: images/image16.png

[image17]: images/image17.png

[image18]: images/image18.png

[image19]: images/image19.png

[image20]: images/image20.png

[image21]: images/image21.png

[image22]: images/image22.png

[image23]: images/image23.png

[image24]: images/image24.png

[image25]: images/image25.png
