<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu15-procesar-pago">CU15 – Procesar pago</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Cliente, Recepcionista/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Registra el pago de un trámite de una gestión.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un Cliente se acerca a la escribanía para abonar una gestión. El Recepcionista/Escribano solicita los datos necesarios para realizar la búsqueda. El sistema presenta los presupuestos pendientes asociados al Cliente. El Recepcionista/Escribano selecciona un presupuesto de la lista, y se muestra la información correspondiente. El Cliente indica el monto que desea abonar, y el Recepcionista/Escribano genera un nuevo pago registrando el monto del mismo, fecha de pago y observaciones adicionales. Finalmente se genera el recibo común correspondiente.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.8.1, CU19, CU60</td>
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
<td><p>1. Un Cliente de acerca a la escribanía y solicita abonar un trámite de una gestión.</p>
<p>2. El Recepcionista/Escribano solicita el número de presupuesto, ó tipo y número de identificación, ó nombre y apellido del cliente, para realizar una búsqueda.</p>
<p>4. El Recepcionista/Escribano selecciona un presupuesto de la lista.</p>
<p>6. El Recepcionista/Escribano solicita al Cliente que le indique el monto del pago a realizar.</p>
<p>7. El Cliente indica el monto que desea abonar.</p>
<p>8. El Recepcionista/Escribano solicita generar un nuevo pago.</p>
<p>10. El Recepcionista/Escribano indica los datos solicitados y guarda los mismos.</p>
<p>12. Solicita la generación del recibo correspondiente.</p>
<p>14. El Recepcionista/Escribano hace entrega del recibo generado al Cliente.</p></td>
<td><p>3. Busca y presenta, el presupuesto encontrado, o la lista de presupuestos pendientes asociados a una gestión o al cliente indicado.</p>
<p>5. Muestra los datos del presupuesto seleccionado:</p>
<ul>
<li><p>Número de gestión a la que pertenece</p></li>
<li><p>Encabezado de gestión</p></li>
<li><p>Número de presupuesto</p></li>
<li><p>Trámite asociado</p></li>
<li><p>Ítems con los valores, porcentajes y observaciones correspondientes</p></li>
<li><p>Total</p></li>
</ul>
<p>9. Solicita el monto del mismo, fecha de pago y observaciones adicionales.</p>
<p>11. Registra el pago realizado, calcula el saldo pendiente y lo muestra.</p>
<p>13. Genera el recibo común correspondiente, donde figura:</p>
<ul>
<li><p>Fecha de Pago</p></li>
<li><p>Número de presupuesto</p></li>
<li><p>Número de pago</p></li>
<li><p>Nombre, Apellido, Tipo y número de identificación del Cliente</p></li>
<li><p>Número de la gestión asociada.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                                                                                      |                                                                 |
|--------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| **Excepciones**                                                                      |                                                                 |
| **Actor**                                                                            | **Sistema**                                                     |
| 3.1.2. El Recepcionista/Escribano informa al Cliente que no tiene deudas pendientes. | 3.1.1. El Cliente indicado no presenta presupuestos pendientes. |

|                 |                                                    |
|-----------------|----------------------------------------------------|
| **Excepciones** |                                                    |
| **Actor**       | **Sistema**                                        |
|                 | 3.2.1. El Cliente no tiene presupuestos asociados. |
