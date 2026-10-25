package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayAdherence
import com.example.data.model.MedicationType
import com.example.data.model.WeeklyStatisticsState

@Composable
fun StatisticsScreen(
    statisticsState: WeeklyStatisticsState,
    patientName: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetToday: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedDayAdherence by remember { mutableStateOf<DayAdherence?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F2033))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp).testTag("stats_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Indietro",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Statistiche di Assunzione",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = "Aderenza settimanale di $patientName",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )
                )
            }

            // Share / Export report button
            IconButton(
                onClick = {
                    val sb = StringBuilder()
                    sb.append("📊 REPORT ADERENZA TERAPIA FARMACOLOGICA\n")
                    sb.append("Paziente: $patientName\n")
                    sb.append("Periodo: ${statisticsState.weekRangeLabel}\n\n")
                    sb.append("• Aderenza complessiva: ${statisticsState.overallPercentage}%\n")
                    sb.append("• Dosi assunte: ${statisticsState.totalTaken} su ${statisticsState.totalScheduled}\n")
                    sb.append("• Serie continuativa: ${statisticsState.consecutiveStreakDays} giorni\n\n")
                    sb.append("Dettaglio giornaliero:\n")
                    statisticsState.dayAdherences.forEach { da ->
                        sb.append("  ${da.dayItem.dayNameShort.uppercase()} ${da.dayItem.dayNumber}: ${da.percentage}% (${da.takenCount}/${da.scheduledCount} dosi)\n")
                    }
                    sb.append("\nDettaglio per farmaco:\n")
                    statisticsState.medicationAdherences.forEach { ma ->
                        sb.append("  ${ma.medication.name} (${ma.medication.dosage}): ${ma.percentage}% (${ma.takenCount}/${ma.scheduledCount} dosi)\n")
                    }
                    sb.append("\nGenerato con l'app Pillora.")

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                        type = "text/plain"
                    }
                    val chooser = Intent.createChooser(sendIntent, "Condividi report statistiche con il medico")
                    context.startActivity(chooser)
                },
                modifier = Modifier.size(32.dp).testTag("stats_share_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Condividi Report",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Week Navigation Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onPreviousWeek,
                            modifier = Modifier.size(28.dp).testTag("stats_prev_week")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Settimana precedente", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = statisticsState.weekRangeLabel.ifBlank { "Settimana corrente" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            if (statisticsState.weekOffset != 0) {
                                Text(
                                    text = "Torna a oggi",
                                    color = Color(0xFF00B4D8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { onResetToday() }
                                        .padding(top = 1.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onNextWeek,
                            modifier = Modifier.size(28.dp).testTag("stats_next_week")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Settimana successiva", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // 2. Weekly Compliance KPI Card
            item {
                WeeklyOverviewKpiCard(state = statisticsState)
            }

            // 3. Interactive Weekly Bar Chart
            item {
                WeeklyBarChartCard(
                    dayAdherences = statisticsState.dayAdherences,
                    selectedDay = selectedDayAdherence,
                    onSelectDay = { day ->
                        selectedDayAdherence = if (selectedDayAdherence == day) null else day
                    }
                )
            }

            // Selected Day detail panel (if user taps on a bar)
            if (selectedDayAdherence != null) {
                item {
                    val da = selectedDayAdherence!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF142436)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${da.percentage}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dettaglio ${da.dayItem.dayNameShort.uppercase()} ${da.dayItem.dayNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = if (da.scheduledCount == 0) "Nessuna dose prevista per questo giorno"
                                    else "${da.takenCount} di ${da.scheduledCount} dosi assunte regolarmente",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = { selectedDayAdherence = null }) {
                                Text("✕", color = Color(0xFF94A3B8), fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // 4. Per-Medication Adherence Breakdown
            item {
                Text(
                    text = "Aderenza per Farmaco",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (statisticsState.medicationAdherences.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Nessun farmaco attivo programmato per questa settimana.",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(statisticsState.medicationAdherences) { medAdherence ->
                    MedicationAdherenceCard(adherence = medAdherence)
                }
            }

            // 5. Time Slots Analysis
            item {
                Text(
                    text = "Distribuzione per Fascia Oraria",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                TimeSlotsAnalysisCard(timeSlots = statisticsState.timeSlotAdherences)
            }

            // Bottom space
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WeeklyOverviewKpiCard(state: WeeklyStatisticsState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_kpi_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF142436)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge for adherence percentage
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedPercentage by animateFloatAsState(
                        targetValue = state.overallPercentage / 100f,
                        animationSpec = tween(durationMillis = 900),
                        label = "kpi_progress"
                    )

                    val gaugeColor = when {
                        state.overallPercentage >= 85 -> Color(0xFF10B981)
                        state.overallPercentage >= 65 -> Color(0xFF00B4D8)
                        state.overallPercentage >= 40 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 6.dp.toPx()
                        // Track circle
                        drawCircle(
                            color = Color(0xFF0F1E2E),
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress Arc
                        drawArc(
                            color = gaugeColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedPercentage,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.overallPercentage}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Aderenza",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val statusText = when {
                        state.overallPercentage >= 90 -> "Aderenza Eccellente 🌟"
                        state.overallPercentage >= 80 -> "Target Raggiunto ✓"
                        state.overallPercentage >= 60 -> "Aderenza Discreta 👍"
                        else -> "Da migliorare ⚠️"
                    }
                    val statusColor = when {
                        state.overallPercentage >= 80 -> Color(0xFF34D399)
                        state.overallPercentage >= 60 -> Color(0xFF38BDF8)
                        else -> Color(0xFFFBBF24)
                    }

                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${state.totalTaken} dosi su ${state.totalScheduled} previste",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Consecutive streak badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0B1928))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (state.consecutiveStreakDays > 0)
                                "${state.consecutiveStreakDays} gg consecutivi"
                            else "Inizia la tua serie!",
                            color = Color(0xFFFDBA74),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChartCard(
    dayAdherences: List<DayAdherence>,
    selectedDay: DayAdherence?,
    onSelectDay: (DayAdherence) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_bar_chart_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Grafico Assunzioni Giornaliere",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "Percentuale di medicine prese giorno per giorno",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                // Target indicator legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Target 80%",
                        color = Color(0xFF6EE7B7),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                // Background target dotted line at 80%
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y80 = size.height * (1f - 0.80f)
                    drawLine(
                        color = Color(0xFF10B981).copy(alpha = 0.35f),
                        start = Offset(0f, y80),
                        end = Offset(size.width, y80),
                        strokeWidth = 2f
                    )
                }

                // Bars Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dayAdherences.forEach { da ->
                        val isSelected = selectedDay == da
                        val isToday = da.dayItem.isToday

                        DayBarColumn(
                            adherence = da,
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = { onSelectDay(da) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Tocca una colonna per il resoconto del giorno.",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DayBarColumn(
    adherence: DayAdherence,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = adherence.percentage
    val hasDoses = adherence.scheduledCount > 0

    val animatedHeightRatio by animateFloatAsState(
        targetValue = if (hasDoses) (pct / 100f).coerceIn(0.06f, 1f) else 0.04f,
        animationSpec = tween(durationMillis = 800),
        label = "bar_height"
    )

    val barBrush = when {
        !hasDoses -> Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
        pct >= 90 -> Brush.verticalGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
        pct >= 70 -> Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
        pct > 0 -> Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706)))
        else -> Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Percentage label
        Text(
            text = if (hasDoses) "$pct%" else "-",
            color = when {
                !hasDoses -> Color(0xFF64748B)
                pct >= 80 -> Color(0xFF34D399)
                pct > 0 -> Color(0xFFFBBF24)
                else -> Color(0xFFF87171)
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // Bar container
        Box(
            modifier = Modifier
                .width(if (isSelected) 22.dp else 18.dp)
                .height(85.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(Color(0xFF0F1E2E)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = animatedHeightRatio)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                    .background(barBrush)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Day of week name (Lun, Mar, ...)
        Text(
            text = adherence.dayItem.dayNameShort.uppercase(),
            color = if (isToday) Color(0xFF38BDF8) else Color(0xFF94A3B8),
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp
        )

        // Day number in circle
        Box(
            modifier = Modifier
                .size(17.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isToday -> Color(0xFF0284C7)
                        isSelected -> Color(0xFF1E3A5F)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${adherence.dayItem.dayNumber}",
                color = if (isToday) Color.White else Color(0xFFCBD5E1),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 9.sp
            )
        }

        // Dose fraction e.g. "2/2"
        Text(
            text = if (hasDoses) "${adherence.takenCount}/${adherence.scheduledCount}" else "0",
            color = Color(0xFF64748B),
            fontSize = 8.sp,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

@Composable
private fun MedicationAdherenceCard(adherence: com.example.data.model.MedicationAdherence) {
    val med = adherence.medication
    val pct = adherence.percentage

    val progressColor = when {
        pct >= 85 -> Color(0xFF10B981)
        pct >= 65 -> Color(0xFF00B4D8)
        pct >= 40 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("med_adherence_card_${med.name}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Medicine icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (med.type) {
                                MedicationType.GOCCE -> Color(0xFF0284C7).copy(alpha = 0.2f)
                                MedicationType.INIEZIONE -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                MedicationType.SCIROPPO -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else -> Color(0xFF00B4D8).copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (med.type) {
                        MedicationType.GOCCE -> Icons.Default.InvertColors
                        MedicationType.INIEZIONE -> Icons.Default.Vaccines
                        MedicationType.SCIROPPO -> Icons.Default.MedicalServices
                        else -> Icons.Default.Medication
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when (med.type) {
                            MedicationType.GOCCE -> Color(0xFF38BDF8)
                            MedicationType.INIEZIONE -> Color(0xFFA78BFA)
                            MedicationType.SCIROPPO -> Color(0xFFFBBF24)
                            else -> Color.White
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "${med.dosage} • ${med.type.displayName}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                // Percentage Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(progressColor.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$pct%",
                        color = progressColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (pct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = Color(0xFF0F1E2E)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${adherence.takenCount} su ${adherence.scheduledCount} dosi settimanali",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )

                val badgeLabel = when {
                    pct == 100 -> "Regolare al 100% ✓"
                    pct >= 80 -> "Aderenza buona"
                    pct > 0 -> "Qualche dimenticanza"
                    else -> "Nessuna dose presa"
                }
                Text(
                    text = badgeLabel,
                    color = progressColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TimeSlotsAnalysisCard(timeSlots: List<com.example.data.model.TimeSlotAdherence>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("time_slots_analysis_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeSlots.forEach { slot ->
                val slotColor = when {
                    slot.percentage >= 80 -> Color(0xFF10B981)
                    slot.percentage >= 60 -> Color(0xFF0284C7)
                    else -> Color(0xFFF59E0B)
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = if (slot.slotName.startsWith("Mattina")) Icons.Default.WbSunny
                            else if (slot.slotName.startsWith("Pomeriggio")) Icons.Default.WbTwilight
                            else Icons.Default.CalendarMonth

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = slot.slotName,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "${slot.percentage}% (${slot.takenCount}/${slot.scheduledCount})",
                            color = slotColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { (slot.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = slotColor,
                        trackColor = Color(0xFF0F1E2E)
                    )
                }
            }
        }
    }
}
