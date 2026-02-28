<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu16-archivar-gestión">CU16 – Archivar Gestión</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Gestor/Protocolista</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Finaliza una gestión.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Gestor/Protocolista selecciona de una lista de gestiones en trámite disponibles, una de ellas y cambia el estado de la gestión a “archivada” e indica observaciones. El sistema genera un nuevo número de archivo indicando número de bibliorato y número de carpeta, para la misma.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.9.1, CU19</td>
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
<td><p>1. El Escribano/Gestor/Protocolista solicita al sistema una lista de gestiones en trámite.</p>
<p>3. El Escribano/Gestor/Protocolista selecciona una gestión de la lista y la registra como “archivada” e indica algunas observaciones.</p></td>
<td><p>2. Busca y presenta la lista de gestiones indicada.</p>
<p>4. Registra los cambios realizados y genera un nuevo número de archivo indicando número de bibliorato y número de carpeta de archivo, para la misma.</p></td>
</tr>
</tbody>
</table>

|                 |                                                         |
|-----------------|---------------------------------------------------------|
| **Excepciones** |                                                         |
| **Actor**       | **Sistema**                                             |
|                 | 2.1. La gestión ya tiene un número de archivo asociado. |

**Nota**: Una gestión puede ser archivada por cancelarse o porque ha finalizado formalmente.
