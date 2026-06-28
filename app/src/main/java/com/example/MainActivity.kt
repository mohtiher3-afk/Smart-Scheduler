package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.screens.CourseScheduleApp
import com.example.screens.MainViewModel
import com.example.screens.MainViewModelFactory
import com.example.ui.material3_foundation.SmartSchedulerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as Application
            
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(app)
            )

            val themeMode by mainViewModel.themeMode.collectAsState()
            val appLanguage by mainViewModel.appLanguage.collectAsState()
            val layoutDirection = if (appLanguage == "ar") androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
            val dynamicColorEnabled by mainViewModel.dynamicColorEnabled.collectAsState()

            SmartSchedulerTheme(themeMode = themeMode, dynamicColorEnabled = dynamicColorEnabled) {
                // Request notification permission launch
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                androidx.compose.runtime.CompositionLocalProvider(
                    com.example.screens.LocalAppLanguage provides appLanguage,
                    androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection
                ) {
                    CourseScheduleApp(
                        viewModel = mainViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
