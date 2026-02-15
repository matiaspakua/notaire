<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu28-ingresar-nuevos-folios">CU28 – Ingresar nuevos folios</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Registra un conjunto de folios de un Escribano.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un Escribano decide registrar el ingreso de nuevos folios. El sistema solicita: el número del primer y último folio del conjunto, número de registro de escribano y año. El escribano ingresa los datos indicados y guarda los cambios.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.3</td>
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
<td><p>1. Un Escribano decide registrar el ingreso de nuevos folios.</p>
<p>3. El escribano ingresa los datos indicados y guarda los cambios.</p></td>
<td><p>2. Solicita: el número del primer y último folio del conjunto, número de registro de escribano y año.</p>
<p>4. Se registra el nuevo conjunto de folios.</p></td>
</tr>
</tbody>
</table>

|                 |             |
|-----------------|-------------|
| **Excepciones** |             |
| **Actor**       | **Sistema** |
|                 |             |

\* El estado del folio será NUEVO por default.
