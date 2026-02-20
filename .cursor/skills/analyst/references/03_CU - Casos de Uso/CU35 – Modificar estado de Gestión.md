<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu35-modificar-estado-de-gestión">CU35 – Modificar estado de Gestión</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Modifica el nombre de un estado.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide modificar los atributos de un estado de gestión. El sistema presenta una lista de todos los estados disponibles y el Escribano selecciona uno se ellos. El sistema muestra el nombre y las observaciones del estado seleccionado. El Escribano modifica algunos de los atributos y guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.11, CU67</td>
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
<td><p>1. El Escribano decide modificar el nombre de un estado de gestión.</p>
<p>3. El Escribano selecciona un estado de la lista.</p>
<p>5. El Escribano modifica el estado u observaciones, y confirma los cambios.</p></td>
<td><p>2. Muestra una lista de todos los estados disponibles.</p>
<p>4. Muestra el nombre y las observaciones del estado.</p>
<p>6. Registra la modificación.</p></td>
</tr>
</tbody>
</table>

|                 |                                                 |
|-----------------|-------------------------------------------------|
| **Excepciones** |                                                 |
| **Actor**       | **Sistema**                                     |
|                 | 2.1. No existen estados de gestión registrados. |

|                 |                                                   |
|-----------------|---------------------------------------------------|
| **Excepciones** |                                                   |
| **Actor**       | **Sistema**                                       |
|                 | 6.1. Alguno de los datos ingresados no es válido. |
