<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu20-dar-alta-usuario">CU20 – Dar alta usuario</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite dar de alta un nuevo usuario.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide dar de alta un nuevo usuario, por lo tanto ingresa el nombre y apellido ó tipo y número de identificación para buscar a la Persona a ser registrada como Usuario. El sistema muestra los datos de la Persona encontrada y solicita los necesarios para asignarle un usuario. El Escribano ingresa los datos solicitados, y registra el nuevo Usuario.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 3.1, CU61</td>
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
<td><p>1. El Escribano decide dar de alta un nuevo usuario.</p>
<p>3. Ingresa el nombre y apellido ó tipo y número de identificación de la persona.</p>
<p>5. Confirma, que es la persona indicada.</p>
<p>7. Ingresa los datos solicitados y confirma la operación.</p></td>
<td><p>2. Solicita nombre y apellido ó tipo y número de identificación de la persona.</p>
<p>4. Busca la persona asociada a los datos ingresados.</p>
<p>Muestra los datos de la persona encontrada.</p>
<p>6. Solicita nombre de usuario, contraseña, tipo de usuario y estado (habilitado / deshabilitado).</p>
<p>8. Registra el nuevo usuario.</p></td>
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
<td><p>5.1 Concurrir a CU 17</p>
<p>5.2. Sigue paso 3</p></td>
<td>4.1. La persona no está registrada en el sistema.</td>
</tr>
</tbody>
</table>
