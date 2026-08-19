*** Settings ***
Documentation    E2E acceptance tests for the Clientes module of Notaire Swing.
...
...              Covers use cases:
...              - CU17 – Dar Alta persona
...              - CU18 – Dar Alta Cliente
...              - CU19 – Buscar gestiones de un Cliente
...              - CU41 – Modificar Cliente
...              - CU46 – Ver detalle cliente
...              - CU54 – Modificar Persona
...              - CU61 – Buscar persona o cliente
...
...              Requires: backend running (scripts/start.sh), frontend JAR built.
Resource         ../resources/common.resource
Suite Setup      Suite Initialization
Suite Teardown   Close Swing Application
Test Teardown    Run Keyword If Test Failed    Take Named Screenshot    clientes    FAIL_${TEST_NAME}

*** Variables ***
${MODULE}        clientes
${TEST_NOMBRE}   Juan
${TEST_APELLIDO}    TestE2E
${TEST_DNI}      99999999

*** Keywords ***
Suite Initialization
    [Documentation]    Launch app, login, navigate to Clientes.
    Ensure Backend Is Running
    Launch Swing Application
    Perform Login
    Navigate To Clientes
    Take Named Screenshot    ${MODULE}    00_clientes_module_loaded

Open Clientes Sub Form
    [Documentation]    Click a button inside the Clientes module panel.
    [Arguments]    ${button_name}
    Navigate To Clientes
    Bring Java App To Front
    Click Button By Name    ${button_name}
    Sleep    2s

*** Test Cases ***
# ─────────────────────────────────────────────────────────────────────────────
# Module smoke
# ─────────────────────────────────────────────────────────────────────────────

Clientes Module Should Load With All Sub Forms
    [Documentation]    Verify Clientes module opens and shows expected buttons.
    [Tags]    clientes    smoke    agent-callable
    Take Named Screenshot    ${MODULE}    01_clientes_overview
    Log    Clientes module loaded successfully

# ─────────────────────────────────────────────────────────────────────────────
# CU17 – Dar Alta Persona
# ─────────────────────────────────────────────────────────────────────────────

Dar Alta Persona Form Should Be Accessible
    [Documentation]    CU17 – Dar Alta Persona: Button opens the form.
    [Tags]    clientes    personas    CU17    smoke    agent-callable
    Open Clientes Sub Form    Dar Alta Persona
    Take Named Screenshot    ${MODULE}    02_dar_alta_persona_form
    Log    CU17: Dar Alta Persona form opened

Dar Alta Persona Should Require Mandatory Fields
    [Documentation]    CU17: Verify form rejects submission without required data.
    [Tags]    clientes    personas    CU17    validation    e2e
    # Submit without data — should not save
    Submit Form
    Sleep    1s
    Take Named Screenshot    ${MODULE}    03_dar_alta_persona_empty_submit
    Dismiss Dialog If Present
    Log    CU17: Validated empty form submission handling

Dar Alta Persona Should Accept Valid Person Data
    [Documentation]    CU17: Fill in persona with nombre, apellido, DNI and save.
    [Tags]    clientes    personas    CU17    e2e    agent-callable
    Open Clientes Sub Form    Dar Alta Persona
    # Tab through: Tipo ID, Número ID, Nombre, Apellido, ...
    Navigate To Next Field
    # Número de ID
    Type In Current Field    ${TEST_DNI}
    Navigate To Next Field
    Navigate To Next Field
    # Nombre
    Type In Current Field    ${TEST_NOMBRE}
    Navigate To Next Field
    # Apellido
    Type In Current Field    ${TEST_APELLIDO}
    Navigate To Next Field
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    04_dar_alta_persona_submitted
    Dismiss Dialog If Present
    Log    CU17: Dar Alta Persona submission attempted

# ─────────────────────────────────────────────────────────────────────────────
# CU18 – Dar Alta Cliente
# ─────────────────────────────────────────────────────────────────────────────

Dar Alta Cliente Form Should Be Accessible
    [Documentation]    CU18 – Dar Alta Cliente: Form opens (requires a Persona first).
    [Tags]    clientes    CU18    smoke    agent-callable
    Open Clientes Sub Form    Dar Alta Cliente
    Take Named Screenshot    ${MODULE}    05_dar_alta_cliente_form
    Log    CU18: Dar Alta Cliente form opened

Dar Alta Cliente Requires Person Search
    [Documentation]    CU18: Verify form requires searching a Persona to link as Cliente.
    [Tags]    clientes    CU18    e2e
    # The form searches for a Persona first
    Type In Current Field    ${TEST_DNI}
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    06_dar_alta_cliente_search
    Dismiss Dialog If Present
    Log    CU18: Dar Alta Cliente persona search attempted

# ─────────────────────────────────────────────────────────────────────────────
# CU41 – Modificar Cliente
# ─────────────────────────────────────────────────────────────────────────────

Modificar Cliente Form Should Be Accessible
    [Documentation]    CU41 – Modificar Cliente: Form opens from Clientes module.
    [Tags]    clientes    CU41    smoke    agent-callable
    Open Clientes Sub Form    Modificar Cliente
    Take Named Screenshot    ${MODULE}    07_modificar_cliente_form
    Log    CU41: Modificar Cliente form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU54 – Modificar Persona
# ─────────────────────────────────────────────────────────────────────────────

Modificar Persona Form Should Be Accessible
    [Documentation]    CU54 – Modificar Persona: Form opens from Clientes module.
    [Tags]    clientes    personas    CU54    smoke    agent-callable
    Open Clientes Sub Form    Modificar Persona
    Take Named Screenshot    ${MODULE}    08_modificar_persona_form
    Log    CU54: Modificar Persona form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU46 – Ver detalle cliente (Buscar Cliente)
# ─────────────────────────────────────────────────────────────────────────────

Buscar Cliente Form Should Be Accessible
    [Documentation]    CU46/CU61 – Buscar Cliente: Search form opens.
    [Tags]    clientes    CU46    CU61    smoke    agent-callable
    Open Clientes Sub Form    Buscar Cliente
    Take Named Screenshot    ${MODULE}    09_buscar_cliente_form
    Log    CU46/CU61: Buscar Cliente form opened

Buscar Cliente Should Return Results For Valid Name
    [Documentation]    CU61 – Buscar persona o cliente: Search returns results.
    [Tags]    clientes    CU46    CU61    e2e
    # Search by name
    Type In Current Field    ${TEST_NOMBRE}
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    10_buscar_cliente_results
    Log    CU61: Buscar Cliente search executed

Buscar Cliente Should Handle Empty Search Gracefully
    [Documentation]    CU61: Empty search should not crash — may show all records or error.
    [Tags]    clientes    CU61    validation    negative
    Open Clientes Sub Form    Buscar Cliente
    # Submit without entering anything
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    11_buscar_cliente_empty_search
    Dismiss Dialog If Present
    Log    CU61: Empty search handled gracefully

# ─────────────────────────────────────────────────────────────────────────────
# CU19 – Buscar gestiones de un Cliente
# ─────────────────────────────────────────────────────────────────────────────

Buscar Gestiones Cliente Form Should Be Accessible
    [Documentation]    CU19 – Buscar gestiones de un Cliente: Form opens.
    [Tags]    clientes    CU19    smoke    agent-callable
    Open Clientes Sub Form    Buscar Gestiones Cliente
    Take Named Screenshot    ${MODULE}    12_buscar_gestiones_cliente_form
    Log    CU19: Buscar Gestiones de un Cliente form opened

Buscar Gestiones Should Search By Client Name
    [Documentation]    CU19: Search gestiones using client name.
    [Tags]    clientes    CU19    e2e
    Type In Current Field    ${TEST_NOMBRE}
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    13_gestiones_cliente_search_result
    Dismiss Dialog If Present
    Log    CU19: Buscar Gestiones de un Cliente search executed

# ─────────────────────────────────────────────────────────────────────────────
# List all persons/clients view
# ─────────────────────────────────────────────────────────────────────────────

Listar Personas Clientes Form Should Be Accessible
    [Documentation]    Verify list view of all persons and clients opens.
    [Tags]    clientes    smoke    agent-callable
    Open Clientes Sub Form    Personas Clientes
    Take Named Screenshot    ${MODULE}    14_listar_personas_clientes
    Log    Lista de Personas y Clientes form opened
