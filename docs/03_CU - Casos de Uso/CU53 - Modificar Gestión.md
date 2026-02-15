<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu53-modificar-gestión">CU53 – Modificar Gestión</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista/Gestor, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Modifica una gestión en particular.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>El Gestor/Recepcionista decide modificar una gestión en particular. Busca la gestión deseada. El sistema muestra los trámites asociados a cada presupuesto, el encabezado de la gestión, las observaciones, el escribano a cargo, el cliente de referencia y los clientes asociados a la gestión. El Gestor/Recepcionista procede a modificar alguno de los datos de la gestión encontrada: El encabezado, las observaciones o los clientes asociados. Finalmente, confirma los cambios realizados.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario y esencial.</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>CU</td>
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
<td><p>1. El Gestor/Recepcionista decide modificar una gestión en particular.</p>
<p>3. El Gestor/Recepcionista ingresa alguno de los datos solicitados.</p>
<p>5. El Gestor/Recepcionista puede modificar:</p>
<ul>
<li><p>Modificar el detalle del encabezado,</p></li>
<li><p>Modificar el detalle de las observaciones,</p></li>
<li><p>Agregar o quitar Clientes asociados</p></li>
</ul>
<p>Y confirma los cambios realizados.</p></td>
<td><p>2. Solicita número de gestión, ó tipo y número de identificación, ó nombre y apellido del cliente asociado a la gestión, para realizar la búsqueda de la misma.</p>
<p>4. Busca y muestra los datos de la gestión encontrada:</p>
<ul>
<li><p>Nombre cliente asociado</p></li>
<li><p>Presupuestos asociados</p></li>
<li><p>Fecha de inicio</p></li>
<li><p>Escribano a cargo</p></li>
<li><p>Clientes asociados</p></li>
</ul>
<p>6. Registra los cambios realizados.</p></td>
</tr>
</tbody>
</table>

|                 |                            |
|-----------------|----------------------------|
| **Excepciones** |                            |
| **Actor**       | **Sistema**                |
|                 | 4.1. La gestión no existe. |

|                 |                                                                                            |
|-----------------|--------------------------------------------------------------------------------------------|
| **Excepciones** |                                                                                            |
| **Actor**       | **Sistema**                                                                                |
|                 | 4.2. La gestión indicada ya se encuentra archivada, por lo tanto, no puede ser modificada. |

|                                                                              |             |
|------------------------------------------------------------------------------|-------------|
| **Excepciones**                                                              |             |
| **Actor**                                                                    | **Sistema** |
| 5.1. No se encuentran registrados más presupuestos para el cliente indicado. |             |

|                                                           |             |
|-----------------------------------------------------------|-------------|
| **Excepciones**                                           |             |
| **Actor**                                                 | **Sistema** |
| 5.1. La gestión no puede no tener presupuestos asociados. |             |

|                                                       |             |
|-------------------------------------------------------|-------------|
| **Excepciones**                                       |             |
| **Actor**                                             | **Sistema** |
| 5.1. La gestión no puede no tener clientes asociados. |             |
