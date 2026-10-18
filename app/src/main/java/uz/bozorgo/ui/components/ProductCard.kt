package uz.bozorgo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.util.Log
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import uz.bozorgo.R
import uz.bozorgo.domain.model.Product
import uz.bozorgo.ui.theme.BozorGoColors

/** Uzoqdan (server) yuklanayotgan rasm uchun qayta urinishlar soni. */
private const val MAX_IMAGE_RETRIES = 1
private const val IMAGE_LOG_TAG = "BozorGoImage"

/**
 * Mahsulot rasmi: server URL bo'lsa undan, aks holda APK ichidagi assets'dan.
 *
 * Ishlash tartibi:
 * 1. Asosiy manba yuklanadi (asset yoki URL).
 * 2. Uzoq URL bo'lsa — yuklanish jarayoni ko'rsatkichi + 1 marta qayta urinish.
 * 3. Baribir yuklanmasa — mahsulot kategoriyasiga mos zaxira rasm (hech qachon
 *    hamma mahsulot uchun bitta kulrang kvadrat qolmaydi).
 * 4. Har bir xatolik sababi logga yoziladi.
 */
@Composable
fun ProductImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackModel: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    // Karta qayta ishlatilganda (Lazy list) holat yangi manba bilan qayta tug'iladi.
    var attempt by remember(model, fallbackModel) { mutableStateOf(0) }
    var useFallback by remember(model, fallbackModel) { mutableStateOf(false) }
    var isLoading by remember(model, fallbackModel) { mutableStateOf(true) }

    val activeModel = if (useFallback) fallbackModel else model
    val isRemote = activeModel?.startsWith("http") == true

    val request = remember(context, activeModel, attempt) {
        ImageRequest.Builder(context)
            .data(activeModel)
            .crossfade(true)
            .listener(
                onStart = { isLoading = true },
                onSuccess = { _, _ -> isLoading = false },
                onCancel = { isLoading = false },
                onError = { failedRequest, result ->
                    Log.w(
                        IMAGE_LOG_TAG,
                        "Rasm yuklanmadi: ${failedRequest.data} — " +
                            "${result.throwable::class.java.simpleName}: ${result.throwable.message}",
                    )
                    // Coil tinglovchilari asosiy (Main) oqimda chaqiriladi.
                    isLoading = false
                    if (!useFallback) {
                        when {
                            // Uzoq rasm: avval qayta urinamiz (cheklangan).
                            isRemote && attempt < MAX_IMAGE_RETRIES -> attempt++
                            // Keyin kategoriyaga mos zaxira rasmga o'tamiz.
                            fallbackModel.isNullOrBlank().not() && fallbackModel != model -> useFallback = true
                        }
                    }
                },
            )
            .build()
    }

    Box(
        modifier = modifier.background(BozorGoColors.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            placeholder = painterResource(R.drawable.img_placeholder),
            error = painterResource(R.drawable.img_placeholder),
            fallback = painterResource(R.drawable.img_placeholder),
        )
        // Faqat internet orqali yuklanayotganda ko'rsatkich: lokal asset bir lahzada chiqadi.
        if (isLoading && isRemote) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}

/** Katalog / bosh sahifa uchun mahsulot kartochkasi. */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BozorGoColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(BozorGoColors.SurfaceVariant),
            ) {
                ProductImage(
                    model = product.imageModel(),
                    fallbackModel = product.fallbackImageModel(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                if (product.hasDiscount && product.discountPercent > 0) {
                    DiscountBadge(
                        percent = product.discountPercent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(34.dp)
                        .padding(end = 2.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isFavorite) BozorGoColors.Error else BozorGoColors.TextTertiary,
                    ),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.home_favorites),
                        modifier = Modifier.size(20.dp),
                    )
                }

                if (!product.inStock) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        color = Color(0xCC16181D),
                    ) {
                        Text(
                            text = stringResource(R.string.product_out_of_stock),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriceView(
                        price = product.effectivePrice,
                        oldPrice = if (product.hasDiscount) product.price else null,
                        modifier = Modifier.weight(1f),
                    )
                    if (product.inStock) {
                        Surface(
                            onClick = onAddToCart,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = stringResource(R.string.product_add_to_cart),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = BozorGoColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                RatingView(rating = product.rating, reviewCount = product.reviewCount)
            }
        }
    }
}

/** Gorizontal ro'yxat uchun ixcham karta. */
@Composable
fun ProductRowCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BozorGoColors.Surface),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(BozorGoColors.SurfaceVariant),
            ) {
                ProductImage(
                    model = product.imageModel(),
                    fallbackModel = product.fallbackImageModel(),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                if (product.hasDiscount && product.discountPercent > 0) {
                    DiscountBadge(
                        percent = product.discountPercent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isFavorite) BozorGoColors.Error else BozorGoColors.TextTertiary,
                    ),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.home_favorites),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                PriceView(
                    price = product.effectivePrice,
                    oldPrice = if (product.hasDiscount) product.price else null,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                if (product.inStock) {
                    Surface(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.product_add_to_cart),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.product_out_of_stock),
                        style = MaterialTheme.typography.labelSmall,
                        color = BozorGoColors.Error,
                    )
                }
            }
        }
    }
}
