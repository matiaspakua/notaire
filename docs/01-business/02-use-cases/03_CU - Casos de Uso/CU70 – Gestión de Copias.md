<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu70-gestion-de-copias">CU70 – Gestión de Copias</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano, Gestor</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite gestionar las copias (testimonios) de las escrituras matrices.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El sistema registra cada copia emitida de una escritura, indicando quién la solicitó, la fecha de emisión y el tipo de copia (primer testimonio, segundo testimonio, etc.).</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 5.2, CU05, CU07</td>
</tr>
<tr class="odd">
<td><strong>GitHub_ID:</strong></td>
<td>#243</td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 49%" />
<col style="width: 50%" />
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
<td><p>1. El Escribano selecciona una escritura.</p>
<p>3. El Escribano indica la generación de una nueva copia.</p>
<p>5. El Escribano confirma la emisión.</p></td>
<td><p>2. El sistema muestra el detalle de la escritura y sus copias anteriores.</p>
<p>4. El sistema solicita el tipo de copia y el solicitante.</p>
<p>6. El sistema registra la copia y permite su impresión.</p></td>
</tr>
</tbody>
</table>

|                 |                                                              |
|-----------------|--------------------------------------------------------------|
| **Excepciones** |                                                              |
| **Actor**       | **Sistema**                                                  |
|                 | 4.1. La escritura no tiene un protocolo asociado aún.        |
