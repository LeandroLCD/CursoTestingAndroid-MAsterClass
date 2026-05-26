package com.aristidevs.cursotestingandroid.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.core.BaseComposeTest
import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SettingsScreenTest : BaseComposeTest() {



    private fun renderContentScreen(
        uiState: SettingsUiState = SettingsUiState(),
        setThemeModeChanged: (ThemeMode) -> Unit = {},
        setInStockOnlyChanged: (Boolean) -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeRule.setContent {
            SettingScreenContent(
                uiState = uiState,
                setThemeModeChanged = setThemeModeChanged,
                setInStockOnlyChanged = setInStockOnlyChanged,
                onBack = onBack
            )
        }
    }

    @Test
    fun should_display_title_appearance_and_filters_texts_when_rendered_in_settingsScreen() {
        //GIVEN
        val uiState = SettingsUiState()

        //WHEN
        renderContentScreen(uiState = uiState)

        //THEN
        composeRule.onNodeWithText(stringResources(R.string.setting)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.settings_appearance)).assertIsDisplayed()
        composeRule.onNodeWithText(stringResources(R.string.filter_and_visualization)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.setting_screen_content)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.in_stock_only_switch)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.show_taxes_switch)).assertIsDisplayed()
    }

    @Test
    fun should_display_components_switch_with_default_values_when_rendered_with_default_state_in_settingScreen() {
        //GIVEN
        val uiState = SettingsUiState(inStockOnly = false, showTaxes = true)

        //WHEN
        renderContentScreen(uiState = uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.in_stock_only_switch)).assertIsOff()
        composeRule.onNodeWithTag(idResources(R.id.show_taxes_switch)).assertIsOn()
    }

    @Test
    fun should_display_components_switch_with_new_values_when_rendered_with_newt_state_in_settingScreen() {
        //GIVEN
        val uiState = SettingsUiState(inStockOnly = true, showTaxes = false)

        //WHEN
        renderContentScreen(uiState = uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.in_stock_only_switch)).assertIsOn()
        composeRule.onNodeWithTag(idResources(R.id.show_taxes_switch)).assertIsOff()
    }
    @Test
    fun should_selected_theme_light__when_light_theme_selected_in_settingScreen() {
        //GIVEN
        val themeMode = ThemeMode.LIGHT
        val uiState = SettingsUiState(themeMode = themeMode)

        //WHEN
        renderContentScreen(uiState = uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.setting_theme, themeMode.id)).assertIsSelected()
    }
    @Test
    fun should_selected_theme_dark__when_dark_theme_selected_in_settingScreen() {
        //GIVEN
        val themeMode = ThemeMode.DARK
        val uiState = SettingsUiState(themeMode = themeMode)

        //WHEN
        renderContentScreen(uiState = uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.setting_theme, themeMode.id)).assertIsSelected()
    }
    @Test
    fun should_selected_theme_system__when_system_theme_selected_in_settingScreen() {
        //GIVEN
        val themeMode = ThemeMode.SYSTEM
        val uiState = SettingsUiState(themeMode = themeMode)

        //WHEN
        renderContentScreen(uiState = uiState)

        //THEN
        composeRule.onNodeWithTag(idResources(R.id.setting_theme, themeMode.id)).assertIsSelected()
    }

    @Test
    fun should_call_back_when_navigator_to_back_is_clicked_in_settingScreen() {
        //GIVEN
        var onBackCalled = false

        //WHEN
        renderContentScreen(onBack = { onBackCalled = true })
        composeRule.onNodeWithTag(idResources(R.id.top_bar_back_navigation_icon)).performClick()

        //THEN
        assertTrue(onBackCalled)
    }

    @Test
    fun should_call_setInStockOnlyChanged_when_in_stock_only_switch_is_clicked_in_settingScreen() {
        //GIVEN
        var inStockOnlyChanged = false
        val uiState = SettingsUiState(inStockOnly = false)

        //WHEN
        renderContentScreen(uiState = uiState, setInStockOnlyChanged = { inStockOnlyChanged = it })
        composeRule.onNodeWithTag(idResources(R.id.in_stock_only_switch)).performClick()

        //THEN
        assertTrue(inStockOnlyChanged)
    }
    @Test
    fun should_call_setThemeModeChanged_when_theme_dark_is_clicked_in_settingScreen(){
        //GIVEN
        var themeModeExpected:ThemeMode? = null
        val uiState = SettingsUiState(themeMode = ThemeMode.LIGHT)

        //WHEN
        renderContentScreen(uiState = uiState, setThemeModeChanged = {themeModeExpected = it})
        composeRule.onNodeWithTag(idResources(R.id.setting_theme, ThemeMode.DARK.id)).performClick()

        //THEN
        assertTrue(themeModeExpected == ThemeMode.DARK)

    }

}