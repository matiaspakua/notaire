<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu57-eliminar-tipo-de-trámite">CU57 – Eliminar tipo de trámite</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Elimina un tipo de trámite (deshabilita)</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide eliminar un tipo de trámite. El sistema presenta una lista de todos los tipos de trámite disponibles. El Escribano selecciona uno de ellos y confirma su eliminación.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.14, CU64</td>
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
<td><p>1. El Escribano decide eliminar un tipo de trámite en particular.</p>
<p>3. El Escribano selecciona un tipo de trámite.</p>
<p>5. El Escribano confirma la eliminación del tipo de trámite.</p></td>
<td><p>2. Muestra una lista de todos los tipos de trámite disponibles.</p>
<p>4. Busca y muestra la información del tipo de trámite seleccionado.</p>
<p>6. Elimina el tipo de trámite seleccionado.</p></td>
</tr>
</tbody>
</table>

|                 |                                               |
|-----------------|-----------------------------------------------|
| **Excepciones** |                                               |
| **Actor**       | **Sistema**                                   |
|                 | 2.1. No existen tipos de trámite disponibles. |
