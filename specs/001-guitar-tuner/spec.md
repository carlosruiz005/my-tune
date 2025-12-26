# Feature Specification: Guitar Tuner Application

**Feature Branch**: `001-guitar-tuner`  
**Created**: December 8, 2025  
**Status**: Draft  
**Input**: User description: "Vamos a construir una app para que ayude con la afinación de una guitarra. La aplicación debe poder realizar las siguientes acciones: El usuario puede afinar una guitarra de manera fácil identificando la nota requerida por cada cuerda. El usuario, a través de un menú, puede seleccionar la afinación de una lista predeterminada de afinaciones para guitarras de 6 cuerdas. Al momento de afinar una cuerda, el usuario puede visualizar si la cuerda ya está afinada a través de una barra y un marcador que indica en qué parte se cuentra la afinación de la cuerda, siendo que, si el marcador está a la mitad de la barra, indicaría que la cuerda está afinada en la nota requerida, mientras que, si está por debajo, la cuerda requiere tensión y viceversa."

## Clarifications

### Session 2025-12-08

- Q: ¿La app requiere conexión a internet o funciona completamente offline? → A: La app funciona completamente offline (sin conectividad requerida)
- Q: ¿Qué retroalimentación visual recibe el usuario cuando el marcador está centrado (cuerda afinada)? → A: Color verde + animación de confirmación (ej: pulso, checkmark)
- Q: ¿Cómo funciona la selección de cuerdas: automático (detecta cualquier nota) o manual (usuario selecciona qué cuerda afinar)? → A: Modo automático: la app detecta cualquier nota y muestra su afinación
- Q: ¿Cuál es el rango mínimo de desafinación (en cents) para que el marcador se mueva visiblemente en la barra? → A: ±10 cents for visible marker movement
- Q: ¿Qué hardware requiere la app además del micrófono (vibración háptica, sensores, etc.)? → A: Micrófono + vibración háptica (opcional para feedback adicional)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Standard Tuning Detection (Priority: P1)

A musician picks up their guitar and needs to tune it using standard tuning (EADGBE). They open the app, and it automatically detects the sound of each string they play in any order, showing them whether the string is in tune, too low, or too high through a visual indicator. No manual string selection is required.

**Why this priority**: This is the core functionality of the app - the ability to detect and indicate string pitch. Without this, the app has no value. Standard tuning is the most commonly used tuning, making this the essential MVP feature.

**Independent Test**: Can be fully tested by playing each of the 6 guitar strings and verifying the app correctly identifies whether each note is sharp, flat, or in tune. Delivers immediate value as a functional tuner.

**Acceptance Scenarios**:

1. **Given** the user opens the app, **When** they pluck the low E string (6th string) at approximately the correct pitch, **Then** the visual indicator shows the marker centered on the tuning bar
2. **Given** the user is tuning their guitar, **When** they pluck a string that is flat (below target pitch), **Then** the visual indicator marker appears below the center of the tuning bar
3. **Given** the user is tuning their guitar, **When** they pluck a string that is sharp (above target pitch), **Then** the visual indicator marker appears above the center of the tuning bar
4. **Given** the user plucks a string, **When** the string is perfectly in tune, **Then** the app provides visual confirmation through a green-colored marker and a confirmation animation (such as a pulse or checkmark), and optionally haptic feedback if the device supports it
5. **Given** the user is on any screen, **When** they pluck a guitar string, **Then** the app detects the pitch and displays the corresponding note being played
6. **Given** the user plucks the A string (5th string), **When** the pitch is slightly flat, **Then** the marker position accurately reflects how far below the target pitch the string is

---

### User Story 2 - Alternative Tuning Selection (Priority: P2)

A guitarist wants to play a song in Drop D tuning. They open the app's tuning menu, select "Drop D" from a list of preset tunings, and proceed to tune their guitar according to the new target notes displayed for each string.

**Why this priority**: Expands the app's usefulness beyond standard tuning, making it valuable for intermediate and advanced players. Still delivers value even if implemented after P1, as users can use the app with standard tuning first.

**Independent Test**: Can be tested independently by selecting different tuning presets from the menu and verifying the app adjusts target notes accordingly. Each preset can be validated by tuning a guitar and playing it to confirm correct pitch targets.

**Acceptance Scenarios**:

1. **Given** the user is on the main tuning screen, **When** they access the tuning menu, **Then** they see a list of available guitar tunings for 6-string guitars
2. **Given** the user views the tuning menu, **When** they look at the available options, **Then** they see preset tunings including Standard (EADGBE), Drop D (DADGBE), and other common tunings
3. **Given** the user selects a tuning from the menu, **When** the selection is confirmed, **Then** the app updates to show the target notes for each string according to the selected tuning
4. **Given** the user has selected Drop D tuning, **When** they return to the tuning screen, **Then** the 6th string shows D as the target note instead of E
5. **Given** the user switches between different tunings, **When** they pluck strings, **Then** the visual indicator reflects whether strings match the currently selected tuning's target notes
6. **Given** a tuning is selected, **When** the user closes and reopens the app, **Then** the previously selected tuning remains active

---

### User Story 3 - String-by-String Identification (Priority: P1)

A beginner guitarist is learning to tune their instrument and needs help identifying which string they're currently plucking. When they play any string, the app displays which note is being played, helping them understand which string they're working with.

**Why this priority**: Essential for user experience and especially critical for beginners who may not know which string corresponds to which note. This should be part of the core MVP as it guides users through the tuning process.

**Independent Test**: Can be tested by plucking each of the 6 strings in random order and verifying the app correctly identifies and displays the note being played, regardless of whether it's in tune.

**Acceptance Scenarios**:

1. **Given** the user plucks any guitar string, **When** the app detects the pitch, **Then** it displays the closest note name (e.g., E, A, D, G, B, E) being played
2. **Given** the user plucks a string that is significantly out of tune, **When** the app identifies the pitch, **Then** it still shows which note is closest to what's being played
3. **Given** the user is a beginner unfamiliar with string positions, **When** they pluck each string, **Then** they can identify which string they're tuning based on the displayed note
4. **Given** the user plucks strings in rapid succession, **When** each new string is played, **Then** the display updates to show the current string's note

---

### Edge Cases

- What happens when the user plays multiple strings simultaneously or creates overtones?
- How does the system handle background noise or ambient sounds that aren't guitar strings?
- What happens when the user is in a noisy environment?
- How does the feature behave when the guitar string is extremely out of tune (more than a whole step off)?
- What happens when the user's device doesn't have microphone access or denies permission?
- How does the app behave on devices that don't support haptic feedback?
- How does the app respond when played notes are too quiet to detect accurately?
- What happens when the battery is low and device performance is limited?
- How does the tuning indicator behave when a string is between two semitones?
- How does the marker behave when pitch deviations are below the ±10 cent threshold?
- How does the app perform on older devices with slower processors or lower-quality microphones?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST detect audio input from the device microphone and identify the fundamental frequency of guitar strings being played
- **FR-002**: System MUST display a visual tuning indicator consisting of a horizontal bar with a movable marker
- **FR-016**: System MUST function completely offline without requiring internet connectivity
- **FR-017**: System MUST operate in automatic detection mode, detecting and displaying tuning information for any string the user plays without requiring manual string selection
- **FR-018**: System MUST display visible marker movement on the tuning bar for pitch deviations of ±10 cents or greater from the target note
- **FR-003**: System MUST position the marker at the center of the tuning bar when the detected pitch matches the target note exactly
- **FR-004**: System MUST position the marker below center when the detected pitch is flat (lower than the target note)
- **FR-005**: System MUST position the marker above center when the detected pitch is sharp (higher than the target note)
- **FR-006**: System MUST display the note name being detected in real-time as the user plays strings
- **FR-007**: System MUST provide a menu interface for selecting different guitar tunings
- **FR-008**: System MUST include preset tunings for 6-string guitars, including at minimum: Standard (EADGBE), Drop D (DADGBE), Half Step Down (Eb Ab Db Gb Bb Eb), Full Step Down (DGCFAD), and Drop C (CGCFAD)
- **FR-009**: System MUST update the target notes for all strings when a new tuning is selected from the menu
- **FR-010**: System MUST persist the user's selected tuning preference between app sessions
- **FR-011**: System MUST indicate visually when a string reaches the exact target pitch by displaying the marker in green color with a confirmation animation (such as a pulse effect or checkmark icon)
- **FR-012**: System MUST display all six target notes for the currently selected tuning
- **FR-013**: System MUST respond to pitch changes in real-time with minimal latency (marker position updates smoothly as string is adjusted)
- **FR-014**: System MUST handle microphone permission requests and inform users if access is denied
- **FR-015**: System MUST differentiate between the six guitar strings based on their frequency ranges
- **FR-019**: System MAY provide haptic feedback (vibration) when a string reaches the exact target pitch, if the device supports haptic capabilities

### Key Entities *(include if feature involves data)*

- **Tuning Preset**: Represents a specific guitar tuning configuration. Contains a name (e.g., "Standard", "Drop D") and the target notes for each of the six strings (from lowest to highest pitch). Each note includes the note name and target frequency.
- **String**: Represents one of the six guitar strings. Contains the string position (1-6), the target note for the current tuning, and the currently detected pitch.
- **Pitch Detection Result**: Represents the output of audio analysis. Contains the detected frequency in Hz, the closest note name, and the cent deviation from the target pitch (for visual indicator positioning). The marker displays visible movement for deviations of ±10 cents or greater.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can successfully tune all six strings of a guitar in standard tuning within 3 minutes
- **SC-002**: The visual indicator updates in real-time with latency no greater than 100 milliseconds from when a string is plucked
- **SC-003**: The pitch detection accuracy is within ±3 cents of the true pitch for notes within the guitar's standard range (82 Hz to 330 Hz)
- **SC-004**: Users can identify and select alternative tunings from the menu in under 30 seconds
- **SC-005**: 90% of users can successfully tune at least one string to the correct pitch on their first attempt without external help
- **SC-006**: The marker position visually represents pitch deviation accurately enough that users can achieve tuning within ±5 cents of the target note
- **SC-007**: The app successfully detects and responds to guitar string plucks in environments with moderate ambient noise (up to 60 dB background noise)
