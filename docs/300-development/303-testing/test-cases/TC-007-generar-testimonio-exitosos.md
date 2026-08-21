# Test Case: Generar Testimonio Exitoso

| Atributo | Detalle |
|---|---|
| **ID** | TC-007-generar-testimonio-exitosos |
| **Título** | Generar Testimonio Exitoso |
| **Nombre** | Generar Testimonio Exitoso |
| **Referencia** | CU07 |
| **Descripción** | Validar que el sistema permite generar el testimonio de una escritura aprobada. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Flujo Principal** | Seleccionar una escritura firmada y generar su testimonio. | El sistema genera el testimonio asociado a la escritura y lo registra en la base de datos. |
| **Validación de Estado** | Intentar generar un testimonio para una escritura que no esté firmada. | El sistema muestra un error indicando que la escritura debe estar firmada antes de generar el testimonio. |
| **Cálculo de Copias** | Verificar que el sistema permite definir la cantidad de copias de testimonios a generar. | El sistema permite ingresar la cantidad de copias y registra la información correctamente. |
| **Visualización de Datos** | Verificar que los datos del testimonio generados coinciden con la información de la escritura. | El sistema muestra correctamente los datos del testimonio y los asocia con la escritura correcta. |
