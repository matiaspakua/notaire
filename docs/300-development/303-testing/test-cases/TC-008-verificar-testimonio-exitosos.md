# Test Case: Verificar Testimonio Exitoso

| Atributo | Detalle |
|---|---|
| **ID** | TC-008-verificar-testimonio-exitosos |
| **Título** | Verificar Testimonio Exitoso |
| **Nombre** | Verificar Testimonio Exitoso |
| **Referencia** | CU08 |
| **Descripción** | Validar que el sistema permite verificar el estado y los detalles de un testimonio existente. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Flujo Principal** | Buscar y visualizar los detalles de un testimonio por su número o mediante la búsqueda de una escritura asociada. | El sistema muestra correctamente los detalles del testimonio, incluyendo estado, fechas y registros. |
| **Validación de No Encontrado** | Intentar consultar un número de testimonio que no existe en la base de datos. | El sistema muestra un mensaje indicando que no se encontraron resultados para ese número. |
| **Validación de Estado** | Verificar que el sistema muestra correctamente el estado actual del testimonio (por ejemplo, "Activo", "Cancelado", "Error"). | El sistema muestra el estado actual de forma clara y legible. |
| **Acceso a Detalles** | Verificar que el sistema permite ver los datos de fechas de registro y presentación de manera legible. | El sistema muestra correctamente todas las fechas y detalles solicitados por el usuario. |
