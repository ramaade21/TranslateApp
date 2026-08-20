package com.linguatranslate.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.linguatranslate.app.presentation.navigation.LinguaTranslateNavHost
import com.linguatranslate.app.presentation.theme.LinguaTranslateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestMicPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* handled reactively by mic button state */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            LinguaTranslateTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LinguaTranslateNavHost()
                }
            }
        }
    }
}
