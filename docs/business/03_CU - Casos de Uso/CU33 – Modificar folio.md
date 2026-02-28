<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu33-modificar-folio">CU33 – Modificar folio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Modifica las características de un folio</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide modificar los datos de un determinado folio. El sistema solicita que se ingrese el año y registro de escribano, y presenta una lista de todos los folios disponibles. El Escribano selecciona un folio, y el sistema presenta los datos del mismo. El escribano modifica el estado del mismo y guarda los cambios.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.9, CU63</td>
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
<td><p>1. El Escribano decide modificar los datos de un determinado folio.</p>
<p>3. El Escribano indica el año y número de registro.</p>
<p>5. El Escribano selecciona un folio.</p>
<p>7. El escribano modifica el estado del mismo confirmando los cambios.</p></td>
<td><p>2. Solicita que se ingrese el año, indicando el año actual por default, y el número de registro de escribano correspondiente.</p>
<p>4. Busca y presenta una lista de todos los folios del año y escribano indicados.</p>
<p>6. Presenta:</p>
<ul>
<li><p>Número de folio</p></li>
<li><p>Tipo de folio</p></li>
<li><p>Estado actual del mismo</p></li>
<li><p>Fecha del último estado</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>8. Guarda los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                                                       |
|-----------------|-------------------------------------------------------|
| **Excepciones** |                                                       |
| **Actor**       | **Sistema**                                           |
|                 | 4.1. No existen folios cargados para el año indicado. |

\*Nota: Los estados del folio pueden ser los siguientes:

- Nuevo

- Utilizado

- No paso

- Errose
