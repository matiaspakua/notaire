<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu17-dar-alta-persona">CU17 – Dar Alta persona</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista, Persona</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite dar de alta una nueva persona en la escribanía.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Una Persona se acerca a la escribanía para realizar algún trámite. El Recepcionista solicita a la Persona la información correspondiente para ser dada de alta en el sistema. La persona informa los datos solicitados, y el Recepcionista la registra.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 2.1, CU61</td>
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
<td><p>1. El recepcionista debe, dar de alta una persona.</p>
<p>3. El Recepcionista solicita que la Persona indiquen los datos solicitados.</p>
<p>4. La persona aporta los datos indicados.</p>
<p>5. El Recepcionista ingresa los datos y confirma los cambios.</p></td>
<td><p>2. Solicita los siguientes datos, de los cuales los 4 primero son obligatorios:</p>
<ol type="1">
<li><p>Nombre</p></li>
<li><p>Apellido</p></li>
<li><p>Tipo de identificación.</p></li>
<li><p>número de identificación</p></li>
<li><p>Teléfono</p></li>
<li><p>Correo electrónico de la persona.</p></li>
</ol>
<p>6. Registra los datos ingresados, dando de alta una persona.</p></td>
</tr>
</tbody>
</table>

|                 |                            |
|-----------------|----------------------------|
| **Excepciones** |                            |
| **Actor**       | **Sistema**                |
|                 | 6.1. La persona ya existe. |

|                 |                                           |
|-----------------|-------------------------------------------|
| **Excepciones** |                                           |
| **Actor**       | **Sistema**                               |
|                 | 6.2. Alguno de los datos son incorrectos. |
