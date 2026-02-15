<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu49-eliminar-plantilla-presupuesto">CU49 – Eliminar Plantilla Presupuesto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Eliminar plantilla de presupuesto</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide eliminar una plantilla de presupuesto, de un tipo de trámite en particular. El sistema presenta una lista de los tipos de trámite disponibles que tienen plantillas de presupuesto asociadas. El Escribano selecciona un tipo de trámite, y confirma que quiere eliminar la plantilla de presupuesto del mismo.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU64</td>
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
<td><p>1. El Escribano decide eliminar una plantilla de presupuesto.</p>
<p>3. El Escribano selecciona un tipo de trámite.</p>
<p>5. Confirma que quiere eliminarla.</p></td>
<td><p>2. Presenta una lista de los tipos de trámite disponibles que tienen plantillas de presupuesto asociadas.</p>
<p>4. Solicita una confirmación para eliminar la plantilla de presupuesto del mismo.</p>
<p>6. Registra eliminación.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                           |
|-----------------|---------------------------------------------------------------------------|
| **Excepciones** |                                                                           |
| **Actor**       | **Sistema**                                                               |
|                 | 2.1. No existen tipos de trámite con plantillas de presupuesto asociadas. |
