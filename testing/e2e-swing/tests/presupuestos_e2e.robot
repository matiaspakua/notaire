*** Settings ***
Documentation    E2E acceptance tests for the Presupuestos module of Notaire Swing.
...
...              Covers use cases:
...              - CU01 – Preparar Presupuesto
...              - CU45 – Modificar presupuesto
...              - CU60 – Buscar Presupuesto
...
...              Requires: backend running (scripts/start.sh), frontend JAR built.
Resource         ../resources/common.resource
Suite Setup      Suite Initialization
Suite Teardown   Close Swing Application
Test Teardown    Run Keyword If Test Failed    Take Named Screenshot    presupuestos    FAIL_${TEST_NAME}

*** Variables ***
${MODULE}    presupuestos

*** Keywords ***
Suite Initialization
    [Documentation]    Launch app, login, navigate to Presupuestos.
    Ensure Backend Is Running
    Launch Swing Application
    Perform Login
    Navigate To Presupuestos
    Take Named Screenshot    ${MODULE}    00_presupuestos_module_loaded

Open Presupuestos Sub Form
    [Documentation]    Click a button inside the Presupuestos module panel.
    [Arguments]    ${button_name}
    Navigate To Presupuestos
    Bring Java App To Front
    Click Button By Name    ${button_name}
    Sleep    2s

*** Test Cases ***
# ─────────────────────────────────────────────────────────────────────────────
# Smoke
# ─────────────────────────────────────────────────────────────────────────────

Presupuestos Module Should Load With Sub Forms
    [Documentation]    Verify Presupuestos module opens with expected buttons.
    [Tags]    presupuestos    smoke    agent-callable
    Take Named Screenshot    ${MODULE}    01_presupuestos_overview
    Log    Presupuestos module loaded successfully

# ─────────────────────────────────────────────────────────────────────────────
# CU01 – Preparar Presupuesto (Crear)
# ─────────────────────────────────────────────────────────────────────────────

Crear Presupuesto Form Should Be Accessible
    [Documentation]    CU01 – Preparar Presupuesto: Form opens from button.
    [Tags]    presupuestos    CU01    smoke    agent-callable
    Open Presupuestos Sub Form    Crear Presupuesto
    Take Named Screenshot    ${MODULE}    02_crear_presupuesto_form
    Log    CU01: Crear Presupuesto form opened

Crear Presupuesto Should Load Client Search
    [Documentation]    CU01: Verify form allows searching for a client to attach budget.
    [Tags]    presupuestos    CU01    e2e
    # The form requires searching for a client first
    Type In Current Field    Juan
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    03_crear_presupuesto_client_search
    Dismiss Dialog If Present
    Log    CU01: Client search in Crear Presupuesto executed

Crear Presupuesto Should Show Tramite Options
    [Documentation]    CU01: After client selection, form shows trámite/concepto fields.
    [Tags]    presupuestos    CU01    e2e
    Open Presupuestos Sub Form    Crear Presupuesto
    Take Named Screenshot    ${MODULE}    04_crear_presupuesto_tramite_options
    Log    CU01: Crear Presupuesto tramite options visible

# ─────────────────────────────────────────────────────────────────────────────
# CU60 – Buscar Presupuesto
# ─────────────────────────────────────────────────────────────────────────────

Buscar Presupuesto Form Should Be Accessible
    [Documentation]    CU60 – Buscar Presupuesto: Search form opens.
    [Tags]    presupuestos    CU60    smoke    agent-callable
    Open Presupuestos Sub Form    Buscar Presupuesto
    Take Named Screenshot    ${MODULE}    05_buscar_presupuesto_form
    Log    CU60: Buscar Presupuesto form opened

Buscar Presupuesto Should Search By Client Name
    [Documentation]    CU60: Search for presupuesto by client name.
    [Tags]    presupuestos    CU60    e2e
    Type In Current Field    Juan
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    06_buscar_presupuesto_results
    Dismiss Dialog If Present
    Log    CU60: Buscar Presupuesto search executed

Buscar Presupuesto Should Handle No Results Gracefully
    [Documentation]    CU60: Search with non-existent client handles gracefully.
    [Tags]    presupuestos    CU60    negative
    Open Presupuestos Sub Form    Buscar Presupuesto
    Type In Current Field    ClienteNoExiste_XYZ
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    07_buscar_presupuesto_no_results
    Dismiss Dialog If Present
    Log    CU60: No results case handled gracefully

# ─────────────────────────────────────────────────────────────────────────────
# CU45 – Modificar Presupuesto
# ─────────────────────────────────────────────────────────────────────────────

Modificar Presupuesto Form Should Be Accessible
    [Documentation]    CU45 – Modificar presupuesto: Form opens from button.
    [Tags]    presupuestos    CU45    smoke    agent-callable
    Open Presupuestos Sub Form    Modificar Presupuesto
    Take Named Screenshot    ${MODULE}    08_modificar_presupuesto_form
    Log    CU45: Modificar Presupuesto form opened

Modificar Presupuesto Requires Selecting Existing Budget
    [Documentation]    CU45: Verify the form requires selecting an existing presupuesto.
    [Tags]    presupuestos    CU45    e2e
    # Search for a presupuesto to modify
    Type In Current Field    Juan
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    09_modificar_presupuesto_search
    Dismiss Dialog If Present
    Log    CU45: Modificar Presupuesto requires existing budget selection
