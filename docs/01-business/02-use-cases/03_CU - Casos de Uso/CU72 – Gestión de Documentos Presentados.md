<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu72-gestion-de-documentos-presentados">CU72 – Gestión de Documentos Presentados</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano, Gestor</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite registrar y realizar el seguimiento de los documentos físicos presentados por los clientes para una gestión.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El sistema registra la recepción de documentos (DNI, Títulos, Planos, etc.) necesarios para llevar adelante una escritura o trámite, permitiendo saber en todo momento qué documentos tiene la escribanía en su poder.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 3.1, CU04, CU43</td>
</tr>
<tr class="odd">
<td><strong>GitHub_ID:</strong></td>
<td>#163</td>
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
<td><p>1. El Gestor selecciona una gestión abierta.</p>
<p>3. El Gestor registra la recepción de un documento físico.</p>
<p>5. El Gestor confirma la recepción.</p></td>
<td><p>2. El sistema muestra la lista de documentos requeridos y presentados para esa gestión.</p>
<p>4. El sistema solicita la fecha de recepción y observaciones.</p>
<p>6. El sistema actualiza el estado del documento a "Presentado".</p></td>
</tr>
</tbody>
</table>
