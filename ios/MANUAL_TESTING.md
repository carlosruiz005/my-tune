# iOS Manual Testing Guide - User Story 3

**Status**: Code implementation complete - Awaiting manual validation
**Tasks**: T094, T095, T096
**Build Verified**: ✅ BUILD SUCCEEDED (December 2024)

## Prerequisites

- Real guitar or tone generator
- iPhone or iPad running iOS 14.0+
- Xcode 15.0+ with iOS Simulator
- Physical device recommended for audio testing (simulator has limited audio capabilities)

## Installation

1. Open `ios/MyTune.xcodeproj` in Xcode
2. Select target device (physical iPhone recommended)
3. Build and run: `⌘+R` or Product → Run
4. Grant microphone permission when prompted

## Test T094: Note Detection for All 6 Strings

**Objective**: Verify the app correctly identifies each guitar string's note name in real-time

### Test Steps

1. Launch MyTune app
2. Tap "Start" button
3. Grant microphone permission if prompted
4. Pluck each string in Standard Tuning (E A D G B E) one at a time
5. Observe the note display for each string

### Expected Results

| String # | Standard Tuning Note | Expected Display | Pass/Fail |
|----------|---------------------|------------------|-----------|
| 6 (lowest) | E2 | E2 (~82 Hz) | [ ] |
| 5 | A2 | A2 (~110 Hz) | [ ] |
| 4 | D3 | D3 (~147 Hz) | [ ] |
| 3 | G3 | G3 (~196 Hz) | [ ] |
| 2 | B3 | B3 (~247 Hz) | [ ] |
| 1 (highest) | E4 | E4 (~330 Hz) | [ ] |

### Acceptance Criteria

- ✅ Note name updates within 100ms of string pluck
- ✅ Correct note displayed for each of 6 strings
- ✅ Frequency value matches expected range (±10 Hz)
- ✅ No crashes or freezes during detection
- ✅ UI remains responsive during audio processing

### Notes

- Test with guitar properly tuned to Standard Tuning
- Pluck strings clearly and cleanly (avoid harmonics)
- Allow 1-2 seconds between plucks for stable readings
- If using tone generator, use sine waves at exact frequencies

---

## Test T095: Edge Cases

**Objective**: Verify the app handles difficult audio conditions gracefully

### Test Case 1: Background Noise

**Steps**:
1. Start the tuner
2. Play background music or white noise at moderate volume (~60 dB)
3. Pluck guitar strings

**Expected**:
- App should still detect notes, though accuracy may decrease slightly
- No crashes or error states
- UI should indicate when confidence is low (if implemented)

**Result**: [ ] Pass / [ ] Fail  
**Notes**: _______________________

### Test Case 2: Quiet Input

**Steps**:
1. Start the tuner
2. Pluck strings very softly (barely audible)
3. Observe detection behavior

**Expected**:
- App should either detect the note or display "Detecting..." state
- No false detections of random notes
- Noise gate threshold prevents spurious detections

**Result**: [ ] Pass / [ ] Fail  
**Notes**: _______________________

### Test Case 3: Very Out-of-Tune Strings

**Steps**:
1. Detune guitar strings significantly (>50 cents off)
2. Pluck each string
3. Verify note identification

**Expected**:
- App correctly identifies the note name (e.g., "E" even if 50 cents flat)
- Frequency display reflects actual pitch
- No crashes or unexpected behavior
- Cents deviation exceeds ±50 (validates tuning state logic)

**Result**: [ ] Pass / [ ] Fail  
**Notes**: _______________________

### Test Case 4: Multiple Strings Simultaneously

**Steps**:
1. Strum multiple strings at once
2. Observe app behavior

**Expected**:
- App should detect the dominant frequency
- UI should not flicker rapidly between notes
- No crashes or error states
- Consider this a "stress test" - perfect accuracy not required

**Result**: [ ] Pass / [ ] Fail  
**Notes**: _______________________

---

## Test T096: Performance Verification

**Objective**: Ensure the app meets performance requirements

### Metric 1: Latency

**Target**: < 100ms from string pluck to display update

**Test Method**:
1. Record video at 120fps of simultaneous string pluck and screen
2. Count frames between pluck and note display update
3. Calculate latency: (frame_count / 120) * 1000 = ms

**Measured Latency**: _______ ms  
**Result**: [ ] Pass (< 100ms) / [ ] Fail

**Alternative Method** (without video):
- Subjective feel test: Does note update feel instantaneous?
- Use tone generator with known transition timing

### Metric 2: UI Frame Rate

**Target**: 60fps maintained during pitch updates

**Test Method**:
1. Enable FPS counter in Xcode: Debug → View Debugging → Show FPS
2. Run app and start tuner
3. Pluck strings continuously for 30 seconds
4. Observe FPS counter

**Observed FPS**: _______ fps (average)  
**Result**: [ ] Pass (≥ 60fps) / [ ] Fail

### Metric 3: Memory Usage

**Target**: < 100MB memory footprint

**Test Method**:
1. Open Xcode Memory Debugger: Debug → Memory Report
2. Start tuner and use for 5 minutes
3. Check peak memory usage

**Peak Memory**: _______ MB  
**Result**: [ ] Pass (< 100MB) / [ ] Fail

### Metric 4: Battery Impact

**Target**: Reasonable battery consumption for continuous use

**Test Method** (on physical device):
1. Fully charge device
2. Run tuner for 30 minutes continuously
3. Check battery percentage drop

**Battery Drop**: _______ % over 30 minutes  
**Result**: [ ] Pass (< 10% drop) / [ ] Fail

---

## Known Issues / Limitations

Document any issues discovered during testing:

1. **Issue**: _______________________  
   **Severity**: Critical / High / Medium / Low  
   **Reproducible**: Yes / No  
   **Steps**: _______________________

2. **Issue**: _______________________  
   **Severity**: Critical / High / Medium / Low  
   **Reproducible**: Yes / No  
   **Steps**: _______________________

---

## Test Environment

**Date**: _______________________  
**Tester**: _______________________  
**Device**: _______________________ (e.g., iPhone 13, iOS 16.5)  
**Build**: _______________________ (e.g., Debug, Release)  
**Xcode Version**: _______________________  
**Guitar Used**: _______________________ (e.g., Fender Stratocaster, Epiphone Les Paul)

---

## Overall Assessment

- [ ] All tests passed - Ready for Phase 4 (User Story 1)
- [ ] Most tests passed - Minor issues to fix
- [ ] Multiple failures - Requires debugging
- [ ] Critical issues found - Implementation review needed

**Summary Notes**:

_______________________________________________________________________________________

_______________________________________________________________________________________

_______________________________________________________________________________________

---

## Next Steps

Once manual testing is complete and all tests pass:

1. ✅ Update tasks.md: Mark T094, T095, T096 as complete
2. 🎯 Begin Phase 4: iOS User Story 1 - Tuning Accuracy Visualization
   - T122-T134: Implement TuningBarView, CentDisplay, StringIndicators
   - Add marker animation and in-tune confirmation
   - Implement haptic feedback
3. 📝 Document any bugs found and create GitHub issues
4. 🔄 Repeat manual testing after Phase 4 completion

---

**Document Version**: 1.0  
**Last Updated**: December 2024  
**Status**: Awaiting execution
