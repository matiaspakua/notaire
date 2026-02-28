<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu63-buscar-folios">CU63 – Buscar Folios</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Busca folios</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El escribano requiere utilizar o modificar un folio o un conjunto de folios, para lo cual ingresa el número de registro de escribano y el año. El sistema busca y devuelve una lista de todos los folios asociados al registro de escribano indicado. El escribano selecciona uno o varios de los folios listados y el sistema presenta la siguiente información de cada uno: el número de folios, el año y el estado actual del mismo.</td>
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
<td><p>1. El escribano requiere utilizar o modificar un folio o un conjunto de folios</p>
<p>3. El escribano ingresa los datos solicitados.</p>
<p>5. El escribano selecciona uno o varios de los folios listados.</p></td>
<td><p>2. Solicita que se indique el número de registro de escribano y el año.</p>
<p>4. Busca y devuelve la lista de folios correspondientes al registro de escribano y el año indicados.</p>
<p>6. Por cada folio, indica:</p>
<ul>
<li><p>el número de folios</p></li>
<li><p>el año</p></li>
<li><p>el estado actual del mismo.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                                      |
|-----------------|----------------------------------------------------------------------|
| **Excepciones** |                                                                      |
| **Actor**       | **Sistema**                                                          |
|                 | 4.1. No existen folios registrados para el registro y año indicados. |
