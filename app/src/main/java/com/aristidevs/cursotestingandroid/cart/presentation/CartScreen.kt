package com.aristidevs.cursotestingandroid.cart.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.cart.domain.model.CartSummary
import com.aristidevs.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.aristidevs.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.aristidevs.cursotestingandroid.core.presentation.components.QuantitySelector
import com.aristidevs.cursotestingandroid.productlist.domain.model.ProductPromotion
import com.aristidevs.cursotestingandroid.ui.utils.testTagRes
import java.text.NumberFormat
import java.util.Currency.getInstance

@Composable
fun CartScreen(
    onBack: () -> Unit, cartViewModel: CartViewModel = hiltViewModel()
) {

    val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        cartViewModel.event.collect { event ->
            when (event) {
                is CartEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    CartScreenContent(
        snackbarHostState = snackbarHostState,
        state = uiState,
        onBack = onBack,
        onIncreaseQuantity = cartViewModel::increaseQuantity,
        onDecreaseQuantity = cartViewModel::decreaseQuantity,
        onRemove = cartViewModel::removeFromCart
    )
}

@Composable
fun CartScreenContent(
    state: CartUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {},
    onIncreaseQuantity: (String, Int) -> Unit = {_,_-> },
    onDecreaseQuantity: (String, Int) -> Unit = {_,_-> },
    onRemove: (String) -> Unit = {}
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { MarketTopAppBar(title = stringResource(R.string.car)) { onBack() } }) { paddingValues ->
        when (state) {
            CartUiState.Loading -> {
                CartLoadingStateScreen(
                    Modifier
                        .fillMaxSize()
                        .testTagRes(R.id.cart_loading)
                        .padding(paddingValues)
                )
            }

            is CartUiState.Error -> {
                CartErrorStateScreen(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state,
                ) {
                    onBack.invoke()
                }
            }

            is CartUiState.Success -> {
                CartSuccessStateScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = state,
                    onIncreaseQuantity = onIncreaseQuantity,
                    onDecreaseQuantity = onDecreaseQuantity,
                    onRemove = onRemove
                )
            }
        }
    }
}

@Composable
fun CartErrorStateScreen(
    modifier: Modifier = Modifier, state: CartUiState.Error, onBackStack: () -> Unit
) {
    Column(
        modifier = modifier
            .testTagRes(R.id.cart_state_error_content)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.error_with_msj, state.message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Button(
            modifier = Modifier.testTagRes(R.id.cart_error_button),
            onClick = { onBackStack.invoke() }) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun CartLoadingStateScreen(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun CartSuccessStateScreen(
    modifier: Modifier = Modifier,
    state: CartUiState.Success,
    onIncreaseQuantity: (String, Int) -> Unit,
    onDecreaseQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
) {

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance().apply {
            currency = getInstance("USD")
        }
    }

    Column(modifier.padding(16.dp)) {
        AnimatedContent(state.cartItems.isEmpty()) { isEmpty ->
            if (isEmpty) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(54.dp))
                    Text(
                        stringResource(R.string.car_icon), style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        stringResource(R.string.car_is_empty),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.add_product),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else {
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .testTagRes(R.id.cart_list_lasy),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.cartItems, key = { it.cartItem.productId }) { itemWithProduct ->
                        CartItemCard(
                            modifier = Modifier
                                 .animateItem(),
                            itemWithProduct = itemWithProduct,
                            currencyFormatter = currencyFormatter,
                            onIncreaseQuantity = { productId, quantity ->
                                onIncreaseQuantity(
                                    productId, quantity
                                )
                            },
                            onDecreaseQuantity = { productId, quantity ->
                                onDecreaseQuantity(
                                    productId, quantity
                                )
                            },
                            onRemove = { id -> onRemove(id) })
                    }
                }
            }
        }

        if (state.cartItems.isNotEmpty() && state.summary != null) {
            CartSummaryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                summary = state.summary,
                currencyFormatter = currencyFormatter
            )
        }
    }
}

@Composable
fun CartSummaryCard(modifier: Modifier, summary: CartSummary, currencyFormatter: NumberFormat) {
    Card(
        modifier = modifier
            .testTagRes(R.id.cart_summary),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.resume_car),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.sub_total),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    currencyFormatter.format(summary.subtotal),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (summary.discountTotal > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().testTagRes(R.id.cart_discount_row),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.discount),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        currencyFormatter.format(summary.discountTotal),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.total),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    currencyFormatter.format(summary.finalTotal),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}

@Composable
fun CartItemCard(
    modifier: Modifier,
    itemWithProduct: CartItemWithPromotion,
    currencyFormatter: NumberFormat,
    onIncreaseQuantity: (String, Int) -> Unit,
    onDecreaseQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit
) {
    val product = itemWithProduct.item.product
    val promotion = itemWithProduct.item.promotion
    val cartItem = itemWithProduct.cartItem

    val unitPrice = when (promotion) {
        is ProductPromotion.Percent -> promotion.discountedPrice
        is ProductPromotion.BuyXPayY -> product.price
        null -> product.price
    }

    val hasDiscount = promotion is ProductPromotion.Percent
    val itemTotal = unitPrice * cartItem.quantity

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onRemove(cartItem.productId)
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        modifier = modifier.testTagRes(R.id.cart_item_card, cartItem.productId),
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }) {
        Card(
            Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp)),
                    model = product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(24.dp))
                Column(
                    modifier = Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hasDiscount) {
                            Text(
                                text = currencyFormatter.format(product.price),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough
                            )

                            Text(
                                text = stringResource(
                                    R.string.c_u,
                                    currencyFormatter.format(unitPrice)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = stringResource(
                                    R.string.c_u,
                                    currencyFormatter.format(unitPrice)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }


                    Text(
                        stringResource(
                            R.string.total_with_amount,
                            currencyFormatter.format(itemTotal)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    QuantitySelector(
                        cartItem = cartItem,
                        modifier = Modifier.testTagRes(R.id.cart_quantity_selector_item, cartItem.productId).background(
                            MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)
                        ),
                        stock = product.stock,
                        onDecreaseSelected = { onDecreaseQuantity(product.id, cartItem.quantity) },
                        onIncreaseSelected = { onIncreaseQuantity(product.id, cartItem.quantity) },
                    )
                }
            }
        }
    }

}












