package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicationWithSchedules
import com.example.data.model.ScheduledDoseItem
import com.example.ui.DayItem
import androidx.compose.material.icons.filled.QueryStats
import java.util.Calendar

@Composable
fun WeeklyScheduleScreen(
    weekDays: List<DayItem>,
    activeMedications: List<MedicationWithSchedules>,
    selectedDayString: String,
    onSelectDay: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onViewStatistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTimePeriod by remember { mutableStateOf("Tutti") }
    val periods = listOf("Tutti", "Mattina", "Pomeriggio", "Sera")

    // Calculate weekly dose count and active medications
    val totalDosesPerWeek = activeMedications.sumOf { med ->
        med.schedules.sumOf { sched ->
            weekDays.count { day -> sched.isScheduledForDate(day.dateString, day.dayOfWeek1To7) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E)),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Programma Settimanale",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Panoramica a colpo d'occhio di tutta la settimana",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousWeek,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("weekly_prev_btn")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onNextWeek,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("weekly_next_btn")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Weekly Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2636)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${activeMedications.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Text("Farmaci attivi", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color(0xFF334155))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalDosesPerWeek",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                        Text("Dosi totali / sett", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color(0xFF334155))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "100%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA78BFA)
                        )
                        Text("Copertura", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onViewStatistics)
                    .testTag("weekly_btn_open_statistics"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142436)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Statistiche e Grafici di Assunzione",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Percentuali settimanali, per farmaco e fasce orarie",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 7-Days selector strip in Weekly view
        item {
            Text(
                text = "Seleziona un giorno:",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    val isDaySelected = day.dateString == selectedDayString
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDaySelected) Color(0xFF0284C7) else Color(0xFF16212E))
                            .clickable { onSelectDay(day.dateString) }
                            .padding(vertical = 5.dp)
                            .testTag("weekly_day_${day.dateString}")
                    ) {
                        Text(
                            text = day.dayNameShort.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDaySelected) Color.White else Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = day.dayNumber.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDaySelected) Color.White else Color(0xFFF1F5F9)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Dose dots indicator
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isDaySelected) Color.White else Color(0xFF00B4D8))
                        )
                    }
                    if (day.dayOfWeek1To7 < 7) {
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }
        }

        // Time Period Filter chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                periods.forEach { period ->
                    FilterChip(
                        selected = selectedTimePeriod == period,
                        onClick = { selectedTimePeriod = period },
                        label = { Text(period, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1B2636),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }

        // Medication Matrix for the Week
        item {
            Text(
                text = "Griglia di assunzione dei farmaci",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        if (activeMedications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2636))
                ) {
                    Text(
                        text = "Nessun farmaco configurato. Aggiungi il tuo primo farmaco!",
                        modifier = Modifier.padding(24.dp),
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(activeMedications, key = { it.medication.id }) { item ->
                WeeklyMedicationRow(
                    item = item,
                    filterPeriod = selectedTimePeriod,
                    weekDays = weekDays
                )
            }
        }
    }
}

@Composable
private fun WeeklyMedicationRow(
    item: MedicationWithSchedules,
    filterPeriod: String,
    weekDays: List<DayItem>
) {
    val dayLabels = listOf("L", "M", "M", "G", "V", "S", "D")

    // Filter schedules by period
    val matchingSchedules = item.schedules.filter { sched ->
        when (filterPeriod) {
            "Mattina" -> sched.timeHour in 5..11
            "Pomeriggio" -> sched.timeHour in 12..17
            "Sera" -> sched.timeHour >= 18 || sched.timeHour < 5
            else -> true
        }
    }

    if (filterPeriod != "Tutti" && matchingSchedules.isEmpty()) {
        return
    }

    val frequencyDesc = matchingSchedules.firstOrNull()?.getFrequencyDescription() ?: "Tutti i giorni"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2636))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.medication.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "${item.medication.dosage} • ${item.medication.type.displayName}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "📅 $frequencyDesc",
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                // Orari badge
                Column(horizontalAlignment = Alignment.End) {
                    matchingSchedules.forEach { s ->
                        Text(
                            text = String.format("%02d:%02d", s.timeHour, s.timeMinute),
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 7 Days Circles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..7).forEach { dayIndex ->
                    val dayItem = weekDays.getOrNull(dayIndex - 1)
                    val isScheduled = matchingSchedules.any { sched ->
                        if (dayItem != null) sched.isScheduledForDate(dayItem.dateString, dayIndex)
                        else sched.isScheduledForDayOfWeek(dayIndex)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = dayLabels[dayIndex - 1],
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isScheduled) Color(0xFF0284C7) else Color(0xFF0F1E2E)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isScheduled) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Text("-", color = Color(0xFF475569), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
