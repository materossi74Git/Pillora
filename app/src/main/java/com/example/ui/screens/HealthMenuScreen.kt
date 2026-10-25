package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SubScreen

@Composable
fun HealthMenuScreen(
    onNavigateSubScreen: (SubScreen) -> Unit,
    onNavigateFarmaci: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E)),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Section title
        item {
            Text(
                text = "Altre funzioni di salute",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // 0. Statistiche & Aderenza
        item {
            MenuNavigationCard(
                title = "Statistiche & Aderenza",
                subtitle = "Grafici settimanali, percentuali di assunzione e trend",
                icon = Icons.Default.QueryStats,
                iconBg = Color(0xFF10B981),
                testTag = "menu_statistiche",
                onClick = { onNavigateSubScreen(SubScreen.STATISTICHE) }
            )
        }

        // 1. Farmaci
        item {
            MenuNavigationCard(
                title = "Farmaci",
                subtitle = "Visualizza e gestisci le tue terapie attive",
                icon = Icons.Default.Medication,
                iconBg = Color(0xFF0284C7),
                testTag = "menu_farmaci",
                onClick = onNavigateFarmaci
            )
        }

        // 2. Tracker di salute e misurazioni
        item {
            MenuNavigationCard(
                title = "Tracker di salute e misurazioni",
                subtitle = "Pressione arteriosa, glicemia, battiti cardiaci e peso",
                icon = Icons.Default.MonitorHeart,
                iconBg = Color(0xFF06B6D4),
                testTag = "menu_tracker_salute",
                onClick = { onNavigateSubScreen(SubScreen.TRACKER_SALUTE) }
            )
        }

        // 3. Appuntamenti
        item {
            MenuNavigationCard(
                title = "Appuntamenti",
                subtitle = "Visite specialistiche, esami clinici e promemoria",
                icon = Icons.Default.CalendarMonth,
                iconBg = Color(0xFF3B82F6),
                testTag = "menu_appuntamenti",
                onClick = { onNavigateSubScreen(SubScreen.APPUNTAMENTI) }
            )
        }

        // 4. Note del diario
        item {
            MenuNavigationCard(
                title = "Note del diario",
                subtitle = "Registra sintomi, benessere quotidiano ed effetti",
                icon = Icons.Default.EditNote,
                iconBg = Color(0xFFF97316),
                testTag = "menu_note_diario",
                onClick = { onNavigateSubScreen(SubScreen.NOTE_DIARIO) }
            )
        }

        // 5. Medici
        item {
            MenuNavigationCard(
                title = "Medici",
                subtitle = "Contatti del medico curante e specialisti",
                icon = Icons.Default.People,
                iconBg = Color(0xFF10B981),
                testTag = "menu_medici",
                onClick = { onNavigateSubScreen(SubScreen.MEDICI) }
            )
        }

        // 6. Ricariche
        item {
            MenuNavigationCard(
                title = "Ricariche",
                subtitle = "Monitoraggio scorte pillole e alert esaurimento",
                icon = Icons.Default.Restore,
                iconBg = Color(0xFF8B5CF6),
                testTag = "menu_ricariche",
                onClick = { onNavigateSubScreen(SubScreen.RICARICHE) }
            )
        }

        // 7. Memoria & Backup Google
        item {
            MenuNavigationCard(
                title = "Memoria & Backup Google",
                subtitle = "Sicurezza dati, stato database locale e backup Google Drive",
                icon = Icons.Default.Assignment,
                iconBg = Color(0xFF0284C7),
                testTag = "menu_backup_info",
                onClick = { onNavigateSubScreen(SubScreen.BACKUP_INFO) }
            )
        }

        // 8. Aggiornamenti App (GitHub)
        item {
            MenuNavigationCard(
                title = "Aggiornamenti App (GitHub)",
                subtitle = "Verifica nuove versioni e scarica gli aggiornamenti con 1 clic",
                icon = Icons.Default.SystemUpdate,
                iconBg = Color(0xFF6366F1),
                testTag = "menu_aggiornamenti_app",
                onClick = { onNavigateSubScreen(SubScreen.AGGIORNAMENTI) }
            )
        }
    }
}

@Composable
private fun MenuNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16212E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
