<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu55-modificar-plantilla-presupuesto">CU55 – Modificar Plantilla Presupuesto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Modificar plantilla de presupuesto</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide modificar una plantilla de presupuesto, para un tipo trámite en particular. El sistema presenta una lista de los tipos de trámite disponibles. El Escribano selecciona un tipo de trámite, y el sistema muestra la lista de conceptos asociados a ese tipo de trámite en la plantilla. El Escribano modifica los conceptos que se deben abonar para el mismo. Luego guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU64, CU66</td>
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
<td><p>1. El Escribano decide modificar una plantilla de presupuesto, para un tipo de trámite en particular.</p>
<p>3. El Escribano selecciona un tipo de trámite de la lista.</p>
<p>5. El Escribano modifica los conceptos que se deben abonar para el mismo, confirmando los datos ingresados.</p></td>
<td><p>2. Presenta una lista de todos los tipos de trámite disponibles.</p>
<p>4. Muestra la lista de conceptos disponibles para el tipo de trámite.</p>
<p>6. Registra la modificación de la plantilla de presupuesto.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                           |
|-----------------|---------------------------------------------------------------------------|
| **Excepciones** |                                                                           |
| **Actor**       | **Sistema**                                                               |
|                 | 2.1. No existen tipos de trámite con plantillas de presupuesto asociadas. |

|                 |                                        |
|-----------------|----------------------------------------|
| **Excepciones** |                                        |
| **Actor**       | **Sistema**                            |
|                 | 2.2. No existen conceptos disponibles. |

|                 |                                        |
|-----------------|----------------------------------------|
| **Excepciones** |                                        |
| **Actor**       | **Sistema**                            |
|                 | 4.1. No existen conceptos registrados. |
