package com.mytune

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mytune.ui.theme.MyTuneTheme
import com.mytune.ui.tuner.TunerScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the MyTune guitar tuner app.
 * 
 * Entry point that sets up the Compose UI with the TunerScreen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTuneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TunerScreen()
                }
            }
        }
    }
}
