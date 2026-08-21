# Test Case: Procesar Solicitud de Presupuestos

| Atributo | Detalle |
|---|---|
| **ID** | TC-002-procesar-solicitud-presupuestos-exitoso |
| **Título** | Procesar Solicitud de Presupuestos |
| **Nombre** | Procesar Solicitud de Presupuestos |
| **Referencia** | CU01, RF #5 (Procesar solicitud de presupuestos) |
| **Descripción** | Validar la captura de datos esenciales de la persona (nombre, apellido, identificación, teléfono, correo) para generar un número de presupuesto y permitir su posterior aceptación. |

## Escenarios de Prueba

| Escenario | Descripción | Resultado Esperado |
|---|---|---|
| **Registro de Datos Básicos** | Ingresar todos los datos requeridos para una nueva solicitud de presupuesto. | El sistema permite el registro y genera un número de identificación para el presupuesto. |
| **Datos de Contacto** | Validar que los campos de teléfono y correo electrónico acepten los formatos correctos. | El sistema valida el formato de correo y teléfono antes de permitir el guardado. |
| **Generación de Número** | Verificar que el sistema asigne un número de presupuesto único y correlativo. | El sistema emite un número de presupuesto válido para la transacción. |
| **Validación de Identificación** | Intentar procesar una solicitud sin el tipo o número de identificación. | El sistema indica que estos campos son obligatorios y bloquea el envío. |
