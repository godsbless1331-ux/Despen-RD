package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PantryCube3D(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cube")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val faceEmojiIndex = ((rotation / 60).toInt()) % 6
    val emojis = listOf("🛒", "🍎", "🧴", "👶", "🥤", "🧽")
    val currentEmoji = emojis.getOrElse(faceEmojiIndex) { "🛒" }

    Box(
        modifier = modifier
            .size(86.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Isometric Cube Container
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    rotationY = rotation
                    rotationX = -18f
                    cameraDistance = 12f * density
                }
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.32f),
                            Color.White.copy(alpha = 0.12f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentEmoji,
                fontSize = 36.sp
            )
        }
    }
}
