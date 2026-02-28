<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu27-ingresar-nuevo-tipo-de-documento">CU27 – Ingresar nuevo tipo de documento</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Da de alta un nuevo tipo de documento.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide dar de alta un nuevo tipo de documento. El sistema solicita: nombre del tipo de documento, y otros datos necesarios. El Escribano ingresa los datos solicitados y decide guardar los cambios, con lo cual queda registrado el nuevo tipo de documento.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.2</td>
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
<td><p>1. El Escribano decide dar de alta un nuevo tipo de documento.</p>
<p>3. El Escribano indica los datos solicitados y confirma los mismos.</p></td>
<td><p>2. Solicita:</p>
<ul>
<li><p>Nombre del documento</p></li>
<li><p>Si posee vencimiento o no</p></li>
<li><p>Cantidad de días de validez</p></li>
<li><p>Quién debe entregar el mismo (Cliente/Entidad Externa)</p></li>
</ul>
<p>4. Registra un nuevo documento en el sistema con los datos indicados.</p></td>
</tr>
</tbody>
</table>

|                 |                              |
|-----------------|------------------------------|
| **Excepciones** |                              |
| **Actor**       | **Sistema**                  |
|                 | 4.1. El documento ya existe. |
