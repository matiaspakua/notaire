<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu30-ingresar-nuevo-estado-de-gestión">CU30 – Ingresar nuevo estado de Gestión</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Da de alta un nuevo estado de gestión.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide dar de alta un nuevo estado, para ser utilizado en el proceso de las gestiones de escrituras. El sistema solicita los datos necesarios. El escribano ingresa los datos solicitados y guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.5</td>
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
<td><p>1. El Escribano decide dar de alta un nuevo estado de gestión.</p>
<p>3. El Escribano indica el nombre del nuevo estado, observaciones, y lo confirma.</p></td>
<td><p>2. Solicita que se ingrese el nombre del estado y observaciones.</p>
<p>4. Registra el nuevo estado.</p></td>
</tr>
</tbody>
</table>

|                 |                           |
|-----------------|---------------------------|
| **Excepciones** |                           |
| **Actor**       | **Sistema**               |
|                 | 4.1. El estado ya existe. |
