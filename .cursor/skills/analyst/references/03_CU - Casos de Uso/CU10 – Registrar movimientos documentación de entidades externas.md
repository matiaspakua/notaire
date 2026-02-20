<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu10-registrar-movimientos-de-documentación-de-entidades-externas">CU10 – Registrar movimientos de documentación de entidades externas</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite registrar la documentación entregada, para una gestión determinada.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Gestor/Escribano selecciona una gestión, de una lista de gestiones en trámite. El sistema muestra número de gestión, personas involucradas, trámites asociados, nomenclatura catastral en caso de tratarse de un inmueble, y documentación asociada a la misma, que debe ser presentada por entidades externas. El Gestor/Escribano registra para un documento los datos necesarios. Luego guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU19</td>
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
<td><p>1. El Gestor/Escribano solicita la lista de gestiones de un cliente determinado.</p>
<p>3. El Gestor/Escribano selecciona una gestión.</p>
<p>5. El Gestor/Escribano los datos correspondientes y confirma los cambios realizados.</p></td>
<td><p>2. Busca y presenta la lista de gestiones indicadas, indicando:</p>
<ul>
<li><p>Numero de gestión</p></li>
<li><p>Encabezado</p></li>
<li><p>Fecha de inicio</p></li>
<li><p>Estado</p></li>
<li><p>Numero de carpeta</p></li>
<li><p>Numero de bibliorato</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>4. Muestra la gestión indicada, detallando:</p>
<ul>
<li><p>Número de gestión,</p></li>
<li><p>Encabezado,</p></li>
<li><p>Fecha de inicio,</p></li>
<li><p>Escribano a cargo,</p></li>
<li><p>Nomenclatura Catastral si corresponde,</p></li>
<li><p>Documentación asociada a la misma, que debe ser presentada por entidades externas, indicando:</p>
<ul>
<li><p>Nombre documento,</p></li>
<li><p>Si fue preparado o no,</p></li>
<li><p>Número de cartón,</p></li>
<li><p>Fecha de ingreso,</p></li>
<li><p>Fecha de salida,</p></li>
<li><p>Si fue observado,</p></li>
<li><p>Monto deuda,</p></li>
<li><p>Fecha de pago,</p></li>
<li><p>Fecha de liberación,</p></li>
<li><p>Observaciones,</p></li>
<li><p>Finalizado o no</p></li>
</ul></li>
</ul>
<p>7. Guarda los cambios realizados y una vez que todos los documentos necesarios han sido registrados, se actualiza el estado de la gestión a “documentación completa”.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                          |
|-----------------|--------------------------------------------------------------------------|
| **Excepciones** |                                                                          |
| **Actor**       | **Sistema**                                                              |
|                 | 2.1. Para la gestión indicada ya fueron entregados todos los documentos. |

|                 |                                                   |
|-----------------|---------------------------------------------------|
| **Excepciones** |                                                   |
| **Actor**       | **Sistema**                                       |
|                 | 7.1. Alguno de los datos ingresados no es valido. |
