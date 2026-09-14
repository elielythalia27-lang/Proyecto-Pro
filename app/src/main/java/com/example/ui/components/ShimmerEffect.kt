package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Creates a modern glowing shimmer brush for skeleton loading states.
 * Uses an angled linear light sweep wave for a fluid, polished effect.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    isDark: Boolean = true
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim = transition.animateFloat(
        initialValue = -600f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_float"
    )

    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF131C31),
            Color(0xFF1E293B),
            Color(0xFF2E3E5B),
            Color(0xFF1E293B),
            Color(0xFF131C31)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFFFFFFF),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )
    }

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim.value - 400f, y = translateAnim.value - 400f),
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    this
        .clip(shape)
        .background(brush)
}

/**
 * Shimmer placeholder for movie cards in grid mode.
 */
@Composable
fun PeliculaGridItemSkeleton(isDark: Boolean = true) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Poster skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.70f)
                    .shimmerEffect(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp), isDark = isDark)
            )
            // Title and badge skeleton
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(13.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp), isDark = isDark)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(11.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp), isDark = isDark)
                )
            }
        }
    }
}

/**
 * Shimmer placeholder for movie cards in list mode.
 */
@Composable
fun PeliculaListItemSkeleton(isDark: Boolean = true) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(66.dp)
                    .fillMaxHeight()
                    .shimmerEffect(RoundedCornerShape(10.dp), isDark = isDark)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(14.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp), isDark = isDark)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(11.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp), isDark = isDark)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(10.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp), isDark = isDark)
                )
            }
        }
    }
}

/**
 * Full skeleton loading grid for the catalog.
 */
@Composable
fun PeliculaGridSkeleton(
    modifier: Modifier = Modifier,
    columnsCount: Int = 2,
    itemCount: Int = 6,
    isDark: Boolean = true
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnsCount),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false,
        modifier = modifier.fillMaxSize()
    ) {
        items(itemCount) {
            PeliculaGridItemSkeleton(isDark = isDark)
        }
    }
}

/**
 * Full skeleton loading list for the catalog.
 */
@Composable
fun PeliculaListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
    isDark: Boolean = true
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false,
        modifier = modifier.fillMaxSize()
    ) {
        items(itemCount) {
            PeliculaListItemSkeleton(isDark = isDark)
        }
    }
}

/**
 * Shimmer image placeholder for SubcomposeAsyncImage loading slots.
 */
@Composable
fun ShimmerImagePlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    isDark: Boolean = true
) {
    Box(
        modifier = modifier.shimmerEffect(shape = shape, isDark = isDark)
    )
}
