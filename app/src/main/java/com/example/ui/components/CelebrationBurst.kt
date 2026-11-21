package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

private val BURST_EMOJIS = listOf("✨", "🛒", "⭐", "🎉", "💙", "🇩🇴", "🥑", "🍌")

@Composable
fun CelebrationBurst(trigger: Long) {
    if (trigger <= 0) return

    val particles = remember(trigger) {
        List(14) { i ->
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val distance = Random.nextDouble(120.0, 320.0)
            val targetX = (Math.cos(angle) * distance).toInt()
            val targetY = (-Math.abs(Math.sin(angle)) * distance - 80).toInt()
            val emoji = BURST_EMOJIS[i % BURST_EMOJIS.size]
            val rot = Random.nextFloat() * 360f - 180f
            ParticleData(emoji, targetX, targetY, rot)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        particles.forEach { p ->
            val progress = remember(trigger) { Animatable(0f) }
            LaunchedEffect(trigger) {
                launch {
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 950)
                    )
                }
            }

            if (progress.value < 1f) {
                val currentX = (p.targetX * progress.value).toInt()
                val currentY = (p.targetY * progress.value).toInt()
                val alpha = (1f - progress.value).coerceIn(0f, 1f)
                val scale = 0.5f + (progress.value * 0.9f)
                val rotation = p.rotation * progress.value

                Text(
                    text = p.emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .offset { IntOffset(currentX, currentY) }
                        .scale(scale)
                        .rotate(rotation)
                        .alpha(alpha)
                )
            }
        }
    }
}

private data class ParticleData(
    val emoji: String,
    val targetX: Int,
    val targetY: Int,
    val rotation: Float
)
