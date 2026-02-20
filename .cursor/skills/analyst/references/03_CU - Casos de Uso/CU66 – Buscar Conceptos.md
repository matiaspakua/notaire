<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu66-buscar-conceptos">CU66 – Buscar Conceptos</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Busca la lista de conceptos registrados.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El escribano solicita la lista de conceptos registrados. El sistema busca y devuelve una lista con todos los conceptos registrados. El escribano selecciona uno de ellos y el sistema muestra el detalle del mismo, donde se indica: el nombre, el valor, el porcentaje, y si se trata de un concepto con valor fijo o no.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario.</td>
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
<td><p>1. El Escribano solicita la lista de conceptos registrados.</p>
<p>3. Selecciona uno de los conceptos.</p></td>
<td><p>2. Busca y devuelve una lista con todos los conceptos registrados.</p>
<p>4. Muestra el detalle del concepto seleccionado:</p>
<ul>
<li><p>el nombre</p></li>
<li><p>el valor</p></li>
<li><p>el porcentaje</p></li>
<li><p>si se trata de un concepto con valor fijo o no.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                        |
|-----------------|----------------------------------------|
| **Excepciones** |                                        |
| **Actor**       | **Sistema**                            |
|                 | 2.1. No existen conceptos registrados. |
