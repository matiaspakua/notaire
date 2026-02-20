<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu29-ingresar-nuevo-concepto">CU29 – Ingresar nuevo concepto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Da de alta un nuevo concepto.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano da de alta un nuevo concepto. El sistema solicita los datos necesarios. El Escribano ingresa los datos solicitados y decide guardar los cambios.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.4</td>
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
<td><p>1. El Escribano decide dar de alta un nuevo tipo de concepto.</p>
<p>3. Ingresa los datos solicitados y confirma los cambios.</p></td>
<td><p>2. Solicita:</p>
<ul>
<li><p>Nombre del concepto</p></li>
<li><p>Valor</p></li>
<li><p>Porcentaje</p></li>
<li><p>Si es valor fijo o variable</p></li>
</ul>
<p>4. Registra los datos ingresados, registrando un nuevo concepto.</p></td>
</tr>
</tbody>
</table>

|                 |                             |
|-----------------|-----------------------------|
| **Excepciones** |                             |
| **Actor**       | **Sistema**                 |
|                 | 4.1. El concepto ya existe. |
