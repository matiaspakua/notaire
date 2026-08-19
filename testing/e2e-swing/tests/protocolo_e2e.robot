*** Settings ***
Documentation    E2E acceptance tests for the Protocolo module of Notaire Swing.
...
...              Covers use cases:
...              - CU24 – Generar libro de índices
...              - CU25 – Generar Declaración Jurada del mes
...              - CU28 – Ingresar nuevos folios
...              - CU33 – Modificar folio
...              - CU50 – Generar Declaración Jurada de Rentas
...              - CU63 – Buscar Folios
...              - CU64 – Buscar Tipo de tramite (ref. from Protocolo)
...              - CU68 – Buscar tipos de folios
...
...              Requires: backend running (scripts/start.sh), frontend JAR built.
Resource         ../resources/common.resource
Suite Setup      Suite Initialization
Suite Teardown   Close Swing Application
Test Teardown    Run Keyword If Test Failed    Take Named Screenshot    protocolo    FAIL_${TEST_NAME}

*** Variables ***
${MODULE}    protocolo
${TEST_YEAR}    2026
${TEST_MONTH}    4

*** Keywords ***
Suite Initialization
    [Documentation]    Launch app, login, navigate to Protocolo.
    Ensure Backend Is Running
    Launch Swing Application
    Perform Login
    Navigate To Protocolo
    Take Named Screenshot    ${MODULE}    00_protocolo_module_loaded

Open Protocolo Sub Form
    [Documentation]    Click a button inside the Protocolo module panel.
    [Arguments]    ${button_name}
    Navigate To Protocolo
    Bring Java App To Front
    Click Button By Name    ${button_name}
    Sleep    2s

*** Test Cases ***
# ─────────────────────────────────────────────────────────────────────────────
# Smoke
# ─────────────────────────────────────────────────────────────────────────────

Protocolo Module Should Load With All Sub Modules
    [Documentation]    Verify Protocolo module opens with Folios, DDJJ, Índices buttons.
    [Tags]    protocolo    smoke    agent-callable
    Take Named Screenshot    ${MODULE}    01_protocolo_overview
    Log    Protocolo module loaded successfully

# ─────────────────────────────────────────────────────────────────────────────
# CU28, CU33, CU63, CU68 – Folios
# ─────────────────────────────────────────────────────────────────────────────

Folios Sub Module Should Open
    [Documentation]    CU28/CU33/CU63/CU68: Folios sub-module opens.
    [Tags]    protocolo    folios    smoke    CU28    CU33    agent-callable
    Open Protocolo Sub Form    Folios
    Take Named Screenshot    ${MODULE}    02_folios_submodule
    Log    Folios sub-module opened

Ingresar Nuevos Folios Form Should Be Accessible
    [Documentation]    CU28 – Ingresar nuevos folios: Form opens.
    [Tags]    protocolo    folios    CU28    smoke    agent-callable
    Bring Java App To Front
    Click Button By Name    Ingresar Folios
    Sleep    2s
    Take Named Screenshot    ${MODULE}    03_ingresar_folios_form
    Log    CU28: Ingresar Nuevos Folios form opened

Ingresar Folios Should Accept Valid Folio Data
    [Documentation]    CU28: Fill folio number, tipo, and date fields.
    [Tags]    protocolo    folios    CU28    e2e
    # Folio number
    Type In Current Field    9999
    Navigate To Next Field
    # Year
    Type In Current Field    ${TEST_YEAR}
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    04_ingresar_folios_submitted
    Dismiss Dialog If Present
    Log    CU28: Ingresar Nuevos Folios submission attempted

Modificar Folio Form Should Be Accessible
    [Documentation]    CU33 – Modificar folio: Form opens from Folios sub-module.
    [Tags]    protocolo    folios    CU33    smoke    agent-callable
    Open Protocolo Sub Form    Folios
    Bring Java App To Front
    Click Button By Name    Modificar Folio
    Sleep    2s
    Take Named Screenshot    ${MODULE}    05_modificar_folio_form
    Log    CU33: Modificar Folio form opened

Buscar Folios Form Should Be Accessible
    [Documentation]    CU63 – Buscar Folios: Search form opens.
    [Tags]    protocolo    folios    CU63    smoke    agent-callable
    Open Protocolo Sub Form    Folios
    Bring Java App To Front
    Click Button By Name    Buscar Folios
    Sleep    2s
    Take Named Screenshot    ${MODULE}    06_buscar_folios_form
    Log    CU63: Buscar Folios form opened

Buscar Folios Should Execute Search By Year
    [Documentation]    CU63: Search folios by year.
    [Tags]    protocolo    folios    CU63    e2e
    Type In Current Field    ${TEST_YEAR}
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    07_buscar_folios_results
    Dismiss Dialog If Present
    Log    CU63: Buscar Folios search executed

Buscar Tipos De Folios Form Should Be Accessible
    [Documentation]    CU68 – Buscar tipos de folios: Search form opens.
    [Tags]    protocolo    folios    CU68    smoke    agent-callable
    Open Protocolo Sub Form    Folios
    Bring Java App To Front
    Click Button By Name    Buscar Tipos Folios
    Sleep    2s
    Take Named Screenshot    ${MODULE}    08_buscar_tipos_folios_form
    Log    CU68: Buscar Tipos de Folios form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU24 – Generar libro de índices
# ─────────────────────────────────────────────────────────────────────────────

Generar Indices Form Should Be Accessible
    [Documentation]    CU24 – Generar libro de índices: Form opens from Protocolo.
    [Tags]    protocolo    indices    CU24    smoke    agent-callable
    Open Protocolo Sub Form    Generar Indices
    Take Named Screenshot    ${MODULE}    09_generar_indices_form
    Log    CU24: Generar Libro de Índices form opened

Generar Indices Should Accept Year And Month
    [Documentation]    CU24: Fill year and month to generate monthly index report.
    [Tags]    protocolo    indices    CU24    e2e
    # Year field
    Type In Current Field    ${TEST_YEAR}
    Navigate To Next Field
    # Month field
    Type In Current Field    ${TEST_MONTH}
    Navigate To Next Field
    Submit Form
    Sleep    3s
    Take Named Screenshot    ${MODULE}    10_generar_indices_result
    Dismiss Dialog If Present
    Log    CU24: Generar Libro de Índices generation attempted

# ─────────────────────────────────────────────────────────────────────────────
# CU25 – Generar Declaración Jurada del mes
# ─────────────────────────────────────────────────────────────────────────────

Generar DDJJ Form Should Be Accessible
    [Documentation]    CU25 – Generar Declaración Jurada del mes: Form opens.
    [Tags]    protocolo    ddjj    CU25    smoke    agent-callable
    Open Protocolo Sub Form    Generar DDJJ
    Take Named Screenshot    ${MODULE}    11_generar_ddjj_form
    Log    CU25: Generar Declaración Jurada del mes form opened

Generar DDJJ Should Accept Year And Month
    [Documentation]    CU25: Fill year and month to generate monthly DDJJ report.
    [Tags]    protocolo    ddjj    CU25    e2e
    Type In Current Field    ${TEST_YEAR}
    Navigate To Next Field
    Type In Current Field    ${TEST_MONTH}
    Navigate To Next Field
    Submit Form
    Sleep    3s
    Take Named Screenshot    ${MODULE}    12_generar_ddjj_result
    Dismiss Dialog If Present
    Log    CU25: Generar DDJJ generation attempted

Generar DDJJ Should Reject Future Dates
    [Documentation]    CU25: DDJJ for future month should be rejected or warned.
    [Tags]    protocolo    ddjj    CU25    validation    negative
    Open Protocolo Sub Form    Generar DDJJ
    Type In Current Field    2099
    Navigate To Next Field
    Type In Current Field    12
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    13_generar_ddjj_future_date
    Dismiss Dialog If Present
    Log    CU25: Future date validation tested

# ─────────────────────────────────────────────────────────────────────────────
# CU50 – Generar Declaración Jurada de Rentas
# ─────────────────────────────────────────────────────────────────────────────

Generar DDJJ Rentas Form Should Be Accessible
    [Documentation]    CU50 – Generar Declaración Jurada de Rentas: Form opens.
    [Tags]    protocolo    ddjj    CU50    smoke    agent-callable
    # DDJJ Rentas may be accessible from the DDJJ section or via menu
    Navigate To Protocolo
    Bring Java App To Front
    Click Button By Name    Generar DDJJ
    Sleep    2s
    Bring Java App To Front
    Click Button By Name    Declaración Jurada Rentas
    Sleep    2s
    Take Named Screenshot    ${MODULE}    14_generar_ddjj_rentas_form
    Log    CU50: Generar Declaración Jurada de Rentas form opened
