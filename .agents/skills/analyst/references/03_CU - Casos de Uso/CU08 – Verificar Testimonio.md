<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu08-verificar-testimonio">CU08 – Verificar Testimonio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Indica la cantidad de testimonios expedidos para una escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano selecciona una escritura, de una lista de escrituras. El sistema presenta la información sobre los testimonios expedidos para la escritura seleccionada, indicando: número de testimonio, fecha de expedición, cantidad de copias generadas y observaciones.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>1.5.1, CU62</td>
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
<td><p>1. El Escribano decide verificar la cantidad de testimonios expedidos para una escritura, por lo que selecciona el número de Registro de escribano de las escrituras a buscar, o el número de Escritura.</p>
<p>3. El Escribano selecciona una escritura de la lista.</p></td>
<td><p>2. Presenta una lista de escrituras, donde figura:</p>
<ul>
<li><p>Escribano</p></li>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Estado</p></li>
<li><p>Matrícula y fecha de inscripción (si corresponde)</p></li>
</ul>
<p>de las Escrituras encontradas.</p>
<p>4. El sistema presenta la información sobre los testimonios expedidos para la escritura seleccionada, indicando:</p>
<ul>
<li><p>Número de testimonio</p></li>
<li><p>Fecha de expedición</p></li>
<li><p>Si está o no inscripto</p></li>
<li><p>Cantidad de Copias generadas</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>de cada Testimonio generado para esa Escritura.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                |
|-----------------|----------------------------------------------------------------|
| **Excepciones** |                                                                |
| **Actor**       | **Sistema**                                                    |
|                 | 2.1. La escritura seleccionada no tiene testimonios expedidos. |
