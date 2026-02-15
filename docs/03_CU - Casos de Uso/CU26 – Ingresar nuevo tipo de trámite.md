<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu26-ingresar-nuevo-tipo-de-trámite">CU26 – Ingresar nuevo tipo de trámite</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Da de alta un nuevo tipo de trámite.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano decide dar de alta un nuevo tipo de trámite. El sistema solicita: nombre del nuevo tipo de trámite, y demás datos, presentando además una lista de todos los documentos registrados posibles para el trámite. El Escribano indica los datos solicitados. Finalmente, guarda los cambios y queda registrado el nuevo tipo de trámite.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.1</td>
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
<td><p>1. El Escribano decide dar de alta un nuevo tipo de trámite.</p>
<p>3. El Escribano ingresa los datos solicitados, confirmando el nuevo tipo de trámite.</p></td>
<td><p>2. Solicita:</p>
<ul>
<li><p>Nombre del nuevo trámite</p></li>
<li><p>Si requiere inscripción o no</p></li>
<li><p>Si se archiva o no</p></li>
<li><p>Si se asocia con inmuebles</p></li>
<li><p>Observaciones</p></li>
<li><p>Muestra además, una lista de todos los documentos registrados posibles para el trámite.</p></li>
</ul>
<p>4. Registra los datos ingresados, creando un nuevo tipo de trámite.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                     |
|-----------------|---------------------------------------------------------------------|
| **Excepciones** |                                                                     |
| **Actor**       | **Sistema**                                                         |
|                 | 2.1. No existen tipos de documento para asociar al tipo de trámite. |

|                 |                                      |
|-----------------|--------------------------------------|
| **Excepciones** |                                      |
| **Actor**       | **Sistema**                          |
|                 | 4.1. El trámite ingresado ya existe. |
