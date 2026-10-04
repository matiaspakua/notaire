#!/bin/bash
# RETIRED (#811 / CU76 / ADR-012): Swing Robot E2E is hard-deprecated.
# Do not wire this script into CI. See testing/e2e-swing/README.md.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

RED='\033[0;31m'
NC='\033[0m'

echo -e "${RED}testing/e2e-swing is RETIRED (issue #811).${NC}"
echo "Do not set up a Robot venv for Swing. Use Playwright:"
echo "  cd testing/e2e && npm test"
exit 2

# --- historical body kept below for reference; unreachable ---

# Check Python 3
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}✗ python3 not found. Install Python 3.9+ first.${NC}"
    exit 1
fi

PYTHON_VERSION=$(python3 -c 'import sys; print(f"{sys.version_info.major}.{sys.version_info.minor}")')
echo -e "${YELLOW}Using Python ${PYTHON_VERSION}${NC}"

# Create venv if missing
if [ ! -d ".venv" ]; then
    echo -e "${YELLOW}Creating virtual environment...${NC}"
    python3 -m venv .venv
    echo -e "${GREEN}✓ Virtual environment created${NC}"
else
    echo -e "${GREEN}✓ Virtual environment already exists${NC}"
fi

# Activate and install
source .venv/bin/activate

echo -e "${YELLOW}Installing dependencies...${NC}"
pip install --upgrade pip -q
pip install -r requirements.txt -q

echo -e "${GREEN}✓ Dependencies installed${NC}"

# Create output directories
mkdir -p screenshots results

# Verify installation
echo -e "${YELLOW}Verifying installation...${NC}"
robot --version || true
echo -e "${GREEN}✓ Environment ready${NC}"
echo ""
echo -e "Activate with: ${YELLOW}source .venv/bin/activate${NC}"
