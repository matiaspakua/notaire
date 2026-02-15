<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu43-reingresar-documentación">CU43 – Reingresar documentación</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite registrar el reingreso de un documento.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un documento ha sido devuelto observado o ha vencido. El Gestor/Escribano solicita el reingreso del mismo, seleccionando una gestión de una lista de gestiones disponibles. El sistema muestra los trámites asociados a la gestión seleccionada. El Gestor/Escribano selecciona un trámite, y el sistema muestra la documentación necesaria para el mismo. El Gestor/Escribano indica el tipo de documento a ser reingresado. El sistema agrega el tipo de documento seleccionado, a la lista de documentos presentados de la gestión.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU19</td>
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
<td><p>1. El Gestor/Escribano solicita la lista de gestiones de un cliente determinado.</p>
<p>3. El Gestor/Escribano selecciona una gestión.</p>
<p>5. El Gestor/Escribano selecciona un trámite.</p>
<p>7. El Gestor/Escribano indica el tipo de documento a ser reingresado.</p></td>
<td><p>2. Busca y presenta la lista de gestiones indicadas, indicando:</p>
<ul>
<li><p>Numero de gestión</p></li>
<li><p>Encabezado</p></li>
<li><p>Fecha de inicio</p></li>
<li><p>Estado</p></li>
<li><p>Numero de carpeta</p></li>
<li><p>Numero de bibliorato</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>4. Muestra la gestión indicada, detallando:</p>
<ul>
<li><p>Trámites asociados</p></li>
<li><p>Documentos por cada trámite</p></li>
</ul>
<p>6. Muestra la documentación necesaria para el trámite indicado.</p>
<p>8. Se agrega el tipo de documento seleccionado, a la lista de documentos presentados de la gestión.</p></td>
</tr>
</tbody>
</table>

|                 |             |
|-----------------|-------------|
| **Excepciones** |             |
| **Actor**       | **Sistema** |
|                 |             |
