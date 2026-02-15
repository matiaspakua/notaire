<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu62-buscar-escritura">CU62 – Buscar Escritura</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Recepcionista/Escribano, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Busca una escritura en particular.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un cliente se acerca a la escribanía para consultar algún dato de una escritura que realizó. El Gestor/Recepcionista/Escribano solicita que se indique qué escribano realizó la misma o el número de escritura. El sistema busca y muestra una lista de todas las escrituras realizadas por el escribano indicado y muestra el detalle de una de las escrituras seleccionadas (si se indico el número de escritura, simplemente se muestra el detalle de esa escritura), donde se indica: el número de escritura, la fecha de escrituración, un detalle del cuerpo, el estado y las observaciones.</td>
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
<td><p>1. Un cliente se acerca a la escribanía para consultar algún dato de una escritura que realizó.</p>
<p>2. Gestor/Recepcionista/Escribano solicita que se indique qué escribano realizó la misma o el número de escritura.</p></td>
<td>3. Busca y retorna una lista con las escrituras del escribano indicado (o el detalle correspondiente al número de escritura indicado).</td>
</tr>
</tbody>
</table>

|                 |                                                             |
|-----------------|-------------------------------------------------------------|
| **Excepciones** |                                                             |
| **Actor**       | **Sistema**                                                 |
|                 | 3.1. El escribano indicado no posee escrituras registradas. |

|                 |                                                                |
|-----------------|----------------------------------------------------------------|
| **Excepciones** |                                                                |
| **Actor**       | **Sistema**                                                    |
|                 | 3.2. El número de escritura indicado no es valido o no existe. |
