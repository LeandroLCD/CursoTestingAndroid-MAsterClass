package com.aristidevs.cursotestingandroid.core.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.ui.utils.testTagRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketTopAppBar(modifier: Modifier = Modifier, title: String, onBackSelected: () -> Unit) {

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
            )
        }, navigationIcon = {
            IconButton(
                modifier = modifier.testTagRes(R.id.top_bar_back_navigation_icon),
                onClick = { onBackSelected() }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}