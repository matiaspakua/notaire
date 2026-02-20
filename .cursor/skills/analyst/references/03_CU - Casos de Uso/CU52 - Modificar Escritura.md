<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu52-modificar-escritura">CU52 – Modificar Escritura</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Modifica una escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano indica los datos necesarios para buscar la Escritura deseada. El Escribano modifica el detalle de la escritura, y luego guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario y esencial</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU62, CU63</td>
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
<td><p>1. Un Escribano decide modificar una Escritura por lo que selecciona el número de Registro de escribano de las escrituras a buscar, o el número de Escritura.</p>
<p>3. El Escribano selecciona una Escritura de la lista.</p>
<p>5. El Escribano modifica alguno de los datos de la escritura, y confirma los cambios realizados.</p></td>
<td><p>2. Muestra una lista de todas las Escrituras encontradas por el valor indicado.</p>
<p>4. Muestra la escritura seleccionada donde figura:</p>
<ul>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Números de folio utilizados (de una lista de folios disponibles)</p></li>
<li><p>Trámites involucrados</p></li>
<li><p>Cuerpo de escritura</p></li>
<li><p>Si fue firmada o no/anulada/no paso</p></li>
</ul>
<p>6. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                                       |
|-----------------|-------------------------------------------------------|
| **Excepciones** |                                                       |
| **Actor**       | **Sistema**                                           |
|                 | 2.1. No existen escrituras con los valores indicados. |

|                 |                                                                                               |
|-----------------|-----------------------------------------------------------------------------------------------|
| **Excepciones** |                                                                                               |
| **Actor**       | **Sistema**                                                                                   |
|                 | 4.1. No se puede modificar el folio hasta, ya que los folios disponibles no son correlativos. |
