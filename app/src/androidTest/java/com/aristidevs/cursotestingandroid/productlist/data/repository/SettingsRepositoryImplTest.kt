package com.aristidevs.cursotestingandroid.productlist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.domain.model.ThemeMode
import com.aristidevs.cursotestingandroid.core.mockwebserver.MockWebServerUrlHolder
import com.aristidevs.cursotestingandroid.core.mockwebserver.rules.MockWebServerRule
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.repository.SettingsRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {

    @get:Rule(order = 0)
    val mockWebServerRule = MockWebServerRule()

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        hiltRule.inject()
    }


    // ─── inStockOnly ─────────────────────────────────────────────────────────────

    @Test
    fun should_emit_false_by_default_in_inStockOnly() = runTest {
        //GIVEN

        //WHEN
        val result = settingsRepository.inStockOnly.first()

        //THEN
        assertFalse(result)
    }

    @Test
    fun should_emit_true_when_set_to_true_in_setInStockOnly() = runTest {
        //GIVEN
        val value = true

        //WHEN
        settingsRepository.setInStockOnly(value)

        //THEN
        assertTrue(settingsRepository.inStockOnly.first())
    }

    @Test
    fun should_emit_false_when_set_to_false_in_setInStockOnly() = runTest {
        //GIVEN
        settingsRepository.setInStockOnly(true)

        //WHEN
        settingsRepository.setInStockOnly(false)

        //THEN
        assertFalse(settingsRepository.inStockOnly.first())
    }

    // ─── filtersVisible ───────────────────────────────────────────────────────────

    @Test
    fun should_emit_true_by_default_in_filtersVisible() = runTest {
        //GIVEN

        //WHEN
        val result = settingsRepository.filtersVisible.first()

        //THEN
        assertTrue(result)
    }

    @Test
    fun should_emit_false_when_set_to_false_in_setFiltersVisible() = runTest {
        //GIVEN
        val value = false

        //WHEN
        settingsRepository.setFiltersVisible(value)

        //THEN
        assertFalse(settingsRepository.filtersVisible.first())
    }

    @Test
    fun should_emit_true_when_set_to_true_in_setFiltersVisible() = runTest {
        //GIVEN
        settingsRepository.setFiltersVisible(false)

        //WHEN
        settingsRepository.setFiltersVisible(true)

        //THEN
        assertTrue(settingsRepository.filtersVisible.first())
    }

    // ─── selectedCategory ────────────────────────────────────────────────────────

    @Test
    fun should_emit_null_by_default_in_selectedCategory() = runTest {
        //GIVEN

        //WHEN
        val result = settingsRepository.selectedCategory.first()

        //THEN
        assertNull(result)
    }

    @Test
    fun should_emit_category_when_set_to_valid_string_in_setSelectedCategory() = runTest {
        //GIVEN
        val category = "fruit"

        //WHEN
        settingsRepository.setSelectedCategory(category)

        //THEN
        assertEquals(category, settingsRepository.selectedCategory.first())
    }

    @Test
    fun should_emit_null_when_set_to_null_after_having_a_value_in_setSelectedCategory() = runTest {
        //GIVEN
        val category = "fruit"
        settingsRepository.setSelectedCategory(category)

        //WHEN
        settingsRepository.setSelectedCategory(null)

        //THEN
        assertNull(settingsRepository.selectedCategory.first())
    }

    // ─── themeMode ────────────────────────────────────────────────────────────────

    @Test
    fun should_emit_SYSTEM_by_default_in_themeMode() = runTest {
        //GIVEN - DataStore limpio (sin valor guardado)

        //WHEN
        val result = settingsRepository.themeMode.first()

        //THEN
        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun should_emit_DARK_when_set_to_DARK_in_setThemeMode() = runTest {
        //GIVEN
        val themeMode = ThemeMode.DARK

        //WHEN
        settingsRepository.setThemeMode(themeMode)

        //THEN
        assertEquals(ThemeMode.DARK, settingsRepository.themeMode.first())
    }

    @Test
    fun should_emit_LIGHT_when_set_to_LIGHT_in_setThemeMode() = runTest {
        //GIVEN
        val themeMode = ThemeMode.LIGHT

        //WHEN
        settingsRepository.setThemeMode(themeMode)

        //THEN
        assertEquals(ThemeMode.LIGHT, settingsRepository.themeMode.first())
    }

    @Test
    fun should_emit_SYSTEM_when_set_to_SYSTEM_in_setThemeMode() = runTest {
        //GIVEN
        settingsRepository.setThemeMode(ThemeMode.DARK)

        //WHEN
        settingsRepository.setThemeMode(ThemeMode.SYSTEM)

        //THEN
        assertEquals(ThemeMode.SYSTEM, settingsRepository.themeMode.first())
    }

    // ─── sortOption ───────────────────────────────────────────────────────────────

    @Test
    fun should_emit_NONE_by_default_in_sortOption() = runTest {
        //GIVEN

        //WHEN
        val result = settingsRepository.sortOption.first()

        //THEN
        assertEquals(SortOption.NONE, result)
    }

    @Test
    fun should_emit_PRICE_ASC_when_set_to_PRICE_ASC_in_setSortOption() = runTest {
        //GIVEN
        val sortOption = SortOption.PRICE_ASC

        //WHEN
        settingsRepository.setSortOption(sortOption)

        //THEN
        assertEquals(SortOption.PRICE_ASC, settingsRepository.sortOption.first())
    }

    @Test
    fun should_emit_PRICE_DESC_when_set_to_PRICE_DESC_in_setSortOption() = runTest {
        //GIVEN
        val sortOption = SortOption.PRICE_DESC

        //WHEN
        settingsRepository.setSortOption(sortOption)

        //THEN
        assertEquals(SortOption.PRICE_DESC, settingsRepository.sortOption.first())
    }

    @Test
    fun should_emit_DISCOUNT_when_set_to_DISCOUNT_in_setSortOption() = runTest {
        //GIVEN
        val sortOption = SortOption.DISCOUNT

        //WHEN
        settingsRepository.setSortOption(sortOption)

        //THEN
        assertEquals(SortOption.DISCOUNT, settingsRepository.sortOption.first())
    }

    @Test
    fun should_emit_NONE_when_set_to_NONE_after_having_another_value_in_setSortOption() = runTest {
        //GIVEN
        settingsRepository.setSortOption(SortOption.PRICE_ASC)

        //WHEN
        settingsRepository.setSortOption(SortOption.NONE)

        //THEN
        assertEquals(SortOption.NONE, settingsRepository.sortOption.first())
    }
}