package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.DownloadStatus
import com.example.data.model.ThemeMode
import com.example.ui.components.AppBottomNav
import com.example.ui.screens.AjustesScreen
import com.example.ui.screens.DescargasScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val homeViewModel: HomeViewModel = viewModel()
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

            val isDark = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            MyApplicationTheme(
                darkTheme = isDark,
                themeColor = uiState.themeColor
            ) {
                MainAppNavigation(
                    viewModel = homeViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun MainAppNavigation(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDark = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val activePlayback = uiState.activePlayback

    if (activePlayback != null) {
        PlayerScreen(
            videoUrl = activePlayback.videoUrl,
            title = activePlayback.title,
            coverUrl = activePlayback.coverUrl,
            year = activePlayback.year,
            type = activePlayback.type,
            initialPositionMs = activePlayback.initialPositionMs,
            onBack = { viewModel.closePlayer() },
            onSavePosition = { pos, dur ->
                viewModel.savePlaybackPosition(
                    videoUrl = activePlayback.videoUrl,
                    title = activePlayback.title,
                    coverUrl = activePlayback.coverUrl,
                    year = activePlayback.year,
                    type = activePlayback.type,
                    positionMs = pos,
                    durationMs = dur
                )
            }
        )
    } else {
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = modifier
        ) {
            composable("splash") {
                SplashScreen(
                    onTimeout = {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("main") {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { 3 }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        when (pageIndex) {
                            // Section 0: Catálogo
                            0 -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onSearchChange = { viewModel.onSearchQueryChange(it) },
                                    onTypeSelect = { viewModel.onTypeSelected(it) },
                                    onClearFilters = { viewModel.clearFilters() },
                                    onPlayPelicula = { pelicula, pos ->
                                        viewModel.playPelicula(pelicula, pos)
                                    },
                                    onDownloadPelicula = { pelicula ->
                                        viewModel.startDownload(pelicula)
                                    },
                                    onRefresh = { viewModel.loadPeliculas(forceRefresh = true) },
                                    onOpenSettings = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(2)
                                        }
                                    },
                                    onDismissContinueWatching = { viewModel.clearContinueWatching() },
                                    onLayoutModeChange = { viewModel.setCatalogLayoutMode(it) }
                                )
                            }

                            // Section 1: Descargas (Activas con Pausa/Reanudar/Cancelar y Notificaciones, y Offline)
                            1 -> {
                                DescargasScreen(
                                    downloads = uiState.downloads,
                                    maxConcurrentDownloads = uiState.maxConcurrentDownloads,
                                    onPlayOffline = { download ->
                                        viewModel.playDownload(download)
                                    },
                                    onPauseDownload = { download ->
                                        viewModel.pauseDownload(download)
                                    },
                                    onResumeDownload = { download ->
                                        viewModel.resumeDownload(download)
                                    },
                                    onCancelDownload = { download ->
                                        viewModel.cancelDownload(download)
                                    },
                                    onForceStartPending = { download ->
                                        viewModel.forceStartPendingDownload(download)
                                    },
                                    onExploreClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(0)
                                        }
                                    },
                                    isDarkTheme = isDark
                                )
                            }

                            // Section 2: Ajustes
                            2 -> {
                                AjustesScreen(
                                    isDarkTheme = isDark,
                                    onDarkThemeChange = { viewModel.setDarkTheme(it) },
                                    themeMode = uiState.themeMode,
                                    onThemeModeChange = { viewModel.setThemeMode(it) },
                                    themeColor = uiState.themeColor,
                                    onThemeColorChange = { viewModel.setThemeColor(it) },
                                    defaultFilterType = uiState.selectedType,
                                    onDefaultFilterTypeChange = { viewModel.setDefaultFilterType(it) },
                                    sortOption = uiState.sortOption,
                                    onSortOptionChange = { viewModel.setSortOption(it) },
                                    onClearCache = { viewModel.clearCache() },
                                    maxConcurrentDownloads = uiState.maxConcurrentDownloads,
                                    onMaxConcurrentDownloadsChange = { viewModel.setMaxConcurrentDownloads(it) },
                                    catalogLayoutMode = uiState.catalogLayoutMode,
                                    onCatalogLayoutModeChange = { viewModel.setCatalogLayoutMode(it) }
                                )
                            }
                        }
                    }

                    // Floating Modern Navigation Pill Bar over the content
                    val downCount = uiState.downloads.count { it.status == DownloadStatus.DOWNLOADING }
                    AppBottomNav(
                        currentPage = pagerState.currentPage,
                        onNavigate = { targetPage ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetPage)
                            }
                        },
                        downloadsCount = downCount,
                        isDarkTheme = isDark,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
