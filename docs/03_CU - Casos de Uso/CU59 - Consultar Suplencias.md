<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu59-consultar-suplencias">CU59 – Consultar Suplencias</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/recepcionista.</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite listar las suplencias registradas.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td><p>El Gestor/recepcionista requiere una lista de o las suplencias registradas en un año indicado. El sistema busca y presenta una lista de todas las suplencias registradas para el año seleccionado, indicando para suplencia, los siguientes datos:</p>
<ul>
<li><p>El escribano suplantado,</p></li>
<li><p>El escribano suplente,</p></li>
<li><p>La fecha de inicio,</p></li>
<li><p>La fecha de fin de suplencia,</p></li>
<li><p>Y las observaciones.</p></li>
</ul></td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU61</td>
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
<td><p>1. El gestor/recepcionista solicita una lista de todas las suplencias.</p>
<p>3. El gestor/recepcionista indica el año.</p></td>
<td><p>2. Solicita que se indique el año para el cual buscar todas las suplencias registradas.</p>
<p>4. Busca y retorna una lista de todas las suplencias registrada para el período indicado.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                  |
|-----------------|------------------------------------------------------------------|
| **Excepciones** |                                                                  |
| **Actor**       | **Sistema**                                                      |
|                 | 4.1. No existen suplencias registradas para el período indicado. |
