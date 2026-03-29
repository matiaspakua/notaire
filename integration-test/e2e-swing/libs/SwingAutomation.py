"""
SwingAutomation - Robot Framework library for Java Swing GUI automation.
Uses PyAutoGUI for cross-platform desktop interaction on macOS.
Designed to be lightweight and agent-friendly.
"""

import pyautogui
import time
import os
from robot.api import logger
from robot.api.deco import keyword


# PyAutoGUI safety settings
pyautogui.FAILSAFE = True
pyautogui.PAUSE = 0.1


class SwingAutomation:
    """Robot Framework library wrapping PyAutoGUI for Swing desktop automation."""

    ROBOT_LIBRARY_SCOPE = "GLOBAL"

    def __init__(self):
        logger.info("SwingAutomation library initialized (PyAutoGUI backend)")

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

    @keyword("Type Text Slowly")
    def type_text_slowly(self, text, interval=0.05):
        """Type text character by character with a small interval."""
        pyautogui.typewrite(text, interval=float(interval))
        logger.info(f"Typed: {text}")

    @keyword("Clear Text Field")
    def clear_text_field(self):
        """Press Backspace multiple times to clear the field."""
        pyautogui.press("backspace", presses=50, interval=0.01)
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
