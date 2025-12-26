package com.mytune.ui.tuner

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytune.data.model.PitchResult
import com.mytune.ui.theme.MyTuneTheme
import com.mytune.ui.theme.TunerTypography

/**
 * Large note display component showing detected note name and octave.
 * 
 * Displays:
 * - Note name (e.g., "E", "A", "C#") in large font
 * - Octave number (e.g., "4") in smaller font
 * - Frequency in Hz below
 * - Fades out when no pitch detected
 */
@Composable
fun NoteDisplay(
    pitchResult: PitchResult,
    modifier: Modifier = Modifier
) {
    val hasPitch = pitchResult.isPitchDetected() && pitchResult.isConfident(0.3)
    
    // Animate alpha based on pitch detection
    val alpha by animateFloatAsState(
        targetValue = if (hasPitch) 1f else 0.3f,
        animationSpec = tween(durationMillis = 300),
        label = "noteAlpha"
    )
    
    // Color based on tuning state
    val noteColor = when {
        !hasPitch -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        pitchResult.isInTune() -> MyTuneTheme.tuningColors.inTune
        else -> MyTuneTheme.noteColors.detected
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Note name and octave
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Note name (e.g., "E", "C#")
            AnimatedContent(
                targetState = if (hasPitch) pitchResult.detectedNote else "--",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "noteNameAnimation"
            ) { noteName ->
                Text(
                    text = noteName,
                    style = TunerTypography.noteDisplay,
                    color = noteColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Octave number (e.g., "4")
            if (hasPitch) {
                AnimatedContent(
                    targetState = pitchResult.detectedOctave,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "octaveAnimation"
                ) { octave ->
                    Text(
                        text = octave.toString(),
                        style = TunerTypography.octaveDisplay,
                        color = noteColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Frequency display
        if (hasPitch) {
            AnimatedContent(
                targetState = pitchResult.detectedFrequency,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(200))
                },
                label = "frequencyAnimation"
            ) { frequency ->
                Text(
                    text = "${String.format("%.2f", frequency)} Hz",
                    style = TunerTypography.frequencyDisplay,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            Text(
                text = "Play a string",
                style = TunerTypography.frequencyDisplay,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        
        // Target string indicator (if matched)
        if (hasPitch && pitchResult.hasTargetString()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            val targetString = pitchResult.targetString!!
            Text(
                text = "String ${targetString.number}: ${targetString.fullNoteName()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Preview for NoteDisplay with detected pitch.
 */
@Composable
private fun NoteDisplayPreview() {
    MyTuneTheme {
        Surface {
            NoteDisplay(
                pitchResult = PitchResult(
                    detectedFrequency = 329.63,
                    detectedNote = "E",
                    detectedOctave = 4,
                    confidence = 0.85,
                    tuningState = com.mytune.data.model.TuningState.IN_TUNE,
                    centsDeviation = 2.0,
                    targetString = null
                ),
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}

/**
 * Preview for NoteDisplay with no pitch detected.
 */
@Composable
private fun NoteDisplayNoPitchPreview() {
    MyTuneTheme {
        Surface {
            NoteDisplay(
                pitchResult = PitchResult.noPitchDetected(),
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}
