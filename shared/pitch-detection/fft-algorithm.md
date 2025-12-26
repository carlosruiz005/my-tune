# FFT Algorithm Implementation

## Overview

This document describes the Fast Fourier Transform (FFT) implementation approach for the Guitar Tuner application. The FFT is used to convert time-domain audio signals into frequency-domain data, enabling pitch detection.

## Algorithm Choice: Cooley-Tukey FFT

We use the **Cooley-Tukey radix-2 decimation-in-time (DIT) algorithm**, which is the most commonly used FFT algorithm.

### Why Cooley-Tukey?

- **Efficiency**: O(N log N) complexity vs O(N²) for DFT
- **Well-documented**: Extensive literature and reference implementations
- **Hardware support**: Optimized libraries available on all platforms (Android, iOS, WebApp)

## Implementation Requirements

### Buffer Size

**Recommended**: 4096 samples (power of 2)

**Rationale**:
- Guitar low E string (82.41 Hz) requires ~0.5s of data for accurate detection
- At 44.1 kHz sample rate: 4096 samples = ~93ms window
- Provides frequency resolution of 44100/4096 ≈ 10.77 Hz per bin
- Good balance between latency and accuracy

### Window Function: Hann Window

**Formula**: w[n] = 0.5 × (1 - cos(2πn / (N-1)))

**Why Hann?**:
- Reduces spectral leakage from non-periodic signals
- Good frequency resolution with moderate side-lobe suppression
- Industry standard for pitch detection applications

**Implementation**:
```
For each sample n in buffer[0...N-1]:
    windowed[n] = buffer[n] × hann_window[n]
```

### Platform-Specific Libraries

#### Android
- **Library**: `android.media.audiofx.Visualizer` (limited) or custom implementation
- **Alternative**: Use KissFFT or implement Cooley-Tukey in Kotlin
- **Performance**: Optimize with inline functions and arrays

#### iOS
- **Library**: `Accelerate.framework` → `vDSP`
- **Function**: `vDSP_fft_zrip` (in-place real FFT)
- **Performance**: Hardware-accelerated on Apple Silicon

#### WebApp
- **Library**: Custom implementation in TypeScript
- **Alternative**: WebAssembly module for performance
- **API**: Use `AudioWorklet` for off-main-thread processing

## Algorithm Steps

### 1. Pre-processing
```
1. Capture audio samples (4096 samples at 44.1 kHz)
2. Apply Hann window function
3. Convert to complex numbers (real part = windowed sample, imaginary = 0)
```

### 2. FFT Execution
```
1. Bit-reverse input array indices
2. Perform butterfly operations in log₂(N) stages
3. Output: Complex frequency bins [0...N/2]
```

### 3. Post-processing
```
1. Calculate magnitude: mag[k] = sqrt(real[k]² + imag[k]²)
2. Focus on bins corresponding to guitar range (80-1000 Hz)
3. Find peak magnitude bin for fundamental frequency estimation
```

## Frequency Bin Mapping

**Formula**: frequency[k] = k × (sample_rate / buffer_size)

**Example** (44.1 kHz, 4096 samples):
- Bin 0: 0 Hz (DC component, ignore)
- Bin 1: 10.77 Hz
- Bin 8: 86.13 Hz (near low E string at 82.41 Hz)
- Bin 41: 441.38 Hz (near A4 at 440 Hz)

## Optimization Considerations

### Memory Management
- **Reuse buffers**: Pre-allocate input/output arrays, avoid GC pressure
- **In-place FFT**: Use algorithms that minimize memory copies

### Performance Targets
- **Android**: < 10ms FFT computation on Snapdragon 6-series
- **iOS**: < 5ms FFT computation on A12+ chip
- **WebApp**: < 15ms FFT computation in AudioWorklet

### Multithreading
- Run FFT on background thread (Android/iOS) or AudioWorklet (WebApp)
- Never block UI thread with FFT computation

## Testing Strategy

### Unit Tests
1. **Sine wave test**: Input pure 440 Hz tone, verify peak at bin 41
2. **Zero input**: Verify no crashes, output all zeros
3. **DC component**: Input constant value, verify peak at bin 0

### Integration Tests
1. **Guitar recording**: Record actual guitar string, verify FFT output matches expected frequency range
2. **Latency test**: Measure time from audio capture to FFT result

## References

- Cooley, J. W., & Tukey, J. W. (1965). "An algorithm for the machine calculation of complex Fourier series"
- Harris, F. J. (1978). "On the use of windows for harmonic analysis with the discrete Fourier transform"
- Apple vDSP documentation: https://developer.apple.com/documentation/accelerate/vdsp

## Implementation Checklist

- [ ] Implement Hann window generation (precompute once)
- [ ] Implement Cooley-Tukey FFT algorithm
- [ ] Add input validation (check buffer size is power of 2)
- [ ] Optimize for target platform (use native libraries where available)
- [ ] Write unit tests with synthetic signals
- [ ] Profile performance on target devices
- [ ] Handle edge cases (silence, overflow, underflow)
