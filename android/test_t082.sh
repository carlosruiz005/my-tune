#!/bin/bash

# T082 Test Script: Verify note detection for all 6 guitar strings
# Test Android app with real guitar: verify note detection for all 6 strings (E A D G B E)

echo "════════════════════════════════════════════════════════════════"
echo "  T082: Android Note Detection Test for 6 Guitar Strings"
echo "════════════════════════════════════════════════════════════════"
echo ""

ADB=~/Library/Android/sdk/platform-tools/adb
PACKAGE="com.mytune"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Expected frequencies for standard tuning (with tolerance)
declare -A EXPECTED_NOTES=(
    ["E2"]="82.41"
    ["A2"]="110.00"
    ["D3"]="146.83"
    ["G3"]="196.00"
    ["B3"]="246.94"
    ["E4"]="329.63"
)

# String names
STRINGS=("6th (E2)" "5th (A2)" "4th (D3)" "3rd (G3)" "2nd (B3)" "1st (E4)")

echo -e "${BLUE}Test Prerequisites:${NC}"
echo "✓ Device connected: $(${ADB} devices | grep device | grep -v "List" | awk '{print $1}')"
echo "✓ App installed: ${PACKAGE}"
echo ""

echo -e "${YELLOW}Starting application...${NC}"
${ADB} shell am start -n ${PACKAGE}/.MainActivity
sleep 3

echo ""
echo -e "${BLUE}Clearing logcat and starting detection monitoring...${NC}"
${ADB} logcat -c

echo ""
echo "════════════════════════════════════════════════════════════════"
echo -e "${YELLOW}INSTRUCTIONS FOR MANUAL TESTING:${NC}"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "1. Tap 'Start Listening' button in the app"
echo "2. Play each guitar string and verify detection:"
echo ""

for i in "${!STRINGS[@]}"; do
    string="${STRINGS[$i]}"
    note_key="${string#* }"
    note_key="${note_key%?}"  # Remove parentheses
    expected="${EXPECTED_NOTES[$note_key]}"
    echo "   ${string}: Expected ~${expected} Hz → Note ${note_key}"
done

echo ""
echo "3. Watch the logs below for detection results"
echo "4. Press Ctrl+C when done testing"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo -e "${GREEN}Monitoring Audio Detection (filtering relevant logs)...${NC}"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Monitor logcat for pitch detection
${ADB} logcat -s AndroidAudioProcessor:D HPSPitchDetector:D | while read -r line; do
    if [[ $line == *"✅ REACHED minSameNoteCount"* ]]; then
        echo -e "${GREEN}$line${NC}"
    elif [[ $line == *"⏳ Waiting for stability"* ]]; then
        echo -e "${YELLOW}$line${NC}"
    elif [[ $line == *"Detected:"* ]]; then
        echo -e "${BLUE}$line${NC}"
    elif [[ $line == *"Low confidence"* ]]; then
        echo -e "${RED}$line${NC}"
    elif [[ $line == *"Pitch detected"* ]]; then
        echo "$line"
    else
        echo "$line"
    fi
done
