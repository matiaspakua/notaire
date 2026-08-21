# Test Case: Preparar Presupuesto Exitoso

| Atributo | Detalle |
|---|---|
| **ID** | TC-001-preparar-presupuesto-exitoso |
| **Título** | Preparar Presupuesto Exitoso |
| **Nombre** | Preparar Presupuesto Exitoso |
| **Referencia** | CU01, RF #4 (Preparar Presupuestos) |
| **Descripción** | Validar que el sistema permite la creación exitosa de un presupuesto para una persona registrada. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Éxito: Registro de presupuesto** | El recepcionista busca una persona válida, selecciona un tipo de trámite y completa el formulario de presupuesto con todos los campos requeridos. | El sistema calcula el total automáticamente y confirma la creación del presupuesto con un número único. |
| **Validación de datos** | El sistema debe validar que los campos obligatorios (Nombre, Apellido, Tipo de Identificación, Número de Identificación) no estén vacíos. | El sistema muestra errores de validación y no permite continuar con la creación si faltan datos. |
| **Validación de ID** | El usuario intenta crear un presupuesto para un ID de persona que no existe. | El sistema muestra un error indicando que la persona no fue encontrada. |
| **Asignación de observaciones** | El usuario ingresa observaciones adicionales durante el proceso de creación. | Las observaciones se guardan correctamente en el registro del presupuesto. |
| **Cálculo de total** | El sistema debe sumar correctamente los valores de los ítems asociados al tipo de trámite seleccionado. | El total mostrado es la suma correcta de los conceptos asociados. |
