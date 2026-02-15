<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu19-buscar-gestiones-de-un-cliente">CU19 – Buscar gestiones de un Cliente</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano, Recepcionista/Gestor, Cliente.</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Presenta una lista de las gestiones realizadas por un Cliente.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Recepcionista busca todas las gestiones asociadas a un Cliente, indicando al sistema nombre, apellido ó tipo y número de identificación del mismo. El sistema muestra las gestiones encontradas. El Escribano/Recepcionista selecciona una gestión en particular, y el sistema muestra el detalle de la misma.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 2.4, CU61</td>
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
<td><p>1. El Escribano/Recepcionista solicita una gestión asociada a un cliente en particular.</p>
<p>3. El Escribano/Recepcionista indica los datos solicitados.</p>
<p>5. El Escribano/Recepcionista selecciona una gestión en particular.</p></td>
<td><p>2. Solicita que se indique el cliente por: nombre, apellido ó tipo y número de identificación del mismo.</p>
<p>4. El sistema muestra las gestiones encontradas asociadas al cliente, indicando:</p>
<ul>
<li><p>Número de gestión</p></li>
<li><p>Encabezado</p></li>
<li><p>Fecha de inicio</p></li>
<li><p>Estado</p></li>
<li><p>Numero de bibliorato</p></li>
<li><p>Observaciones.</p></li>
</ul>
<p>6. El sistema muestra el detalle de la gestión, indicando:</p>
<ul>
<li><p>Numero de gestión</p></li>
<li><p>Encabezado</p></li>
<li><p>Fecha de inicio</p></li>
<li><p>Escribano a cargo</p></li>
<li><p>Estado actual</p></li>
<li><p>Número de bibliorato</p></li>
<li><p>Trámites asociados</p></li>
<li><p>Clientes involucrados</p></li>
<li><p>Escrituras asociadas</p></li>
<li><p>Observaciones.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                 |
|-----------------|-------------------------------------------------|
| **Excepciones** |                                                 |
| **Actor**       | **Sistema**                                     |
|                 | 4.1. No existen gestiones asociadas al Cliente. |
