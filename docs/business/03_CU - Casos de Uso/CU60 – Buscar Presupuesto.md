<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu60-buscar-presupuesto">CU60 – Buscar Presupuesto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista/Gestor</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Busca un presupuesto.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Recepcionista/Gestor requiere buscar un presupuesto en particular, para lo cual informa al sistema alguno de los siguientes datos: el número de presupuesto ó el nombre y apellido del cliente ó el tipo y número de identificación. El sistema busca y retorna el o los presupuesto encontrados y muestra un reporte con la información del mismo.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU61</td>
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
<td><p>1. El Recepcionista/Gestor requiere un presupuesto.</p>
<p>3. Indica alguno de los datos solicitados.</p>
<p>5. Selecciona un presupuesto.</p></td>
<td><p>2. Solicita que se indique alguno de los siguientes datos:</p>
<ul>
<li><p>el número de presupuesto</p></li>
<li><p>el nombre y apellido del cliente</p></li>
<li><p>el tipo y número de identificación</p></li>
</ul>
<p>4. Busca y retorna el / los presupuesto correspondientes.</p>
<p>6. Presenta un reporte del presupuesto indicado.</p></td>
</tr>
</tbody>
</table>

|                 |                                                |
|-----------------|------------------------------------------------|
| **Excepciones** |                                                |
| **Actor**       | **Sistema**                                    |
|                 | 4.1. No existe el / los presupuesto indicados. |
