# Proceso de desarrollo

1. Una vez identificados los errores o nuevas feature, primero se deben detallar claramente en ISSUES de GitHub, con una descripción clara, criterios de aceptación y los LABEL adecuados. Ver la lista de LABEL disponibles y asignarlos.
2. Para cada ISSUE, lo primero es crear una nueva branch saliendo desde main, sincronizado con lo ùltimo (siempre hace pull antes de crear una branch).
2.1. Toda Issue deberá estar asociada a una funcionalidad (Caso de Uso del sistema), siempre, sin ecxepción.
2.2. En caso de no existir un Caso de Uso, entonces se deberà crearlo, documentación y actualizar toda la documentación asociada.
3. La branch debera seguir las convenciones de https://conventionalbranch.org/ y el ID de la issue de Github.
4. Una vez creada la branch e iniciado el proceso de trabajo, la issue en github debe pasar a IN PROGRESS.
5. Para la implementación, siempre iniciar con el enfoque TDD: analizar la issue, crear los test, hacer que fallen y luego implementar, hacer pasar los test, refactorizar y mejorar el còdigo.
5.1. Si el cambio involucra crear un nuevo endpoint, el mismo se debera implementar, testear, documentar (OpenAPI), y probar como el resto de los endpoints.
5.2. Todos los endpoints deberàn ser invocados desde la UI al menos una vez, esto se debe cumplir en todos los casos. Asegurar esta trazabilidad de una forma estandar, clara y sobre todo muy sencilla.
5.3. Si la implementación involcra cambios en la base de datos, entonces se deberà utilizar flyway para el control de versiones y gestión de configuraciones de la base de datos.
5.4. Si la implementación involucra UI, se deberán implementar las mejores prácticas de UI/UX para asegurar simplicidad, claridad, uso eficiente de la interfaz Humano-Maquina (formularios claros, sencillos, navegación, uso de click, texto en pantalla). Todas las pantallas/formularios deberàn poseer test completos E2E en playwrigh.
5.5.  Si se encuentra codigo muerto, eliminarlo. Si hay codigo duplicado, refactorizar para simplicar. Evitar complejidad ciclomatica y cognitiva, todo el código debe ser auto-explicativo, sencillo y facil de leer, seguir y entender. Evitar o refactorizar codigo complejo. No usar comentarios en el código salvo que sea extrictamente necesario.
6. Una vez finalizada la implemmentación, hacer el build, test y deploy local, ejecutar los test E2E con playwrigh y los test de integración de la API completos (nunca hacer skip de test salvo exceciones muy justificadas).
7. La implementación debera seguir las normas de KIS (Keep it simple): codigo limpio, sencillo, claro, facil de mantener, cambiar y evolucionar.
8. Siempre seguir el patron SRP (Single responsability principle), tanto en el código como en los test.
9. Una vez todos los test pases, se debera hacer commit, push y crear una PR.
10. Siempre se deberá revisar y actualizar la documentación del proyecto, desde documentos de negocio y documento de ingenieria. Los documentos deberán ser consistentes, legibles y estar actualizar. Evitar duplicar información (si se encuentras estos casos, centralizar la información en el lugar más coherente), si falta documentación agregarla/crearla, si sobra documentación o no es aplicable o es vieja, llevarla a una carpeta de "archivo".


# Errores
1. Documentos -> Nuevo Documento -> Error al crear nuevos documentos.

2. Administración -> Usuarios -> Error al editar datos de usuario.

3. Administración -> Usuarios -> Al eliminar usuario, el mensaje dice que fue con exito, pero el usuario no se elimina.

4. Administración -> Folios -> no se puede crear Folios por que no hay "Estados" disponibles o no se sabe cuales son los estados disponibles. Para este caso, investigar en la documentación cuales son los estados de un folio, hacer que estos estados aparezcan como una lista desplegable y probar que se pueda, crear, editar, filtar, listar y eliminar folios. Para los casos de Editar y Eliminar, permitirlo solo en el caso de que no estén siendo utilizados.

5. Administración -> Nueva Plantilla de Presupuesto. Error al crear plantilla de presupuesto. Revisar todos las listas de seleccion de la aplicación, cuando el usuario seleccciona uno de los elementos no se settea automaticamente sino hasta que se hace ENTER, cambiar esto, la selección con el click o el ENTER deberían tener el mismo comportamiento. Otra falla es que al seleccionar un tipo de tramite en la selección aparece el ID en lugar del nombre, lo mismo ocurre con Concepto, aparece el ID en lugar del Nombre, corregir este comportamiento aqui y en todos los lugares donde en lugar de poner el nombre de la entidad se ponga el ID. El ID normalmente no se utiliza salvo para identificar un numero de gestión o nùmero de tramite. Corregir este comportamiento en todo el sistema.

# Nueva Feature
1. Administración -> Usuarios. Los tipos y permisos (a modulos del sistema) no se pueden editar. Agregar la posibilidad de crear nuevo roles de usuario y asignarles accesos y permisos a distintos modulos del sistema.

2. Administración -> Conceptos -> No se pueden Editar ni filtar ni eliminar conceptos. Para Editar (si el concepto ya està siendo utilizado, no se puede editar, sino que se tiene que crear uno nuevo). Lo mismo para eliminar, solo se puede eliminar si no está siendo utilizado (integridad referencial)

3. Administración -> Tipos de Documentos -> No se pueden Editar ni filtar ni eliminar Tipos de Documentos. Para Editar (si el tipo de documentos ya està siendo utilizado, no se puede editar, sino que se tiene que crear uno nuevo). Lo mismo para eliminar, solo se puede eliminar si no está siendo utilizado (integridad referencial)

4. En Administración faltan un barra superior (breadcrumb) para poder navegar hacia atras en la jerarquia de niveles de los formularios.

5. Administración -> Tipo de Tramite: Agregar la posibilidad de editar, filtar y elimianr tipos de tramites. En Editar y eliminar tener en cuenta la integridad referencial, si ya estàn siendo usado en otras entidades, entonces no se pueden eliminar.

6. Administración -> Estado de Gestion. Mostrar como un diagrama de grafos cada uno de los estados, donde hay un nodo inicial, nodos intermedios que pueden ser secuenciales o paralelos y uno o más nodos finales. EL administrados debera ser capas de organizarlos, reordenarlos y poner restrucciones respecto al estado de las entidades asociadas para poder llevar un control, estado, registro y seguimientos detallado del estado de cada tramite durance el ciclo de vida de las gestiones. Es importante que haya disintos tipos de workflows de grafos, de manera que el escribano pueda organizar su trabajo en workflows  y sabes paso a paso que se debe hacer, el estado de cada tramite y como debe continuar. El administrador debera validar que el workflow es consistente con todos los tramites (y entidades, referncias, claves foraneas, etc) antes de "habilitar" un workflow, para que una gestión pueda finalizar end-to-end.

7. Administración -> Estado de Gestion. Se debe permite editar, filtar y eliminar estados de gestión. Para los casos de editar y eliminar, se deberá asegurar la integridad referencia.

