package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.MedicationApplication
import com.example.data.model.IntakeStatus
import com.example.notification.MedicationAlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MEDICATION_REMINDER = "com.example.ACTION_MEDICATION_REMINDER"
        const val ACTION_MARK_AS_TAKEN = "com.example.ACTION_MARK_AS_TAKEN"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MEDICATION_REMINDER -> {
                val medId = intent.getLongExtra("MEDICATION_ID", 0L)
                val schedId = intent.getLongExtra("SCHEDULE_ID", 0L)
                val medName = intent.getStringExtra("MEDICATION_NAME") ?: "Farmaco"
                val dosage = intent.getStringExtra("DOSAGE") ?: "Dose prescritta"
                val instructions = intent.getStringExtra("INSTRUCTIONS") ?: ""
                val customMemo = intent.getStringExtra("CUSTOM_MEMO") ?: ""
                val vibration = intent.getBooleanExtra("VIBRATION", true)
                val sound = intent.getBooleanExtra("SOUND", true)
                val soundTone = intent.getStringExtra("SOUND_TONE") ?: NotificationHelper.SOUND_PILL_SHAKE
                val notifId = intent.getIntExtra("NOTIFICATION_ID", (medId * 100 + schedId).toInt())

                NotificationHelper.showReminderNotification(
                    context = context,
                    notificationId = notifId,
                    medicationId = medId,
                    scheduleId = schedId,
                    medicationName = medName,
                    dosage = dosage,
                    instructions = instructions,
                    customMemo = customMemo,
                    vibrationEnabled = vibration,
                    soundEnabled = sound,
                    soundTone = soundTone
                )

                // Reschedule for next cycle
                val app = context.applicationContext as? MedicationApplication
                app?.let { application ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val schedule = application.repository.getScheduleById(schedId)
                        val medication = application.repository.getMedicationById(medId)
                        if (schedule != null && medication != null && medication.isActive) {
                            MedicationAlarmScheduler.scheduleDoseReminder(context, schedule, medication)
                        }
                    }
                }
            }

            ACTION_MARK_AS_TAKEN -> {
                val medId = intent.getLongExtra("MEDICATION_ID", 0L)
                val schedId = intent.getLongExtra("SCHEDULE_ID", 0L)
                val notifId = intent.getIntExtra("NOTIFICATION_ID", 0)
                val medName = intent.getStringExtra("MEDICATION_NAME") ?: "Farmaco"

                NotificationHelper.cancelNotification(context, notifId)

                // Update database
                val app = context.applicationContext as? MedicationApplication
                app?.let {
                    val repo = it.repository
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                    CoroutineScope(Dispatchers.IO).launch {
                        repo.markDoseStatus(
                            medicationId = medId,
                            scheduleId = schedId,
                            dateString = todayStr,
                            scheduledTime = timeStr,
                            newStatus = IntakeStatus.TAKEN
                        )
                    }
                }
                Toast.makeText(context, "$medName registrato come assunto! ✓", Toast.LENGTH_SHORT).show()
            }

            ACTION_SNOOZE -> {
                val medId = intent.getLongExtra("MEDICATION_ID", 0L)
                val schedId = intent.getLongExtra("SCHEDULE_ID", 0L)
                val notifId = intent.getIntExtra("NOTIFICATION_ID", 0)
                val medName = intent.getStringExtra("MEDICATION_NAME") ?: "Farmaco"
                val dosage = intent.getStringExtra("DOSAGE") ?: ""

                NotificationHelper.cancelNotification(context, notifId)
                MedicationAlarmScheduler.scheduleSnoozeReminder(
                    context = context,
                    scheduleId = schedId,
                    medicationId = medId,
                    medicationName = medName,
                    dosage = dosage,
                    snoozeMinutes = 15
                )
                Toast.makeText(context, "Promemoria per $medName posticipato di 15 min ⏱", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
