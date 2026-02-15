<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu39-crear-plantilla-presupuesto">CU39 – Crear Plantilla Presupuesto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Crea la plantilla de un presupuesto</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide crear la plantilla de un presupuesto, para un tipo de trámite en particular. El sistema presenta una lista de todos los tipos de trámite disponibles. El Escribano selecciona un tipo de trámite, y el sistema muestra la lista de conceptos disponibles. El Escribano indica los conceptos que se deben abonar para el mismo. Luego guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 7.2.1, CU64, CU66</td>
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
<td><p>1. El Escribano decide crear la plantilla de un presupuesto, para un tipo de trámite en particular.</p>
<p>3. El Escribano selecciona un tipo de trámite de la lista.</p>
<p>5. El Escribano selecciona los conceptos asociados al mismo, y luego confirma la nueva plantilla de presupuesto.</p></td>
<td><p>2. Presenta una lista de todos los tipos de trámite disponibles.</p>
<p>4. Muestra la lista de conceptos disponibles para ese trámite.</p>
<p>6. Registra una nueva plantilla de presupuesto.</p></td>
</tr>
</tbody>
</table>

|                 |                                                 |
|-----------------|-------------------------------------------------|
| **Excepciones** |                                                 |
| **Actor**       | **Sistema**                                     |
|                 | 2.1.1. No existen tipos de trámite disponibles. |

|                 |                                                                                        |
|-----------------|----------------------------------------------------------------------------------------|
| **Excepciones** |                                                                                        |
| **Actor**       | **Sistema**                                                                            |
|                 | 2.1.2. Los tipos de trámite existentes ya tiene una plantilla de presupuesto asociada. |

|                 |                                        |
|-----------------|----------------------------------------|
| **Excepciones** |                                        |
| **Actor**       | **Sistema**                            |
|                 | 4.1. No existen conceptos registrados. |
