*** Settings ***
Documentation    E2E Login test for Notaire Swing frontend.
...              Launches the Swing app, enters credentials, and verifies login.
...              Requires: backend running (scripts/start.sh), frontend JAR built.
Resource         ../resources/common.resource
Suite Setup      Suite Initialization
Suite Teardown   Close Swing Application
Test Teardown    Run Keyword If Test Failed    Take Named Screenshot    login    FAIL_${TEST_NAME}

*** Variables ***
${LOGIN_USER}        admin
${LOGIN_PASSWORD}    admin

*** Keywords ***
Suite Initialization
    [Documentation]    Verify prerequisites and launch the Swing application.
    Ensure Backend Is Running
    Launch Swing Application

*** Test Cases ***
Login Window Should Appear On Startup
    [Documentation]    Verify the Login window is displayed after app launch.
    [Tags]    smoke    login    agent-callable
    Take Named Screenshot    login    01_login_window_visible

Login With Valid Credentials Should Succeed
    [Documentation]    Enter admin/admin credentials and verify successful login.
    [Tags]    login    e2e    agent-callable
    # Step 1: Type username (first field is focused by default)
    Type In Current Field    ${LOGIN_USER}
    Take Named Screenshot    login    02_username_entered

    # Step 2: Tab to password field and type password
    Navigate To Next Field
    Type In Current Field    ${LOGIN_PASSWORD}
    Take Named Screenshot    login    03_password_entered

    # Step 3: Tab to Aceptar button and press enter to click it
    Navigate To Next Field
    Submit Form
    Wait For Principal Window

    # Step 4: Capture result
    Take Named Screenshot    login    04_login_result
