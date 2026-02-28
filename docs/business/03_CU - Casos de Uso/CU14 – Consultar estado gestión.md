<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu14-consultar-estado-gestión-eliminado">CU14 – Consultar estado gestión (ELIMINADO *)</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Cliente, Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite informar el estado actual de una gestión.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Cliente se acerca a la escribanía para consultar el estado de su gestión. El Gestor/Escribano solicita el nombre, apellido ó tipo y número de documento, para realizar la búsqueda. El sistema presenta una lista de gestiones asociadas al Cliente indicado. El Gestor/Escribano selecciona una gestión en particular. Se informa el estado actual y demás datos de la misma.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.7.1</td>
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
<td><p>1. El Cliente decide consultar el estado de su gestión.</p>
<p>2. El Gestor/Escribano solicita al Cliente el nombre, apellido ó tipo y número de documento, para realizar la búsqueda.</p>
<p>4. El Gestor/Escribano selecciona una gestión en particular.</p></td>
<td><p>2. Solicita nombre, apellido ó tipo y número de documento.</p>
<p>3. Busca las gestiones asociadas al cliente indicado y muestra una lista de las mismas.</p>
<p>5. Muestra:</p>
<ul>
<li><p>Estado actual,</p></li>
<li><p>Número de gestión,</p></li>
<li><p>Encabezado,</p></li>
<li><p>Fecha de inicio,</p></li>
<li><p>Escribano a cargo,</p></li>
<li><p>Número de bibliorato,</p></li>
<li><p>Trámites asociados,</p></li>
<li><p>Escrituras asociadas si corresponde,</p></li>
<li><p>Observaciones,</p></li>
<li><p>Nombre del/los clientes de la gestión seleccionada.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                                     |
|-----------------|-----------------------------------------------------|
| **Excepciones** |                                                     |
| **Actor**       | **Sistema**                                         |
|                 | No existen gestiones asociadas al Cliente indicado. |

**Nota**: Este caso de uso queda reemplazado por el la pantalla: Ver detalle Gestión, en la cual esta incluido el estado actual de la gestión.
