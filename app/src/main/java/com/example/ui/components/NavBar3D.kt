package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavDockItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isSelected: Boolean,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun NavBar3D(
    items: List<NavDockItem>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    val basePlinthStart = lerp(primary, Color.Black, 0.35f)
    val basePlinthEnd = lerp(tertiary, Color.Black, 0.35f)

    // Subtle 4D ambient light sweep along the dock border
    val infiniteTransition = rememberInfiniteTransition(label = "dock_4d")
    val shimmerShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dock_shimmer"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("bottom_navigation_bar")
    ) {
        // 3D Extruded Bottom Plinth (creates solid physical thickness underneath the dock)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 6.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = primary.copy(alpha = 0.5f),
                    spotColor = primary.copy(alpha = 0.55f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(basePlinthStart, basePlinthEnd)
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = lerp(primary, Color.Black, 0.55f),
                    shape = RoundedCornerShape(26.dp)
                )
        )

        // Elevated Top Face of the 3D/4D Dock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            surface,
                            lerp(surface, surfaceVariant, 0.55f)
                        )
                    )
                )
                .drawBehind {
                    // Subtle 4D top specular highlight sweep
                    val xCenter = size.width * shimmerShift
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            startX = xCenter - size.width * 0.35f,
                            endX = xCenter + size.width * 0.35f
                        )
                    )
                    // Crisp inner top bevel line
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(24f, 2f),
                        end = Offset(size.width - 24f, 2f),
                        strokeWidth = 2.5f
                    )
                }
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.85f),
                            lerp(primary, Color.Black, 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    NavDockButton3D(
                        item = item,
                        primary = primary,
                        secondary = secondary,
                        onSurface = onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavDockButton3D(
    item: NavDockItem,
    primary: Color,
    secondary: Color,
    onSurface: Color,
    modifier: Modifier = Modifier
) {
    val popOffsetY by animateDpAsState(
        targetValue = if (item.isSelected) (-4).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_pop_y"
    )
    val scale by animateFloatAsState(
        targetValue = if (item.isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_scale"
    )
    val contentColor by animateColorAsState(
        targetValue = if (item.isSelected) Color.White else onSurface.copy(alpha = 0.78f),
        label = "nav_content_color"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val pillShape = RoundedCornerShape(18.dp)
    val pillBaseColor = lerp(primary, Color.Black, 0.45f)

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .offset(y = popOffsetY)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag(item.testTag),
        contentAlignment = Alignment.Center
    ) {
        if (item.isSelected) {
            // 3D Raised Keycap Base for the Active Item
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 3.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = pillShape,
                        spotColor = primary.copy(alpha = 0.6f)
                    )
                    .clip(pillShape)
                    .background(pillBaseColor)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (item.isSelected) Modifier.padding(bottom = 3.dp) else Modifier)
                .clip(pillShape)
                .then(
                    if (item.isSelected) {
                        Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(secondary, primary)
                                )
                            )
                            .border(
                                width = 1.4.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.78f),
                                        Color.White.copy(alpha = 0.22f)
                                    )
                                ),
                                shape = pillShape
                            )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = onSurface.copy(alpha = 0.12f),
                            shape = pillShape
                        )
                    }
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onClick = item.onClick
                )
                .padding(vertical = 7.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (item.isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = if (item.isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
