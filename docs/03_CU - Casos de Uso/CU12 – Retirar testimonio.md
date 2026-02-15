<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu12-retirar-testimonio">CU12 – Retirar testimonio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Cliente, Recepcionista</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Registra el retiro de copias, de un testimonio, de una escritura.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Una Persona se acerca a la escribanía, y solicita retirar las copias de testimonio, de una escritura, para lo cual busca todas las escrituras firmadas del escribano indicado ó si se indica el número de escritura se muestra el detalle de la misma. El Recepcionista indica los datos necesarios para registrar el retiro de las copias.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>1.5.5, CU62</td>
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
<td><p>1. Una Persona se acerca a la escribanía, y solicita retirar copias de un testimonio, de una escritura.</p>
<p>2. El Recepcionista solicita el retiro de copias de un testimonio de una Escritura en particular.</p>
<p>4. El Recepcionista selecciona la escritura correspondiente.</p>
<p>6. El Recepcionista solicita al Cliente el nombre, apellido, tipo y número de identificación.</p>
<p>7. El Cliente indica los datos solicitados.</p>
<p>9. El Recepcionista indica los datos al sistema y la cantidad de copias a retirar.</p></td>
<td><p>3. Busca y muestra todas las escrituras firmadas.</p>
<p>5. Muestra los datos de los testimonios asociados a la escritura:</p>
<ul>
<li><p>Registro de Escribano</p></li>
<li><p>Numero de Escritura</p></li>
<li><p>Fecha de Escrituración</p></li>
<li><p>Folio desde y hasta utilizados</p></li>
</ul>
<p>Por cada testimonio generado:</p>
<ul>
<li><p>Numero de Testimonio</p></li>
<li><p>Fecha de Impresión</p></li>
<li><p>Si fue inscripto o no</p></li>
<li><p>Cantidad de copias generadas</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>y solicita nombre, apellido, tipo y número de identificación, del Cliente que retira la copia.</p>
<p>10. Registra el retiro de la copia y la fecha de retiro.</p></td>
</tr>
</tbody>
</table>

|                                                                                           |                                                                                                                 |
|-------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| **Excepciones**                                                                           |                                                                                                                 |
| **Actor**                                                                                 | **Sistema**                                                                                                     |
| 10.2. El Recepcionista informa al Cliente de la deuda registrada. (Seguir en el punto 6.) | 10.1. Informa que el Cliente registra deudas sobre el presupuesto de la gestión, asociada a la copia a retirar. |

|                 |                                                                                |
|-----------------|--------------------------------------------------------------------------------|
| **Excepciones** |                                                                                |
| **Actor**       | **Sistema**                                                                    |
|                 | 5.2. No existen copias de testimonio a retirar, asociadas al Cliente indicado. |

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 49%" />
</colgroup>
<tbody>
<tr class="odd">
<td colspan="2"><strong>Excepciones</strong></td>
</tr>
<tr class="even">
<td><strong>Actor</strong></td>
<td><strong>Sistema</strong></td>
</tr>
<tr class="odd">
<td><p>10.2.1. El Recepcionista informa al Cliente de la deuda registrada.</p>
<p>10.2.2. El Cliente decide no retirar la copia.</p></td>
<td></td>
</tr>
</tbody>
</table>

NOTA: Las excepciones de deudas se implementarán cuando se implemente el módulo de Pagos.
