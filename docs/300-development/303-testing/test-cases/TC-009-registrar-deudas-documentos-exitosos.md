# Test Case: Registrar Deudas Documentos Exitoso

| Atributo | Detalle |
|---|---|
| **ID** | TC-009-registrar-deudas-documentos-exitosos |
| **Título** | Registrar Deudas Documentos Exitosos |
| **Nombre** | Registrar Deudas Documentos Exitosos |
| **Referencia** | CU09 |
| **Descripción** | Validar que el sistema permite registrar deudas por documentos de un cliente para el seguimiento de su estado. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Flujo Principal** | Seleccionar un cliente y registrar una nueva deuda por un documento específico. | El sistema valida la información y registra la deuda correctamente asociada al cliente y al documento. |
| **Validación de Identificación** | Intentar registrar una deuda para un cliente que no existe en el sistema. | El sistema muestra un error indicando que el cliente no fue encontrado. |
| **Validación de Cantidades** | Ingresar valores negativos o inválidos para el monto de la deuda. | El sistema indica que los valores deben ser positivos y bloquea el registro. |
| **Verificación de Registro** | Validar que la deuda aparezca en el listado de deudas pendientes del cliente. | El sistema muestra la deuda correctamente en el historial de documentos del cliente. |
