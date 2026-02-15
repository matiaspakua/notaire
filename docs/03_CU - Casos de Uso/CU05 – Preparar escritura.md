<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu05-preparar-escritura">CU05 – Preparar escritura</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Genera una nueva escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano selecciona una gestión, de la lista de gestiones habilitadas para escriturar, para preparar una escritura. El sistema muestra la gestión seleccionada donde figuran las personas involucradas (Clientes y Escribano titular/suplente), el/los trámite/s de la gestión, los folios utilizados y la fecha de inicio de la misma. El Escribano completa el detalle de la escritura, luego el Escribano guarda los datos indicados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.6.1, CU19, CU63</td>
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
<td><p>1. Un Escribano decide generar una nueva Escritura.</p>
<p>3. El Escribano selecciona una Gestión para escriturar de la lista.</p>
<p>5. El Escribano completa el detalle de la escritura, Registrando:</p>
<ul>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Números de folio utilizados (de una lista de folios disponibles)</p></li>
<li><p>Trámites involucrados</p></li>
<li><p>Cuerpo de escritura</p></li>
<li><p>Si fue <strong>firmada</strong> o no/<strong>anulada</strong>/<strong>no paso (*)</strong></p></li>
</ul></td>
<td><p>2. Muestra una lista de todas las Gestiones habilitadas para Escriturar.</p>
<p>4. Muestra la gestión seleccionada donde figura:</p>
<ul>
<li><p>Número de gestión</p></li>
<li><p>Encabezado</p></li>
<li><p>Fecha de inicio</p></li>
<li><p>Escribano a cargo</p></li>
<li><p>Nomenclatura catastral (si corresponde)</p></li>
<li><p>Trámites asociados</p></li>
<li><p>Clientes involucrados</p></li>
<li><p>Lista de folios disponibles.</p></li>
</ul>
<p>6. Registra la operación realizada por el usuario y cambia el estado de la Gestión.</p></td>
</tr>
</tbody>
</table>

|                 |                                                        |
|-----------------|--------------------------------------------------------|
| **Excepciones** |                                                        |
| **Actor**       | **Sistema**                                            |
|                 | 2.1. No existen gestiones habilitadas para escriturar. |

|                 |                                 |
|-----------------|---------------------------------|
| **Excepciones** |                                 |
| **Actor**       | **Sistema**                     |
|                 | 4.1. No hay folios disponibles. |

\* Nota: Los posibles estados de una escritura son:

1.  Firmada

2.  No firmada

3.  Anulada (finaliza)

4.  No paso (finaliza)
