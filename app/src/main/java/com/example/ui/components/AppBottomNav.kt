package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ScreenRoute(
    val route: String,
    val title: String,
    val pageIndex: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Inicio", 0, Icons.Filled.Home, Icons.Outlined.Home),
    DOWNLOADS("downloads", "Descargas", 1, Icons.Filled.Download, Icons.Outlined.Download),
    SETTINGS("settings", "Ajustes", 2, Icons.Filled.Settings, Icons.Outlined.Settings)
}

/**
 * Modern floating ergonomic navigation pill bar with substantial height,
 * comfortable touch targets, smooth spring animations, and dynamic dark/light theme support.
 */
@Composable
fun AppBottomNav(
    currentPage: Int,
    onNavigate: (Int) -> Unit,
    downloadsCount: Int,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDarkTheme) 14.dp else 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f),
                    ambientColor = if (isDarkTheme) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f)
                )
                .border(
                    width = if (isDarkTheme) 1.dp else 1.5.dp,
                    color = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color(0xFF94A3B8),
                    shape = RoundedCornerShape(32.dp)
                )
                .testTag("floating_bottom_nav"),
            shape = RoundedCornerShape(32.dp),
            color = if (isDarkTheme) Color(0xD00D1527) else Color(0xD8FFFFFF),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScreenRoute.entries.forEach { screen ->
                    val isSelected = currentPage == screen.pageIndex
                    val interactionSource = remember { MutableInteractionSource() }
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 0.96f,
                        animationSpec = spring(
                            dampingRatio = 0.68f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "scale_anim"
                    )
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.18f else 0.12f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "container_color"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF334155)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "content_color"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(containerColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onNavigate(screen.pageIndex)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("nav_item_${screen.route}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Icon with badge if downloads active
                            Box(
                                modifier = Modifier.scale(iconScale),
                                contentAlignment = Alignment.Center
                            ) {
                                if (screen == ScreenRoute.DOWNLOADS && downloadsCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = Color.White,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .offset(x = 5.dp, y = (-4).dp)
                                            ) {
                                                Text(
                                                    text = "$downloadsCount",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title,
                                            tint = contentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Smoothly expanding text label when selected
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn() + expandHorizontally(
                                    animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
                                ),
                                exit = fadeOut() + shrinkHorizontally(
                                    animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = screen.title,
                                        color = contentColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
