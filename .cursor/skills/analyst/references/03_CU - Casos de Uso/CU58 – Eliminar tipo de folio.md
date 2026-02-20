<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu58-eliminar-tipo-de-folio">CU58 – Eliminar tipo de folio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite dar de baja/deshabilitar un tipo de folio.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El escribano decide dar de baja un tipo de folio existente, para lo cual solicita al sistema la lista de tipos de folios registrados. El sistema presenta la lista de todos los tipos de folios existentes. El escribano selecciona uno de ellos e indica que se deshabilite (dar de baja) y el sistema guarda los cambios indicados.</td>
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
<td><p>1. El escribano decide deshabilitar / dar de baja un tipo de folio y solicita al sistema la lista de folios disponibles.</p>
<p>3. El escribano selecciona un tipo de folio y guarda los cambios.</p></td>
<td><p>2. Busca y presenta una lista de todos los tipos de folios registrados.</p>
<p>4. Procede a cambiar el estado del tipo de folio a “deshabilitado” y guarda los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                              |
|-----------------|----------------------------------------------|
| **Excepciones** |                                              |
| **Actor**       | **Sistema**                                  |
|                 | 2.1. No existen tipos de folios registrados. |
