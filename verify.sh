#!/bin/bash

# NetworkResponse Adapter - Verification Script
# This script verifies that the library is working correctly

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                                                              ║"
echo "║   🧪 NetworkResponse Adapter - Verification Script          ║"
echo "║                                                              ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

# Function to run test
run_test() {
    local test_name=$1
    local command=$2
    
    echo -n "Testing: $test_name... "
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PASSED${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}❌ FAILED${NC}"
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Running verification tests..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test 1: Clean build
run_test "Clean build" "./gradlew clean"

# Test 2: Library assembly
run_test "Library assembly" "./gradlew :networkresponse:assembleRelease"

# Test 3: Unit tests
run_test "Unit tests" "./gradlew :app:testDebugUnitTest"

# Test 4: Publish to Maven Local
run_test "Publish to Maven Local" "./gradlew :networkresponse:publishToMavenLocal"

# Test 5: Check if AAR exists
run_test "AAR file exists" "test -f networkresponse/build/outputs/aar/networkresponse-release.aar"

# Test 6: Check Maven Local publication
run_test "Maven Local publication" "test -d ~/.m2/repository/com/github/navgurukul/network-response-adapter/1.0.0/"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test Results"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                                                              ║${NC}"
    echo -e "${GREEN}║   ✅ All tests passed! Your library is working correctly!   ║${NC}"
    echo -e "${GREEN}║                                                              ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Go to https://jitpack.io"
    echo "2. Enter: navgurukul/NetworkResponseAdapter"
    echo "3. Click 'Get it' next to v1.0.0"
    echo ""
    exit 0
else
    echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║                                                              ║${NC}"
    echo -e "${RED}║   ❌ Some tests failed. Please check the errors above.      ║${NC}"
    echo -e "${RED}║                                                              ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo "For help, check TESTING_GUIDE.md"
    echo ""
    exit 1
fi
