package com.example.myapplication.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.data.PreloadManager
import com.example.myapplication.core.data.PreloadState
import com.example.myapplication.presentation.components.LoadingView
import com.example.myapplication.presentation.main.MainScreen
import com.example.myapplication.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preloadManager: PreloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }

    @Composable
    private fun MainContent() {
        val preloadState by preloadManager.preloadState.collectAsState()
        var showMainScreen by remember { mutableStateOf(false) }

        LaunchedEffect(preloadState) {
            if (preloadState is PreloadState.Completed) {
                showMainScreen = true
            }
        }

        if (showMainScreen) {
            MainScreen(
                viewModel = hiltViewModel()
            )
        } else {
            LoadingView(
                state = preloadState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
} 