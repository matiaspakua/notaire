<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu73-registro-de-auditoria">CU73 – Registro de Auditoría</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Administrador</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite consultar el historial de acciones realizadas por los usuarios en el sistema.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El sistema registra automáticamente todas las operaciones sensibles (creación, modificación, eliminación) y permite al administrador filtrar y visualizar quién realizó qué acción y cuándo.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 8.1, CU23</td>
</tr>
<tr class="odd">
<td><strong>GitHub_ID:</strong></td>
<td>#309</td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 49%" />
<col style="width: 50%" />
</colgroup>
<tbody>
<tr class="odd">
<td colspan="2"><strong>Curso de Eventos</strong></td>
</tr>
<tr class="even">
<td><strong>Actor</strong></td>
<td><strong>Sistema</strong></td>
</tr>
<tr class="odd">
<td><p>1. El Administrador selecciona la opción de Auditoría.</p>
<p>3. El Administrador aplica filtros (usuario, fecha, tipo de operación).</p>
<p>5. El Administrador visualiza el detalle de una operación.</p></td>
<td><p>2. El sistema muestra la lista general de eventos de auditoría.</p>
<p>4. El sistema filtra la lista y la presenta al administrador.</p>
<p>6. El sistema muestra los datos técnicos del cambio (valor anterior, valor nuevo).</p></td>
</tr>
</tbody>
</table>
