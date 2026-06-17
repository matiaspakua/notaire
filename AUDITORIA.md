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


# Historial

El backlog original de Errores (#1-5) y Nueva Feature (#1-7) que vivía en
esta sección fue resuelto en su totalidad — ver issues cerrados #426-#436 y
#450-#455. El detalle histórico se conserva en
`docs/archive/testing-reports-2026-06/` para referencia, pero ya no es
accionable: nuevos errores o features deben reportarse como Issues de
GitHub nuevos siguiendo el "Proceso de desarrollo" de arriba, no agregados
a este archivo.
