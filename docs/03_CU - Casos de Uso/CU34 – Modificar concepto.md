<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu34-modificar-concepto">CU34 – Modificar concepto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Modifica los datos de un concepto.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide modificar los datos de un concepto. El sistema presenta la lista de conceptos disponibles y el escribano selecciona uno de ellos. El sistema presenta los datos del mismo. El escribano modifica alguno de los datos indicados y guarda los cambios.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.10, CU66</td>
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
<td><p>1. El Escribano decide modificar los datos de un concepto.</p>
<p>3. El Escribano selecciona uno de ellos.</p>
<p>5. El escribano modifica alguno de los datos indicados.</p></td>
<td><p>2. Presenta la lista de conceptos disponibles.</p>
<p>4. Presenta:</p>
<ul>
<li><p>Nombre del concepto</p></li>
<li><p>Valor</p></li>
<li><p>Porcentaje</p></li>
<li><p>Si es valor fijo o variable</p></li>
</ul>
<p>6. Guarda el concepto modificado.</p></td>
</tr>
</tbody>
</table>

|                 |                                        |
|-----------------|----------------------------------------|
| **Excepciones** |                                        |
| **Actor**       | **Sistema**                            |
|                 | 2.1. No existen conceptos disponibles. |
