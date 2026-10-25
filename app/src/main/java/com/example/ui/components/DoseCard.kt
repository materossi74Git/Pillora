package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.IntakeStatus
import com.example.data.model.MedicationType
import com.example.data.model.ScheduledDoseItem
import com.example.notification.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DoseCard(
    doseItem: ScheduledDoseItem,
    onMarkStatus: (IntakeStatus) -> Unit,
    onSnooze: () -> Unit,
    onTestNotification: () -> Unit,
    onEditSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val status = doseItem.status
    val isTaken = status == IntakeStatus.TAKEN
    val isSkipped = status == IntakeStatus.SKIPPED
    val isSnoozed = status == IntakeStatus.SNOOZED

    // Default to compact if taken; default to expanded if pending
    var isExpanded by remember(status, doseItem.dateString, doseItem.schedule.id) {
        mutableStateOf(!isTaken)
    }

    // Taken time label
    val takenTimeLabel = remember(doseItem.log?.actionTimestamp) {
        val ts = doseItem.log?.actionTimestamp
        if (ts != null && ts > 0) {
            val formatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
            "Preso alle $formatted"
        } else {
            "Preso"
        }
    }

    // =========================================================
    // 1. COMPACT VIEW (Default for TAKEN, or when collapsed by user)
    // =========================================================
    if (!isExpanded) {
        val compactBgColor = if (isTaken) Color(0xFF0F2820) else Color(0xFF132030)
        val compactBorderColor = if (isTaken) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF334155)

        Card(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, compactBorderColor, RoundedCornerShape(12.dp))
                .clickable { isExpanded = true }
                .testTag("compact_dose_card_${doseItem.medication.name}_${doseItem.timeFormatted}"),
            colors = CardDefaults.cardColors(containerColor = compactBgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status icon circle
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (isTaken) Color(0xFF065F46)
                            else if (isSnoozed) Color(0xFF78350F)
                            else Color(0xFF0284C7).copy(alpha = 0.25f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = if (isTaken) Icons.Default.Check
                    else when (doseItem.medication.type) {
                        MedicationType.GOCCE -> Icons.Default.InvertColors
                        MedicationType.INIEZIONE -> Icons.Default.Vaccines
                        MedicationType.SCIROPPO -> Icons.Default.MedicalServices
                        else -> Icons.Default.Medication
                    }
                    val iconTint = if (isTaken) Color(0xFF34D399)
                    else if (isSnoozed) Color(0xFFFBBF24)
                    else Color(0xFF38BDF8)

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Essential info: Name & Dosage
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = doseItem.medication.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${doseItem.schedule.dosageQuantity})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isTaken) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Status Badge or Quick action
                if (isTaken) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF064E3B))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$takenTimeLabel ✓",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // Quick take button in compact view
                    ElevatedButton(
                        onClick = {
                            NotificationHelper.playPillShakeSound(context)
                            isExpanded = false
                            onMarkStatus(IntakeStatus.TAKEN)
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Prendi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Expand button
                IconButton(
                    onClick = { isExpanded = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Espandi",
                        tint = if (isTaken) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        return
    }

    // =========================================================
    // 2. FULL DETAILED VIEW
    // =========================================================
    val cardBgColor = when {
        isTaken -> Color(0xFF142B24)
        isSkipped -> Color(0xFF232528)
        else -> Color(0xFF1E2836)
    }

    val accentBorderColor = when {
        isTaken -> Color(0xFF10B981)
        isSnoozed -> Color(0xFFF59E0B)
        isSkipped -> Color(0xFF64748B)
        else -> Color(0xFF334155)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, accentBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag("dose_card_${doseItem.medication.name}_${doseItem.timeFormatted}"),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row (Clickable to collapse)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = false }
            ) {
                // Medication Type Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isTaken -> Color(0xFF065F46)
                                doseItem.medication.type == MedicationType.GOCCE -> Color(0xFF0284C7).copy(alpha = 0.2f)
                                doseItem.medication.type == MedicationType.INIEZIONE -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                else -> Color(0xFF00B4D8).copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when {
                        isTaken -> Icons.Default.Check
                        doseItem.medication.type == MedicationType.GOCCE -> Icons.Default.InvertColors
                        doseItem.medication.type == MedicationType.INIEZIONE -> Icons.Default.Vaccines
                        doseItem.medication.type == MedicationType.SCIROPPO -> Icons.Default.MedicalServices
                        else -> Icons.Default.Medication
                    }
                    val iconTint = when {
                        isTaken -> Color(0xFF34D399)
                        doseItem.medication.type == MedicationType.GOCCE -> Color(0xFF38BDF8)
                        doseItem.medication.type == MedicationType.INIEZIONE -> Color(0xFFA78BFA)
                        else -> Color(0xFFFFFFFF)
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = doseItem.medication.type.displayName,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Vertical accent stripe
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                isTaken -> Color(0xFF10B981)
                                isSnoozed -> Color(0xFFF59E0B)
                                else -> Color(0xFF38BDF8)
                            }
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Medication Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doseItem.medication.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    val detailsText = "${doseItem.schedule.dosageQuantity}, ${doseItem.medication.instructions}"
                    Text(
                        text = detailsText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isTaken) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )

                    if (doseItem.schedule.frequencyType != com.example.data.model.FrequencyType.DAILY) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "📅 ${doseItem.schedule.getFrequencyDescription()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Status Badge
                if (isTaken) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF065F46))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$takenTimeLabel ✓",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (isSnoozed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF78350F))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Posticipato",
                            color = Color(0xFFFBBF24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Collapse Arrow Button
                IconButton(
                    onClick = { isExpanded = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Comprimi scheda",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Custom Notification memo if present
            if (doseItem.schedule.customMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F1E2E))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Nota promemoria",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = doseItem.schedule.customMessage,
                        color = Color(0xFFBAE6FD),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isTaken) {
                    ElevatedButton(
                        onClick = {
                            NotificationHelper.playPillShakeSound(context)
                            isExpanded = false
                            onMarkStatus(IntakeStatus.TAKEN)
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("btn_take_${doseItem.medication.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prendi ora", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFBBF24)
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_snooze_${doseItem.medication.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Snooze,
                            contentDescription = "Posticipa",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("15 min", fontSize = 11.sp)
                    }
                } else {
                    // Already taken: action to undo and action to collapse
                    OutlinedButton(
                        onClick = {
                            isExpanded = true
                            onMarkStatus(IntakeStatus.PENDING)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF87171)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Annulla", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { isExpanded = false },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Comprimi",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Comprimi", fontSize = 11.sp)
                    }
                }

                // Test push notification button
                IconButton(
                    onClick = onTestNotification,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A5F))
                        .testTag("btn_test_notif_${doseItem.medication.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Prova notifica",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Edit dose schedule button
                IconButton(
                    onClick = onEditSchedule,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A5F))
                        .testTag("btn_edit_dose_${doseItem.medication.name}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Personalizza dose",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
