<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu69-gestion-de-inmuebles">CU69 – Gestión de Inmuebles</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano, Gestor</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite gestionar la información de los inmuebles asociados a las escrituras y trámites.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El sistema permite dar de alta, modificar, consultar y eliminar información técnica y catastral de inmuebles (propiedades). Esta información es fundamental para los trámites que requieren inscripción o verificación de deuda catastral.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 6.1, CU02, CU11, CU56</td>
</tr>
<tr class="odd">
<td><strong>GitHub_ID:</strong></td>
<td>#292</td>
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
<td><p>1. El Escribano selecciona la opción de gestión de inmuebles.</p>
<p>3. El Escribano ingresa los datos del inmueble:</p>
<ul>
<li><p>Nomenclatura Catastral</p></li>
<li><p>Partida Inmobiliaria</p></li>
<li><p>Valuación Fiscal</p></li>
<li><p>Domicilio (Calle, Número, Localidad)</p></li>
<li><p>Tipo de Inmueble (Urbano, Rural)</p></li>
</ul>
<p>5. El Escribano confirma el registro.</p></td>
<td><p>2. El sistema muestra la interfaz de gestión de inmuebles y solicita los datos.</p>
<p>4. El sistema valida los datos ingresados.</p>
<p>6. El sistema guarda la información y confirma el éxito de la operación.</p></td>
</tr>
</tbody>
</table>

|                 |                                                              |
|-----------------|--------------------------------------------------------------|
| **Excepciones** |                                                              |
| **Actor**       | **Sistema**                                                  |
|                 | 4.1. El inmueble ya existe con esa nomenclatura catastral.  |
