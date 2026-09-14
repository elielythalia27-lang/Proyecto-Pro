package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.DescargasScreen
import com.example.ui.screens.AjustesScreen
import com.example.ui.theme.AppThemeColor
import com.example.data.model.SortOption
import com.example.data.model.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GreetingScreenshotTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testDescargasRenders() {
    composeTestRule.setContent {
      MyApplicationTheme {
        DescargasScreen(
          downloads = emptyList(),
          onPlayOffline = {},
          onPauseDownload = {},
          onResumeDownload = {},
          onCancelDownload = {},
          onExploreClick = {}
        )
      }
    }
  }

  @Test
  fun testAjustesRenders() {
    composeTestRule.setContent {
      MyApplicationTheme {
        AjustesScreen(
          isDarkTheme = true,
          onDarkThemeChange = {},
          themeMode = ThemeMode.DARK,
          onThemeModeChange = {},
          themeColor = AppThemeColor.BLUE,
          onThemeColorChange = {},
          defaultFilterType = "TODOS",
          onDefaultFilterTypeChange = {},
          sortOption = SortOption.NAME_AZ,
          onSortOptionChange = {},
          onClearCache = {},
          maxConcurrentDownloads = 3,
          onMaxConcurrentDownloadsChange = {},
          catalogLayoutMode = "grid_2",
          onCatalogLayoutModeChange = {}
        )
      }
    }
  }
}
