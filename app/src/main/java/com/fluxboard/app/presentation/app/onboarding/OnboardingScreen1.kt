package com.fluxboard.app.presentation.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.fluxboard.app.R

/**
 * Pantalla 1 - El gancho emocional.
 */
@Composable
fun OnboardingScreen1(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_1_title),
        body = stringResource(R.string.onboarding_1_body),
        activeIndex = 0,
        ctaLabel = stringResource(R.string.action_continue),
        onCtaClick = onContinue,
        modifier = modifier
    )
}
