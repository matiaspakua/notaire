<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu03-lista-documentos-y-certificados-necesarios">CU03 – Lista documentos y certificados necesarios</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Persona/Cliente, Recepcionista/Gestor</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Genera una lista de los certificados y documentos necesarios para un trámite.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Una Persona se acerca a la escribanía y solicita los documentos necesarios para realizar un determinado trámite. El Recepcionista consulta al sistema los documentos y/o certificados necesarios para el trámite, de una lista de trámites disponibles. El sistema presenta la información solicitada indicando: nombre, si vence o no, días de validez y quién hace entrega (Cliente/Entidad Externa)* de cada documento o certificado. Finalmente, se proporciona la información a la Persona.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.3.1, RF 1.3.2</td>
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
<td><p>1. Una Persona se acerca a la escribanía y solicita los documentos necesarios para realizar un determinado trámite.</p>
<p>2. El Recepcionista solicita a la Persona que le indique qué trámite desea realizar.</p>
<p>3. La Persona indica el trámite que desea realizar.</p>
<p>4. El Recepcionista solicita una lista de trámites disponibles.</p>
<p>6. El Recepcionista selecciona un trámite de la lista.</p>
<p>8. El Recepcionista solicita la impresión de la información obtenida.</p>
<p>10. El Recepcionista hace entrega de la lista de documentos y/o certificados necesarios para el trámite, a la Persona.</p></td>
<td><p>5. Busca y presenta una lista de trámites disponibles.</p>
<p>7. Presenta la información solicitada indicando por cada documento correspondiente:</p>
<ul>
<li><p>Nombre,</p></li>
<li><p>Si vence o no,</p></li>
<li><p>Días de validez</p></li>
<li><p>Quién hace entrega de cada documento o certificado.</p></li>
</ul>
<p>9. Realiza la impresión de la información solicitada.</p></td>
</tr>
</tbody>
</table>

|                 |                                                 |
|-----------------|-------------------------------------------------|
| **Excepciones** |                                                 |
| **Actor**       | **Sistema**                                     |
|                 | 7.1. No se encuentra la información solicitada. |

\*Nota: En caso de que el documento deba ser solicitado a una entidad externa, dicho trámite es gestionado por la escribanía.
