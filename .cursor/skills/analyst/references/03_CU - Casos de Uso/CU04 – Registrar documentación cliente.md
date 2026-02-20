<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu04-registrar-documentación-cliente">CU04 – Registrar documentación cliente</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Recepcionista, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Registra y controla documentos entregados por un Cliente.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Gestor/Recepcionista busca la gestión asociada a un cliente en particular. El sistema presenta toda la documentación necesaria para dicha gestión. El Gestor/Recepcionista verifica e indica los documentos entregados por el Cliente, y guarda los cambios realizados. Una vez que todos los documentos necesarios han sido registrados, se actualiza el estado de la gestión a documentación completa.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.3.5, CU19</td>
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
<td><p>1. El Gestor/Recepcionista busca la gestión asociada a un cliente en particular, indicando nombre y apellido, ó tipo y número de identificación del cliente.</p>
<p>3. El Gestor/Recepcionista verifica e indica los documentos entregados por el Cliente y los registra.</p></td>
<td><p>2. El sistema presenta:</p>
<ul>
<li><p>Numero de gestión,</p></li>
<li><p>Encabezado,</p></li>
<li><p>Fecha de inicio,</p></li>
<li><p>Escribano a cargo,</p></li>
<li><p>Documentos asociados:</p>
<ul>
<li><p>Tipo de documento</p></li>
<li><p>Si fue entregado o no</p></li>
</ul></li>
</ul>
<p>4. Guarda los cambios realizados y una vez que todos los documentos necesarios han sido registrados, se actualiza el estado de la gestión a “documentación completa”.</p></td>
</tr>
</tbody>
</table>

|                 |                           |
|-----------------|---------------------------|
| **Excepciones** |                           |
| **Actor**       | **Sistema**               |
|                 | 2.1. No existe la gestión |
