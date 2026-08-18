"""
SwingAutomation - Robot Framework library for Java Swing GUI automation.
Uses PyAutoGUI for desktop interaction and AppleScript (macOS) for
accessibility-based element finding by name.
Designed to be lightweight and agent-friendly.
"""

import pyautogui
import time
import os
import subprocess
import re
from robot.api import logger
from robot.api.deco import keyword


# PyAutoGUI safety settings
pyautogui.FAILSAFE = True
pyautogui.PAUSE = 0.1


class SwingAutomation:
    """Robot Framework library wrapping PyAutoGUI + AppleScript for Swing desktop automation."""

    ROBOT_LIBRARY_SCOPE = "GLOBAL"

    def __init__(self):
        logger.info("SwingAutomation library initialized (PyAutoGUI + AppleScript backend)")

    # ─────────────────────────────────────────────────────────────────────────
    # Screenshot
    # ─────────────────────────────────────────────────────────────────────────

    @keyword("Capture Desktop Screenshot")
    def capture_desktop_screenshot(self, filepath):
        """Take a full desktop screenshot and save to filepath."""
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        try:
            screenshot = pyautogui.screenshot()
            screenshot.save(filepath)
            logger.info(f"Screenshot saved: {filepath}")
        except Exception as e:
            logger.warn(f"Failed to capture screenshot (permissions issue?): {e}")
        return filepath

    # ─────────────────────────────────────────────────────────────────────────
    # Typing & keyboard
    # ─────────────────────────────────────────────────────────────────────────

    @keyword("Type Text Slowly")
    def type_text_slowly(self, text, interval=0.05):
        """Type text character by character with a small interval."""
        pyautogui.typewrite(text, interval=float(interval))
        logger.info(f"Typed: {text}")

    @keyword("Clear Text Field")
    def clear_text_field(self):
        """Select all text and delete it."""
        pyautogui.hotkey("command", "a")
        time.sleep(0.1)
        pyautogui.press("backspace")
        time.sleep(0.2)

    @keyword("Press Tab Key")
    def press_tab_key(self):
        """Press Tab key to navigate to the next field."""
        pyautogui.press("tab")
        time.sleep(0.3)

    @keyword("Press Enter Key")
    def press_enter_key(self):
        """Press Enter key."""
        pyautogui.press("enter")
        time.sleep(0.3)

    @keyword("Press Escape Key")
    def press_escape_key(self):
        """Press Escape key to dismiss dialogs."""
        pyautogui.press("escape")
        time.sleep(0.3)

    @keyword("Press Space Key")
    def press_space_key(self):
        """Press Space key (activates focused button)."""
        pyautogui.press("space")
        time.sleep(0.3)

    @keyword("Click At Coordinates")
    def click_at_coordinates(self, x, y):
        """Click at specific screen coordinates."""
        pyautogui.click(int(x), int(y))
        logger.info(f"Clicked at ({x}, {y})")

    @keyword("Move Mouse To")
    def move_mouse_to(self, x, y):
        """Move mouse to specific coordinates."""
        pyautogui.moveTo(int(x), int(y))

    @keyword("Press Key Combination")
    def press_key_combination(self, *keys):
        """Press a key combination (e.g., 'command', 'a')."""
        pyautogui.hotkey(*keys)
        logger.info(f"Pressed: {'+'.join(keys)}")

    @keyword("Wait Seconds")
    def wait_seconds(self, seconds):
        """Wait for specified seconds."""
        time.sleep(float(seconds))

    @keyword("Select All Text")
    def select_all_text(self):
        """Press Cmd+A to select all text."""
        pyautogui.hotkey("command", "a")

    # ─────────────────────────────────────────────────────────────────────────
    # AppleScript-based accessibility (macOS Swing automation)
    # ─────────────────────────────────────────────────────────────────────────

    def _run_applescript(self, script):
        """Run an AppleScript and return stdout."""
        result = subprocess.run(
            ["osascript", "-e", script],
            capture_output=True, text=True, timeout=10
        )
        if result.returncode != 0:
            logger.warn(f"AppleScript error: {result.stderr.strip()}")
        return result.stdout.strip(), result.returncode

    @keyword("Click Button By Name")
    def click_button_by_name(self, button_name, window_title="Notaire"):
        """Click a button by its accessibility label (text) using AppleScript.

        Works for Swing buttons when macOS Accessibility is enabled.
        Raises RuntimeError if the button is not found.
        """
        script = f'''
tell application "System Events"
    tell process "java"
        set frontmost to true
        delay 0.3
        try
            click button "{button_name}" of window 1
            return "clicked"
        on error errMsg
            try
                -- Try searching in all UI elements recursively
                set allButtons to every button of window 1
                repeat with btn in allButtons
                    if description of btn is "{button_name}" then
                        click btn
                        return "clicked by description"
                    end if
                end repeat
            end try
            error errMsg
        end try
    end tell
end tell
'''
        out, rc = self._run_applescript(script)
        if rc != 0 or "clicked" not in out:
            logger.warn(f"AppleScript click failed for '{button_name}', trying image/coordinate fallback")
            raise RuntimeError(f"Button '{button_name}' not found via AppleScript.")
        logger.info(f"Clicked button: '{button_name}' via AppleScript")
        time.sleep(0.5)

    @keyword("Click Menu Item")
    def click_menu_item(self, menu_name, item_name=None):
        """Click a menu bar item, optionally selecting a sub-item."""
        if item_name:
            script = f'''
tell application "System Events"
    tell process "java"
        set frontmost to true
        delay 0.2
        click menu item "{item_name}" of menu "{menu_name}" of menu bar 1
    end tell
end tell
'''
        else:
            script = f'''
tell application "System Events"
    tell process "java"
        set frontmost to true
        delay 0.2
        click menu "{menu_name}" of menu bar 1
    end tell
end tell
'''
        out, rc = self._run_applescript(script)
        if rc != 0:
            logger.warn(f"Menu click failed: {menu_name} > {item_name}")
        time.sleep(0.5)

    @keyword("Get Screen Size")
    def get_screen_size(self):
        """Return screen width and height as a tuple."""
        size = pyautogui.size()
        logger.info(f"Screen size: {size.width}x{size.height}")
        return size.width, size.height

    @keyword("Bring Java App To Front")
    def bring_java_app_to_front(self):
        """Activate the Java process (Swing app) window."""
        script = '''
tell application "System Events"
    tell process "java"
        set frontmost to true
    end tell
end tell
'''
        self._run_applescript(script)
        time.sleep(0.5)

    @keyword("Get Swing Window Title")
    def get_swing_window_title(self):
        """Return the title of the current Swing window."""
        script = '''
tell application "System Events"
    tell process "java"
        return name of window 1
    end tell
end tell
'''
        out, rc = self._run_applescript(script)
        logger.info(f"Window title: {out}")
        return out

    @keyword("Click Left Panel Button")
    def click_left_panel_button(self, button_text):
        """Click one of the main navigation buttons in the left sidebar.

        This uses a Tab-navigation approach to find buttons in the left panel.
        Buttons order (top to bottom): Clientes, Presupuestos, Gestiones,
        Protocolo, Pagos, Administración, Salir.
        """
        button_order = {
            "Clientes": 1,
            "Presupuestos": 2,
            "Gestiones": 3,
            "Protocolo": 4,
            "Pagos": 5,
            "Administración": 6,
            "Administracion": 6,
        }
        # First try AppleScript
        try:
            self.click_button_by_name(button_text)
            return
        except Exception:
            pass

        # Fallback: Tab navigation from window focus
        logger.warn(f"AppleScript click failed for '{button_text}', using Tab navigation")
        self.bring_java_app_to_front()
        time.sleep(0.3)
        tabs = button_order.get(button_text, 1)
        for _ in range(tabs):
            pyautogui.press("tab")
            time.sleep(0.15)
        pyautogui.press("space")
        time.sleep(0.5)

    @keyword("Dismiss Dialog If Present")
    def dismiss_dialog_if_present(self):
        """Press Enter to dismiss any open dialog box."""
        pyautogui.press("enter")
        time.sleep(0.5)

    @keyword("Read Log File")
    def read_log_file(self, log_path):
        """Read and return the content of a log file."""
        try:
            with open(log_path, "r", encoding="utf-8", errors="replace") as f:
                return f.read()
        except Exception as e:
            logger.warn(f"Could not read log file {log_path}: {e}")
            return ""

    @keyword("Log Contains Message")
    def log_contains_message(self, log_path, message):
        """Check if a log file contains a specific message. Returns True/False."""
        content = self.read_log_file(log_path)
        found = message in content
        if found:
            logger.info(f"Found in log: '{message}'")
        else:
            logger.warn(f"NOT found in log: '{message}'")
        return found
