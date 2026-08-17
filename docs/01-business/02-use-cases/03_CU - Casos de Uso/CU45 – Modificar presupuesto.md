# CU45 – Modificar presupuesto

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU45 – Modificar presupuesto |
| **Actores** | Escribano/Recepcionista |
| **Propósito** | Permite agregar un ítem extra a un presupuesto en particular. |
| **Descripción** | El Escribano/Recepcionista busca un presupuesto, y modifica alguno de los valores de los ítems o agrega un nuevo ítem con los datos correspondientes. Luego guarda los cambios realizados. |
| **Tipo** | Secundario |
| **Referencias Cruzadas** | RF #4 (Preparar Presupuestos), RF #7 (Imprimir presupuestos), RF #8 (Modificar presupuestos), RF #9 (Agregar ítems adicionales a los presupuestos); CU60 |
| **GitHub ID** | #198 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El Escribano/Recepcionista busca un presupuesto, por número de presupuesto o busca los presupuestos de un cliente en particular, por: Nombre, apellido o tipo y número de identificación. |  |
| 2 |  | Muestra una lista de presupuestos del cliente y por cada uno muestra: (Número y encabezado de gestión si corresponde.; Número de presupuesto.; Tipo de trámite; Fecha de emisión; Total; Saldo; Observaciones) |
| 3 | El Escribano/Recepcionista selecciona un presupuesto de la lista. |  |
| 4 |  | Muestra: (Nombre de la persona encontrada) (Nombre del trámite; Ítems [Nombre del Ítem, Valor, Porcentaje (valor variable), Observaciones (valor variable)]; Total del presupuesto; Observaciones; Detalle del inmueble asociado, si corresponde) |
| 5 | El Escribano/Recepcionista modifica las observaciones o ítems del presupuesto. |  |
| 6 | El Escribano solicita calcular el nuevo total. |  |
| 7 |  | Calcula el nuevo total, mostrando el resultado. |
| 8 | El Escribano confirma los cambios realizados. |  |
| 9 |  | Registra los cambios. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 2.1 | 1. La persona buscada no existe. | El sistema gestiona la excepción y notifica al usuario. |
| 2.1 | 2. La persona buscada no tiene presupuestos asociados. | El sistema gestiona la excepción y notifica al usuario. |
| 2.1 | 3. El presupuesto no existe. | El sistema gestiona la excepción y notifica al usuario. |
