# Test Case: Registrar Documentación Cliente

| Atributo | Detalle |
|---|---|
| **ID** | TC-004-registrar-documentacion-cliente-exitoso |
| **Título** | Registrar Documentación Cliente |
| **Nombre** | Registrar Documentación Cliente |
| **Referencia** | CU04 |
| **Descripción** | Validar que el sistema permite registrar la documentación técnica y legal de un cliente. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Flujo Principal** | Seleccionar un cliente y registrar su documentación asociada. | El sistema valida la información y asocia el documento al perfil del cliente. |
| **Validación de Archivos** | Intentar cargar un archivo de documento en un formato no permitido. | El sistema muestra un error indicando el formato de archivo no permitido. |
| **Validación de Datos** | Ingresar datos de documento sin los campos obligatorios. | El sistema impide el registro y muestra los errores de validación. |
| **Múltiples Documentos** | Verificar que el sistema permite registrar múltiples documentos para un mismo cliente. | El sistema permite agregar y asociar varios documentos a un mismo cliente. |
