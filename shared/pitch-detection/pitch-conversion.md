# Pitch Conversion: Hz to Musical Note

## Overview

This document describes the mathematical formulas and implementation approach for converting frequency (Hz) to musical note names and calculating tuning accuracy in cents.

## Scientific Pitch Notation

Musical notes follow **equal temperament tuning** where each semitone is a constant frequency ratio.

### Standard Reference

**A4 = 440 Hz** (international standard pitch)

### Semitone Ratio

**Formula**: Each semitone up multiplies frequency by 2^(1/12) ≈ 1.059463

## Frequency to MIDI Note Number

### Formula

```
MIDI_note = 69 + 12 × log₂(frequency / 440)
```

**Where**:
- 69 = MIDI note number for A4
- frequency = detected frequency in Hz
- log₂(x) = natural log(x) / natural log(2)

### Implementation (Platform Agnostic)

```
function frequencyToMidiNote(frequency: number): number {
    return 69 + 12 * Math.log2(frequency / 440)
}
```

### Example Calculations

| Frequency (Hz) | Calculation | MIDI Note | Result |
|----------------|-------------|-----------|--------|
| 440.00 | 69 + 12×log₂(440/440) | 69 | A4 |
| 329.63 | 69 + 12×log₂(329.63/440) | 64 | E4 |
| 82.41 | 69 + 12×log₂(82.41/440) | 40 | E2 |

## MIDI Note to Note Name

### Note Name Mapping

**MIDI Note % 12** maps to:

| Remainder | Note Name |
|-----------|-----------|
| 0 | C |
| 1 | C# / Db |
| 2 | D |
| 3 | D# / Eb |
| 4 | E |
| 5 | F |
| 6 | F# / Gb |
| 7 | G |
| 8 | G# / Ab |
| 9 | A |
| 10 | A# / Bb |
| 11 | B |

### Octave Calculation

**Octave = floor(MIDI_note / 12) - 1**

### Implementation

```
function midiNoteToName(midiNote: number): { note: string, octave: number } {
    const noteNames = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']
    const noteName = noteNames[Math.round(midiNote) % 12]
    const octave = Math.floor(Math.round(midiNote) / 12) - 1
    return { note: noteName, octave }
}
```

### Example Results

| MIDI Note | Note Name | Octave | Display |
|-----------|-----------|--------|---------|
| 69 | A | 4 | A4 |
| 64 | E | 4 | E4 |
| 40 | E | 2 | E2 |

## Frequency to Note Name (Combined)

```
function frequencyToNoteName(frequency: number): string {
    const midiNote = frequencyToMidiNote(frequency)
    const { note, octave } = midiNoteToName(midiNote)
    return `${note}${octave}`
}
```

## Cent Deviation Calculation

**Cent**: 1/100th of a semitone (logarithmic unit)

### Formula

```
cents = 1200 × log₂(detectedFreq / targetFreq)
```

**Where**:
- detectedFreq = frequency detected by pitch detector
- targetFreq = expected frequency for the target note
- 1200 = cents per octave (100 cents × 12 semitones)

### Interpretation

| Cents | Meaning |
|-------|---------|
| 0 | Perfect tuning |
| +10 | 10 cents sharp (slightly high) |
| -10 | 10 cents flat (slightly low) |
| +50 | Half a semitone sharp |
| -100 | One full semitone flat |

### Implementation

```
function calculateCents(detectedFreq: number, targetFreq: number): number {
    return 1200 * Math.log2(detectedFreq / targetFreq)
}
```

### Example Calculations

| Detected | Target | Cents | Tuning State |
|----------|--------|-------|--------------|
| 440.0 Hz | 440.0 Hz | 0 | Perfect |
| 443.0 Hz | 440.0 Hz | +11.8 | Slightly sharp |
| 437.0 Hz | 440.0 Hz | -11.8 | Slightly flat |
| 329.63 Hz | 329.63 Hz | 0 | Perfect |
| 335.0 Hz | 329.63 Hz | +27.8 | Sharp |

## Tuning State Classification

Based on cent deviation, classify tuning state for UI feedback:

```
function getTuningState(cents: number): TuningState {
    const absCents = Math.abs(cents)
    
    if (absCents <= 3) return 'IN_TUNE'       // Green
    if (absCents <= 10) return 'NEARLY_IN_TUNE' // Yellow
    if (cents > 10) return 'SHARP'            // Orange/Red
    return 'FLAT'                              // Orange/Red
}
```

### Thresholds

- **±0-3 cents**: In-tune (green marker, haptic feedback)
- **±3-10 cents**: Nearly in-tune (yellow marker)
- **±10+ cents**: Out of tune (red/orange marker)

## String Identification Logic

When a frequency is detected, identify which guitar string it matches:

### Algorithm

1. Convert detected frequency to MIDI note
2. Round to nearest integer MIDI note
3. Compare with target tuning's string frequencies
4. Find closest match within ±50 cents (prevents misidentification)

### Implementation

```
function identifyString(
    detectedFreq: number, 
    targetTuning: Tuning
): { stringNumber: number, cents: number } | null {
    
    const detectedMidi = frequencyToMidiNote(detectedFreq)
    
    for (const string of targetTuning.strings) {
        const targetMidi = frequencyToMidiNote(string.frequency)
        const cents = 1200 * Math.log2(detectedFreq / string.frequency)
        
        // Match if within ±50 cents (half a semitone)
        if (Math.abs(cents) <= 50) {
            return { stringNumber: string.number, cents }
        }
    }
    
    return null // No matching string found
}
```

## Edge Cases

### Very Low/High Frequencies

- **Below 60 Hz**: Likely noise, ignore
- **Above 1000 Hz**: Harmonics, not fundamental (for guitar)

### Ambiguous Notes

If frequency is exactly between two semitones (±50 cents from both):
- Display nearest note name
- Show large cent deviation to indicate out-of-tune state

### Enharmonic Equivalents

- Use sharp notation by default (C#, F#, G#, etc.)
- Alternative: Allow user preference for flat notation (Db, Gb, Ab)
- For this MVP: **Use sharp notation only**

## Performance Optimization

### Precompute Target Frequencies

```
// At app startup, precompute MIDI notes for all tuning presets
const targetMidiNotes = tuning.strings.map(s => 
    frequencyToMidiNote(s.frequency)
)
```

### Lookup Table for Note Names

```
const NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']
```

Pre-allocate this array to avoid repeated allocations.

### Integer Arithmetic Where Possible

For display purposes, round cent deviations to nearest integer:

```
const displayCents = Math.round(cents)
```

## Testing Strategy

### Unit Tests

1. **Reference frequencies**: Test A4 (440 Hz), E2 (82.41 Hz), E4 (329.63 Hz)
2. **Cent calculations**: Verify ±10, ±20, ±50 cents
3. **Edge cases**: 0 Hz, negative frequency (should reject), very high frequency

### Integration Tests

1. **Real guitar data**: Record tuned strings, verify correct note names
2. **Out-of-tune**: Record slightly flat/sharp strings, verify cent accuracy

## Mathematical Precision

- Use **64-bit floating point** (double) for all calculations
- Acceptable error margin: ±0.1 cents (imperceptible to humans)
- Avoid integer truncation until final display step

## References

- MIDI Tuning Standard: https://www.midi.org/specifications
- Equal Temperament: https://en.wikipedia.org/wiki/Equal_temperament
- Cent (music): https://en.wikipedia.org/wiki/Cent_(music)

## Implementation Checklist

- [ ] Implement frequencyToMidiNote with Math.log2
- [ ] Implement midiNoteToName with lookup table
- [ ] Implement calculateCents formula
- [ ] Implement identifyString matching logic
- [ ] Add input validation (frequency > 0)
- [ ] Write unit tests for all conversion functions
- [ ] Test with real guitar recordings
- [ ] Verify cent accuracy within ±0.5 cents of expected
