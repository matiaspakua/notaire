<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu23-ver-registro-de-actividades-de-usuario">CU23 – Ver registro de actividades de Usuario</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite visualizar el historial de actividades de un Usuario.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano selecciona un Usuario de una lista de Usuarios disponibles. El sistema solicita que se indique un intervalo de fechas para auditar. El Escribano ingresa los datos requeridos, y el sistema presenta la lista de actividades llevadas a cabo por el Usuario.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 3.2, CU61</td>
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
<td><p>1. El Escribano decide ver el historial de actividades de un usuario.</p>
<p>3. Ingresa el nombre y apellido ó tipo y número de identificación de la persona.</p>
<p>5. Confirma, que es la persona indicada.</p>
<p>7. Imprime un reporte del historial de actividades del usuario.</p></td>
<td><p>2. Solicita nombre y apellido ó tipo y número de identificación de la persona.</p>
<p>4. Busca la persona asociada a los datos ingresados.</p>
<p>6. Muestra una lista, con las actividades realizadas por el usuario indicando:</p>
<ul>
<li><p>Fecha</p></li>
<li><p>Modulo</p></li>
<li><p>Nombre de usuario</p></li>
<li><p>Cambios realizados</p></li>
</ul>
<p>con la posibilidad de imprimir un reporte.</p></td>
</tr>
</tbody>
</table>

|                 |                                                       |
|-----------------|-------------------------------------------------------|
| **Excepciones** |                                                       |
| **Actor**       | **Sistema**                                           |
|                 | 6.1. El usuario no tiene un historial de movimientos. |
