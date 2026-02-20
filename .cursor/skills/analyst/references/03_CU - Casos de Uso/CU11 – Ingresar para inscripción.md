<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu11-ingresar-para-inscripción">CU11 – Ingresar para inscripción</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Registrar el ingreso, para la inscripción, de un testimonio perteneciente a una escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Gestor/Escribano selecciona una escritura, de una lista de escrituras. El sistema muestra los datos de la Escritura, su Testimonio para Inscribir y sus respectivas nomenclaturas catastrales en caso de tratarse de un inmueble. El Gestor/Escribano indica los datos para ingresar el Testimonio a inscribir.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.5.3, CU62</td>
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
<td><p>1. El Gestor/Escribano decide Registrar la inscripción, de un testimonio, de una escritura.</p>
<p>3. El Gestor/Escribano selecciona un escritura, para registrar un inscripción.</p>
<p>5. El Gestor/Escribano registra: fecha en que fue ingresado para inscribir, número de cartón, junto con algunas observaciones.</p>
<p>7. El testimonio es llevado a inscribir.</p></td>
<td><p>2. El sistema muestra una lista de escrituras.</p>
<p>4. Muestra:</p>
<ul>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Folios utilizados</p></li>
<li><p>Nomenclaturas catastrales asociadas (si corresponde)</p></li>
</ul>
<p>Y solicita:</p>
<ul>
<li><p>Fecha de ingreso</p></li>
<li><p>Número de cartón</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>6. Registra lo datos ingresados.</p></td>
</tr>
</tbody>
</table>

|                 |                                                   |
|-----------------|---------------------------------------------------|
| **Excepciones** |                                                   |
| **Actor**       | **Sistema**                                       |
|                 | 2.1. No existen escrituras listas para inscribir. |
