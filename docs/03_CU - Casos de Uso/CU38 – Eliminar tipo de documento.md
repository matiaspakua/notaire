<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu38-eliminar-tipo-de-documento">CU38 – Eliminar tipo de documento</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Elimina un tipo de documento.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide eliminar un tipo de documento. El sistema presenta una lista de todos los tipos de documento disponibles. El Escribano selecciona uno de ellos y confirma su eliminación.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.14, CU65</td>
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
<td><p>1. El Escribano decide eliminar un tipo de documento en particular.</p>
<p>3. Selecciona un tipo de documento y confirma su eliminación.</p></td>
<td><p>2. Muestra una lista de todos los tipos de documento disponibles.</p>
<p>4. Elimina el documento seleccionado.</p></td>
</tr>
</tbody>
</table>

|                 |                                         |
|-----------------|-----------------------------------------|
| **Excepciones** |                                         |
| **Actor**       | **Sistema**                             |
|                 | 2.1. No existen documentos disponibles. |
