package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DoseScheduleEntity
import com.example.notification.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsDialog(
    medicationName: String,
    schedule: DoseScheduleEntity,
    onDismiss: () -> Unit,
    onSave: (DoseScheduleEntity) -> Unit,
    onTestNotification: (DoseScheduleEntity) -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(schedule.customNotificationEnabled) }
    var advanceMinutes by remember { mutableIntStateOf(schedule.advanceNoticeMinutes) }
    var vibration by remember { mutableStateOf(schedule.vibrationEnabled) }
    var sound by remember { mutableStateOf(schedule.soundEnabled) }
    var soundTone by remember { mutableStateOf(schedule.soundTone) }
    var customMessage by remember { mutableStateOf(schedule.customMessage) }
    var snoozeMinutes by remember { mutableIntStateOf(schedule.snoozeMinutes) }

    val advanceOptions = listOf(
        0 to "All'orario esatto",
        5 to "5 minuti prima",
        10 to "10 minuti prima",
        15 to "15 minuti prima",
        30 to "30 minuti prima"
    )
    var advanceDropdownExpanded by remember { mutableStateOf(false) }

    val snoozeOptions = listOf(
        5 to "5 minuti",
        10 to "10 minuti",
        15 to "15 minuti",
        30 to "30 minuti"
    )
    var snoozeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true, dismissOnBackPress = true),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifiche per $medicationName",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        maxLines = 1
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Personalizza l'avviso per la dose delle ${String.format("%02d:%02d", schedule.timeHour, schedule.timeMinute)}:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Notifica
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Promemoria attivo", fontWeight = FontWeight.SemiBold)
                        Text("Ricevi notifiche push per questa dose", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        modifier = Modifier.testTag("switch_notif_enabled")
                    )
                }

                if (enabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Anticipo promemoria Dropdown
                    Text("Anticipo notifica", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = advanceDropdownExpanded,
                        onExpandedChange = { advanceDropdownExpanded = !advanceDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = advanceOptions.firstOrNull { it.first == advanceMinutes }?.second ?: "All'orario esatto",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = advanceDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = advanceDropdownExpanded,
                            onDismissRequest = { advanceDropdownExpanded = false }
                        ) {
                            advanceOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.second) },
                                    onClick = {
                                        advanceMinutes = opt.first
                                        advanceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle Suono
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Suono notifica", fontSize = 14.sp)
                        }
                        Switch(
                            checked = sound,
                            onCheckedChange = { sound = it }
                        )
                    }

                    if (sound) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scegli il suono per questo farmaco:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val tones = listOf(
                            Triple(NotificationHelper.SOUND_PILL_SHAKE, "Pastiglie scosse", "💊 Flacone pillole"),
                            Triple(NotificationHelper.SOUND_DEFAULT, "Campanello standard", "🔔 Notifica di sistema"),
                            Triple(NotificationHelper.SOUND_ALARM, "Sveglia medica", "⏰ Allarme forte"),
                            Triple(NotificationHelper.SOUND_RINGTONE, "Suoneria continua", "📞 Tono chiamata")
                        )

                        tones.forEach { (toneId, toneName, toneSub) ->
                            val isSelected = soundTone == toneId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF0F2C47) else Color(0xFF0F2033))
                                    .clickable { soundTone = toneId }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = toneName,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(toneSub, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }

                                TextButton(
                                    onClick = { NotificationHelper.playSoundPreview(context, toneId) },
                                    modifier = Modifier.testTag("btn_preview_sound_$toneId")
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF38BDF8))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ascolta", fontSize = 12.sp, color = Color(0xFF38BDF8))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle Vibrazione
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vibrazione", fontSize = 14.sp)
                        }
                        Switch(
                            checked = vibration,
                            onCheckedChange = { vibration = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Intervallo Snooze Dropdown
                    Text("Intervallo Posticipa (Snooze)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = snoozeDropdownExpanded,
                        onExpandedChange = { snoozeDropdownExpanded = !snoozeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = snoozeOptions.firstOrNull { it.first == snoozeMinutes }?.second ?: "15 minuti",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = snoozeDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = snoozeDropdownExpanded,
                            onDismissRequest = { snoozeDropdownExpanded = false }
                        ) {
                            snoozeOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.second) },
                                    onClick = {
                                        snoozeMinutes = opt.first
                                        snoozeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Message Memo
                    Text("Messaggio / Memo personalizzato", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        placeholder = { Text("es. Prendere a stomaco pieno con acqua abbondante") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_message_input"),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Immediate test notification button
                    OutlinedButton(
                        onClick = {
                            val currentEditedSchedule = schedule.copy(
                                customNotificationEnabled = enabled,
                                advanceNoticeMinutes = advanceMinutes,
                                vibrationEnabled = vibration,
                                soundEnabled = sound,
                                soundTone = soundTone,
                                customMessage = customMessage,
                                snoozeMinutes = snoozeMinutes
                            )
                            onTestNotification(currentEditedSchedule)
                            Toast.makeText(context, "Notifica di prova inviata! 🔔", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_notif_dialog"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Invia notifica di prova adesso")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = schedule.copy(
                        customNotificationEnabled = enabled,
                        advanceNoticeMinutes = advanceMinutes,
                        vibrationEnabled = vibration,
                        soundEnabled = sound,
                        soundTone = soundTone,
                        customMessage = customMessage,
                        snoozeMinutes = snoozeMinutes
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("btn_save_notif_settings")
            ) {
                Text("Salva impostazioni")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
