## Purpose

Extiende el trace de workflow de una gestión y su visualización animada para
cubrir el circuito post-firma hasta inscripción, mostrando el bucle de
reingreso de un testimonio como una línea de movimientos en vez de forzarlo
dentro del grafo de nodos mutuamente excluyentes.

## ADDED Requirements

### Requirement: Estados de gestión para el circuito post-firma hasta inscripción
El sistema SHALL representar "Testimonio Generado", "Testimonio Ingresado a
Inscripción" y "Testimonio Retirado" como `EstadoDeGestion` propios,
alcanzables desde "Gestión con Escritura Firmada" en el `WorkflowDefinition`
estándar.

#### Scenario: Generar el testimonio de una escritura firmada avanza el estado de la gestión
- **WHEN** se genera el testimonio de una escritura firmada de una gestión cuyo tipo de trámite tiene asignado el `WorkflowDefinition` estándar
- **THEN** el estado de la gestión pasa a "Testimonio Generado"

#### Scenario: Ingresar el testimonio a inscripción avanza el estado de la gestión
- **WHEN** un testimonio en estado "Testimonio Generado" se presenta para inscripción
- **THEN** el estado de la gestión pasa a "Testimonio Ingresado a Inscripción"

#### Scenario: Retirar el testimonio avanza el estado de la gestión
- **WHEN** un testimonio inscripto se retira
- **THEN** el estado de la gestión pasa a "Testimonio Retirado"

### Requirement: El trace de workflow incluye los movimientos de testimonio de la gestión
`GET /api/v1/gestiones/{id}/workflow-trace` SHALL incluir, junto al trace de
nodos existente, los `MovimientoTestimonio` asociados a la gestión cuando
existan, sin alterar la forma del trace de nodos ya consumida por el resto
de la UI.

#### Scenario: Gestión con testimonio en curso incluye sus movimientos en el trace
- **WHEN** se consulta el trace de una gestión cuyo testimonio tiene al menos un `MovimientoTestimonio` registrado
- **THEN** la respuesta incluye la lista de movimientos ordenada cronológicamente, cada uno con su fecha de ingreso, fecha de salida (si existe) y si volvió observado

#### Scenario: Gestión sin testimonio no incluye movimientos
- **WHEN** se consulta el trace de una gestión cuya escritura todavía no fue firmada
- **THEN** la respuesta no incluye movimientos de testimonio, y el trace de nodos existente se comporta exactamente igual que antes de este cambio

#### Scenario: Un reingreso agrega un nuevo movimiento sin perder los anteriores
- **WHEN** un testimonio que ya tiene movimientos de ingreso/observación reingresa a inscripción
- **THEN** el trace incluye el nuevo movimiento junto a todos los movimientos previos del mismo testimonio, en orden cronológico

### Requirement: El diagrama animado muestra el conteo de reingresos del testimonio vigente
El diagrama animado de workflow de una gestión SHALL mostrar, en el nodo
"Testimonio Ingresado a Inscripción", el número de reingresos del
testimonio vigente cuando sea mayor a cero.

#### Scenario: Testimonio con reingresos muestra el conteo en el nodo de inscripción
- **WHEN** el trace de la gestión incluye un testimonio con 2 movimientos marcados como "volvió observado"
- **THEN** el nodo "Testimonio Ingresado a Inscripción" del diagrama animado muestra un indicador con el conteo de reingresos (2)

#### Scenario: Testimonio sin reingresos no muestra el indicador
- **WHEN** el trace de la gestión incluye un testimonio con un único movimiento de ingreso, sin observaciones
- **THEN** el nodo "Testimonio Ingresado a Inscripción" del diagrama animado no muestra ningún indicador de reingreso

#### Scenario: Gestión cuyo tipo de trámite no tiene el workflow post-firma configurado
- **WHEN** se consulta el trace de una gestión cuyo `WorkflowDefinition` asignado no incluye los nodos "Testimonio Generado"/"Testimonio Ingresado a Inscripción"/"Testimonio Retirado"
- **THEN** el diagrama animado se degrada al comportamiento actual (sin esos nodos ni la línea de movimientos), sin error visible para el usuario
