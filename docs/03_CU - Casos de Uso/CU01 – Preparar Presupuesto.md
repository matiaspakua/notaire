<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu01-preparar-presupuesto">CU01 – Preparar Presupuesto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista, Persona</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite generar un presupuesto a una persona, en base a la solicitud de un trámite.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Una Persona se acerca a la escribanía para realizar un presupuesto. El Recepcionista pregunta a la Persona si desea que se le genere un presupuesto, para esto le solicita el nombre, apellido, tipo y número de identificación para buscarla en el sistema. El recepcionista solicita la generación de un presupuesto para la persona, seleccionando un tipo de trámite. Finalmente, el recepcionista ingresa los datos correspondientes, y confirma el presupuesto. El presupuesto es generado con un número asociado al mismo, y la fecha actual.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.1, CU61, CU64</td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 49%" />
<col style="width: 50%" />
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
<td><p>1. Una persona se acerca a la escribanía para realizar un presupuesto.</p>
<p>3. El Recepcionista, solicita a la persona los siguientes datos, para buscarla en el sistema:</p>
<ul>
<li><p>Nombre y Apellido, ó</p></li>
<li><p>Tipo y número de identificación</p></li>
</ul>
<p>5. El Recepcionista solicita la generación de un presupuesto para la persona, seleccionando un tipo de trámite de una lista de trámites disponibles.</p>
<p>7. El recepcionista ingresa los datos correspondientes, y confirma la configuración del presupuesto.</p>
<p>9. El Recepcionista ingresa observaciones del presupuesto, si corresponde, y confirma la creación del mismo.</p></td>
<td><p>2. Solicita la búsqueda de la persona.</p>
<p>4. Busca la persona y muestra los datos correspondientes, y los tipos de trámites disponibles.</p>
<p>6. Busca el tipo de trámite y muestra:</p>
<ul>
<li><p>Nombre del trámite</p></li>
<li><p>Ítem asociados</p>
<ul>
<li><p>Nombre del Item</p></li>
<li><p>Valor</p></li>
<li><p>Porcentaje (valores variables)</p></li>
<li><p>Observaciones (valores variables)</p></li>
</ul></li>
</ul>
<p>Y solicita en caso de que corresponda :</p>
<ul>
<li><p>Nomenclatura catastral</p></li>
<li><p>Valuación fiscal</p></li>
<li><p>Domicilio</p></li>
<li><p>Tipo de inmueble</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>del inmueble asociado.</p>
<p>8. Calcula el total del presupuesto y lo muestra.</p>
<p>10. Crea el nuevo presupuesto para la persona indicada con la fecha actual, e indica el número generado para el mismo. Muestra la opción de imprimir el mismo.</p></td>
</tr>
</tbody>
</table>

|                 |                                                   |
|-----------------|---------------------------------------------------|
| **Excepciones** |                                                   |
| **Actor**       | **Sistema**                                       |
|                 | 4.1. La persona no existe, se debe darla de alta. |

|                 |                                               |
|-----------------|-----------------------------------------------|
| **Excepciones** |                                               |
| **Actor**       | **Sistema**                                   |
|                 | 6.1. No existen tipos de trámite disponibles. |
