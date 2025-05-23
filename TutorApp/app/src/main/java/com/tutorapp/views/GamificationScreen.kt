package com.tutorapp.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutorapp.models.LeaderboardEntry
import com.tutorapp.viewModels.GamificationViewModel

@Composable
fun GamificationScreen(viewModel: GamificationViewModel) {
    val profile = viewModel.profile

    Column(Modifier.padding(16.dp)) {
        Text("Puntos: ${profile?.points ?: 0}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text("Logros desbloqueados:")
        profile?.achievements?.forEach { Text("🏆 $it") }
    }
}

@Composable
fun LeaderboardScreen(entries: List<LeaderboardEntry>) {
    Column(Modifier.padding(16.dp)) {
        Text("Top 10", fontWeight = FontWeight.Bold)
        entries.forEachIndexed { index, entry ->
            Text("${index + 1}. ${entry.name}: ${entry.points} pts")
        }
    }
}


@Composable
fun AchievementDialog(achievement: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        title = { Text("🎉 ¡Logro desbloqueado!") },
        text = { Text("Has desbloqueado: $achievement") }
    )
}
