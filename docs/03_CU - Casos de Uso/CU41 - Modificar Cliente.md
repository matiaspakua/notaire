<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu41-modificar-cliente">CU41 – Modificar Cliente</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Recepcionista, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite modificar los datos de un Cliente.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Recepcionista solicita al Cliente su nombre y apellido ó tipo y número de identificación, para buscarlo en el sistema. El sistema muestra los datos pertenecientes al Cliente, y éste indica que alguno es incorrecto o ha cambiado. El Escribano/Recepcionista procede a modificar los datos incorrectos y guarda los cambios.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario.</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU61</td>
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
<td><p>1. El Escribano/Recepcionista solicita al Cliente su nombre y apellido ó tipo y número de identificación.</p>
<p>2. El cliente aporta los datos solicitados.</p>
<p>3. EL cliente indica que algunos de los datos son incorrectos o han cambiado.</p>
<p>4. El Escribano/Recepcionista procede a modificar los datos indicados y confirma los cambios.</p></td>
<td><p>2. Busca el Cliente, asociado a los datos ingresados y muestra los datos pertenecientes al Cliente.</p>
<ul>
<li><p>Nombre</p></li>
<li><p>Apellido</p></li>
<li><p>Tipo y número de identificación</p></li>
<li><p>Teléfono</p></li>
<li><p>correo electrónico</p></li>
<li><p>Nacionalidad</p></li>
<li><p>Fecha de nacimiento estado civil</p></li>
<li><p>Cuit/Cuil</p></li>
<li><p>En caso de ser casado/divorciado, número de nupcias</p></li>
<li><p>Sexo</p></li>
<li><p>Ocupación</p></li>
<li><p>Domicilio</p></li>
</ul>
<p>5. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                                       |
|-----------------|-------------------------------------------------------|
| **Excepciones** |                                                       |
| **Actor**       | **Sistema**                                           |
|                 | 2.1. La persona no se encuentra cargada como Cliente. |

|                 |                                           |
|-----------------|-------------------------------------------|
| **Excepciones** |                                           |
| **Actor**       | **Sistema**                               |
|                 | 5.1. Alguno de los datos son incorrectos. |
