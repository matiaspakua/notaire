#!/bin/bash
# Run Robot Framework E2E tests for Notaire Swing frontend.
# Usage: bash run_tests.sh [--test "Test Name"] [--suite suite_name] [--dryrun]
#
# Exit codes:
#   0 = all tests passed
#   1 = test failures
#   2 = environment error

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Check venv exists
if [ ! -d ".venv" ]; then
    echo -e "${RED}✗ Virtual environment not found. Run: bash setup_env.sh${NC}"
    exit 2
fi

# Activate venv
source .venv/bin/activate

# Ensure output dirs exist
mkdir -p results screenshots

# Build robot command
ROBOT_ARGS="--outputdir results --loglevel INFO"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --test|-t)
            ROBOT_ARGS="$ROBOT_ARGS --test \"$2\""
            shift 2
            ;;
        --suite|-s)
            ROBOT_ARGS="$ROBOT_ARGS --suite $2"
            shift 2
            ;;
        --dryrun)
            ROBOT_ARGS="$ROBOT_ARGS --dryrun"
            shift
            ;;
        --include|-i)
            ROBOT_ARGS="$ROBOT_ARGS --include $2"
            shift 2
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 2
            ;;
    esac
done

echo -e "${YELLOW}Running Robot Framework E2E tests...${NC}"
echo -e "  Output: ${SCRIPT_DIR}/results/"
echo -e "  Screenshots: ${SCRIPT_DIR}/screenshots/"
echo ""

# Run tests
eval robot $ROBOT_ARGS tests/
RC=$?

if [ $RC -eq 0 ]; then
    echo -e "\n${GREEN}✓ All tests passed${NC}"
else
    echo -e "\n${RED}✗ Some tests failed (exit code: $RC)${NC}"
fi

echo -e "  Report: ${SCRIPT_DIR}/results/report.html"
echo -e "  Log:    ${SCRIPT_DIR}/results/log.html"

exit $RC
