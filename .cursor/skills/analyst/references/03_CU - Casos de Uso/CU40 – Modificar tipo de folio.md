<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu40-modificar-tipo-de-folio">CU40 – Modificar tipo de folio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite modificar un tipo de folio.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El escribano decide modificar el nombre y/o observaciones de un tipo de folio existente, para lo cual solicita al sistema la lista de todos los tipos de folios registrados. El sistema presenta la lista de folios disponibles, el escribano selecciona uno de ellos y cambia el nombre y/o observaciones del mismo. Luego el sistema registra los cambios indicados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU68</td>
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
<td><p>1. El escribano decide cambiar el nombre y/o observaciones de un tipo de folios.</p>
<p>3. El escribano selecciona uno de ellos y modifica el nombre y/o observaciones, y guarda los cambios realizados.</p></td>
<td><p>2. Presenta una lista de todos los tipos de folios registrados.</p>
<p>4. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                             |
|-----------------|---------------------------------------------|
| **Excepciones** |                                             |
| **Actor**       | **Sistema**                                 |
|                 | 2.1. No existen tipo de folios registrados. |

|                 |                                                  |
|-----------------|--------------------------------------------------|
| **Excepciones** |                                                  |
| **Actor**       | **Sistema**                                      |
|                 | 4.1. Alguno de los datos indicados no es válido. |
