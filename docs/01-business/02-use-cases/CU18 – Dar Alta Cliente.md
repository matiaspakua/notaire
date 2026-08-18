# CU18 – Dar de Alta Cliente

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU18 – Dar de Alta Cliente |
| **Actores** | Recepcionista, Persona, Cliente |
| **Propósito** | Permite dar de alta un Cliente. |
| **Descripción** | Una Persona se acerca a la escribanía para iniciar un trámite, por lo tanto, el Recepcionista solicita nombre y apellido o tipo y número de identificación para buscarla en el sistema. El sistema devuelve los datos de la Persona encontrada y solicita información adicional para dar de alta como cliente. El Recepcionista ingresa los datos y el Cliente queda registrado. |
| **Tipo** | Primario |
| **Referencias Cruzadas** | RF #12 (Verificar clientes), RF #38 (Administrar clientes), RF #39 (Registrar nuevos clientes); CU61 |
| **GitHub ID** | #171 |

## Curso de Eventos

| Paso | Actor | Sistema |
|---|---|---|
| 2 | El Recepcionista solicita nombre y apellido o tipo y número de identificación, para buscarla en el sistema. |  |
| 3 |  | Busca la Persona indicada y muestra: (Nombre; Apellido; Tipo y número de identificación; Teléfono; correo electrónico; Nacionalidad; Fecha de nacimiento estado civil; Cuit/Cuil; En caso de ser casado/divorciado, número de nupcias; Sexo; Ocupación; Domicilio) |
| 4 | El Recepcionista solicita los datos necesarios a la Persona: (Nacionalidad; Fecha de nacimiento estado civil; Cuit/Cuil; En caso de ser casado/divorciado, número de nupcias; Sexo; Ocupación; Domicilio) |  |
| 5 | La Persona indica los datos solicitados. |  |
| 6 | El Recepcionista ingresa los datos y guarda los cambios. |  |
| 7 |  | Registra a la Persona como Cliente. |

## Excepciones / Flujos Alternativos

| Paso | Condición / Evento | Acción del Sistema / Actor |
|---|---|---|
| 4.1 | Concurrir a CU 17 Continuar en paso 2 | No se encuentra la Persona indicada. |
| 7.1 | Alguno de los datos no son validos. | El sistema gestiona la excepción y notifica al usuario. |
| 7.2 | El cliente ya se encuentra registrado. | El sistema gestiona la excepción y notifica al usuario. |
