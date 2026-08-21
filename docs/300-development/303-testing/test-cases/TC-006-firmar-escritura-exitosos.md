# Test Case: Firmar Escritura Exitoso

| Atributo | Detalle |
|---|---|
| **ID** | TC-006-firmar-escritura-exitosos |
| **Título** | Firmar Escritura Exitoso |
| **Nombre** | Firmar Escritura Exitoso |
| **Referencia** | CU06 |
| **Descripción** | Validar que el sistema permite registrar la firma de una escritura por parte del escribano y los clientes intervinientes. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Flujo Principal** | Seleccionar una escritura preparada y marcarla como firmada por todas las partes requeridas. | El sistema valida la firma y actualiza el estado de la escritura a "Firmada". |
| **Validación de Firmas Faltantes** | Intentar finalizar la firma cuando falta una de las partes necesarias. | El sistema indica los errores de validación y no permite finalizar el proceso hasta que todas las firmas sean registradas. |
| **Registro de Firmas** | Verificar que el registro de firmas guarda correctamente la fecha y el usuario que realizó la acción. | El sistema guarda la fecha de la firma y el usuario que la autorizó. |
| **Acceso a Escrituras Firmadas** | Verificar que una escritura firmada sea visible en el listado de escrituras firmadas. | La escritura firmada aparece correctamente en la lista de escrituras. |
