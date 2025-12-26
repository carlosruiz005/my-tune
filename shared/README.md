# Shared Resources

This directory contains cross-platform resources shared across Android, iOS, and WebApp implementations of the Guitar Tuner application.

## Directory Structure

### `design-tokens/`
Contains UI design tokens (colors, typography, spacing) exported as JSON files. These tokens ensure visual consistency across all platforms.

**Files**:
- `colors.json` - Color palettes for light and dark themes
- `typography.json` - Font scales and text styles
- `spacing.json` - Layout spacing and padding values

**Usage**: Import these tokens into platform-specific theme files. Use the `tools/scripts/generate-design-tokens.js` script to convert JSON tokens into native format (Kotlin/Swift/TypeScript).

### `tuning-presets/`
Contains guitar tuning preset definitions in JSON format. Each preset defines the target frequencies for all 6 strings.

**Files**:
- `presets.json` - Array of tuning configurations

**Usage**: Load these presets at runtime to populate the tuning selection menu. Each platform's TuningRepository reads from this file.

### `pitch-detection/`
Contains documentation for the pitch detection algorithm implementation. These are reference documents, not executable code.

**Files**:
- `fft-algorithm.md` - Fast Fourier Transform implementation approach
- `pitch-conversion.md` - Frequency (Hz) to musical note conversion formulas
- `frequency-ranges.md` - Reference table of guitar string frequencies

**Usage**: Consult these documents when implementing audio processing modules on each platform. Ensures algorithmic consistency across Android, iOS, and WebApp.

## Maintenance

When updating design tokens or tuning presets:
1. Edit the JSON files in this directory
2. Run `node tools/scripts/generate-design-tokens.js` to regenerate platform-specific files
3. Validate tuning presets with `node tools/scripts/validate-tuning-presets.js`
4. Commit both the source JSON and generated files

## Cross-Platform Consistency

All platforms MUST use these shared resources to maintain consistency:
- Android: Load from `assets/` directory
- iOS: Load from `Resources/` bundle
- WebApp: Import directly from `shared/` via relative paths
