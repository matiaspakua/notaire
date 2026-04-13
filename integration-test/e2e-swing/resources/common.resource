*** Settings ***
Documentation    Reusable keywords for Notaire Swing E2E tests.
...              Uses PyAutoGUI + AppleScript for macOS Swing GUI automation.
Library          Process
Library          OperatingSystem
Library          String
Library          DateTime
Library          ../libs/SwingAutomation.py

*** Variables ***
${REPO_DIR}              ${CURDIR}${/}..${/}..${/}..
${FRONTEND_JAR}          ${REPO_DIR}${/}frontend-swing${/}target${/}frontend-swing-1.0-SNAPSHOT-jar-with-dependencies.jar
${SCREENSHOT_DIR}        ${CURDIR}${/}..${/}screenshots
${STARTUP_WAIT}          8s
${ACTION_DELAY}          1s
${NAV_DELAY}             2s
${LOGIN_USER}            admin
${LOGIN_PASSWORD}        admin

*** Keywords ***
# ─────────────────────────────────────────────────────────────────────────────
# Infrastructure
# ─────────────────────────────────────────────────────────────────────────────

Ensure Backend Is Running
    [Documentation]    Verify backend API is reachable at localhost:8080.
    ${result}=    Run Process    curl    -sf    http://localhost:8080/swagger-ui.html
    ...    shell=False    timeout=10s
    IF    ${result.rc} != 0
        Fatal Error    Backend API is not running. Run: bash scripts/start.sh
    END
    Log    Backend API is healthy.

Ensure Frontend Jar Exists
    [Documentation]    Check that the frontend JAR exists.
    File Should Exist    ${FRONTEND_JAR}
    ...    msg=Frontend JAR not found. Run: mvn clean install -pl frontend-swing -am

Launch Swing Application
    [Documentation]    Start the Swing JAR in background and wait for initialization.
    Ensure Frontend Jar Exists
    ${log_file}=    Set Variable    ${CURDIR}${/}..${/}results${/}swing.log
    ${proc}=    Start Process    java    -jar    ${FRONTEND_JAR}
    ...    shell=False    alias=swing_app    stdout=${log_file}    stderr=STDOUT
    Set Suite Variable    ${SWING_PROC}    ${proc}
    Set Suite Variable    ${SWING_LOG}     ${log_file}
    Sleep    ${STARTUP_WAIT}    reason=Wait for Swing app to initialize

Close Swing Application
    [Documentation]    Terminate the Swing application process.
    ${running}=    Is Process Running    swing_app
    IF    ${running}
        Terminate Process    swing_app    kill=True
        Log    Swing application terminated.
    END

# ─────────────────────────────────────────────────────────────────────────────
# Screenshots
# ─────────────────────────────────────────────────────────────────────────────

Take Named Screenshot
    [Documentation]    Take a screenshot with a descriptive name and timestamp, organized by module.
    [Arguments]    ${module}    ${name}
    ${timestamp}=    Get Current Date    result_format=%Y%m%d_%H%M%S
    ${module_dir}=    Set Variable    ${SCREENSHOT_DIR}${/}${module}
    ${filename}=    Set Variable    ${module_dir}${/}${name}_${timestamp}.png
    Create Directory    ${module_dir}
    Capture Desktop Screenshot    ${filename}
    Log    Screenshot saved: ${filename}
    RETURN    ${filename}

# ─────────────────────────────────────────────────────────────────────────────
# Field interaction
# ─────────────────────────────────────────────────────────────────────────────

Type In Current Field
    [Documentation]    Clear the field, then type text in the currently focused field.
    [Arguments]    ${text}
    Clear Text Field
    Sleep    0.3s
    Type Text Slowly    ${text}
    Sleep    0.5s

Navigate To Next Field
    [Documentation]    Press Tab to move to the next GUI field.
    Press Tab Key
    Sleep    0.5s

Submit Form
    [Documentation]    Press Enter to submit.
    Press Enter Key
    Sleep    ${ACTION_DELAY}

# ─────────────────────────────────────────────────────────────────────────────
# Login
# ─────────────────────────────────────────────────────────────────────────────

Perform Login
    [Documentation]    Full login flow: type user, tab to password, tab to button, enter.
    [Arguments]    ${user}=${LOGIN_USER}    ${password}=${LOGIN_PASSWORD}
    Type In Current Field    ${user}
    Navigate To Next Field
    Type In Current Field    ${password}
    Navigate To Next Field
    Submit Form
    Wait For Principal Window

Wait For Principal Window
    [Documentation]    Polls the swing log for "Ventana Principal abierta" indicating successful login.
    Wait Until Keyword Succeeds    30s    2s    Check Principal Window Log

Check Principal Window Log
    [Documentation]    Helper to grep the swing log for the success message.
    ${log_content}=    Get File    ${SWING_LOG}
    Should Contain    ${log_content}    Ventana Principal abierta satisfactoriamente

# ─────────────────────────────────────────────────────────────────────────────
# Module navigation (left sidebar)
# ─────────────────────────────────────────────────────────────────────────────

Navigate To Module
    [Documentation]    Click a left-sidebar module button and wait for the log confirmation.
    [Arguments]    ${module_name}    ${log_message}
    Bring Java App To Front
    Sleep    0.3s
    Click Left Panel Button    ${module_name}
    Sleep    ${NAV_DELAY}
    ${log_content}=    Get File    ${SWING_LOG}
    Should Contain    ${log_content}    ${log_message}
    Log    Navigated to module: ${module_name}

Navigate To Clientes
    [Documentation]    Open the Clientes module.
    Navigate To Module    Clientes    Usuario navego al modulo: Clientes

Navigate To Presupuestos
    [Documentation]    Open the Presupuestos module.
    Navigate To Module    Presupuestos    Usuario navego al modulo: Presupuestos

Navigate To Gestiones
    [Documentation]    Open the Gestiones module.
    Navigate To Module    Gestiones    Usuario navego al modulo: Gestiones

Navigate To Protocolo
    [Documentation]    Open the Protocolo module.
    Navigate To Module    Protocolo    Usuario navego al modulo: Protocolo

Navigate To Administracion
    [Documentation]    Open the Administración module.
    Navigate To Module    Administración    Usuario navego al modulo: Administracion

# ─────────────────────────────────────────────────────────────────────────────
# Sub-form navigation (click a button inside a module panel)
# ─────────────────────────────────────────────────────────────────────────────

Open Sub Form
    [Documentation]    Click a button inside a module panel (e.g., "Dar Alta Persona").
    [Arguments]    ${button_name}
    Bring Java App To Front
    Sleep    0.3s
    Click Button By Name    ${button_name}
    Sleep    ${NAV_DELAY}

# ─────────────────────────────────────────────────────────────────────────────
# Log verification
# ─────────────────────────────────────────────────────────────────────────────

Verify Log Contains
    [Documentation]    Assert that the swing log contains an expected message.
    [Arguments]    ${expected_message}
    ${log_content}=    Get File    ${SWING_LOG}
    Should Contain    ${log_content}    ${expected_message}
    Log    Verified log: "${expected_message}"

# ─────────────────────────────────────────────────────────────────────────────
# Suite helpers
# ─────────────────────────────────────────────────────────────────────────────

Full Suite Setup
    [Documentation]    Standard setup: verify backend, launch app, login.
    Ensure Backend Is Running
    Launch Swing Application
    Take Named Screenshot    setup    01_app_started
    Perform Login
    Take Named Screenshot    setup    02_logged_in

Full Suite Teardown
    [Documentation]    Standard teardown: close the Swing app.
    Close Swing Application
