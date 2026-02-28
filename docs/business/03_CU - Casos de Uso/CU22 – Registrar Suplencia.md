<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu22-registrar-suplencia">CU22 – Registrar Suplencia</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Registra la licencia de un escribano.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un Escribano inicia una licencia, por lo tanto, solicita al sistema los escribanos habilitados para poder suplantarlo. Luego selecciona uno de los escribanos habilitados, e indica el período de la licencia a realizar. El Escribano confirma los datos indicados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 3.3, CU61</td>
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
<td><p>1. Un Escribano inicia una licencia, por lo tanto, solicita al sistema los escribanos habilitados para poder suplantarlo.</p>
<p>3. El Escribano selecciona a un suplente, de la lista presentada e indica el período de la licencia y algunas observaciones.</p></td>
<td><p>2. Busca y presenta la lista de escribanos habilitados para realizar suplencias.</p>
<p>4. Se registra la nueva suplencia para el período indicado.</p></td>
</tr>
</tbody>
</table>

|                 |                                         |
|-----------------|-----------------------------------------|
| **Excepciones** |                                         |
| **Actor**       | **Sistema**                             |
|                 | 2.1. No existen escribanos registrados. |

|                 |                                        |
|-----------------|----------------------------------------|
| **Excepciones** |                                        |
| **Actor**       | **Sistema**                            |
|                 | 4.1. El periodo indicado no es válido. |
