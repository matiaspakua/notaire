<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu61-buscar-persona-o-cliente">CU61 – Buscar persona o cliente</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Recepcionista/Escribano, Persona/Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite buscar una persona o cliente.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un usuario necesita buscar una persona o cliente, por lo tanto indica el nombre y apellido ó el tipo y número de identificación de la persona que desea buscar. El sistema busca y devuelve el ó las personas (ya sean “personas” o “clientes”) que concuerdan con los datos indicados.</td>
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
<td><p>1. Un usuario necesita buscar una persona o cliente.</p>
<p>3. El usuario indica alguno de los datos solicitados.</p></td>
<td><p>2. Solicita que se indique alguno de los siguientes datos:</p>
<ul>
<li><p>el nombre y apellido</p></li>
<li><p>el tipo y número de identificación</p></li>
</ul>
<p>4. Busca y devuelve el o las personas o clientes que concuerdan con los datos indicados.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                             |
|-----------------|-----------------------------------------------------------------------------|
| **Excepciones** |                                                                             |
| **Actor**       | **Sistema**                                                                 |
|                 | 4.1. No existen personas o clientes que concuerden con los datos indicados. |
