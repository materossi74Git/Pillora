package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.DoseScheduleEntity
import com.example.data.model.MedicationEntity
import com.example.receiver.MedicationReminderReceiver
import java.util.Calendar

object MedicationAlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleDoseReminder(
        context: Context,
        schedule: DoseScheduleEntity,
        medication: MedicationEntity
    ) {
        if (!schedule.customNotificationEnabled) {
            cancelDoseReminder(context, schedule.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_MEDICATION_REMINDER
            putExtra("MEDICATION_ID", medication.id)
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("DOSAGE", schedule.dosageQuantity)
            putExtra("INSTRUCTIONS", medication.instructions)
            putExtra("CUSTOM_MEMO", schedule.customMessage)
            putExtra("VIBRATION", schedule.vibrationEnabled)
            putExtra("SOUND", schedule.soundEnabled)
            putExtra("SOUND_TONE", schedule.soundTone)
            putExtra("NOTIFICATION_ID", schedule.id.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = computeNextTriggerTime(schedule)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for ${medication.name} at epoch $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission missing, fallback scheduled", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelDoseReminder(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_MEDICATION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleSnoozeReminder(
        context: Context,
        scheduleId: Long,
        medicationId: Long,
        medicationName: String,
        dosage: String,
        snoozeMinutes: Int = 15
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_MEDICATION_REMINDER
            putExtra("MEDICATION_ID", medicationId)
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("MEDICATION_NAME", medicationName)
            putExtra("DOSAGE", dosage)
            putExtra("INSTRUCTIONS", "Dose posticipata")
            putExtra("CUSTOM_MEMO", "Hai posticipato questo promemoria di $snoozeMinutes min")
            putExtra("VIBRATION", true)
            putExtra("SOUND", true)
            putExtra("NOTIFICATION_ID", (scheduleId + 99999).toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (scheduleId + 99999).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling snooze", e)
        }
    }

    private fun computeNextTriggerTime(schedule: DoseScheduleEntity): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.timeHour)
            set(Calendar.MINUTE, schedule.timeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // subtract advance notice minutes
            if (schedule.advanceNoticeMinutes > 0) {
                add(Calendar.MINUTE, -schedule.advanceNoticeMinutes)
            }
        }

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val maxDays = when (schedule.frequencyType) {
            com.example.data.model.FrequencyType.INTERVAL_DAYS -> (schedule.intervalDays * 3).coerceAtLeast(90)
            else -> 14
        }

        // Loop forward to find the next day where the schedule is active
        for (i in 0..maxDays) {
            val checkCal = (candidate.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, i)
            }
            if (checkCal.timeInMillis > now.timeInMillis) {
                val dayOfWeek1To7 = when (checkCal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }
                val dateStr = sdf.format(checkCal.time)
                if (schedule.isScheduledForDate(dateStr, dayOfWeek1To7)) {
                    return checkCal.timeInMillis
                }
            }
        }

        // Default to candidate tomorrow if not matched
        candidate.add(Calendar.DAY_OF_YEAR, 1)
        return candidate.timeInMillis
    }
}
