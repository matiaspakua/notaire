<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu07-generar-testimonio">CU07 – Generar testimonio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite generar un testimonio de una escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano selecciona una escritura, de una lista de escrituras firmadas. El sistema muestra los datos principales de la escritura seleccionada. El Escribano indica datos necesarios para generar un testimonio. Una vez finalizado esto, se registra el nuevo testimonio y sus copias.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.7.1, CU62</td>
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
<td><p>1. El escribano desea generar el Testimonio de una Escritura por lo que selecciona el número de Registro de escribano de las escrituras a buscar, o el número de Escritura.</p>
<p>3. El Escribano selecciona una escritura.</p>
<p>5. El Escribano indica:</p>
<ul>
<li><p>Fecha de generación</p></li>
<li><p>Cantidad de copias a generar del Testimonio</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>Confirma la generación del Testimonio.</p></td>
<td><p>2. Presenta una lista de escrituras, donde figura:</p>
<ul>
<li><p>Escribano</p></li>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Estado</p></li>
<li><p>Matrícula y fecha de inscripción (si corresponde)</p></li>
</ul>
<p>de las Escrituras encontradas.</p>
<p>4. El sistema muestra los datos principales de la escritura seleccionada:</p>
<ul>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Folios utilizados</p></li>
</ul>
<p>6. Registra el nuevo Testimonio.</p></td>
</tr>
</tbody>
</table>

|                 |                                      |
|-----------------|--------------------------------------|
| **Excepciones** |                                      |
| **Actor**       | **Sistema**                          |
|                 | 2.1. No existen Escrituras Firmadas. |
