<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu37-eliminar-concepto">CU37 – Eliminar concepto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Elimina un tipo de concepto.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide eliminar un concepto. El sistema presenta una lista de todos los conceptos disponibles. El Escribano selecciona uno de ellos y confirma su eliminación.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.13, CU66</td>
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
<td><p>1. El Escribano decide eliminar un concepto.</p>
<p>3. El Escribano selecciona uno de ellos y confirma su eliminación.</p></td>
<td><p>2. Presenta una lista de todos los conceptos disponibles.</p>
<p>4. Elimina el concepto indicado.</p></td>
</tr>
</tbody>
</table>

|                 |                                        |
|-----------------|----------------------------------------|
| **Excepciones** |                                        |
| **Actor**       | **Sistema**                            |
|                 | 2.1. No existen conceptos disponibles. |
