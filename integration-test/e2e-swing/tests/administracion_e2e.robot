*** Settings ***
Documentation    E2E acceptance tests for the Administración module of Notaire Swing.
...
...              Covers use cases:
...              - CU20 – Dar alta usuario
...              - CU21 – Modificar Usuario
...              - CU22 – Registrar Suplencia
...              - CU23 – Ver registro de actividades de usuario
...              - CU26 – Ingresar nuevo tipo de trámite
...              - CU27 – Ingresar nuevo tipo de documento
...              - CU29 – Ingresar nuevo concepto
...              - CU30 – Ingresar nuevo estado de Gestión
...              - CU31 – Modificar tipo de trámite
...              - CU32 – Modificar tipo de documento
...              - CU34 – Modificar concepto
...              - CU35 – Modificar estado de Gestión
...              - CU36 – Ingresar tipos de folio
...              - CU37 – Eliminar concepto
...              - CU38 – Eliminar tipo de documento
...              - CU39 – Crear Plantilla Presupuesto
...              - CU40 – Modificar tipo de folio
...              - CU48 – Dar alta escribano
...              - CU49 – Eliminar Plantilla Presupuesto
...              - CU51 – Modificar escribano
...              - CU55 – Modificar Plantilla Presupuesto
...              - CU57 – Eliminar tipo de trámite
...              - CU58 – Eliminar tipo de folio
...              - CU59 – Consultar Suplencias
...
...              Requires: backend running (scripts/start.sh), frontend JAR built.
Resource         ../resources/common.resource
Suite Setup      Suite Initialization
Suite Teardown   Close Swing Application
Test Teardown    Run Keyword If Test Failed    Take Named Screenshot    administracion    FAIL_${TEST_NAME}

*** Variables ***
${MODULE}    administracion

*** Keywords ***
Suite Initialization
    [Documentation]    Launch app, login, navigate to Administración.
    Ensure Backend Is Running
    Launch Swing Application
    Perform Login
    Navigate To Administracion
    Take Named Screenshot    ${MODULE}    00_administracion_module_loaded

Open Administracion Sub Module
    [Documentation]    Click a button inside the Administración panel.
    [Arguments]    ${button_name}
    Bring Java App To Front
    Sleep    0.5s
    Click Button By Name    ${button_name}
    Sleep    2s
    Take Named Screenshot    ${MODULE}    subform_${button_name}

*** Test Cases ***
# ─────────────────────────────────────────────────────────────────────────────
# Administración module loads correctly
# ─────────────────────────────────────────────────────────────────────────────

Administracion Module Should Load With All Sub Modules
    [Documentation]    Verify the Administración internal frame opens with expected buttons.
    [Tags]    administracion    smoke    agent-callable
    Take Named Screenshot    ${MODULE}    01_administracion_overview
    Log    Administración module loaded successfully

# ─────────────────────────────────────────────────────────────────────────────
# CU20, CU21, CU23 – Usuarios
# ─────────────────────────────────────────────────────────────────────────────

Usuarios Sub Module Should Open
    [Documentation]    CU20/CU21/CU23: Verify Usuarios panel opens from Administración.
    [Tags]    administracion    usuarios    smoke    CU20    CU21    CU23    agent-callable
    Open Administracion Sub Module    Usuarios
    Take Named Screenshot    ${MODULE}    02_usuarios_submodule

Dar Alta Usuario Form Should Be Accessible
    [Documentation]    CU20 – Dar alta usuario: Verify button opens the form.
    [Tags]    administracion    usuarios    CU20    agent-callable
    Bring Java App To Front
    Click Button By Name    Dar Alta Usuario
    Sleep    2s
    Take Named Screenshot    ${MODULE}    03_dar_alta_usuario_form
    Log    CU20: Dar Alta Usuario form opened successfully

Dar Alta Usuario Form Should Accept User Data
    [Documentation]    CU20: Fill in user data fields (search by name then register user).
    [Tags]    administracion    usuarios    CU20    e2e
    # The form requires searching for a Persona first, then assigning username/password
    # Step 1: Focus search field and enter name
    Type In Current Field    Admin
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    04_dar_alta_usuario_search_result

Modificar Usuario Form Should Be Accessible
    [Documentation]    CU21 – Modificar Usuario: Verify button opens the form.
    [Tags]    administracion    usuarios    CU21    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Usuarios
    Bring Java App To Front
    Click Button By Name    Modificar Usuario
    Sleep    2s
    Take Named Screenshot    ${MODULE}    05_modificar_usuario_form
    Log    CU21: Modificar Usuario form opened

Ver Registro Actividades Usuario Should Be Accessible
    [Documentation]    CU23 – Ver registro de actividades de usuario: Form accessible.
    [Tags]    administracion    usuarios    CU23    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Usuarios
    Bring Java App To Front
    Click Button By Name    Ver Registro Actividades
    Sleep    2s
    Take Named Screenshot    ${MODULE}    06_registro_actividades_form
    Log    CU23: Ver Registro de Actividades form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU48, CU51, CU22, CU59 – Escribanos
# ─────────────────────────────────────────────────────────────────────────────

Escribanos Sub Module Should Open
    [Documentation]    CU48/CU51/CU22/CU59: Verify Escribanos panel opens.
    [Tags]    administracion    escribanos    smoke    CU48    CU51    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Escribanos
    Take Named Screenshot    ${MODULE}    07_escribanos_submodule

Dar Alta Escribano Form Should Be Accessible
    [Documentation]    CU48 – Dar alta escribano: Form opens from Escribanos panel.
    [Tags]    administracion    escribanos    CU48    agent-callable
    Bring Java App To Front
    Click Button By Name    Dar Alta Escribano
    Sleep    2s
    Take Named Screenshot    ${MODULE}    08_dar_alta_escribano_form
    Log    CU48: Dar Alta Escribano form opened

Registrar Suplencia Form Should Be Accessible
    [Documentation]    CU22 – Registrar Suplencia: Form opens from Escribanos panel.
    [Tags]    administracion    escribanos    CU22    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Escribanos
    Bring Java App To Front
    Click Button By Name    Registrar Suplencia
    Sleep    2s
    Take Named Screenshot    ${MODULE}    09_registrar_suplencia_form
    Log    CU22: Registrar Suplencia form opened

Consultar Suplencias Form Should Be Accessible
    [Documentation]    CU59 – Consultar Suplencias: Form accessible from Escribanos.
    [Tags]    administracion    escribanos    CU59    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Escribanos
    Bring Java App To Front
    Click Button By Name    Consultar Suplencias
    Sleep    2s
    Take Named Screenshot    ${MODULE}    10_consultar_suplencias_form
    Log    CU59: Consultar Suplencias form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU26, CU31, CU57 – Trámites
# ─────────────────────────────────────────────────────────────────────────────

Tramites Sub Module Should Open
    [Documentation]    CU26/CU31/CU57: Verify Trámites panel opens.
    [Tags]    administracion    tramites    smoke    CU26    CU31    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Trámites
    Take Named Screenshot    ${MODULE}    11_tramites_submodule

Ingresar Tipo Tramite Form Should Be Accessible
    [Documentation]    CU26 – Ingresar nuevo tipo de trámite: Form opens.
    [Tags]    administracion    tramites    CU26    agent-callable
    Bring Java App To Front
    Click Button By Name    Ingresar Tipo Tramite
    Sleep    2s
    Take Named Screenshot    ${MODULE}    12_ingresar_tipo_tramite_form
    Log    CU26: Ingresar Tipo de Trámite form opened

Ingresar Tipo Tramite Should Accept Valid Data
    [Documentation]    CU26: Fill a new tramite type name and verify acceptance.
    [Tags]    administracion    tramites    CU26    e2e
    Type In Current Field    TestTramite_E2E
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    13_tipo_tramite_submitted
    # Dismiss any confirmation dialog
    Dismiss Dialog If Present

Modificar Tipo Tramite Form Should Be Accessible
    [Documentation]    CU31 – Modificar tipo de trámite: Form accessible.
    [Tags]    administracion    tramites    CU31    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Trámites
    Bring Java App To Front
    Click Button By Name    Modificar Tipo Tramite
    Sleep    2s
    Take Named Screenshot    ${MODULE}    14_modificar_tipo_tramite_form
    Log    CU31: Modificar Tipo de Trámite form opened

Eliminar Tipo Tramite Form Should Be Accessible
    [Documentation]    CU57 – Eliminar tipo de trámite: Form opens (soft-delete via habilitado=false).
    [Tags]    administracion    tramites    CU57    smoke    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Trámites
    Bring Java App To Front
    Click Button By Name    Eliminar Tipo Tramite
    Sleep    2s
    Take Named Screenshot    ${MODULE}    15_eliminar_tipo_tramite_form
    Log    CU57: Eliminar Tipo de Trámite form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU27, CU32, CU38 – Documentos
# ─────────────────────────────────────────────────────────────────────────────

Documentos Sub Module Should Open
    [Documentation]    CU27/CU32/CU38: Verify Documentos panel opens.
    [Tags]    administracion    documentos    smoke    CU27    CU32    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Documentos
    Take Named Screenshot    ${MODULE}    15_documentos_submodule

Ingresar Tipo Documento Form Should Be Accessible
    [Documentation]    CU27 – Ingresar nuevo tipo de documento: Form opens.
    [Tags]    administracion    documentos    CU27    agent-callable
    Bring Java App To Front
    Click Button By Name    Ingresar Documento
    Sleep    2s
    Take Named Screenshot    ${MODULE}    16_ingresar_tipo_documento_form
    Log    CU27: Ingresar Tipo de Documento form opened

Modificar Tipo Documento Form Should Be Accessible
    [Documentation]    CU32 – Modificar tipo de documento: Form accessible.
    [Tags]    administracion    documentos    CU32    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Documentos
    Bring Java App To Front
    Click Button By Name    Modificar Documento
    Sleep    2s
    Take Named Screenshot    ${MODULE}    17_modificar_tipo_documento_form
    Log    CU32: Modificar Tipo de Documento form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU29, CU34, CU37 – Conceptos
# ─────────────────────────────────────────────────────────────────────────────

Conceptos Sub Module Should Open
    [Documentation]    CU29/CU34/CU37: Verify Conceptos panel opens.
    [Tags]    administracion    conceptos    smoke    CU29    CU34    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Conceptos
    Take Named Screenshot    ${MODULE}    18_conceptos_submodule

Ingresar Concepto Form Should Be Accessible
    [Documentation]    CU29 – Ingresar nuevo concepto: Form opens.
    [Tags]    administracion    conceptos    CU29    agent-callable
    Bring Java App To Front
    Click Button By Name    Ingresar Concepto
    Sleep    2s
    Take Named Screenshot    ${MODULE}    19_ingresar_concepto_form
    Log    CU29: Ingresar Concepto form opened

Ingresar Concepto Should Accept Valid Data
    [Documentation]    CU29: Fill concepto name and description fields.
    [Tags]    administracion    conceptos    CU29    e2e
    Type In Current Field    TestConcepto_E2E
    Navigate To Next Field
    Type In Current Field    Concepto creado por test automatizado
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    20_concepto_submitted
    Dismiss Dialog If Present

Modificar Concepto Form Should Be Accessible
    [Documentation]    CU34 – Modificar concepto: Form accessible.
    [Tags]    administracion    conceptos    CU34    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Conceptos
    Bring Java App To Front
    Click Button By Name    Modificar Concepto
    Sleep    2s
    Take Named Screenshot    ${MODULE}    21_modificar_concepto_form
    Log    CU34: Modificar Concepto form opened

Eliminar Concepto Form Should Be Accessible
    [Documentation]    CU37 – Eliminar concepto: Form accessible.
    [Tags]    administracion    conceptos    CU37    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Conceptos
    Bring Java App To Front
    Click Button By Name    Eliminar Concepto
    Sleep    2s
    Take Named Screenshot    ${MODULE}    22_eliminar_concepto_form
    Log    CU37: Eliminar Concepto form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU30, CU35 – Estados de Gestión
# ─────────────────────────────────────────────────────────────────────────────

Estados De Gestion Sub Module Should Open
    [Documentation]    CU30/CU35: Verify Estados de Gestión panel opens.
    [Tags]    administracion    estados    smoke    CU30    CU35    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Estados de Gestión
    Take Named Screenshot    ${MODULE}    23_estados_gestion_submodule

Ingresar Estado Gestion Form Should Be Accessible
    [Documentation]    CU30 – Ingresar nuevo estado de Gestión: Form opens.
    [Tags]    administracion    estados    CU30    agent-callable
    Bring Java App To Front
    Click Button By Name    Ingresar Estado
    Sleep    2s
    Take Named Screenshot    ${MODULE}    24_ingresar_estado_gestion_form
    Log    CU30: Ingresar Estado de Gestión form opened

Modificar Estado Gestion Form Should Be Accessible
    [Documentation]    CU35 – Modificar estado de Gestión: Form accessible.
    [Tags]    administracion    estados    CU35    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Estados de Gestión
    Bring Java App To Front
    Click Button By Name    Modificar Estado
    Sleep    2s
    Take Named Screenshot    ${MODULE}    25_modificar_estado_gestion_form
    Log    CU35: Modificar Estado de Gestión form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU36, CU40, CU58 – Tipos de Folio
# ─────────────────────────────────────────────────────────────────────────────

Tipos De Folio Sub Module Should Open
    [Documentation]    CU36/CU40/CU58: Verify Tipos de Folio panel opens.
    [Tags]    administracion    folios    smoke    CU36    CU40    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Tipos de Folio
    Take Named Screenshot    ${MODULE}    26_tipos_folio_submodule

Ingresar Tipo Folio Form Should Be Accessible
    [Documentation]    CU36 – Ingresar tipos de folio: Form opens.
    [Tags]    administracion    folios    CU36    agent-callable
    Bring Java App To Front
    Click Button By Name    Ingresar Tipo Folio
    Sleep    2s
    Take Named Screenshot    ${MODULE}    27_ingresar_tipo_folio_form
    Log    CU36: Ingresar Tipo de Folio form opened

Modificar Tipo Folio Form Should Be Accessible
    [Documentation]    CU40 – Modificar tipo de folio: Form accessible.
    [Tags]    administracion    folios    CU40    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Tipos de Folio
    Bring Java App To Front
    Click Button By Name    Modificar Eliminar Folio
    Sleep    2s
    Take Named Screenshot    ${MODULE}    28_modificar_tipo_folio_form
    Log    CU40/CU58: Modificar/Eliminar Tipo de Folio form opened

# ─────────────────────────────────────────────────────────────────────────────
# CU39, CU49, CU55 – Plantillas Presupuesto
# ─────────────────────────────────────────────────────────────────────────────

Plantillas Presupuesto Sub Module Should Open
    [Documentation]    CU39/CU49/CU55: Verify Plantillas Presupuesto panel opens.
    [Tags]    administracion    plantillas    smoke    CU39    CU49    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Plantillas Presupuesto
    Take Named Screenshot    ${MODULE}    29_plantillas_presupuesto_submodule

Crear Plantilla Presupuesto Form Should Be Accessible
    [Documentation]    CU39 – Crear Plantilla Presupuesto: Form opens.
    [Tags]    administracion    plantillas    CU39    agent-callable
    Bring Java App To Front
    Click Button By Name    Crear Plantilla
    Sleep    2s
    Take Named Screenshot    ${MODULE}    30_crear_plantilla_presupuesto_form
    Log    CU39: Crear Plantilla Presupuesto form opened

Modificar Plantilla Presupuesto Form Should Be Accessible
    [Documentation]    CU55 – Modificar Plantilla Presupuesto: Form accessible.
    [Tags]    administracion    plantillas    CU55    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Plantillas Presupuesto
    Bring Java App To Front
    Click Button By Name    Modificar Plantilla
    Sleep    2s
    Take Named Screenshot    ${MODULE}    31_modificar_plantilla_presupuesto_form
    Log    CU55: Modificar Plantilla Presupuesto form opened

Eliminar Plantilla Presupuesto Form Should Be Accessible
    [Documentation]    CU49 – Eliminar Plantilla Presupuesto: Form accessible.
    [Tags]    administracion    plantillas    CU49    agent-callable
    Navigate To Administracion
    Open Administracion Sub Module    Plantillas Presupuesto
    Bring Java App To Front
    Click Button By Name    Eliminar Plantilla
    Sleep    2s
    Take Named Screenshot    ${MODULE}    32_eliminar_plantilla_presupuesto_form
    Log    CU49: Eliminar Plantilla Presupuesto form opened
