<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu67-buscar-estados-de-gestión">CU67 – Buscar Estados de Gestión</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Busca la lista de estados de gestión registrados.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El escribano solicita la lista de estados de gestión registrados. El sistema busca y devuelve una lista de todos los estados de gestión registrados. El escribano selecciona uno de ellos y el sistema muestra el detalle del mismo, donde se indica: el nombre del estado de gestión y las observaciones.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>n/a</td>
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
<td><p>1. El escribano solicita la lista de estados de gestión.</p>
<p>3. El escribano selecciona uno de los estados de gestión.</p></td>
<td><p>2. Busca y devuelve una lista de todos los estados de gestión disponibles.</p>
<p>4. Muestra el detalle del estado de gestión seleccionado, donde se indica:</p>
<ul>
<li><p>El nombre del estado de gestión.</p></li>
<li><p>Las observaciones.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                 |
|-----------------|-------------------------------------------------|
| **Excepciones** |                                                 |
| **Actor**       | **Sistema**                                     |
|                 | 2.1. No existen estados de gestión registrados. |
