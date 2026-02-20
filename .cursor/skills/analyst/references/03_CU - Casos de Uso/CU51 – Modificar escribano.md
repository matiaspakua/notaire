<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu51-modificar-escribano">CU51 – Modificar escribano</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Administrador</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite modificar un escribano.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Administrador decide modificar los datos de un escribano, por lo tanto ingresa el nombre y apellido ó tipo y número de identificación para realizar la búsqueda. El sistema muestra los datos del Escribano encontrado y el Escribano/Administrador modifica los datos necesarios, guardando los cambios realizados</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
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
<td><p>1. El Escribano decide modificar un escribano.</p>
<p>3. Ingresa el nombre y apellido ó tipo y número de identificación de la persona.</p>
<p>5. Modificas el número de registro del escribano y confirma la operación.</p></td>
<td><p>2. Solicita nombre y apellido ó tipo y número de identificación de la persona.</p>
<p>4. Busca la persona asociada a los datos ingresados, permitiendo modificar el número de registro del escribano.</p>
<p>6. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                                   |
|-----------------|---------------------------------------------------|
| **Excepciones** |                                                   |
| **Actor**       | **Sistema**                                       |
|                 | 4.1. La persona no esta registrada en el sistema. |
