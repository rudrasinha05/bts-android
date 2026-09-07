package com.babatiffin.bts.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.babatiffin.bts.core.design.BtsSpacing

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(BtsSpacing.Xl),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(BtsSpacing.Md),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
