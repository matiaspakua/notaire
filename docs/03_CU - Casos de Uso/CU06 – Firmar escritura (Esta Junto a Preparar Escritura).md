<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu06-firmar-escritura">CU06 – Firmar escritura</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite aprobar una escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El/los Clientes se acercan a la escribanía para firmar una escritura. El Escribano selecciona de la lista de escrituras disponibles para firmar, aquella correspondiente. Luego se realiza la firma de la escritura, por cada una de las partes, incluido el Escribano. Finalmente éste indica al sistema que la escritura ha sido firmada.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.6.2, CU62</td>
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
<td><p>1. Un/os Clientes se acercan a la escribanía para firmar una escritura.</p>
<p>2. El Escribano solicita la lista de escrituras disponibles para firmar.</p>
<p>4. El Escribano selecciona la escritura necesaria.</p>
<p>6. El Escribano indica que la escritura ha sido firmada.</p></td>
<td><p>3. Busca y presenta una lista de escrituras habilitadas para firmar.</p>
<p>5. Muestra la información de la escritura seleccionada:</p>
<ul>
<li><p>Número de Escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Estado</p></li>
<li><p>Folios utilizados</p></li>
<li><p>Matrícula de inscripción y fecha (si corresponde)</p></li>
<li><p>Observaciones</p></li>
<li><p>Clientes Involucrados</p></li>
</ul>
<p>7. Guarda los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                                                                                             |             |
|---------------------------------------------------------------------------------------------|-------------|
| **Excepciones**                                                                             |             |
| **Actor**                                                                                   | **Sistema** |
| 4.1. La escritura no se encuentra dentro de la lista de escrituras disponibles para firmar. |             |
