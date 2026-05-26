package com.aristidevs.cursotestingandroid.core

import android.content.Context
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule

abstract class BaseComposeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    val appContext: Context? = ApplicationProvider.getApplicationContext<Context>()

    val resources: Resources? = appContext?.resources

    protected fun stringResources(@StringRes id: Int) = resources?.getString(id) ?: throw IllegalStateException("Resources not found")

    protected fun idResources(@IdRes id: Int) = resources?.getResourceEntryName(id) ?: throw IllegalStateException("Resources not found")

    protected fun idResources(@IdRes id: Int, vararg formatArgs: Any?, separator: Char = '_'): String {
        val tag = resources?.getResourceEntryName(id) ?: throw IllegalStateException("Resources not found")
        return tag.plus(separator).plus(formatArgs.joinToString(separator = separator.toString()))
    }
}