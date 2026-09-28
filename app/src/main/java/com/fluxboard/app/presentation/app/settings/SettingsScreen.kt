package com.fluxboard.app.presentation.app.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluxboard.app.R
import com.fluxboard.app.core.utils.FluxColors
import com.fluxboard.app.core.utils.InterFontFamily
import com.fluxboard.app.core.utils.JetBrainsMonoFontFamily

/** URLs placeholder de las páginas legales (pendientes de publicación). */
private const val PRIVACY_POLICY_URL = "https://fluxboard.app/privacy"
private const val TERMS_OF_SERVICE_URL = "https://fluxboard.app/terms"

/**
 * Pantalla de Ajustes (Sprint V1.1).
 *
 * Reúne los tres bloques normativos mínimos: cuenta (identidad anónima),
 * suscripción (estado y gestión vía RevenueCat) y legal (privacidad y términos),
 * obligatorios para la publicación en tienda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FluxColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = FluxColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                            tint = FluxColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FluxColors.Background,
                    titleContentColor = FluxColors.TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AccountSection(anonymizedUserId = uiState.anonymizedUserId)

            SubscriptionSection(
                isPro = uiState.isPro,
                isManagingSubscription = uiState.isManagingSubscription,
                onManageSubscription = viewModel::onManageSubscriptionClicked
            )

            LegalSection(
                onOpenPrivacyPolicy = { context.openUrl(PRIVACY_POLICY_URL) },
                onOpenTermsOfService = { context.openUrl(TERMS_OF_SERVICE_URL) }
            )
        }
    }
}

@Composable
private fun AccountSection(anonymizedUserId: String?) {
    SettingsSection(title = stringResource(R.string.settings_account_section)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_account_id_label),
                fontFamily = InterFontFamily,
                color = FluxColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = anonymizedUserId ?: stringResource(R.string.settings_account_id_unknown),
                fontFamily = JetBrainsMonoFontFamily,
                color = FluxColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SubscriptionSection(
    isPro: Boolean,
    isManagingSubscription: Boolean,
    onManageSubscription: () -> Unit
) {
    SettingsSection(title = stringResource(R.string.settings_subscription_section)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPro) {
                    stringResource(R.string.settings_subscription_pro)
                } else {
                    stringResource(R.string.settings_subscription_free)
                },
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = if (isPro) FluxColors.Success else FluxColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onManageSubscription,
            enabled = !isManagingSubscription,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FluxColors.Primary,
                contentColor = Color.White,
                disabledContainerColor = FluxColors.Primary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = stringResource(R.string.settings_manage_subscription),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LegalSection(
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsOfService: () -> Unit
) {
    SettingsSection(title = stringResource(R.string.settings_legal_section)) {
        LegalLink(
            label = stringResource(R.string.settings_privacy_policy),
            onClick = onOpenPrivacyPolicy
        )
        LegalDivider()
        LegalLink(
            label = stringResource(R.string.settings_terms_of_service),
            onClick = onOpenTermsOfService
        )
    }
}

@Composable
private fun LegalLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        fontFamily = InterFontFamily,
        color = FluxColors.TextSecondary,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun LegalDivider() {
    HorizontalDivider(color = FluxColors.TextSecondary.copy(alpha = 0.2f))
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = FluxColors.TextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = FluxColors.Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

private fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
