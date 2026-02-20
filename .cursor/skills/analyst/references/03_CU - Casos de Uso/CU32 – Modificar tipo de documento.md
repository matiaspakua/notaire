<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu32-modificar-tipo-de-documento">CU32 – Modificar tipo de documento</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Modifica las características de un documento.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide modificar los datos de un tipo de documento en particular. El sistema presenta una lista de todos los tipos de documento disponibles. El Escribano selecciona uno de ellos, y el sistema los datos del mismo. El Escribano modifica algunos de los datos solicitados y guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.8, CU65</td>
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
<td><p>1. El Escribano decide modificar los datos de un tipo de documento en particular.</p>
<p>3. Selecciona uno de ellos.</p>
<p>5. Modifica los datos necesarios y confirma los cambios realizados.</p></td>
<td><p>2. Muestra una lista de todos los tipos de documento disponibles.</p>
<p>4. Presenta:</p>
<ul>
<li><p>Nombre del documento</p></li>
<li><p>Si posee o no vencimiento</p></li>
<li><p>Cantidad de días de validez</p></li>
<li><p>Quién lo entrega</p></li>
</ul>
<p>6. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                     |
|-----------------|-------------------------------------|
| **Excepciones** |                                     |
| **Actor**       | **Sistema**                         |
|                 | 2.1. No hay documentos disponibles. |
