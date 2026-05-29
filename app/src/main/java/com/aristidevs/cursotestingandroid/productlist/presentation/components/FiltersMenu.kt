package com.aristidevs.cursotestingandroid.productlist.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption.DISCOUNT
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption.PRICE_ASC
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption.PRICE_DESC
import com.aristidevs.cursotestingandroid.productlist.presentation.ProductListUiState
import com.aristidevs.cursotestingandroid.ui.utils.testTagRes

@Composable
fun FiltersMenu(
    modifier: Modifier = Modifier,
    state: ProductListUiState.Success,
    onCategorySelected: (String?) -> Unit,
    onSortSelected: (SortOption) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTagRes(R.id.filter_menu)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.category),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    modifier = Modifier.testTagRes(R.id.category),
                    selected = state.selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text(stringResource(R.string.all), style = MaterialTheme.typography.labelSmall) }
                )
                state.categories.forEach { category ->
                    FilterChip(
                        modifier = Modifier.testTagRes(R.id.category, category),
                        selected = category.equals(state.selectedCategory, ignoreCase = true),
                        onClick = { onCategorySelected(category) },
                        label = { Text(category, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            HorizontalDivider()

            Text(
                stringResource(R.string.sort_by),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.sortOption == PRICE_ASC,
                    onClick = { onSortSelected(PRICE_ASC) },
                    label = { Text(stringResource(R.string.sort_price_asc), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.sortOption == PRICE_DESC,
                    onClick = { onSortSelected(PRICE_DESC) },
                    label = { Text(stringResource(R.string.sort_price_desc), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.sortOption == DISCOUNT,
                    onClick = { onSortSelected(DISCOUNT) },
                    label = { Text(stringResource(R.string.sort_discount), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}