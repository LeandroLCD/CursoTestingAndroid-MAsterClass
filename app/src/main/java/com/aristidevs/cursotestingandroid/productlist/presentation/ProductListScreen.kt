package com.aristidevs.cursotestingandroid.productlist.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductWithPromotion
import com.aristidevs.cursotestingandroid.productlist.domain.model.SortOption
import com.aristidevs.cursotestingandroid.productlist.presentation.components.FiltersMenu
import com.aristidevs.cursotestingandroid.productlist.presentation.components.HomeTopAppBar
import com.aristidevs.cursotestingandroid.productlist.presentation.components.ProductItem
import com.aristidevs.cursotestingandroid.ui.utils.testTagRes

@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel(),
    navigateToSettings: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
    navigateToCart: () -> Unit
) {

    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val filterVisible by productListViewModel.filterVisible.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        productListViewModel.events.collect { event ->
            when (event) {
                is ProductListEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val cartItemCount by productListViewModel.cartItemCount.collectAsStateWithLifecycle()

    ProductListScreenContent(
        uiState = uiState,
        filterVisible = filterVisible,
        cartItemCount = cartItemCount,
        snackbarHostState = snackbarHostState,
        navigateToSettings = navigateToSettings,
        navigateToProductDetail = navigateToProductDetail,
        navigateToCart = navigateToCart,
        setFilterVisible = { showFilters ->
            productListViewModel.setFilterVisible(
                showFilters
            )
        }
    )
}

@Composable
fun ProductListScreenContent(
    uiState: ProductListUiState,
    snackbarHostState: SnackbarHostState,
    filterVisible: Boolean,
    cartItemCount: Int,
    navigateToSettings: () -> Unit = {},
    navigateToProductDetail: (String) -> Unit = {},
    navigateToCart: () -> Unit = {},
    setFilterVisible: (Boolean) -> Unit = {},
    onCategorySelected: (String?) -> Unit = {},
    onSortSelected: (SortOption) -> Unit = {}
) {
    Scaffold(topBar = {
        HomeTopAppBar(
            filtersVisible = filterVisible,
            cartItemCount = cartItemCount,
            onFiltersSelected = setFilterVisible,
            onSettingsSelected = { navigateToSettings() },
            onCartSelected = { navigateToCart() })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValue ->
        when (uiState) {
            is ProductListUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.testTagRes(R.id.loading_progress_indicator))
                }
            }

            is ProductListUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text(
                            stringResource(R.string.error),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                    }
                }
            }

            is ProductListUiState.Success -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValue)
                ) {
                    AnimatedVisibility(
                        visible = filterVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        FiltersMenu(
                            state = uiState,
                            onCategorySelected = onCategorySelected,
                            onSortSelected = onSortSelected
                        )
                    }

                    Text(
                        stringResource(R.string.products, uiState.products.size),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )


                    AnimatedContent(uiState.products.isEmpty()){ targetState->
                        if (targetState){
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🔍", style = MaterialTheme.typography.displayMedium)
                                    Text(
                                        stringResource(R.string.products_not_found),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }else{
                            LazyColumn(Modifier.testTagRes(R.id.product_list_lazy)) {
                                items(uiState.products) { item: ProductWithPromotion ->
                                    ProductItem(
                                        item = item,
                                        onClick = { navigateToProductDetail(it.product.id) })
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

