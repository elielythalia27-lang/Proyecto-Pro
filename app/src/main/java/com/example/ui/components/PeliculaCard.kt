package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.Pelicula

/**
 * Standard Uniform Movie Card.
 * Adheres strictly to dark and light mode theming, ensuring no mismatched white blocks in dark mode
 * and crisp borders with comfortable elevation in light mode.
 */
@Composable
fun PeliculaCard(
    pelicula: Pelicula,
    downloadItem: DownloadItem?,
    onCardClick: () -> Unit,
    onDownloadClick: () -> Unit,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardBg = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val cardBorder = if (isDarkTheme) Color(0xFF26354D) else Color(0xFFA0AEC0)
    val titleColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF334155)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pelicula_card_${pelicula.id}")
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCardClick() },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (isDarkTheme) 1.dp else 1.5.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Poster area with compact height (aspect ratio 0.88f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.88f)
                    .background(if (isDarkTheme) Color(0xFF161F33) else Color(0xFFE2E8F0))
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(pelicula.safeCoverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = pelicula.safeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmerEffect(isDark = isDarkTheme)
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isDarkTheme) Color(0xFF131A2B) else Color(0xFFCBD5E1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f),
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sin portada",
                                    color = if (isDarkTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                )

                // Type or Year Badge on top right
                if (pelicula.isVideo) {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE50914)
                    ) {
                        Text(
                            text = "YouTube",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    val yearBadgeText = if (pelicula.safeYear.isNotEmpty()) pelicula.safeYear else "Película"
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xDD0F172A)
                    ) {
                        Text(
                            text = yearBadgeText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Download status indicator overlay on top left
                if (downloadItem != null) {
                    when (downloadItem.status) {
                        DownloadStatus.DOWNLOADING -> {
                            Surface(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopStart),
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xDD000000)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${downloadItem.progress}%",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        DownloadStatus.COMPLETED -> {
                            Surface(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopStart),
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xEE10B981)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Descargada",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Descargada",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Title Container: clear legible title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(cardBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pelicula.safeTitle,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = titleColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Lightweight, high-performance pulse effect modifier replacing heavy linear gradient shimmers.
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulse_transition")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    return this.background(Color(0xFF1E293B).copy(alpha = alphaAnim))
}
