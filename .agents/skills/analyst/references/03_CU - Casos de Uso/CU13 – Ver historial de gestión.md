<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu13-ver-historial-de-gestión">CU13 – Ver historial de gestión</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite visualizar los antecedentes de una gestión.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Gestor/Escribano requiere información sobre una determinada gestión, por lo tanto busca en su lista de gestiones, aquella que le interesa, por nombre de cliente ó tipo y número de documento. El Gestor/Escribano selecciona la gestión deseada del resultado de la búsqueda. El sistema presenta el historial de la gestión, donde se listan todos los estados por los cuales ha pasado la gestión, indicando para cada estado: número de gestión, encabezado, fecha de inicio, estado, número de archivo, número de bibliorato y observaciones.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.6.1, CU19</td>
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
<td><p>1. El Gestor/Escribano requiere información sobre una determinada gestión, por lo tanto, solicita al sistema una lista de gestiones, indicando el nombre y apellido, ó tipo y número de identificación de un cliente.</p>
<p>3. El Gestor/Escribano selecciona una gestión en particular.</p></td>
<td><p>2. Busca y presenta una lista de gestiones, según los parámetros indicados.</p>
<p>4. El sistema presenta el historial de la gestión, donde figuran:</p>
<ul>
<li><p>Numero de Gestión</p></li>
<li><p>Encabezado</p></li>
<li><p>Fecha de inicio</p></li>
<li><p>Escribano a cargo</p></li>
<li><p>Estados por lo cuales ha pasado con sus fechas y observaciones asociados en cada paso.</p></li>
<li><p>Número de archivo,</p></li>
<li><p>Número de bibliorato.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                                               |
|-----------------|-------------------------------------------------------------------------------|
| **Excepciones** |                                                                               |
| **Actor**       | **Sistema**                                                                   |
|                 | 2.1. Dentro del resultado de la búsqueda, no se encuentra la gestión deseada. |
