package com.fluxboard.app.presentation.keyboard.components

import android.text.format.DateUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluxboard.app.core.utils.FluxColors
import com.fluxboard.app.core.utils.InterFontFamily
import com.fluxboard.app.core.utils.JetBrainsMonoFontFamily
import com.fluxboard.app.domain.models.ClipItem

/**
 * Tarjeta visual de un recorte (Clip Card).
 *
 * Contenedor #1E1E1E con radio 8dp, texto en JetBrains Mono 14sp truncado a 3
 * líneas y borde que se ilumina en #00E5FF al pulsar.
 */
@Composable
fun ClipCard(
    clip: ClipItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) FluxColors.Accent else Color.Transparent,
        label = "clipCardBorder"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FluxColors.Surface)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = clip.textContent,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 14.sp,
                color = FluxColors.TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (clip.isSynced) Icons.Filled.CloudDone else Icons.Filled.Cloud,
                        contentDescription = null,
                        tint = if (clip.isSynced) FluxColors.Success else FluxColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = relativeTime(clip),
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        color = FluxColors.TextSecondary
                    )
                }

                if (clip.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = null,
                        tint = FluxColors.Accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun relativeTime(clip: ClipItem): String =
    DateUtils.getRelativeTimeSpanString(
        clip.createdAt.time,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
