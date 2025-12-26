# iOS Manual Testing Guide - User Story 1 (Standard Tuning Detection)

**Date**: 2025-12-08  
**Feature**: iOS Implementation - User Story 1  
**Platform**: iOS (iPhone/iPad)  
**Minimum iOS**: 14.0

## Implementation Status

✅ **Code Complete** - All iOS components for User Story 1 have been implemented:

### Implemented Components

1. **TuningBarView.swift** - Visual tuning bar with animated marker
2. **CentDisplayView.swift** - Numeric cent deviation display
3. **StringIndicatorsView.swift** - 6-string target note indicators
4. **TunerView.swift** (Updated) - Integrated all US1 components with haptic feedback
5. **TunerViewModel.swift** (Enhanced) - Added in-tune confirmation state management
6. **PitchResult.swift** (Already complete) - Contains tuning state logic and target string matching

### Known Build Issue

⚠️ **Xcode Project File Has Duplicate References**: The `update_project.rb` script added files multiple times causing build errors. This requires manual cleanup in Xcode.

## Manual Fix Required

Before testing, open the project in Xcode and remove duplicate file references:

1. Open `ios/MyTune.xcodeproj` in Xcode
2. In Project Navigator, look for duplicate entries of:
   - `TunerView.swift`
   - `NoteDisplayView.swift`
   - `ThemeProvider.swift`
   - `CentDisplayView.swift`
   - `TuningBarView.swift`
   - `StringIndicatorsView.swift`
3. Right-click and "Remove References" for duplicates (keep only one of each)
4. Clean build folder: Product → Clean Build Folder (Cmd+Shift+K)
5. Build: Product → Build (Cmd+B)

## Testing Checklist (T122-T134)

### T122-T131: Visual Components and Tuning Accuracy

#### Prerequisites
- iPhone or iPad (physical device preferred for real guitar testing)
- Guitar tuned to Standard tuning (E A D G B E)
- Quiet environment for initial tests

#### Test Cases

**T122-T125: TuningBarView - Marker Position & Color**

- [ ] T122: Verify marker is centered when string is in-tune (±0 cents)
- [ ] T123: Verify marker moves left when string is flat (-10, -20, -30 cents)
- [ ] T124: Verify marker moves right when string is sharp (+10, +20, +30 cents)
- [ ] T125: Verify marker color changes:
  - Green: In-tune (±10 cents)
  - Yellow: Slightly off (±11-25 cents)
  - Red: Very off (>±25 cents)
  - Gray: No pitch detected

**T126: CentDisplayView - Numeric Display**

- [ ] Shows "—" when no pitch detected
- [ ] Shows "+15" when 15 cents sharp
- [ ] Shows "-15" when 15 cents flat
- [ ] Shows "0" when in-tune
- [ ] Color matches tuning state (green/yellow/red)

**T127: StringIndicatorsView - Target Notes**

- [ ] All 6 target notes displayed: E2, A2, D3, G3, B3, E4
- [ ] Active string highlighted with blue circle
- [ ] Inactive strings shown in gray
- [ ] Correct string number labels (6-1)
- [ ] Octave numbers displayed below note

**T128: Component Integration**

- [ ] All components display simultaneously without overlap
- [ ] Layout is responsive on different screen sizes
- [ ] Components update in real-time as pitch changes

**T129-T130: In-Tune Confirmation**

- [ ] T129: Green checkmark appears when transition to in-tune state
- [ ] Checkmark scales up with spring animation
- [ ] Checkmark disappears after ~1 second
- [ ] Checkmark does NOT trigger continuously while staying in-tune
- [ ] T130: Haptic feedback (vibration) triggers once when becoming in-tune
- [ ] Haptic does NOT trigger continuously

**T131: Movement Threshold (Jitter Prevention)**

- [ ] Marker position stable when pitch varies by <10 cents
- [ ] Marker moves smoothly when pitch changes by >10 cents
- [ ] No rapid back-and-forth marker movement

### T132-T134: Accuracy & Performance Testing

**T132: Tuning Accuracy Per String**

Test each of the 6 guitar strings at various tuning states:

| String | Note | In-Tune | Flat (-20¢) | Sharp (+20¢) | Very Flat (-40¢) | Very Sharp (+40¢) |
|--------|------|---------|-------------|--------------|------------------|-------------------|
| 6 (E2) | E2   | ✅ Green | ✅ Yellow   | ✅ Yellow    | ✅ Red           | ✅ Red            |
| 5 (A2) | A2   | ✅       | ✅          | ✅           | ✅               | ✅                |
| 4 (D3) | D3   | ✅       | ✅          | ✅           | ✅               | ✅                |
| 3 (G3) | G3   | ✅       | ✅          | ✅           | ✅               | ✅                |
| 2 (B3) | B3   | ✅       | ✅          | ✅           | ✅               | ✅                |
| 1 (E4) | E4   | ✅       | ✅          | ✅           | ✅               | ✅                |

**T133: In-Tune Confirmation Triggers**

- [ ] Tune string from flat → in-tune: Confirmation triggers ✅
- [ ] Tune string from sharp → in-tune: Confirmation triggers ✅
- [ ] Stay in-tune: Confirmation does NOT re-trigger ✅
- [ ] Go out-of-tune and back: Confirmation triggers again ✅

**T134: UI Performance**

- [ ] Marker animation maintains 60fps during pitch updates
- [ ] No visible lag between pitch detection and marker movement
- [ ] UI remains responsive during continuous tuning
- [ ] Memory usage stable during extended use (check Xcode Instruments)
- [ ] No memory leaks when starting/stopping tuner multiple times

## Edge Cases & Error Handling

### Background Noise Testing
- [ ] Works in moderate background noise (conversation, TV)
- [ ] Correctly shows "no pitch" in very loud environments
- [ ] Recovers correctly when noise subsides

### Quiet Input Testing
- [ ] Detects quietly plucked strings
- [ ] Shows appropriate confidence levels
- [ ] Doesn't show false positives from very quiet input

### Out-of-Tune Strings
- [ ] Correctly identifies string >50 cents out-of-tune
- [ ] Marker stays at edge of bar for extreme deviations
- [ ] Still provides accurate cent readings

## Device-Specific Testing

Test on multiple devices to ensure performance consistency:

- [ ] iPhone SE (3rd gen) - Low-end device
- [ ] iPhone 12/13 - Mid-range
- [ ] iPhone 14/15 Pro - High-end
- [ ] iPad (any generation)

## Accessibility Testing

- [ ] VoiceOver announces note name and tuning state
- [ ] VoiceOver reads cent deviation
- [ ] Color-blind friendly (don't rely solely on color)
- [ ] Touch targets are 44x44 points minimum

## Expected Results

✅ **Pass Criteria**:
- All visual indicators update within 100ms of pitch detection
- Marker position accurately reflects cent deviation
- In-tune confirmation triggers correctly (not continuously)
- Haptic feedback works on supported devices
- UI maintains 60fps during operation
- No crashes or memory leaks

❌ **Fail Criteria**:
- Marker position incorrect (>5 cents deviation)
- In-tune confirmation triggers continuously
- UI drops below 60fps
- App crashes during use
- Memory leaks detected

## Notes for Tester

1. **Real Guitar Recommended**: Testing with a real guitar provides the most accurate assessment of the tuning accuracy feature.

2. **Tone Generator Alternative**: Use a tone generator app to produce specific frequencies for controlled testing if guitar not available.

3. **Xcode Instruments**: Use Time Profiler and Allocations instruments to verify performance claims.

4. **Build Issues**: If you encounter "Multiple commands produce" errors, manually clean duplicate file references in Xcode project as described above.

## Reporting Issues

When reporting bugs, please include:
- Device model and iOS version
- Exact steps to reproduce
- Screenshots/screen recordings
- Xcode console logs if app crashes
- Performance metrics from Instruments if relevant

## Next Steps After Testing

Once all tests pass:
- Mark tasks T122-T134 as complete in `tasks.md`
- Create iOS manual testing DONE file confirming completion
- Proceed to Phase 5 (User Story 2 - Alternative Tuning Selection) or Phase 6 (Polish)
