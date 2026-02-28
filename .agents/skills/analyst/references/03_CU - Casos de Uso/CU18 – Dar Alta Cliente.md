<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu18-dar-de-alta-cliente">CU18 – Dar de Alta Cliente</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista, Persona, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite dar de alta un Cliente.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Una Persona se acerca a la escribanía para iniciar un trámite, por lo tanto, el Recepcionista solicita nombre y apellido ó tipo y número de identificación para buscarla en el sistema. El sistema devuelve los datos de la Persona encontrada y solicita información adicional para dar de alta como cliente. El Recepcionista ingresa los datos y el Cliente queda registrado.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 2.2, CU61</td>
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
<td><ol type="1">
<li><p>Una Persona se acerca a la escribanía para iniciar un trámite.</p></li>
</ol>
<p>2. El Recepcionista solicita nombre y apellido ó tipo y número de identificación, para buscarla en el sistema.</p>
<p>4. El Recepcionista solicita los datos necesarios a la Persona:</p>
<ul>
<li><p>Nacionalidad</p></li>
<li><p>Fecha de nacimiento estado civil</p></li>
<li><p>Cuit/Cuil</p></li>
<li><p>En caso de ser casado/divorciado, número de nupcias</p></li>
<li><p>Sexo</p></li>
<li><p>Ocupación</p></li>
<li><p>Domicilio</p></li>
</ul>
<p>5. La Persona indica los datos solicitados.</p>
<p>6. El Recepcionista ingresa los datos y guarda los cambios.</p></td>
<td><p>3. Busca la Persona indicada y muestra:</p>
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
<p>7. Registra a la Persona como Cliente.</p></td>
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
<td colspan="2"><strong>Excepciones</strong></td>
</tr>
<tr class="even">
<td><strong>Actor</strong></td>
<td><strong>Sistema</strong></td>
</tr>
<tr class="odd">
<td><p>4.1. Concurrir a CU 17</p>
<p>4.2. Continuar en paso 2</p></td>
<td>3.1. No se encuentra la Persona indicada.</td>
</tr>
</tbody>
</table>

|                 |                                          |
|-----------------|------------------------------------------|
| **Excepciones** |                                          |
| **Actor**       | **Sistema**                              |
|                 | 7.1. Alguno de los datos no son validos. |

|                 |                                             |
|-----------------|---------------------------------------------|
| **Excepciones** |                                             |
| **Actor**       | **Sistema**                                 |
|                 | 7.2. El cliente ya se encuentra registrado. |
