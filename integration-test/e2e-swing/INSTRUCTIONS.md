# Notaire Swing E2E Tests — Robot Framework

Automated GUI testing for the Notaire Java Swing frontend using [Robot Framework](https://robotframework.org/) and [PyAutoGUI](https://pyautogui.readthedocs.io/) for macOS desktop automation.

## Prerequisites

| Requirement | Version | Check |
|---|---|---|
| Python | 3.9+ | `python3 --version` |
| Java | 21 | `java --version` |
| Docker + Compose | latest | `docker compose version` |
| macOS | 13+ (Ventura) | Required for Quartz/accessibility APIs |

## Quick Start

```bash
# 1. Setup environment (one-time)
bash setup_env.sh

# 2. Start backend stack (DB + API)
bash ../../scripts/start.sh --no-admin

# 3. Run all E2E tests
bash run_tests.sh
```

## Environment Setup

### 1. Create Python Virtual Environment

```bash
cd integration-test/e2e-swing
bash setup_env.sh
```

This creates `.venv/`, installs Robot Framework 7.2 and PyAutoGUI 0.9.54.

### 2. macOS Accessibility & Screen Recording Permissions (Required)

PyAutoGUI uses macOS accessibility and screen recording APIs. You must grant permissions:

1. Open **System Preferences → Privacy & Security → Accessibility**
2. Add your terminal app (Terminal, iTerm2, VS Code, etc.)
3. Toggle permission **ON**
4. Also grant **Screen Recording** permission for screenshot capability

> ⚠️ Without this, Robot Framework cannot click, type, or take screenshots.

### 3. Build Frontend JAR (if not built)

```bash
cd <project-root>
mvn clean install -pl frontend-swing -am -DskipTests
```

JAR location: `frontend-swing/target/frontend-swing-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Running Tests

### Run All Tests
```bash
bash run_tests.sh
```

### Run Specific Test
```bash
bash run_tests.sh --test "Login With Valid Credentials Should Succeed"
```

### Run by Tag
```bash
bash run_tests.sh --include agent-callable
```

### Dry Run (Syntax Validation)
```bash
bash run_tests.sh --dryrun
```

### Run from AI Agent

```bash
cd integration-test/e2e-swing && bash run_tests.sh --include agent-callable
```

**Exit codes:** `0` = pass, `1` = failure, `2` = environment error.
**Structured output:** XML results at `results/output.xml` for programmatic parsing.

## Output Locations

| Output | Path |
|---|---|
| Screenshots | `screenshots/` (timestamped PNGs) |
| Robot report | `results/report.html` |
| Robot log | `results/log.html` |
| XML output | `results/output.xml` |

## Test Credentials

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `admin` |

## Architecture

```
e2e-swing/
├── INSTRUCTIONS.md          # This file
├── requirements.txt         # Python dependencies
├── setup_env.sh             # Venv setup (idempotent)
├── run_tests.sh             # Test runner (agent-friendly)
├── libs/
│   └── SwingAutomation.py   # Custom RF library (PyAutoGUI wrapper)
├── resources/
│   └── common.resource      # Reusable Robot Framework keywords
├── tests/
│   └── login_e2e.robot      # Login E2E test
├── screenshots/             # Auto-generated screenshots (gitignored)
└── results/                 # Robot Framework output (gitignored)
```

### Custom Library: SwingAutomation.py

Thin wrapper around PyAutoGUI exposing Robot Framework keywords:
- `Capture Desktop Screenshot` — full-screen PNG capture
- `Type Text Slowly` — character-by-character typing
- `Select All Text` — Cmd+A
- `Press Tab Key` / `Press Enter Key` — navigation
- `Click At Coordinates` — coordinate-based click
- `Press Key Combination` — arbitrary hotkeys

## Troubleshooting

| Problem | Solution |
|---|---|
| `Backend API is not running` | Run `bash scripts/start.sh` from project root |
| `Frontend JAR not found` | Run `mvn clean install -pl frontend-swing -am` |
| No GUI interaction | Grant Accessibility permissions (see above) |
| Screenshots are blank | Grant Screen Recording permission |
| `ModuleNotFoundError` | Activate venv: `source .venv/bin/activate` |
| PyAutoGUI: `FailSafeException` | Move mouse to corner to trigger failsafe; restart test |
