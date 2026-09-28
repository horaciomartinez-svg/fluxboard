package com.fluxboard.app.presentation.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fluxboard.app.core.analytics.AnalyticsEvent
import com.fluxboard.app.core.analytics.AnalyticsTracker
import com.fluxboard.app.core.utils.ActivityProvider
import com.fluxboard.app.core.utils.FluxBoardTheme
import com.fluxboard.app.presentation.app.onboarding.OnboardingScreen1
import com.fluxboard.app.presentation.app.onboarding.OnboardingScreen2
import com.fluxboard.app.presentation.app.onboarding.OnboardingViewModel
import com.fluxboard.app.presentation.app.paywall.PaywallScreen
import com.fluxboard.app.presentation.app.settings.KeyboardActivationScreen
import com.fluxboard.app.presentation.app.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Única Activity de FluxBoard. Monta el [NavHost] central del flujo principal.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityProvider.set(this)
        setContent {
            FluxBoardTheme {
                FluxBoardApp(
                    onFinish = { finish() },
                    onOnboardingStarted = {
                        analyticsTracker.track(AnalyticsEvent.OnboardingStarted)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityProvider.set(this)
    }

    override fun onDestroy() {
        activityProvider.clear()
        super.onDestroy()
    }
}

/** Rutas de navegación del flujo principal (Fase 4). */
object FluxRoutes {
    const val Onboarding1 = "onboarding_1"
    const val Onboarding2 = "onboarding_2"
    const val Paywall = "paywall"
    const val ActivationModal = "activation_modal"
    const val Settings = "settings"
}

@Composable
fun FluxBoardApp(
    onFinish: () -> Unit,
    onOnboardingStarted: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FluxRoutes.Onboarding1
    ) {
        composable(FluxRoutes.Onboarding1) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                onOnboardingStarted()
                onboardingViewModel.ensureAnonymousSession()
            }
            OnboardingScreen1(
                onContinue = { navController.navigate(FluxRoutes.Onboarding2) }
            )
        }

        composable(FluxRoutes.Onboarding2) {
            OnboardingScreen2(
                onContinue = { navController.navigate(FluxRoutes.Paywall) }
            )
        }

        composable(FluxRoutes.Paywall) {
            PaywallScreen(
                onPurchaseSuccess = { navController.navigateToActivation() },
                onSkip = { navController.navigateToActivation() }
            )
        }

        composable(FluxRoutes.ActivationModal) {
            KeyboardActivationScreen(
                onDone = {
                    navController.navigate(FluxRoutes.Settings) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(FluxRoutes.Settings) {
            SettingsScreen(onBack = onFinish)
        }
    }
}

private fun NavHostController.navigateToActivation() {
    navigate(FluxRoutes.ActivationModal) {
        popUpTo(FluxRoutes.Onboarding1) { inclusive = true }
    }
}
