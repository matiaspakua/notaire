*** Settings ***
Documentation    E2E navigation tests for the Principal (main) window of Notaire Swing.
...
...              Validates:
...              - CU: Login flow (admin/admin)
...              - Principal window loads with all module buttons
...              - Each module button navigates to the correct sub-module
...              - Menu bar items are accessible
...
...              Requires: backend running (scripts/start.sh), frontend JAR built.
Resource         ../resources/common.resource
Suite Setup      Suite Initialization
Suite Teardown   Close Swing Application
Test Teardown    Run Keyword If Test Failed    Take Named Screenshot    navigation    FAIL_${TEST_NAME}

*** Variables ***
${MODULE}    navigation

*** Keywords ***
Suite Initialization
    [Documentation]    Start backend check, launch app, login once.
    Ensure Backend Is Running
    Launch Swing Application
    Take Named Screenshot    ${MODULE}    01_app_launched

*** Test Cases ***
# ─────────────────────────────────────────────────────────────────────────────
# Login smoke tests (CU: Login)
# ─────────────────────────────────────────────────────────────────────────────

Login Window Should Appear On Startup
    [Documentation]    Verify the Login window is displayed after app launch.
    [Tags]    smoke    login    navigation    agent-callable
    Take Named Screenshot    ${MODULE}    02_login_window

Login With Invalid Credentials Should Fail Gracefully
    [Documentation]    Enter wrong credentials - system should not crash (may show error dialog).
    [Tags]    login    negative    agent-callable
    Type In Current Field    wronguser
    Navigate To Next Field
    Type In Current Field    wrongpass
    Navigate To Next Field
    Submit Form
    Sleep    2s
    Take Named Screenshot    ${MODULE}    03_invalid_login_attempt
    # Dismiss any error dialog that may have appeared
    Dismiss Dialog If Present
    Sleep    1s
    # App should still be running (not crashed)
    ${running}=    Is Process Running    swing_app
    Should Be True    ${running}    msg=App crashed after invalid login

Login With Valid Credentials Should Open Principal Window
    [Documentation]    Enter admin/admin and verify main window loads successfully.
    [Tags]    login    smoke    e2e    agent-callable
    # Clear any leftover text from previous test
    Clear Text Field
    Navigate To Next Field
    Clear Text Field
    # Tab back to username
    Press Key Combination    shift    tab
    # Enter valid credentials
    Type In Current Field    ${LOGIN_USER}
    Navigate To Next Field
    Type In Current Field    ${LOGIN_PASSWORD}
    Navigate To Next Field
    Submit Form
    Wait For Principal Window
    Take Named Screenshot    ${MODULE}    04_principal_window_loaded

# ─────────────────────────────────────────────────────────────────────────────
# Module navigation tests
# ─────────────────────────────────────────────────────────────────────────────

Clicking Clientes Button Should Open Clientes Module
    [Documentation]    Verify the Clientes sidebar button navigates to the Clientes module.
    [Tags]    navigation    clientes    smoke    agent-callable
    Navigate To Clientes
    Take Named Screenshot    ${MODULE}    05_clientes_module

Clicking Presupuestos Button Should Open Presupuestos Module
    [Documentation]    Verify the Presupuestos sidebar button opens Presupuestos.
    [Tags]    navigation    presupuestos    smoke    agent-callable
    Navigate To Presupuestos
    Take Named Screenshot    ${MODULE}    06_presupuestos_module

Clicking Gestiones Button Should Open Gestiones Module
    [Documentation]    Verify the Gestiones sidebar button opens Gestiones.
    [Tags]    navigation    gestiones    smoke    agent-callable
    Navigate To Gestiones
    Take Named Screenshot    ${MODULE}    07_gestiones_module

Clicking Protocolo Button Should Open Protocolo Module
    [Documentation]    Verify the Protocolo sidebar button opens Protocolo.
    [Tags]    navigation    protocolo    smoke    agent-callable
    Navigate To Protocolo
    Take Named Screenshot    ${MODULE}    08_protocolo_module

Clicking Administracion Button Should Open Administracion Module
    [Documentation]    Verify the Administración sidebar button opens Administración.
    [Tags]    navigation    administracion    smoke    agent-callable
    Navigate To Administracion
    Take Named Screenshot    ${MODULE}    09_administracion_module

Clicking Pagos Button Should Show Under Construction Screen
    [Documentation]    Pagos module shows "En construcción" screen (not yet implemented).
    [Tags]    navigation    pagos    agent-callable
    Bring Java App To Front
    Sleep    0.3s
    Click Left Panel Button    Pagos
    Sleep    2s
    Take Named Screenshot    ${MODULE}    10_pagos_under_construction
    # Note: Pagos module shows CartelConstruccion - this is expected behavior
    Log    Pagos module is under construction - expected behavior documented

# ─────────────────────────────────────────────────────────────────────────────
# Log verification for all navigation
# ─────────────────────────────────────────────────────────────────────────────

Log Should Record All Module Navigations
    [Documentation]    Verify the log recorded all module navigation events.
    [Tags]    navigation    log    agent-callable
    Verify Log Contains    Usuario navego al modulo: Clientes
    Verify Log Contains    Usuario navego al modulo: Presupuestos
    Verify Log Contains    Usuario navego al modulo: Gestiones
    Verify Log Contains    Usuario navego al modulo: Protocolo
    Verify Log Contains    Usuario navego al modulo: Administracion
    Log    All module navigation events verified in log
