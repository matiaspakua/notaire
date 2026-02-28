<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu50-generar-declaración-jurada-de-rentas">CU50 – Generar Declaración Jurada de Rentas</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Recepcionista</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Genera la declaración jurada de Rentas, de un mes.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Recepcionista decide generar las DDJJ de un mes, para lo cual el sistema solicita, que se indique el mes, año y Registro de Escribano, para generar la misma. El Escribano/Recepcionista ingresa los datos solicitados, y el sistema presenta la declaración jurada correspondiente donde figuran los datos del Escribano titular/suplente, número de registro, tipo de protocolo y demás datos necesarios.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 5.1, CU62, CU19</td>
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
<td><p>1. El Escribano/Recepcionista decide generar las DDJJ de un mes.</p>
<p>3. El Escribano/Recepcionista ingresa los datos solicitados.</p></td>
<td><p>2. Solicita, que se indique el mes, año y Registro de Escribano, para generar la misma</p>
<p>4. Presenta la declaración jurada correspondiente donde figuran los datos del Escribano titular/suplente, número de registro, y:</p>
<ul>
<li><p>Número de escritura</p></li>
<li><p>Compraventa:</p>
<ul>
<li><p>Total o terreno</p></li>
<li><p>Proporción parte indivisa</p></li>
<li><p>Remate: Judicial o Banco Hipotecario.</p></li>
</ul></li>
<li><p>Precio de la operación</p></li>
<li><p>Forma de pago:</p>
<ul>
<li><p>Contado</p></li>
<li><p>Hipoteca: Saldo</p></li>
</ul></li>
<li><p>Nomenclatura Catastral</p></li>
<li><p>Valuación Fiscal</p></li>
<li><p>Impuesto</p></li>
<li><p>Total impuesto a pagar</p></li>
</ul></td>
</tr>
</tbody>
</table>

|                 |             |
|-----------------|-------------|
| **Excepciones** |             |
| **Actor**       | **Sistema** |
|                 |             |
