package com.fluxboard.app.presentation.keyboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluxboard.app.core.utils.FluxColors
import com.fluxboard.app.core.utils.InterFontFamily
import com.fluxboard.app.domain.models.ClipItem

/**
 * Raíz visual del teclado: fondo #121212 y lista reactiva de Clip Cards.
 */
@Composable
fun FluxKeyboardScreen(
    clips: List<ClipItem>,
    onClipSelected: (ClipItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FluxColors.Background)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FluxBoard",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = FluxColors.TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${clips.size} recortes",
                fontFamily = InterFontFamily,
                fontSize = 11.sp,
                color = FluxColors.TextSecondary
            )
        }

        if (clips.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = clips, key = { it.id }) { clip ->
                    ClipCard(
                        clip = clip,
                        onClick = { onClipSelected(clip) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.ContentPaste,
                contentDescription = null,
                tint = FluxColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aún no hay recortes guardados",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = FluxColors.TextSecondary
            )
        }
    }
}
