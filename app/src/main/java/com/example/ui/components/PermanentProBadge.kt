package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MonetizationConstants

@Composable
fun PermanentProBadge(
    isProUser: Boolean,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isProUser) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFFFD700).copy(alpha = 0.18f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
            modifier = modifier
                .clip(RoundedCornerShape(50))
                .clickable { onOpenPaywall() }
                .testTag("permanent_pro_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "👑", fontSize = 14.sp)
                Text(
                    text = "PRO ACTIVO",
                    color = Color(0xFFB8860B),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFFFD700).copy(alpha = 0.22f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD4AF37)),
            modifier = modifier
                .clip(RoundedCornerShape(50))
                .clickable { onOpenPaywall() }
                .testTag("permanent_pro_button")
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFDF00).copy(alpha = 0.35f),
                                Color(0xFFFFA500).copy(alpha = 0.2f)
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Stars,
                    contentDescription = null,
                    tint = Color(0xFFB8860B),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Desbloquear Pro",
                    color = Color(0xFF946200),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
