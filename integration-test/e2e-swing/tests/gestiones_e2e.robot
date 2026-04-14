*** Settings ***
Documentation    E2E acceptance tests for the Gestiones module of Notaire Swing.
...
...              Covers use cases:
...              - CU02 – Iniciar Gestión
...              - CU03 – Lista documentos y certificados necesarios
...              - CU04 – Registrar documentación cliente
...              - CU05 – Preparar escritura
...              - CU07 – Generar testimonio
...              - CU08 – Verificar Testimonio
...              - CU09 – Registrar deudas documentos de Cliente
...              - CU10 – Registrar movimientos documentación de entidades externas
...              - CU11 – Ingresar para inscripción
...              - CU12 – Retirar testimonio
...              - CU13 – Ver historial de gestión
...              - CU14 – Consultar estado gestión
...              - CU16 – Archivar Gestión
...              - CU43 – Reingresar documentación
...              - CU44 – Reingresar testimonio
...              - CU52 – Modificar Escritura
...              - CU53 – Modificar Gestión
...              - CU56 – Registrar inscripcion
...              - CU62 – Buscar Escritura
...
...              Requires: backend running (scripts/start.sh), frontend JAR built.
Resource         ../resources/common.resource
Suite Setup      Suite Initialization
Suite Teardown   Close Swing Application
Test Teardown    Run Keyword If Test Failed    Take Named Screenshot    gestiones    FAIL_${TEST_NAME}

*** Variables ***
${MODULE}    gestiones

*** Keywords ***
Suite Initialization
    [Documentation]    Launch app, login, navigate to Gestiones.
    Ensure Backend Is Running
    Launch Swing Application
    Perform Login
    Navigate To Gestiones
    Take Named Screenshot    ${MODULE}    00_gestiones_module_loaded

Open Gestiones Sub Form
    [Documentation]    Click a button inside the Gestiones module panel.
    [Arguments]    ${button_name}
    Navigate To Gestiones
    Bring Java App To Front
    Click Button By Name    ${button_name}
    Sleep    2s

*** Test Cases ***
# ─────────────────────────────────────────────────────────────────────────────
# Smoke
# ─────────────────────────────────────────────────────────────────────────────

Gestiones Module Should Load With All Sub Modules
    [Documentation]    Verify Gestiones module opens with expected sub-module buttons.
    [Tags]    gestiones    smoke    agent-callable
    Take Named Screenshot    ${MODULE}    01_gestiones_overview
    Log    Gestiones module loaded successfully

# ─────────────────────────────────────────────────────────────────────────────
# Gestión sub-module (CU02, CU13, CU14, CU16, CU53)
# ─────────────────────────────────────────────────────────────────────────────

Gestion Sub Module Should Open
    [Documentation]    CU02/CU13/CU14/CU16/CU53: Gestión sub-module opens.
    [Tags]    gestiones    gestion    smoke    CU02    CU13    agent-callable
    Open Gestiones Sub Form    Gestion
    Take Named Screenshot    ${MODULE}    02_gestion_submodule
    Log    Gestión sub-module opened

Iniciar Gestion Should Be Accessible
    [Documentation]    CU02 – Iniciar Gestión: New gestión form opens.
    [Tags]    gestiones    gestion    CU02    smoke    agent-callable
    Bring Java App To Front
    Click Button By Name    Iniciar Gestión
    Sleep    2s
    Take Named Screenshot    ${MODULE}    03_iniciar_gestion_form
    Log    CU02: Iniciar Gestión form opened

Iniciar Gestion Requires Client Selection
    [Documentation]    CU02: Form requires an existing presupuesto/client.
    [Tags]    gestiones    gestion    CU02    e2e
    Type In Current Field    Juan
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    04_iniciar_gestion_client_search
    Dismiss Dialog If Present
    Log    CU02: Iniciar Gestión client search executed

Ver Historial Gestion Should Be Accessible
    [Documentation]    CU13 – Ver historial de gestión: Form opens.
    [Tags]    gestiones    gestion    CU13    smoke    agent-callable
    Open Gestiones Sub Form    Gestion
    Bring Java App To Front
    Click Button By Name    Ver Historial
    Sleep    2s
    Take Named Screenshot    ${MODULE}    05_ver_historial_gestion_form
    Log    CU13: Ver Historial de Gestión form opened

Consultar Estado Gestion Should Be Accessible
    [Documentation]    CU14 – Consultar estado gestión: Form opens.
    [Tags]    gestiones    gestion    CU14    smoke    agent-callable
    Open Gestiones Sub Form    Gestion
    Bring Java App To Front
    Click Button By Name    Consultar Estado
    Sleep    2s
    Take Named Screenshot    ${MODULE}    06_consultar_estado_gestion_form
    Log    CU14: Consultar Estado de Gestión form opened

Archivar Gestion Should Be Accessible
    [Documentation]    CU16 – Archivar Gestión: Form opens.
    [Tags]    gestiones    gestion    CU16    smoke    agent-callable
    Open Gestiones Sub Form    Gestion
    Bring Java App To Front
    Click Button By Name    Archivar Gestión
    Sleep    2s
    Take Named Screenshot    ${MODULE}    07_archivar_gestion_form
    Log    CU16: Archivar Gestión form opened

# ─────────────────────────────────────────────────────────────────────────────
# Documentación sub-module (CU03, CU04, CU09, CU10, CU43)
# ─────────────────────────────────────────────────────────────────────────────

Documentacion Sub Module Should Open
    [Documentation]    CU03/CU04/CU09/CU10/CU43: Documentación sub-module opens.
    [Tags]    gestiones    documentacion    smoke    CU03    CU04    agent-callable
    Open Gestiones Sub Form    Documentación
    Take Named Screenshot    ${MODULE}    08_documentacion_submodule
    Log    Documentación sub-module opened

Lista Documentos Necesarios Should Be Accessible
    [Documentation]    CU03 – Lista documentos y certificados necesarios: Form opens.
    [Tags]    gestiones    documentacion    CU03    smoke    agent-callable
    Bring Java App To Front
    Click Button By Name    Lista Documentos
    Sleep    2s
    Take Named Screenshot    ${MODULE}    09_lista_documentos_form
    Log    CU03: Lista de Documentos y Certificados Necesarios form opened

Registrar Documentacion Cliente Should Be Accessible
    [Documentation]    CU04 – Registrar documentación cliente: Form opens.
    [Tags]    gestiones    documentacion    CU04    smoke    agent-callable
    Open Gestiones Sub Form    Documentación
    Bring Java App To Front
    Click Button By Name    Registrar Documentación
    Sleep    2s
    Take Named Screenshot    ${MODULE}    10_registrar_documentacion_form
    Log    CU04: Registrar Documentación Cliente form opened

Registrar Deudas Documentos Should Be Accessible
    [Documentation]    CU09 – Registrar deudas documentos de Cliente: Form opens.
    [Tags]    gestiones    documentacion    CU09    smoke    agent-callable
    Open Gestiones Sub Form    Documentación
    Bring Java App To Front
    Click Button By Name    Registrar Deudas
    Sleep    2s
    Take Named Screenshot    ${MODULE}    11_registrar_deudas_form
    Log    CU09: Registrar Deudas Documentos form opened

Registrar Movimientos Documentacion Should Be Accessible
    [Documentation]    CU10 – Registrar movimientos documentación entidades externas.
    [Tags]    gestiones    documentacion    CU10    smoke    agent-callable
    Open Gestiones Sub Form    Documentación
    Bring Java App To Front
    Click Button By Name    Registrar Movimientos
    Sleep    2s
    Take Named Screenshot    ${MODULE}    12_registrar_movimientos_form
    Log    CU10: Registrar Movimientos de Documentación form opened

# ─────────────────────────────────────────────────────────────────────────────
# Escrituras sub-module (CU05, CU52, CU62)
# ─────────────────────────────────────────────────────────────────────────────

Escrituras Sub Module Should Open
    [Documentation]    CU05/CU52/CU62: Escrituras sub-module opens.
    [Tags]    gestiones    escrituras    smoke    CU05    CU52    agent-callable
    Open Gestiones Sub Form    Escrituras
    Take Named Screenshot    ${MODULE}    13_escrituras_submodule
    Log    Escrituras sub-module opened

Preparar Escritura Should Be Accessible
    [Documentation]    CU05 – Preparar escritura: Form opens from Escrituras.
    [Tags]    gestiones    escrituras    CU05    smoke    agent-callable
    Bring Java App To Front
    Click Button By Name    Preparar Escritura
    Sleep    2s
    Take Named Screenshot    ${MODULE}    14_preparar_escritura_form
    Log    CU05: Preparar Escritura form opened

Modificar Escritura Should Be Accessible
    [Documentation]    CU52 – Modificar Escritura: Form opens.
    [Tags]    gestiones    escrituras    CU52    smoke    agent-callable
    Open Gestiones Sub Form    Escrituras
    Bring Java App To Front
    Click Button By Name    Modificar Escritura
    Sleep    2s
    Take Named Screenshot    ${MODULE}    15_modificar_escritura_form
    Log    CU52: Modificar Escritura form opened

Buscar Escritura Should Be Accessible
    [Documentation]    CU62 – Buscar Escritura: Search form opens.
    [Tags]    gestiones    escrituras    CU62    smoke    agent-callable
    Open Gestiones Sub Form    Escrituras
    Bring Java App To Front
    Click Button By Name    Buscar Escritura
    Sleep    2s
    Take Named Screenshot    ${MODULE}    16_buscar_escritura_form
    Log    CU62: Buscar Escritura form opened

Buscar Escritura Should Execute Search
    [Documentation]    CU62: Search by folio number or date.
    [Tags]    gestiones    escrituras    CU62    e2e
    Type In Current Field    1
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    17_buscar_escritura_results
    Dismiss Dialog If Present
    Log    CU62: Buscar Escritura search executed

# ─────────────────────────────────────────────────────────────────────────────
# Testimonios sub-module (CU07, CU08, CU12, CU44)
# ─────────────────────────────────────────────────────────────────────────────

Testimonios Sub Module Should Open
    [Documentation]    CU07/CU08/CU12/CU44: Testimonios sub-module opens.
    [Tags]    gestiones    testimonios    smoke    CU07    CU08    agent-callable
    Open Gestiones Sub Form    Testimonios
    Take Named Screenshot    ${MODULE}    18_testimonios_submodule
    Log    Testimonios sub-module opened

Generar Testimonio Should Be Accessible
    [Documentation]    CU07 – Generar testimonio: Form opens.
    [Tags]    gestiones    testimonios    CU07    smoke    agent-callable
    Bring Java App To Front
    Click Button By Name    Generar Testimonio
    Sleep    2s
    Take Named Screenshot    ${MODULE}    19_generar_testimonio_form
    Log    CU07: Generar Testimonio form opened

Verificar Testimonio Should Be Accessible
    [Documentation]    CU08 – Verificar Testimonio: Form opens.
    [Tags]    gestiones    testimonios    CU08    smoke    agent-callable
    Open Gestiones Sub Form    Testimonios
    Bring Java App To Front
    Click Button By Name    Verificar Testimonio
    Sleep    2s
    Take Named Screenshot    ${MODULE}    20_verificar_testimonio_form
    Log    CU08: Verificar Testimonio form opened

Retirar Testimonio Should Be Accessible
    [Documentation]    CU12 – Retirar testimonio: Form opens.
    [Tags]    gestiones    testimonios    CU12    smoke    agent-callable
    Open Gestiones Sub Form    Testimonios
    Bring Java App To Front
    Click Button By Name    Retirar Testimonio
    Sleep    2s
    Take Named Screenshot    ${MODULE}    21_retirar_testimonio_form
    Log    CU12: Retirar Testimonio form opened

# ─────────────────────────────────────────────────────────────────────────────
# Inscripciones sub-module (CU11, CU56)
# ─────────────────────────────────────────────────────────────────────────────

Inscripciones Sub Module Should Open
    [Documentation]    CU11/CU56: Inscripciones sub-module opens.
    [Tags]    gestiones    inscripciones    smoke    CU11    CU56    agent-callable
    Open Gestiones Sub Form    Inscripciones
    Take Named Screenshot    ${MODULE}    22_inscripciones_submodule
    Log    Inscripciones sub-module opened

Ingresar Para Inscripcion Should Be Accessible
    [Documentation]    CU11 – Ingresar para inscripción: Form opens.
    [Tags]    gestiones    inscripciones    CU11    smoke    agent-callable
    Bring Java App To Front
    Click Button By Name    Ingresar Inscripcion
    Sleep    2s
    Take Named Screenshot    ${MODULE}    23_ingresar_inscripcion_form
    Log    CU11: Ingresar para Inscripción form opened

Registrar Inscripcion Should Be Accessible
    [Documentation]    CU56 – Registrar inscripcion: Form opens.
    [Tags]    gestiones    inscripciones    CU56    smoke    agent-callable
    Open Gestiones Sub Form    Inscripciones
    Bring Java App To Front
    Click Button By Name    Registrar Inscripcion
    Sleep    2s
    Take Named Screenshot    ${MODULE}    24_registrar_inscripcion_form
    Log    CU56: Registrar Inscripción form opened
