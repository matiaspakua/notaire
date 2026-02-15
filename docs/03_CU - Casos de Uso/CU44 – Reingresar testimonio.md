<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu44-reingresar-testimonio">CU44 – Reingresar testimonio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Gestor/Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite registrar el reingreso de un testimonio.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un testimonio ha sido devuelto observado. El Gestor/Escribano solicita el reingreso del mismo, seleccionando una escritura de una lista de escrituras disponibles. El sistema muestra el testimonio a reingresar, asociado a la escritura seleccionada. El Gestor/Escribano ingresa los datos correspondientes, y el sistema registra el reingreso del mismo.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU62</td>
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
<td><p>1. Un testimonio ha sido devuelto observado.</p>
<p>2. El Gestor/Escribano decide registrar el reingreso de un testimonio.</p>
<p>4. El Gestor/Escribano selecciona una escritura.</p>
<p>6. El Gestor/Escribano indica los datos solicitados, y confirma su reingreso.</p></td>
<td><p>3. Muestra una lista de disponibles.</p>
<p>5. Muestra:</p>
<ul>
<li><p>Número de escritura</p></li>
<li><p>Fecha</p></li>
<li><p>Folios utilizados</p></li>
<li><p>Nomenclaturas catastrales asociadas (si corresponde)</p></li>
</ul>
<p>Y solicita:</p>
<ul>
<li><p>Fecha salida</p></li>
<li><p>Fecha de reingreso</p></li>
<li><p>Número de cartón</p></li>
<li><p>Si fue observado o no</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>7. Registra el reingreso del testimonio.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                                  |
|-----------------|----------------------------------------------------------------------------------|
| **Excepciones** |                                                                                  |
| **Actor**       | **Sistema**                                                                      |
|                 | 5.1. La escritura no tiene testimonios que hayan sido ingresados para inscribir. |
