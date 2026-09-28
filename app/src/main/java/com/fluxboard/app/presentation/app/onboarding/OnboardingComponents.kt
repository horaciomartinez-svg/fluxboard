package com.fluxboard.app.presentation.app.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluxboard.app.core.utils.FluxColors
import com.fluxboard.app.core.utils.InterFontFamily

/**
 * Estructura común de las pantallas de Onboarding: titular, cuerpo, CTA y
 * paginación inferior.
 */
@Composable
fun OnboardingPage(
    title: String,
    body: String,
    activeIndex: Int,
    ctaLabel: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FluxColors.Background)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = title,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 38.sp,
            color = FluxColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = body,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = FluxColors.TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCtaClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FluxColors.Primary,
                contentColor = FluxColors.TextPrimary
            )
        ) {
            Text(
                text = ctaLabel,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PaginationDots(
            totalDots = 3,
            activeIndex = activeIndex,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun PaginationDots(
    totalDots: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val isActive = index == activeIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (isActive) 24.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) FluxColors.Primary else FluxColors.TextSecondary)
            )
        }
    }
}
