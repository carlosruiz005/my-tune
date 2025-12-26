# Guitar String Frequency Ranges

## Standard Tuning Reference Table

**Standard tuning (E A D G B E)** - from lowest to highest pitch:

| String | Note | Octave | Fundamental Frequency (Hz) | MIDI Note | Harmonics (Hz) |
|--------|------|--------|----------------------------|-----------|----------------|
| 6th (lowest) | E | 2 | 82.41 | 40 | 164.82, 247.23, 329.64 |
| 5th | A | 2 | 110.00 | 45 | 220.00, 330.00, 440.00 |
| 4th | D | 3 | 146.83 | 50 | 293.66, 440.49, 587.32 |
| 3rd | G | 3 | 196.00 | 55 | 392.00, 588.00, 784.00 |
| 2nd | B | 3 | 246.94 | 59 | 493.88, 740.82, 987.76 |
| 1st (highest) | E | 4 | 329.63 | 64 | 659.26, 988.89, 1318.52 |

## Frequency Detection Range

### Fundamental Range for Guitar

**Minimum**: 80 Hz (below low E)  
**Maximum**: 350 Hz (above high E)

**Rationale**:
- Covers all 6 strings in standard tuning with buffer
- Excludes most harmonics (which start at 2× fundamental)
- Reduces false detections from room noise and overtones

### Extended Range for Alternative Tunings

**Minimum**: 60 Hz (for Drop C, 7-string, bass guitar)  
**Maximum**: 1000 Hz (to capture harmonics for HPS algorithm)

## Harmonic Series Explanation

When a guitar string vibrates, it produces:
1. **Fundamental frequency** (loudest, what we perceive as the note)
2. **Harmonics** (integer multiples: 2f, 3f, 4f, etc.)

### Example: Low E String (82.41 Hz)

| Harmonic | Frequency | Musical Interval | Relative Amplitude |
|----------|-----------|------------------|--------------------|
| 1st (fundamental) | 82.41 Hz | E2 | 100% |
| 2nd | 164.82 Hz | E3 (octave) | 50-70% |
| 3rd | 247.23 Hz | B3 (perfect 5th) | 30-50% |
| 4th | 329.64 Hz | E4 (2 octaves) | 20-40% |
| 5th | 411.55 Hz | G#4 (major 3rd) | 10-30% |

**Challenge**: Harmonics can be louder than fundamental (especially on acoustic guitars)

**Solution**: Use Harmonic Product Spectrum (HPS) algorithm to identify true fundamental

## String Spacing in Frequency

### Adjacent String Ratios

| String Pair | Interval | Frequency Ratio | Cents |
|-------------|----------|-----------------|-------|
| E2 → A2 | Perfect 4th | 1.335 | 500 |
| A2 → D3 | Perfect 4th | 1.335 | 500 |
| D3 → G3 | Perfect 4th | 1.335 | 500 |
| G3 → B3 | Major 3rd | 1.260 | 400 |
| B3 → E4 | Perfect 4th | 1.335 | 500 |

**Note**: G to B is a major 3rd (400 cents), all others are perfect 4ths (500 cents)

### Minimum String Separation

**Smallest gap**: G3 (196 Hz) to B3 (246.94 Hz) = **50.94 Hz**

**Implication**: Pitch detector must resolve frequencies within 50 Hz to distinguish adjacent strings

**FFT Resolution Requirement**:
- With 4096 samples at 44.1 kHz: 10.77 Hz/bin ✅ (sufficient)
- With 2048 samples: 21.53 Hz/bin ⚠️ (marginal, may confuse G/B strings)

## Alternative Tuning Ranges

### Drop D

| String | Note | Frequency (Hz) |
|--------|------|----------------|
| 6th | D | 73.42 |
| 5th | A | 110.00 |
| 4th | D | 146.83 |
| 3rd | G | 196.00 |
| 2nd | B | 246.94 |
| 1st | E | 329.63 |

**Extended range needed**: Down to 73 Hz

### Half Step Down (Eb Standard)

| String | Note | Frequency (Hz) |
|--------|------|----------------|
| 6th | Eb | 77.78 |
| 5th | Ab | 103.83 |
| 4th | Db | 138.59 |
| 3rd | Gb | 185.00 |
| 2nd | Bb | 233.08 |
| 1st | Eb | 311.13 |

**Range**: 77-311 Hz (within standard range)

### Full Step Down (D Standard)

| String | Note | Frequency (Hz) |
|--------|------|----------------|
| 6th | D | 73.42 |
| 5th | G | 98.00 |
| 4th | C | 130.81 |
| 3rd | F | 174.61 |
| 2nd | A | 220.00 |
| 1st | D | 293.66 |

**Extended range needed**: Down to 73 Hz

### Drop C

| String | Note | Frequency (Hz) |
|--------|------|----------------|
| 6th | C | 65.41 |
| 5th | G | 98.00 |
| 4th | C | 130.81 |
| 3rd | F | 174.61 |
| 2nd | A | 220.00 |
| 1st | D | 293.66 |

**Extended range needed**: Down to 65 Hz (lowest in MVP presets)

## Detection Range Recommendation

### For MVP (5 Tuning Presets)

**Minimum frequency**: 60 Hz (covers Drop C at 65.41 Hz)  
**Maximum frequency**: 1000 Hz (captures harmonics for HPS)

### Filtering Strategy

1. **High-pass filter**: Reject < 60 Hz (eliminates sub-bass noise)
2. **Low-pass filter**: Reject > 1000 Hz (eliminates high-frequency noise)
3. **Focus range**: 60-350 Hz for fundamental detection
4. **Extended range**: 350-1000 Hz used only for HPS harmonic analysis

## Frequency Resolution Requirements

### Minimum Resolution

To distinguish E2 (82.41 Hz) from F2 (87.31 Hz):
- Gap = 4.9 Hz
- Required resolution: < 2.5 Hz/bin

**Achieved with**:
- 4096 samples @ 44.1 kHz: 10.77 Hz/bin (acceptable, use HPS for refinement)
- 8192 samples @ 44.1 kHz: 5.38 Hz/bin (ideal for direct peak detection)

### Cent Accuracy

To detect ±3 cents deviation:
- E2 @ 82.41 Hz: ±0.14 Hz
- E4 @ 329.63 Hz: ±0.57 Hz

**Solution**: Use parabolic interpolation on FFT bins to achieve sub-bin accuracy

## Noise Frequency Ranges to Ignore

### Common Interference

| Source | Frequency Range | Mitigation |
|--------|-----------------|------------|
| Mains hum (US) | 60 Hz | High-pass filter > 65 Hz |
| Mains hum (EU) | 50 Hz | High-pass filter > 65 Hz |
| HVAC rumble | 40-70 Hz | Noise gate threshold |
| Finger noise | 1000-5000 Hz | Low-pass filter < 1000 Hz |
| Room resonance | Variable | Use confidence threshold |

### Confidence Thresholding

Reject detections where:
- Peak magnitude < 2× average magnitude (likely noise)
- Multiple peaks of similar magnitude (ambiguous pitch)
- Frequency outside 60-1000 Hz range

## Practical Detection Examples

### Scenario 1: Low E String Slightly Flat

- **Expected**: 82.41 Hz
- **Detected**: 81.50 Hz
- **Cents**: -19.2 cents (flat)
- **Display**: "E2" with marker 19% below center

### Scenario 2: A String In-Tune

- **Expected**: 110.00 Hz
- **Detected**: 109.95 Hz
- **Cents**: -0.8 cents
- **Display**: "A2" with marker centered, green color

### Scenario 3: High E String Very Sharp

- **Expected**: 329.63 Hz
- **Detected**: 335.00 Hz
- **Cents**: +27.8 cents (very sharp)
- **Display**: "E4" with marker 28% above center, red color

## Implementation Considerations

### Sampling Rate

**Recommended**: 44.1 kHz (CD quality)

**Why not higher?**:
- 48 kHz: Minimal benefit for guitar (fundamental < 350 Hz)
- 96 kHz: Overkill, increases CPU/memory usage
- 22.05 kHz: Nyquist at 11 kHz still covers harmonics, but less headroom

### Buffer Overlap

Use **50% overlap** (hop size = 2048 samples for 4096 buffer):
- Doubles update rate (faster visual response)
- Improves accuracy by averaging adjacent windows
- Minimal CPU overhead with optimized FFT

## Testing Data

### Synthetic Test Signals

Generate pure sine waves at these frequencies for unit tests:

| Frequency | Purpose |
|-----------|---------|
| 82.41 Hz | Low E string (standard tuning) |
| 110.00 Hz | A string (also 2nd harmonic of low E) |
| 440.00 Hz | A4 reference pitch |
| 65.41 Hz | Low C (Drop C tuning, minimum for MVP) |
| 329.63 Hz | High E string (maximum fundamental) |

### Real Guitar Recordings

Record 5-second samples of:
1. Each string played open (Standard tuning)
2. Each string slightly flat (-10 cents)
3. Each string slightly sharp (+10 cents)
4. Each string very out-of-tune (±30 cents)

Store as `.wav` files (44.1 kHz, 16-bit, mono) for integration testing.

## References

- Physics of guitar strings: https://en.wikipedia.org/wiki/String_vibration
- Standard tuning frequencies: https://pages.mtu.edu/~suits/notefreqs.html
- Harmonic series: https://en.wikipedia.org/wiki/Harmonic_series_(music)

## Implementation Checklist

- [ ] Configure high-pass filter at 60 Hz
- [ ] Configure low-pass filter at 1000 Hz
- [ ] Implement frequency range validation (reject < 60 Hz, > 1000 Hz)
- [ ] Create lookup table of target frequencies for all 5 tuning presets
- [ ] Implement confidence threshold to reject weak detections
- [ ] Test with synthetic sine waves at string frequencies
- [ ] Test with real guitar recordings
- [ ] Verify correct string identification for all 6 strings
- [ ] Handle edge case: harmonic louder than fundamental (use HPS)
