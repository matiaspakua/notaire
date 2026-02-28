<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu21-modificar-usuario">CU21 – Modificar Usuario</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite modificar alguno de los datos de un Usuario.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide modificar alguno de los datos de un Usuario, por lo tanto, solicita al sistema que le presente una lista de todos los Usuarios registrados. El sistema presenta la lista solicitada y el Escribano selecciona un Usuario. El sistema presenta los datos del Usuario seleccionado. El Escribano modifica alguno de los datos y guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 3.4, CU61</td>
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
<td><p>1. El Escribano decide modificar alguno de los datos de un Usuario.</p>
<p>3. El Escribano selecciona un Usuario de la lista.</p>
<p>5. El Escribano modifica alguno de los datos y guarda los cambios realizados.</p></td>
<td><p>2. Muestra una lista de Usuarios disponibles.</p>
<p>4. Presenta los datos del Usuario seleccionado: nombre, apellido, nombre de usuario, tipo de usuario y estado.</p>
<p>6. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                                        |             |
|----------------------------------------|-------------|
| **Excepciones**                        |             |
| **Actor**                              | **Sistema** |
| 3.1. El Usuario no existe en la lista. |             |

|                 |                                                  |
|-----------------|--------------------------------------------------|
| **Excepciones** |                                                  |
| **Actor**       | **Sistema**                                      |
|                 | 6.1. Alguno de los datos indicados no es valido. |
