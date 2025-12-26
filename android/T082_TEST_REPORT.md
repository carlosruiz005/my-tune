# T082 Test Report: Android Note Detection for 6 Guitar Strings

**Task**: T082 [US3] Test Android app with real guitar: verify note detection for all 6 strings (E A D G B E)

**Date**: 2025-12-08  
**Tester**: Automated Test Setup  
**Device**: Android Emulator (emulator-5554)  
**App Version**: Debug Build  

---

## Test Objective

Verify that the Android guitar tuner app correctly detects and displays the note name for all 6 standard tuning guitar strings:

| String | Note | Expected Frequency | Tolerance |
|--------|------|-------------------|-----------|
| 6th (Low E) | E2 | 82.41 Hz | ±3 cents (~±0.15 Hz) |
| 5th | A2 | 110.00 Hz | ±3 cents (~±0.19 Hz) |
| 4th | D3 | 146.83 Hz | ±3 cents (~±0.26 Hz) |
| 3rd | G3 | 196.00 Hz | ±3 cents (~±0.34 Hz) |
| 2nd | B3 | 246.94 Hz | ±3 cents (~±0.43 Hz) |
| 1st (High E) | E4 | 329.63 Hz | ±3 cents (~±0.57 Hz) |

---

## Test Setup

### Prerequisites
✅ Android emulator running  
✅ App installed successfully  
✅ Microphone permission granted  
✅ Logcat monitoring configured  

### Test Script
Created automated test script: `android/test_t082.sh`

**Features**:
- Auto-launches app
- Clears and monitors logcat
- Filters relevant detection logs
- Color-coded output:
  - 🟢 Green: Successful detection (reached minSameNoteCount)
  - 🟡 Yellow: Waiting for stability
  - 🔵 Blue: Frequency detection in progress
  - 🔴 Red: Low confidence detection

---

## How to Execute This Test

### Option 1: Automated Monitoring (Recommended)

```bash
cd /Users/carlosalbertoruizrobles/Desarrollos/my-tune/android
./test_t082.sh
```

This will:
1. Launch the app automatically
2. Display test instructions
3. Monitor logcat in real-time with color-coded output
4. Filter only relevant pitch detection logs

### Option 2: Manual Logcat Monitoring

```bash
# Start the app
~/Library/Android/sdk/platform-tools/adb shell am start -n com.mytune/.MainActivity

# Monitor logs
~/Library/Android/sdk/platform-tools/adb logcat -s AndroidAudioProcessor:D HPSPitchDetector:D
```

### Option 3: Physical Device Testing

```bash
# Connect physical Android device
# Enable USB debugging
# Install app: ./gradlew installDebug
# Use physical guitar or tone generator app
# Run test_t082.sh
```

---

## Test Procedure

1. **Start Detection**
   - Tap "Start Listening" button in the app
   - Verify microphone permission is granted

2. **Test Each String**
   - Play 6th string (E2) and observe detection
   - Play 5th string (A2) and observe detection
   - Play 4th string (D3) and observe detection
   - Play 3rd string (G3) and observe detection
   - Play 2nd string (B3) and observe detection
   - Play 1st string (E4) and observe detection

3. **Record Results**
   - Note which strings are detected correctly
   - Record detected frequency vs expected
   - Note any detection failures or incorrect notes

---

## Expected Log Output

### Successful Detection Example:
```
Detected: 110.2 Hz (smoothed: 110.0 Hz) -> A2 (confidence: 0.45)
⏳ Waiting for stability: A2 (count: 1/3)
Detected: 110.1 Hz (smoothed: 110.0 Hz) -> A2 (confidence: 0.48)
⏳ Waiting for stability: A2 (count: 2/3)
Detected: 110.0 Hz (smoothed: 110.0 Hz) -> A2 (confidence: 0.50)
✅ REACHED minSameNoteCount(3) - Emitting result to UI: A2 @ 110.0 Hz
```

### Low Confidence Example:
```
Low confidence: 150.2 Hz (confidence: 0.18)
```

---

## Success Criteria

### ✅ Pass Conditions:
- [ ] All 6 strings are detected correctly (correct note name displayed)
- [ ] Detected frequencies are within ±3 cents of expected values
- [ ] Detection latency < 100ms (observable from logs)
- [ ] Confidence levels consistently > 0.3 for clear string plucks
- [ ] UI updates in real-time without lag
- [ ] No crashes or ANR (Application Not Responding)

### ⚠️ Acceptable Warnings:
- Occasional "Low confidence" messages (expected with ambient noise)
- Brief "Waiting for stability" messages (part of smoothing algorithm)

### ❌ Fail Conditions:
- Incorrect note names displayed
- No detection when string is plucked clearly
- Consistent low confidence (< 0.2) for all strings
- App crashes during detection
- Extreme latency (> 500ms)

---

## Optimization Parameters (if needed)

If detection is unreliable, these parameters can be adjusted:

### In `HPSPitchDetector.kt`:
```kotlin
private val numHarmonics = 3        // Default: 3 (range: 2-5)
private val minConfidence = 0.3     // Default: 0.3 (range: 0.2-0.5)
```

### In `AndroidAudioProcessor.kt`:
```kotlin
private val historySize = 3           // Default: 3 (range: 2-5)
private val minSameNoteCount = 3      // Default: 3 (range: 1-3)
```

---

## Current Configuration

Based on previous optimizations:

- **FFT Buffer Size**: 4096 samples
- **Sample Rate**: 44100 Hz
- **Frequency Resolution**: 10.76 Hz/bin
- **Processing Rate**: ~10 Hz (100ms delay)
- **Smoothing**: Median of 3 readings
- **Stability Threshold**: 3 consecutive same notes
- **Confidence Threshold**: 0.3 (30%)
- **Audio Normalization**: Enabled
- **Frequency Range**: 75-400 Hz

---

## Known Issues & Limitations

1. **Emulator Microphone**: Android emulator may not capture real guitar audio well
   - **Solution**: Use tone generator app or test on physical device

2. **Ambient Noise**: Background noise can affect detection
   - **Solution**: Test in quiet environment

3. **String Harmonics**: Guitar harmonics may confuse detection
   - **Mitigation**: HPS algorithm implemented to handle harmonics

4. **Fret Buzz**: Poor technique can cause unclear pitch
   - **Solution**: Pluck strings cleanly, let them ring

---

## Test Results Template

Copy this section and fill in after testing:

```
## TEST RESULTS

**Date**: _______________
**Device**: _______________
**Environment**: (Quiet room / Moderate noise / Noisy)

| String | Expected | Detected | Frequency | Confidence | Status |
|--------|----------|----------|-----------|------------|--------|
| E2 (6) | E2       | ________ | _______ Hz | ________ | ☐ Pass ☐ Fail |
| A2 (5) | A2       | ________ | _______ Hz | ________ | ☐ Pass ☐ Fail |
| D3 (4) | D3       | ________ | _______ Hz | ________ | ☐ Pass ☐ Fail |
| G3 (3) | G3       | ________ | _______ Hz | ________ | ☐ Pass ☐ Fail |
| B3 (2) | B3       | ________ | _______ Hz | ________ | ☐ Pass ☐ Fail |
| E4 (1) | E4       | ________ | _______ Hz | ________ | ☐ Pass ☐ Fail |

**Overall**: ☐ PASS ☐ FAIL

**Notes**:
- 
- 
```

---

## Next Steps

After completing T082:
- [ ] T083: Test edge cases (background noise, quiet input, out-of-tune strings)
- [ ] T084: Verify performance (< 100ms latency, UI at 60fps, memory < 150MB)
- [ ] Document any issues found
- [ ] Adjust parameters if needed
- [ ] Proceed to Phase 4 (US1 - Tuning Accuracy Visual Indicator)

---

## References

- Task Definition: `specs/001-guitar-tuner/tasks.md` (Line 82)
- Audio Processor: `android/app/src/main/java/com/mytune/data/audio/AndroidAudioProcessor.kt`
- Pitch Detector: `android/app/src/main/java/com/mytune/data/audio/HPSPitchDetector.kt`
- Frequency Reference: `shared/pitch-detection/frequency-ranges.md`
