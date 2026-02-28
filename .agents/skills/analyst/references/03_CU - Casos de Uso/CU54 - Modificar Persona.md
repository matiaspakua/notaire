<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu54-modificar-persona">CU54 – Modificar Persona</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Recepcionista, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite modificar los datos de una persona.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Recepcionista solicita al Cliente su nombre y apellido ó tipo y número de identificación, para buscarlo en el sistema. El sistema muestra los datos pertenecientes a la persona, y éste indica que alguno es incorrecto. El Escribano/Recepcionista procede a modificar los datos incorrectos. EL Escribano/Recepcionista guarda los cambios realizados.</td>
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
<td><p>1. El Escribano/Recepcionista solicita al Cliente su nombre y apellido ó tipo y número de identificación.</p>
<p>3. La persona indica que algunos de los datos son incorrectos o han cambiado.</p>
<p>4. El Escribano/Recepcionista procede a modificar los datos incorrectos, ya sea:</p>
<ul>
<li><p>Nombre</p></li>
<li><p>Apellido</p></li>
<li><p>Tipo y número de identificación</p></li>
<li><p>Teléfono</p></li>
<li><p>Correo electrónico</p></li>
</ul>
<p>y confirma los cambios.</p></td>
<td><p>2. Busca la Persona, asociada a los datos ingresados y muestra los datos pertenecientes a la persona.</p>
<p>5. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                         |
|-----------------|-----------------------------------------|
| **Excepciones** |                                         |
| **Actor**       | **Sistema**                             |
|                 | 2.1.La persona no se encuentra cargada. |

|                 |                                           |
|-----------------|-------------------------------------------|
| **Excepciones** |                                           |
| **Actor**       | **Sistema**                               |
|                 | 5.1. Alguno de los datos son incorrectos. |
