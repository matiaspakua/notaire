# Test Case: Imprimir Presupuesto Exitoso

| Atributo | Detalle |
|---|---|
| **ID** | TC-003-imprimir-presupuesto-exitosos |
| **Título** | Imprimir Presupuesto Exitoso |
| **Nombre** | Imprimir Presupuesto Exitoso |
| **Referencia** | CU01, RF #7 (Imprimir presupuestos) |
| **Descripción** | Validar que el sistema permite la impresión de los presupuestos existentes en papel o formato PDF. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Impresión de Presupuesto Existente** | Seleccionar un presupuesto válido y solicitar su impresión. | El sistema genera la vista previa y permite la impresión en papel o la descarga como PDF. |
| **Validación de Disponibilidad** | Intentar imprimir un presupuesto que no existe o ha sido cancelado. | El sistema indica que el presupuesto no está disponible para impresión. |
| **Formato de Impresión** | Verificar que la información impresa coincida con los datos registrados (Nombre, Fecha, Conceptos, Totales). | El documento impreso refleja correctamente los datos del presupuesto seleccionado. |
| **Acceso a Impresión** | Validar que solo los usuarios con permisos de impresión puedan acceder a la función. | El sistema restringe el acceso a la impresión para usuarios sin permisos adecuados. |
