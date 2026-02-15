<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu47--consultar-pago">CU47- Consultar pago</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Consulta los pagos realizados, por gestión.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Recepcionista/Escribano decide consultar los pagos realizados, hasta el momento, en base a una gestión en particular. El Recepcionista/Escribano busca la gestión por número de presupuesto, ó por nombre y apellido, ó tipo y número de identificación. El sistema presenta la lista de presupuestos asociados al cliente buscado. El Recepcionista/Escribano selecciona un presupuesto en particular, y el sistema detalla el mismo.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU19, CU60</td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 49%" />
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
<td><p>1. El Recepcionista/Escribano decide consultar los pagos realizados por un cliente.</p>
<p>3. El Recepcionista/Escribano ingresa los datos solicitados.</p>
<p>5. El Recepcionista/Escribano selecciona un presupuesto.</p></td>
<td><p>2. El sistema solicita, número de presupuesto, ó nombre y apellido, ó tipo y número de identificación del cliente, para la búsqueda.</p>
<p>4. Presenta la lista de presupuestos asociados al cliente.</p>
<p>6. Presenta:</p>
<ul>
<li><p>Número de gestión</p></li>
<li><p>Encabezado</p></li>
<li><p>Número de presupuesto</p></li>
<li><p>Total</p></li>
<li><p>Saldo</p></li>
<li><p>Número de pago</p></li>
<li><p>Monto de pago</p></li>
<li><p>Fecha de pago</p></li>
<li><p>Observaciones</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                       |
|-----------------|-------------------------------------------------------|
| **Excepciones** |                                                       |
| **Actor**       | **Sistema**                                           |
|                 | 4.1. No existen presupuestos asociados a la búsqueda. |
