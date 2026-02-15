<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu56-registrar-inscripción">CU56 – Registrar inscripción</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Registrar la inscripción, de un testimonio, perteneciente a una escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Luego de que es devuelto el testimonio ya inscripto, se registra: fecha de inscripción y el número de matrícula con el que fue inscripto y observaciones, donde finalmente queda habilitado para ser retirado por el cliente.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU62</td>
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
<td><p>1. El Gestor/Escribano decide registrar la inscripción de un testimonio, para una escritura.</p>
<p>3. El Gestor/Escribano selecciona un escritura, para registrar un inscripción.</p>
<p>5. El Escribano, ingresa los datos solicitados, y confirma la inscripción.</p></td>
<td><p>2. El sistema muestra una lista de escrituras que poseen testimonio para inscribir, y sus respectivas nomenclaturas catastrales en caso de tratarse de un inmueble.</p>
<p>4. Muestra:</p>
<ul>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Folios utilizados</p></li>
<li><p>Nomenclaturas catastrales asociadas (si corresponde)</p></li>
<li><p>Número de cartón</p></li>
<li><p>Fecha de ingreso</p></li>
</ul>
<p>Y solicita:</p>
<ul>
<li><p>Fecha de salida</p></li>
<li><p>Fecha de inscripción</p></li>
<li><p>Número de Matrícula</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>Para registrar la inscripción.</p>
<p>6. Registra los datos ingresados.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                       |
|-----------------|-----------------------------------------------------------------------|
| **Excepciones** |                                                                       |
| **Actor**       | **Sistema**                                                           |
|                 | 4.1. La escritura seleccionada no tiene testimonios para inscripción. |
