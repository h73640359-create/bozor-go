package uz.bozorgo.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.bozorgo.R
import uz.bozorgo.core.util.MoneyFormatter
import uz.bozorgo.ui.theme.BozorGoColors

/** Bo'lim sarlavhasi ("Mashhur mahsulotlar" + "Barchasini ko'rish"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = BozorGoColors.TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onActionClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = actionLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Narx bloki: asosiy narx + (agar bo'lsa) chizilgan eski narx. */
@Composable
fun PriceView(
    price: Double,
    oldPrice: Double?,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = MoneyFormatter.formatUzs(price),
            style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = BozorGoColors.TextPrimary,
            maxLines = 1,
        )
        if (oldPrice != null && oldPrice > price) {
            Text(
                text = MoneyFormatter.formatUzs(oldPrice),
                style = MaterialTheme.typography.bodySmall,
                color = BozorGoColors.TextTertiary,
                maxLines = 1,
                textAlign = TextAlign.Start,
            )
        }
    }
}

/** Chegirma foizi belgisi. */
@Composable
fun DiscountBadge(
    percent: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = BozorGoColors.Error,
        contentColor = Color.White,
    ) {
        Text(
            text = "-$percent%",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/** Reyting yulduzchasi. */
@Composable
fun RatingView(
    rating: Double,
    reviewCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            tint = BozorGoColors.Star,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = String.format(java.util.Locale.US, "%.1f", rating),
            style = MaterialTheme.typography.labelMedium,
            color = BozorGoColors.TextPrimary,
        )
        if (reviewCount > 0) {
            Spacer(Modifier.width(3.dp))
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.labelSmall,
                color = BozorGoColors.TextTertiary,
            )
        }
    }
}

/** Miqdor o'zgartirgich (- 2 +). */
@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val size = if (compact) 30.dp else 38.dp
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(size / 2))
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDecrease,
            modifier = Modifier.size(size),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.product_quantity))
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = BozorGoColors.TextPrimary,
        )
        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(size),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.product_quantity))
        }
    }
}

/** Bo'sh holat (savatcha bo'sh, natija topilmadi va h.k.). */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = BozorGoColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = BozorGoColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** Xatolik holati (qayta urinish tugmasi bilan). */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.common_error),
            style = MaterialTheme.typography.titleMedium,
            color = BozorGoColors.Error,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = BozorGoColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
        }
    }
}

/** Yuklanish paytida ko'rsatiladigan skeleton. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerProgress",
    )
    val brush = Brush.linearGradient(
        colors = listOf(BozorGoColors.Shimmer, Color(0xFFF7F8FA), BozorGoColors.Shimmer),
        start = Offset(0f, 0f),
        end = Offset(1000f * progress, 1000f * progress),
    )
    Box(modifier = modifier.background(brush, RoundedCornerShape(12.dp)))
}

/** Yuklanish holati (katta ekranlar uchun). */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(10.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
    }
}
