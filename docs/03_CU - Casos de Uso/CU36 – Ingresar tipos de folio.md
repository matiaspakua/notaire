<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu36-ingresar-tipos-de-folio">CU36 – Ingresar tipos de folio</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite dar de alta un nuevo tipo de folio.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide dar de alta un tipo de folio. El sistema solicita que se indique el nombre y/o observaciones para el nuevo tipo de folio. El escribano ingresa los datos indicados y el sistema registra el nuevo tipo de folio.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.12</td>
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
<td><p>1. El Escribano decide agregar un nuevo tipo de folio.</p>
<p>3. El escribano ingresa los datos indicados y guarda los cambios.</p></td>
<td><p>2. Solicita que se indique el nombre del nuevo tipo de folio y/o observaciones para el mismo.</p>
<p>4. Registra el nuevo tipo de folio.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                                                                                           |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| **Excepciones** |                                                                                                                                           |
| **Actor**       | **Sistema**                                                                                                                               |
|                 | 4.1. El tipo de folio indicado ya se encuentra registrado, pero ha sido deshabilitado, por lo tanto, se procede a habilitarlo nuevamente. |

\*Nota: Los tipos de folios disponibles son los siguientes:

- Protocolo principal

- Testimonio

- Concuerda

- Certificación de firmas
