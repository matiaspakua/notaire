<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu65-buscar-tipos-de-documentos">CU65 – Buscar Tipos de documentos</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Gestor/Recepcionista</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Busca la lista de todos los tipos de documentos registrados.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Gestor/Recepcionista solicita un tipo de documento en particular. El sistema busca y devuelve una lista de todos los tipos de documentos registrados. El Escribano/Gestor/Recepcionista selecciona uno de ellos y el sistema muestra el detalle del mismo, donde se indica: El nombre del tipo de documento, si se vence o no, los días de valides y qué entidad hace entrega del mismo (cliente o entidad externa).</td>
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
<td><p>1. El Escribano/Gestor/Recepcionista solicita un tipo de documento.</p>
<p>3. El Escribano/Gestor/Recepcionista selecciona un tipo de documento.</p></td>
<td><p>2. El sistema busca y devuelve una lista de todos los tipos de documentos registrados.</p>
<p>4. Muestra el detalle del tipo de documento, donde se indica:</p>
<ul>
<li><p>El nombre del tipo de documento</p></li>
<li><p>si se vence o no</p></li>
<li><p>los días de valides</p></li>
<li><p>Qué entidad hace entrega del mismo (cliente o entidad externa).</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                  |
|-----------------|--------------------------------------------------|
| **Excepciones** |                                                  |
| **Actor**       | **Sistema**                                      |
|                 | 2.1. No existen tipos de documentos registrados. |
