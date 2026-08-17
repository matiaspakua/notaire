# CU79 – Administrar Plantillas de Trámite

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU79 – Administrar Plantillas de Trámite |
| **Actores** | Escribano, Administrador |
| **Propósito** | Permite crear, modificar y eliminar/deshabilitar plantillas de trámites, definiendo la lista de documentos y certificados necesarios que se requerirán automáticamente al iniciar un trámite de ese tipo. |
| **Descripción** | El Escribano o Administrador gestiona la composición estándar de documentos y certificados oficiales exigidos para cada tipo de trámite notarial (ej. Compraventa, Donación, Hipoteca, Poder). Al crear o modificar una plantilla, selecciona los documentos requeridos desde el catálogo base y establece si son obligatorios o tienen vencimiento/deuda asociada. |
| **Tipo** | Primario / Administración |
| **Referencias Cruzadas** | RF #61 (Administrar plantillas), RF #62 (Plantillas de trámites), RF #63 (Crear nuevas plantillas de trámites), RF #64 (Modificar plantillas de trámites), RF #65 (Eliminar plantillas de trámites); CU26, CU27, CU03 |
| **GitHub ID** | #310 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano solicita administrar las plantillas de trámites. | Muestra la lista de plantillas de trámites existentes con sus tipos de trámite asociados y estado (activa/inactiva). |
| 2 | El Escribano selecciona la opción de crear una nueva plantilla o editar una existente. | Solicita seleccionar el tipo de trámite y muestra el catálogo de documentos y certificados disponibles. |
| 3 | El Escribano selecciona el tipo de trámite y compone la lista de documentos y certificados requeridos. | Valida la selección y presenta los documentos seleccionados con opciones de obligatoriedad y vigencia. |
| 4 | El Escribano confirma la configuración de la plantilla de trámite. | Guarda la plantilla, valida que no existan duplicados y emite confirmación de registro exitoso. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | El tipo de trámite ya cuenta con una plantilla activa | El sistema informa la existencia de la plantilla previa y ofrece la opción de modificarla. |
| 3.1 | No se seleccionó ningún documento o certificado para la plantilla | El sistema advierte que la plantilla debe contener al menos un documento o certificado requerido. |
| 4.1 | El Escribano solicita deshabilitar/eliminar una plantilla existente | El sistema verifica si existen gestiones activas en curso basadas en la plantilla y solicita confirmación antes de deshabilitarla para futuros trámites. |
