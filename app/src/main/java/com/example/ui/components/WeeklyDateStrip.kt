package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DayItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeeklyDateStrip(
    weekDays: List<DayItem>,
    selectedCalendar: Calendar,
    onDaySelected: (String) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetToday: () -> Unit,
    isCurrentWeek: Boolean,
    modifier: Modifier = Modifier
) {
    // Format subheader: e.g. "Oggi, 8 ott" or "Giovedì, 8 ott 2026"
    val todayCal = Calendar.getInstance()
    val isSelectedToday = todayCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
            todayCal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)

    val dayNameFull = SimpleDateFormat("EEEE", Locale.ITALIAN).format(selectedCalendar.time)
        .replaceFirstChar { it.uppercase() }
    val dayNum = selectedCalendar.get(Calendar.DAY_OF_MONTH)
    val monthShort = SimpleDateFormat("MMM", Locale.ITALIAN).format(selectedCalendar.time)

    val dateSubheader = if (isSelectedToday) {
        "Oggi, $dayNum $monthShort"
    } else {
        "$dayNameFull, $dayNum $monthShort"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F2033))
            .padding(bottom = 2.dp)
    ) {
        // Week days row with left & right navigation arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPreviousWeek,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("prev_week_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Settimana precedente",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }

            // 7 Days
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { day ->
                    DayCell(
                        day = day,
                        onClick = { onDaySelected(day.dateString) }
                    )
                }
            }

            IconButton(
                onClick = onNextWeek,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("next_week_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Settimana successiva",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Subheader showing date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateSubheader,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp
                )
            )

            if (!isSelectedToday || !isCurrentWeek) {
                Text(
                    text = "Oggi",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0C3860))
                        .clickable { onResetToday() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("reset_today_btn")
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: DayItem,
    onClick: () -> Unit
) {
    val dayLabel = when (day.dayOfWeek1To7) {
        1 -> "LU"
        2 -> "MA"
        3 -> "ME"
        4 -> "GI"
        5 -> "VE"
        6 -> "SA"
        7 -> "DO"
        else -> day.dayNameShort.take(2).uppercase()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 1.dp, vertical = 1.dp)
            .testTag("day_cell_${day.dateString}")
    ) {
        // "LU", "MA", etc.
        Text(
            text = dayLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (day.isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )

        Spacer(modifier = Modifier.height(1.dp))

        // Day Number with Circular indicator if selected
        Box(
            modifier = Modifier
                .size(24.dp)
                .then(
                    if (day.isSelected) {
                        Modifier.background(Color(0xFF00B4D8), CircleShape)
                    } else if (day.isToday) {
                        Modifier.background(Color(0xFF1E3A5F), CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayNumber.toString(),
                fontSize = 12.sp,
                fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (day.isSelected) Color.White else if (day.isToday) Color(0xFF38BDF8) else Color(0xFFE2E8F0),
                textAlign = TextAlign.Center
            )
        }
    }
}
