<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu45-modificar-presupuesto">CU45 – Modificar presupuesto</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Escribano/Recepcionista</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Permite agregar un ítem extra a un presupuesto en particular.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Escribano/Recepcionista busca un presupuesto, y modifica alguno de los valores de los ítems o agrega un nuevo ítem con los datos correspondientes. Luego guarda los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Secundario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU60</td>
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
<td><p>1. El Escribano/Recepcionista busca un presupuesto, por número de presupuesto o busca los presupuestos de un cliente en particular, por: Nombre, apellido ó tipo y número de identificación.</p>
<p>3. El Escribano/Recepcionista selecciona un presupuesto de la lista.</p>
<p>5. El Escribano/Recepcionista modifica las observaciones o ítems del presupuesto.</p>
<p>6. El Escribano solicita calcular el nuevo total.</p>
<p>8. El Escribano confirma los cambios realizados.</p></td>
<td><p>2. Muestra una lista de presupuestos del cliente y por cada uno muestra:</p>
<ul>
<li><p>Número y encabezado de gestión si corresponde.</p></li>
<li><p>Número de presupuesto.</p></li>
<li><p>Tipo de trámite</p></li>
<li><p>Fecha de emisión</p></li>
<li><p>Total</p></li>
<li><p>Saldo</p></li>
<li><p>Observaciones</p></li>
</ul>
<p>4. Muestra:</p>
<ul>
<li><p>Nombre de la persona encontrada</p></li>
</ul>
<ul>
<li><p>Nombre del trámite</p></li>
<li><p>Ítems</p>
<ul>
<li><p>Nombre del Ítem</p></li>
<li><p>Valor</p></li>
<li><p>Porcentaje (valor variable)</p></li>
<li><p>Observaciones (valor variable)</p></li>
</ul></li>
<li><p>Total del presupuesto</p></li>
<li><p>Observaciones</p></li>
<li><p>Detalle del inmueble asociado, si corresponde</p></li>
</ul>
<p>7. Calcula el nuevo total, mostrando el resultado.</p>
<p>9. Registra los cambios.</p></td>
</tr>
</tbody>
</table>

|                 |                                      |
|-----------------|--------------------------------------|
| **Excepciones** |                                      |
| **Actor**       | **Sistema**                          |
|                 | 2.1.1. La persona buscada no existe. |

|                 |                                                            |
|-----------------|------------------------------------------------------------|
| **Excepciones** |                                                            |
| **Actor**       | **Sistema**                                                |
|                 | 2.1.2. La persona buscada no tiene presupuestos asociados. |

|                 |                                  |
|-----------------|----------------------------------|
| **Excepciones** |                                  |
| **Actor**       | **Sistema**                      |
|                 | 2.1.3. El presupuesto no existe. |
