<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu64-buscar-tipo-de-tramite">CU64 – Buscar Tipo de tramite</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Gestor/Recepcionista</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Busca la lista de tipo de trámites disponibles.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Gestor/Recepcionista requiere utilizar un tipo de trámite, para lo cual solicita al sistema dicha lista. El sistema busca y devuelve una lista de todos los tipos de trámites registrados. El Escribano/Gestor/Recepcionista selecciona un tipo de tramite y el sistema muestra el detalle del mismo, en el cual se indica: El nombre del tipo de trámite, si se archiva o no, si se inscribe o no, si tiene asociado inmueble y las observaciones.</td>
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
<td><p>1. El Escribano/Gestor/Recepcionista requiere utilizar un tipo de trámite.</p>
<p>3. El Escribano/Gestor/Recepcionista selecciona un tipo de tramite.</p></td>
<td><p>2. Busca y presenta la lista de todos los tipos de trámites registrados.</p>
<p>4. Muestra el detalle del tipo de tramite, donde indica:</p>
<ul>
<li><p>El nombre del tipo de trámite</p></li>
<li><p>si se archiva o no</p></li>
<li><p>si se inscribe o no</p></li>
<li><p>si tiene asociado inmueble</p></li>
<li><p>las observaciones.</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                               |
|-----------------|-----------------------------------------------|
| **Excepciones** |                                               |
| **Actor**       | **Sistema**                                   |
|                 | 2.1. no existen tipo de tramites registrados. |
