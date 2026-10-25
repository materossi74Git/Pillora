package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseScheduleEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.ScheduledDoseItem
import com.example.ui.components.DoseCard
import com.example.ui.components.NotificationSettingsDialog
import java.util.Calendar

import androidx.compose.material.icons.filled.QueryStats

@Composable
fun DailyScheduleScreen(
    doses: List<ScheduledDoseItem>,
    onMarkStatus: (ScheduledDoseItem, IntakeStatus) -> Unit,
    onSnooze: (ScheduledDoseItem) -> Unit,
    onTestNotification: (ScheduledDoseItem) -> Unit,
    onUpdateSchedule: (DoseScheduleEntity) -> Unit,
    onAddMedicationClick: () -> Unit,
    onViewWeeklySchedule: () -> Unit,
    onViewStatistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingScheduleItem by remember { mutableStateOf<ScheduledDoseItem?>(null) }

    // Summary calculations
    val totalDoses = doses.size
    val takenDoses = doses.count { it.status == IntakeStatus.TAKEN }
    val progress = if (totalDoses > 0) takenDoses.toFloat() / totalDoses else 0f

    // Find next upcoming reminder
    val nextUpcomingDose = doses.firstOrNull { it.status == IntakeStatus.PENDING }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B131E)),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Compact Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("today_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2636)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (totalDoses == 1) "1 dose pianificata per oggi" else "$totalDoses dosi per oggi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                )

                                if (nextUpcomingDose != null) {
                                    Text(
                                        text = "Prossima: ${nextUpcomingDose.timeFormatted} (${nextUpcomingDose.medication.name})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                } else if (totalDoses > 0 && takenDoses == totalDoses) {
                                    Text(
                                        text = "Tutte le dosi di oggi completate ✓",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF34D399),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            if (totalDoses > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0F2033))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$takenDoses/$totalDoses (${(progress * 100).toInt()}%)",
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (totalDoses > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF00B4D8),
                                trackColor = Color(0xFF0F1E2E)
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onViewWeeklySchedule,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .testTag("btn_check_full_schedule"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF38BDF8)
                                )
                            ) {
                                Text("Settimana", fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            }

                            Button(
                                onClick = onViewStatistics,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .testTag("btn_view_statistics"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueryStats,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Statistiche", fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Doses by Time Sections
            if (doses.isEmpty()) {
                item {
                    EmptyDosesCard(onAddMedication = onAddMedicationClick)
                }
            } else {
                // Group doses by time slot
                val groupedByTime = doses.groupBy { it.timeFormatted }

                groupedByTime.forEach { (timeSlot, itemsInSlot) ->
                    item(key = "header_$timeSlot") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp, bottom = 1.dp)
                        ) {
                            Text(
                                text = timeSlot,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Color(0xFF1E293B))
                            )
                        }
                    }

                    items(itemsInSlot, key = { "dose_${it.schedule.id}_${it.dateString}" }) { item ->
                        DoseCard(
                            doseItem = item,
                            onMarkStatus = { status -> onMarkStatus(item, status) },
                            onSnooze = { onSnooze(item) },
                            onTestNotification = { onTestNotification(item) },
                            onEditSchedule = { editingScheduleItem = item }
                        )
                    }
                }
            }
        }

        // Floating Action Button matching screenshot 4 (pink/coral plus icon)
        FloatingActionButton(
            onClick = onAddMedicationClick,
            shape = CircleShape,
            containerColor = Color(0xFFF43F5E), // Vivid pink/coral accent like in the photo
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_medication")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Aggiungi medicinale",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Notification Customization Dialog
    editingScheduleItem?.let { item ->
        NotificationSettingsDialog(
            medicationName = item.medication.name,
            schedule = item.schedule,
            onDismiss = { editingScheduleItem = null },
            onSave = { updatedSchedule ->
                onUpdateSchedule(updatedSchedule)
                editingScheduleItem = null
            },
            onTestNotification = { updatedSchedule ->
                onTestNotification(item.copy(schedule = updatedSchedule))
            }
        )
    }
}

@Composable
private fun EmptyDosesCard(onAddMedication: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2636))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F2033)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.EventAvailable,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nessuna medicina per questa giornata",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Puoi aggiungere nuovi farmaci o pianificare orari personalizzati.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8)),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddMedication,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.testTag("btn_empty_add_med")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aggiungi farmaco")
            }
        }
    }
}
