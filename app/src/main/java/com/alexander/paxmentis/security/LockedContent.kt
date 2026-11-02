package com.alexander.paxmentis.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Zeigt [content] nur, wenn die Sitzung entsperrt ist. Andernfalls erscheint ein
 * Sperrbildschirm, der beim ersten Anzeigen automatisch den Entsperr-Dialog öffnet.
 */
@Composable
fun LockedContent(
    isUnlocked: Boolean,
    authMessage: String?,
    onRequestUnlock: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isUnlocked) {
        content()
        return
    }

    // rememberSaveable: Nach einer Drehung nicht erneut automatisch prompten,
    // der System-Dialog bleibt ohnehin bestehen.
    var autoPrompted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!autoPrompted) {
            autoPrompted = true
            onRequestUnlock()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "Dieser Bereich ist geschützt",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Deine persönlichen Einträge sind nur nach dem Entsperren sichtbar.",
            textAlign = TextAlign.Center
        )
        if (authMessage != null) {
            Text(authMessage, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Button(onClick = onRequestUnlock) {
            Text("Entsperren")
        }
    }
}
