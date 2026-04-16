package com.aristidevs.cursotestingandroid.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.aristidevs.cursotestingandroid.cart.presentation.CartScreen
import com.aristidevs.cursotestingandroid.detail.presentation.ProductDetailScreen
import com.aristidevs.cursotestingandroid.detail.presentation.ProductDetailViewModel
import com.aristidevs.cursotestingandroid.productlist.presentation.ProductListScreen
import com.aristidevs.cursotestingandroid.settings.presentation.SettingsScreen

@Composable
fun NavGraph() {
    val backStack = rememberNavBackStack(Screen.ProductList)
    val entries = entryProvider<NavKey> {
        entry<Screen.ProductList> {
            ProductListScreen(
                navigateToSettings = { backStack.add(Screen.Setting) },
                navigateToProductDetail = { productId ->
                    backStack.add(
                        Screen.ProductDetail(
                            productId
                        )
                    )
                },
                navigateToCart = {
                    backStack.add(Screen.Cart)
                })
        }
        entry<Screen.Cart> {
            CartScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<Screen.Setting> {
            SettingsScreen(onBack = { backStack.removeLastOrNull() })
        }
        entry<Screen.ProductDetail> {product->
            ProductDetailScreen(
                productDetailViewModel = hiltViewModel<ProductDetailViewModel, ProductDetailViewModel.Factory>(
                    creationCallback = {factory->
                        factory.create(product.productId)
                    }
                )
            ) { backStack.removeLastOrNull() }
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entries,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = { backStack.removeLastOrNull() })

}
