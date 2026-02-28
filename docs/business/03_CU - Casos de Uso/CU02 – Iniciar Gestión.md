<table>
<colgroup>
<col style="width: 17%" />
<col style="width: 82%" />
</colgroup>
<tbody>
<tr class="odd">
<td><strong>Caso de Uso:</strong></td>
<td><h3 id="cu02-iniciar-gestión">CU02 – Iniciar Gestión</h3></td>
</tr>
<tr class="even">
<td><strong>Actores:</strong></td>
<td>Recepcionista/Gestor, Cliente</td>
</tr>
<tr class="odd">
<td><strong>Propósito:</strong></td>
<td>Inicia una nueva gestión en la escribanía.</td>
</tr>
<tr class="even">
<td><strong>Descripción:</strong></td>
<td>Un Cliente se acerca a la escribanía y solicita iniciar una gestión en base a un/os presupuestos. El Gestor/Recepcionista busca los presupuestos por su número ó nombre y apellido ó tipo y número de identificación del Cliente. El sistema muestra los trámites asociados a cada presupuesto. El Gestor/Recepcionista procede al inicio de una gestión, indicando fecha de inicio de la misma, el número de gestión, un detalle de encabezado y confirma los trámites a realizar. Finalmente, selecciona un escribano para dicha gestión, generando una lista de documentos, certificados necesarios para cada trámite (ver CU03), indica / selecciona un numero de la nueva gestión, las observaciones adicionales, y si van a haber otros clientes involucrados en la gestión.</td>
</tr>
<tr class="odd">
<td><strong>Tipo:</strong></td>
<td>Primario</td>
</tr>
<tr class="even">
<td><strong>Referencias Cruzadas:</strong></td>
<td>RF 1.2.1, RF 1.2.2, RF 1.2.3, RF 1.3.2, CU60</td>
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
<td><p>1. Un Cliente se acerca a la escribanía y solicita iniciar una gestión en base a un/os presupuestos.</p>
<p>2. El Gestor/Recepcionista solicita el número de presupuesto ó nombre y apellido ó tipo y número de identificación del Cliente.</p>
<p>3. El cliente brinda los datos correspondientes.</p>
<p>4. El Gestor/Recepcionista procede a la búsqueda del presupuesto, ingresando los datos en el sistema.</p>
<p>6. El Gestor/Recepcionista, selecciona uno de los prepuestos y procede al inicio de la Gestión de dicho presupuesto.</p>
<p>8. El Gestor/ Recepcionista ingresa los datos solicitados.</p>
<p>11. El Gestor/Recepcionista selecciona un escribano.</p>
<p>14. EL Gestor/Recepcionista confirma la Gestión, indicando o seleccionando un número de gestión y un detalle para el encabezado.</p></td>
<td><p>5. El sistema busca el presupuesto solicitado. Se muestra una lista con la descripción del/los presupuesto, asociados al cliente.</p>
<p>7. Solicita al Gestor/Recepcionista fecha de inicio de la gestión, y trámites que deben ser confirmados, para la gestión.</p>
<p>9. Registra, el cliente, fecha de inicio de la gestión, y trámites que deben ser confirmados.</p>
<p>10. Solicita que se indique un escribano para ser asociado a la gestión y se muestra una lista de Escribanos para su selección.</p>
<p>12. Asocia el Escribano a la Gestión.</p>
<p>13. Solicita la confirmación final del trámite.</p>
<p>15. Se registra la nueva Gestión.</p></td>
</tr>
</tbody>
</table>

|                 |                                                   |
|-----------------|---------------------------------------------------|
| **Excepciones** |                                                   |
| **Actor**       | **Sistema**                                       |
|                 | 5.1. El número de presupuesto indicado no existe. |

|                 |                                                                         |
|-----------------|-------------------------------------------------------------------------|
| **Excepciones** |                                                                         |
| **Actor**       | **Sistema**                                                             |
|                 | 5.2. El presupuesto indicado ya se encuentra registrado en una gestión. |

|                 |                                                    |
|-----------------|----------------------------------------------------|
| **Excepciones** |                                                    |
| **Actor**       | **Sistema**                                        |
|                 | 9.1. La persona no esta dada de alta como cliente. |

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 49%" />
</colgroup>
<tbody>
<tr class="odd">
<td colspan="2"><strong>Excepciones</strong></td>
</tr>
<tr class="even">
<td><strong>Actor</strong></td>
<td><strong>Sistema</strong></td>
</tr>
<tr class="odd">
<td><p>14.1. El cliente indica que van a haber otros clientes involucrados en la gestión.</p>
<p>14.2. El Gestor/Recepcionista solicita que se indiquen los nombres de los clientes involucrados para ser registrados en la gestión.</p>
<p>14.3. El cliente brinda los datos solicitados.</p></td>
<td><p>14.4. Se buscan y asocian los clientes indicados a la nueva gestión.</p>
<p>14.5. Vuelve al paso 15.</p></td>
</tr>
</tbody>
</table>

|                 |                                                                                                                                               |
|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| **Excepciones** |                                                                                                                                               |
| **Actor**       | **Sistema**                                                                                                                                   |
|                 | 15.1 El número de gestión indicado por el usuario no es válido o ya se encuentra registrado. Se solicita ingresar un nuevo número de gestión. |
