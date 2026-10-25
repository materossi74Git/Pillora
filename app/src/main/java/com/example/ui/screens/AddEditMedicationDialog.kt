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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.data.model.FrequencyType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseScheduleEntity
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationType
import com.example.data.model.MedicationWithSchedules

import com.example.data.model.UserProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationDialog(
    initialMedication: MedicationWithSchedules?,
    profiles: List<UserProfileEntity> = emptyList(),
    defaultProfileId: Long = 0L,
    onDismiss: () -> Unit,
    onSave: (MedicationEntity, List<DoseScheduleEntity>) -> Unit
) {
    val existingMed = initialMedication?.medication

    var name by remember { mutableStateOf(existingMed?.name ?: "") }
    var dosage by remember { mutableStateOf(existingMed?.dosage ?: "") }
    var selectedType by remember { mutableStateOf(existingMed?.type ?: MedicationType.COMPRESSA) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // Profile selection for medication
    val initialProfileId = existingMed?.profileId ?: (if (defaultProfileId > 0) defaultProfileId else profiles.firstOrNull()?.id ?: 1L)
    var selectedProfileId by remember { mutableStateOf(initialProfileId) }
    var profileDropdownExpanded by remember { mutableStateOf(false) }

    var instructions by remember { mutableStateOf(existingMed?.instructions ?: "Prendi con acqua a stomaco pieno") }
    var stockRemaining by remember { mutableStateOf(existingMed?.stockRemaining?.toString() ?: "30") }
    var stockThreshold by remember { mutableStateOf(existingMed?.stockThresholdAlert?.toString() ?: "5") }
    var notes by remember { mutableStateOf(existingMed?.notes ?: "") }

    // Schedules list
    var schedules by remember {
        mutableStateOf(
            if (initialMedication != null && initialMedication.schedules.isNotEmpty()) {
                initialMedication.schedules
            } else {
                listOf(
                    DoseScheduleEntity(
                        medicationId = 0,
                        timeHour = 8,
                        timeMinute = 0,
                        dosageQuantity = "1 dose",
                        daysOfWeekMask = 127,
                        customNotificationEnabled = true,
                        advanceNoticeMinutes = 0,
                        vibrationEnabled = true,
                        soundEnabled = true,
                        customMessage = ""
                    )
                )
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingMed == null) "Nuovo Farmaco" else "Modifica Farmaco",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile selector (if multiple profiles exist)
                if (profiles.isNotEmpty()) {
                    val currentProf = profiles.firstOrNull { it.id == selectedProfileId } ?: profiles.first()
                    ExposedDropdownMenuBox(
                        expanded = profileDropdownExpanded,
                        onExpandedChange = { profileDropdownExpanded = !profileDropdownExpanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = "${currentProf.name} (${currentProf.relationship})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assegna al profilo / utente") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = profileDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = profileDropdownExpanded,
                            onDismissRequest = { profileDropdownExpanded = false }
                        ) {
                            profiles.forEach { prof ->
                                DropdownMenuItem(
                                    text = { Text("${prof.name} (${prof.relationship})") },
                                    onClick = {
                                        selectedProfileId = prof.id
                                        profileDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome del farmaco *") },
                    placeholder = { Text("es. Exemestane, Oki, Augmentin...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dosage & Type Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosaggio") },
                        placeholder = { Text("es. 25 mg") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("med_dosage_input"),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Forma") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            MedicationType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        selectedType = type
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Instructions
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Istruzioni d'assunzione") },
                    placeholder = { Text("es. Prendi 1 compressa a stomaco pieno dopo colazione") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scorte e ricariche
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockRemaining,
                        onValueChange = { stockRemaining = it },
                        label = { Text("Scorta residua") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stockThreshold,
                        onValueChange = { stockThreshold = it },
                        label = { Text("Avviso a n.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION: Orari e Notifiche
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Orari & Promemoria Dosi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(
                        onClick = {
                            schedules = schedules + DoseScheduleEntity(
                                medicationId = existingMed?.id ?: 0,
                                timeHour = 12,
                                timeMinute = 0,
                                dosageQuantity = "1 dose",
                                daysOfWeekMask = 127,
                                customNotificationEnabled = true,
                                advanceNoticeMinutes = 0,
                                customMessage = ""
                            )
                        },
                        modifier = Modifier.testTag("btn_add_dose_time")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi orario", tint = Color(0xFF0284C7))
                    }
                }

                schedules.forEachIndexed { index, schedule ->
                    DoseScheduleEditor(
                        index = index,
                        schedule = schedule,
                        onUpdate = { updated ->
                            schedules = schedules.toMutableList().also { it[index] = updated }
                        },
                        onDelete = {
                            if (schedules.size > 1) {
                                schedules = schedules.toMutableList().also { it.removeAt(index) }
                            }
                        },
                        canDelete = schedules.size > 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val chosenProf = profiles.firstOrNull { it.id == selectedProfileId }
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val cleanedSchedules = schedules.map { s ->
                            if (s.frequencyType == FrequencyType.INTERVAL_DAYS && s.startDateString.isBlank()) {
                                s.copy(startDateString = todayStr)
                            } else {
                                s
                            }
                        }
                        val med = MedicationEntity(
                            id = existingMed?.id ?: 0,
                            name = name.trim(),
                            dosage = dosage.trim(),
                            type = selectedType,
                            instructions = instructions.trim(),
                            stockRemaining = stockRemaining.toIntOrNull() ?: 30,
                            stockThresholdAlert = stockThreshold.toIntOrNull() ?: 5,
                            notes = notes.trim(),
                            profileId = selectedProfileId,
                            profileName = chosenProf?.name ?: ""
                        )
                        onSave(med, cleanedSchedules)
                    }
                },
                modifier = Modifier.testTag("btn_save_medication"),
                enabled = name.isNotBlank()
            ) {
                Text("Salva farmaco")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun DoseScheduleEditor(
    index: Int,
    schedule: DoseScheduleEntity,
    onUpdate: (DoseScheduleEntity) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val context = androidx.compose.ui.platform.LocalContext.current

    var hour by remember { mutableIntStateOf(schedule.timeHour) }
    var minute by remember { mutableIntStateOf(schedule.timeMinute) }
    var showTimePicker by remember { mutableStateOf(false) }
    var notifEnabled by remember { mutableStateOf(schedule.customNotificationEnabled) }
    var customMemo by remember { mutableStateOf(schedule.customMessage) }
    var dosageQty by remember { mutableStateOf(schedule.dosageQuantity) }
    var soundTone by remember { mutableStateOf(schedule.soundTone) }

    var freqType by remember { mutableStateOf(schedule.frequencyType) }
    var intervalText by remember { mutableStateOf(if (schedule.intervalDays > 1) schedule.intervalDays.toString() else "15") }
    var startDateText by remember { mutableStateOf(if (schedule.startDateString.isNotBlank()) schedule.startDateString else todayDateStr) }
    var daysMask by remember { mutableIntStateOf(schedule.daysOfWeekMask) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Time + Posologia + Switch + Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time Button (e.g. 08:00)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F2033))
                        .clickable { showTimePicker = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("%02d:%02d", hour, minute),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Dose quantity field inline
                OutlinedTextField(
                    value = dosageQty,
                    onValueChange = {
                        dosageQty = it
                        onUpdate(schedule.copy(dosageQuantity = it))
                    },
                    label = { Text("Posologia dose") },
                    placeholder = { Text("es. 1 dose, 25 mg") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    singleLine = true
                )

                // Notification toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (notifEnabled) Color(0xFF0284C7) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = notifEnabled,
                        onCheckedChange = {
                            notifEnabled = it
                            onUpdate(schedule.copy(customNotificationEnabled = it))
                        }
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Elimina orario",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SECTION: FREQUENZA DI ASSUNZIONE
            Text(
                text = "Cadenza / Frequenza di assunzione:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Frequency mode selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Chip 1: Tutti i giorni
                FilterChip(
                    selected = freqType == FrequencyType.DAILY,
                    onClick = {
                        freqType = FrequencyType.DAILY
                        onUpdate(
                            schedule.copy(
                                frequencyType = FrequencyType.DAILY,
                                intervalDays = 1
                            )
                        )
                    },
                    label = { Text("Tutti i giorni", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F2033),
                        labelColor = Color(0xFF94A3B8)
                    )
                )

                // Chip 2: Ogni X giorni (Intervallo personalizzato)
                FilterChip(
                    selected = freqType == FrequencyType.INTERVAL_DAYS,
                    onClick = {
                        freqType = FrequencyType.INTERVAL_DAYS
                        val days = intervalText.toIntOrNull() ?: 15
                        val sDate = if (startDateText.isNotBlank()) startDateText else todayDateStr
                        onUpdate(
                            schedule.copy(
                                frequencyType = FrequencyType.INTERVAL_DAYS,
                                intervalDays = days,
                                startDateString = sDate
                            )
                        )
                    },
                    label = { Text("Ogni X giorni (Intervallo)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F2033),
                        labelColor = Color(0xFF94A3B8)
                    )
                )

                // Chip 3: Giorni specifici
                FilterChip(
                    selected = freqType == FrequencyType.DAYS_OF_WEEK,
                    onClick = {
                        freqType = FrequencyType.DAYS_OF_WEEK
                        onUpdate(
                            schedule.copy(
                                frequencyType = FrequencyType.DAYS_OF_WEEK,
                                daysOfWeekMask = daysMask
                            )
                        )
                    },
                    label = { Text("Giorni sett.", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F2033),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }

            // Sub-configuration depending on frequency type
            when (freqType) {
                FrequencyType.DAILY -> {
                    // Daily info box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2033))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "✓ Promemoria programmato tutti i giorni alle ${String.format("%02d:%02d", hour, minute)}",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp
                        )
                    }
                }

                FrequencyType.INTERVAL_DAYS -> {
                    // Interval configuration
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A1929))
                            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Intervallo tra le assunzioni:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick interval preset chips (e.g. 2 gg alterni, 7 gg, 14 gg, 28 gg puntura, 30 gg)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                2 to "A giorni alterni",
                                7 to "7 gg",
                                14 to "14 gg",
                                28 to "28 gg (Puntura 💉)",
                                30 to "30 gg"
                            ).forEach { (presetDays, label) ->
                                val isSelected = intervalText == presetDays.toString()
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B))
                                        .clickable {
                                            intervalText = presetDays.toString()
                                            val sDate = if (startDateText.isNotBlank()) startDateText else todayDateStr
                                            onUpdate(
                                                schedule.copy(
                                                    frequencyType = FrequencyType.INTERVAL_DAYS,
                                                    intervalDays = presetDays,
                                                    startDateString = sDate
                                                )
                                            )
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom interval days input & anchor start date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = intervalText,
                                onValueChange = {
                                    val filtered = it.filter { char -> char.isDigit() }
                                    intervalText = filtered
                                    val parsed = filtered.toIntOrNull() ?: 1
                                    val sDate = if (startDateText.isNotBlank()) startDateText else todayDateStr
                                    onUpdate(
                                        schedule.copy(
                                            frequencyType = FrequencyType.INTERVAL_DAYS,
                                            intervalDays = parsed.coerceAtLeast(1),
                                            startDateString = sDate
                                        )
                                    )
                                },
                                label = { Text("Ogni quanti giorni?") },
                                placeholder = { Text("es. 15") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = startDateText,
                                onValueChange = {
                                    startDateText = it
                                    val days = intervalText.toIntOrNull() ?: 15
                                    onUpdate(
                                        schedule.copy(
                                            frequencyType = FrequencyType.INTERVAL_DAYS,
                                            intervalDays = days,
                                            startDateString = it
                                        )
                                    )
                                },
                                label = { Text("Data prima assunzione") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.weight(1.3f),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            startDateText = todayDateStr
                                            val days = intervalText.toIntOrNull() ?: 15
                                            onUpdate(
                                                schedule.copy(
                                                    frequencyType = FrequencyType.INTERVAL_DAYS,
                                                    intervalDays = days,
                                                    startDateString = todayDateStr
                                                )
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = "Oggi",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val daysVal = intervalText.toIntOrNull() ?: 15
                        Text(
                            text = "🗓 Assunzione programmata ogni $daysVal giorni a partire dal $startDateText alle ${String.format("%02d:%02d", hour, minute)}",
                            fontSize = 11.sp,
                            color = Color(0xFF34D399)
                        )
                    }
                }

                FrequencyType.DAYS_OF_WEEK -> {
                    // Day of week selection circles (Lun - Dom)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A1929))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Seleziona i giorni della settimana:",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val dayNames = listOf("L", "M", "M", "G", "V", "S", "D")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            (1..7).forEach { dayNum ->
                                val isDaySelected = (daysMask and (1 shl (dayNum - 1))) != 0
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isDaySelected) Color(0xFF0284C7) else Color(0xFF1E293B))
                                        .clickable {
                                            val newMask = if (isDaySelected) {
                                                daysMask and (1 shl (dayNum - 1)).inv()
                                            } else {
                                                daysMask or (1 shl (dayNum - 1))
                                            }
                                            daysMask = newMask
                                            onUpdate(
                                                schedule.copy(
                                                    frequencyType = FrequencyType.DAYS_OF_WEEK,
                                                    daysOfWeekMask = newMask
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNames[dayNum - 1],
                                        color = if (isDaySelected) Color.White else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Memo per dose
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = customMemo,
                onValueChange = {
                    customMemo = it
                    onUpdate(schedule.copy(customMessage = it))
                },
                placeholder = { Text("Nota o promemoria dose (es. 'A stomaco pieno')") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Suono del promemoria per questa dose
            if (notifEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Suono notifica promemoria:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val tones = listOf(
                    Triple(com.example.notification.NotificationHelper.SOUND_PILL_SHAKE, "Pastiglie 💊", "Flacone pillole"),
                    Triple(com.example.notification.NotificationHelper.SOUND_DEFAULT, "Campanello 🔔", "Notifica standard"),
                    Triple(com.example.notification.NotificationHelper.SOUND_ALARM, "Sveglia ⏰", "Allarme forte"),
                    Triple(com.example.notification.NotificationHelper.SOUND_RINGTONE, "Suoneria 📞", "Chiamata continua")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tones.forEach { (tId, tLabel, _) ->
                        val isSelected = soundTone == tId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF0F2033))
                                .clickable {
                                    soundTone = tId
                                    onUpdate(schedule.copy(soundTone = tId))
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tLabel,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { com.example.notification.NotificationHelper.playSoundPreview(context, soundTone) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ascolta suono selezionato", fontSize = 11.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        SimpleTimePickerModal(
            currentHour = hour,
            currentMinute = minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                hour = h
                minute = m
                onUpdate(schedule.copy(timeHour = h, timeMinute = m))
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun SimpleTimePickerModal(
    currentHour: Int,
    currentMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(currentHour) }
    var minute by remember { mutableIntStateOf(currentMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Imposta orario dose", fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Hour buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ora (0-23)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { hour = if (hour > 0) hour - 1 else 23 }) { Text("-", fontSize = 20.sp) }
                        Text(String.format("%02d", hour), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { hour = (hour + 1) % 24 }) { Text("+", fontSize = 20.sp) }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                Text(":", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(16.dp))

                // Quick Minute buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Minuti (0-59)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { minute = if (minute >= 5) minute - 5 else 55 }) { Text("-5", fontSize = 16.sp) }
                        Text(String.format("%02d", minute), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { minute = (minute + 5) % 60 }) { Text("+5", fontSize = 16.sp) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(hour, minute) }) {
                Text("Imposta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
