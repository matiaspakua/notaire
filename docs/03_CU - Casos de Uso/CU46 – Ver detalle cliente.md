<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu46-ver-detalle-cliente">CU46 – Ver detalle cliente</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista/Gestor</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite visualizar toda la información de un cliente.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El recepcionista necesita ver la información de un determinado cliente. El sistema solicita que se indique el nombre y apellido, o tipo y número de identificación para buscar al cliente deseado. El recepcionista ingresa los datos solicitados y se presenta toda la información asociada al cliente encontrado.</td>
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
<td><p>1. El recepcionista necesita ver la información de un determinado cliente.</p>
<p>3. El recepcionista ingresa los datos solicitados.</p></td>
<td><p>2. Solicita que se indique el nombre y apellido, o tipo y número de identificación para buscar al cliente deseado.</p>
<p>4. Busca y presenta la información asociada al cliente encontrado:</p>
<ul>
<li><p>Nombre</p></li>
<li><p>Apellido</p></li>
<li><p>Nacionalidad</p></li>
<li><p>fecha de nacimiento</p></li>
<li><p>estado civil / número de nupcias</p></li>
<li><p>ocupación</p></li>
<li><p>domicilio</p></li>
<li><p>teléfono / e-mail</p></li>
<li><p>tipo y número de identificación</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |                                     |
|-----------------|-------------------------------------|
| **Excepciones** |                                     |
| **Actor**       | **Sistema**                         |
|                 | 4.1. No existe el cliente indicado. |
