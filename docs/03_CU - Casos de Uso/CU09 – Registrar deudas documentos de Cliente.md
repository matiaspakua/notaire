<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu09-registrar-deudas-documentos-de-cliente">CU09 – Registrar deudas documentos de Cliente</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Verifica si en la documentación que entrega el cliente, existen deudas para cancelar, y permite registrar el pago de la misma.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Gestor/Escribano busca las gestiones de un cliente, indicando el nombre y apellido o tipo y número de identificación del mismo, luego selecciona una gestión en particular. El sistema presenta el detalle de la gestión y la lista de todos los documentos entregados por el cliente. Además se indica, para los que presentan deudas, el monto de la misma, y de los que registran pago, la fecha en que se realizó el mismo. Para los documentos que presentan y que aún no han sido registrados los datos de la misma, el Gestor/Escribano indica el monto y/o fecha de pago de los mismos y guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.5.2, CU19</td>
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
<td><p>1. El Gestor/Escribano busca las gestiones de un cliente, indicando el nombre y apellido o tipo y número de identificación del mismo.</p>
<p>3. El Gestor/Escribano selecciona una gestión de la lista.</p></td>
<td><p>2. Busca y presenta la lista de gestiones del cliente indicado.</p>
<p>4. Presenta el detalle de la gestión y la lista de todos los documentos entregados por el cliente. Además se indica, para los que presentan deudas, el monto de la misma, y de los que registran pago, la fecha en que se realizó el mismo. Muestra por cada uno:</p>
<ul>
<li><p>Nombre,</p></li>
<li><p>Si presenta deuda o no,</p></li>
<li><p>Monto de deuda,</p></li>
<li><p>Fecha de pago</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                          |
|-----------------|----------------------------------------------------------|
| **Excepciones** |                                                          |
| **Actor**       | **Sistema**                                              |
|                 | 2.1. No existen gestiones asociadas al cliente indicado. |

|                 |                                                  |
|-----------------|--------------------------------------------------|
| **Excepciones** |                                                  |
| **Actor**       | **Sistema**                                      |
|                 | 4.1. No existen documentos con pagos pendientes. |
