package com.fluxboard.app.presentation.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.fluxboard.app.R

/**
 * Pantalla 2 - El momento 'Aha' (historial en el teclado).
 */
@Composable
fun OnboardingScreen2(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_2_title),
        body = stringResource(R.string.onboarding_2_body),
        activeIndex = 1,
        ctaLabel = stringResource(R.string.action_continue),
        onCtaClick = onContinue,
        modifier = modifier
    )
}
