<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu42-informar-próximos-vencimientos">CU42 – Informar próximos vencimientos</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor, Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Informa los próximos vencimientos de los documentos.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td><p>Se verifica en todos los documentos presentados, si poseen cuales están próximos a vencer y se le informa al Gestor/Escribano el día de vencimiento de cada uno de los documentos, junto a los siguientes datos de cada uno:</p>
<ul>
<li><p>Nombre de documento,</p></li>
<li><p>Número de gestión,</p></li>
<li><p>Encabezado,</p></li>
<li><p>Si fue preparado,</p></li>
<li><p>Fecha de Ingreso,</p></li>
<li><p>Fecha de Salida,</p></li>
<li><p>Número de cartón,</p></li>
<li><p>Si fue observado,</p></li>
<li><p>Monto de deuda($),</p></li>
<li><p>Fecha de pago,</p></li>
<li><p>Fecha de liberación</p></li>
<li><p>Observaciones</p></li>
</ul></td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>n\a</td>
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
<td></td>
<td><p>1. Busca en todos los documentos presentados que estén próximos a vencer, y se informan los siguientes datos de cada uno:</p>
<ul>
<li><p>Nombre de documento,</p></li>
<li><p>Número de gestión,</p></li>
<li><p>Encabezado,</p></li>
<li><p>Si fue preparado,</p></li>
<li><p>Fecha de Ingreso,</p></li>
<li><p>Fecha de Salida,</p></li>
<li><p>Número de cartón,</p></li>
<li><p>Si fue observado,</p></li>
<li><p>Monto de deuda($),</p></li>
<li><p>Fecha de pago,</p></li>
<li><p>Fecha de liberación</p></li>
<li><p>Observaciones</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                               |
|-----------------|-----------------------------------------------|
| **Excepciones** |                                               |
| **Actor**       | **Sistema**                                   |
|                 | 1.1. No existen documentos próximos a vencer. |
