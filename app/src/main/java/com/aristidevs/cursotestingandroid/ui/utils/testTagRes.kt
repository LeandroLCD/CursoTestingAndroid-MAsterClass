package com.aristidevs.cursotestingandroid.ui.utils

import android.annotation.SuppressLint
import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag

/**
 * Aplica un [testTag] cuyo valor es el nombre del recurso obtenido desde ids.xml
 * usando el [id] proporcionado (R.id.<nombre>).
 *
 * Ejemplo:
 * ```
 * Box(modifier = Modifier.testTagRes(R.id.box_detail))
 * ```
 */
@SuppressLint("LocalContextResourcesRead", "LocalContextGetResourceValueCall")
@Composable
fun Modifier.testTagRes(@IdRes id: Int) = apply {
    testTag(LocalResources.current.getResourceEntryName(id))
}

@SuppressLint("LocalContextResourcesRead", "LocalContextGetResourceValueCall")
@Composable
fun Modifier.testTagRes(@IdRes id: Int, vararg formatArgs: Any?, separator: Char = '_') = apply {
    val tag = LocalResources.current.getResourceEntryName(id)
    testTag(tag.plus(separator).plus(formatArgs.joinToString(separator = separator.toString())))
}