*** Settings ***
Documentation    Reusable keywords for Notaire Swing E2E tests.
...              Uses PyAutoGUI for macOS GUI automation with Robot Framework.
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

*** Keywords ***
Ensure Backend Is Running
    [Documentation]    Verify backend API is reachable.
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

Wait For Principal Window
    [Documentation]    Polls the java output log for the 'Ventana Principal abierta' message indicating successful login and main GUI load.
    Wait Until Keyword Succeeds    30s    2s    Check Principal Window Log

Check Principal Window Log
    [Documentation]    Helper to grep the swing output log for the success message.
    ${log_content}=    Get File    ${SWING_LOG}
    Should Contain    ${log_content}    Ventana Principal abierta satisfactoriamente
