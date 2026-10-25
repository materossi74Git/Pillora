package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.MedicationReminderReceiver

object NotificationHelper {

    const val SOUND_PILL_SHAKE = "PILL_SHAKE"
    const val SOUND_DEFAULT = "DEFAULT_NOTIFICATION"
    const val SOUND_ALARM = "ALARM_CLOCK"
    const val SOUND_RINGTONE = "CHIME_RINGTONE"

    const val CHANNEL_PILL_SHAKE = "med_channel_pill_shake"
    const val CHANNEL_DEFAULT = "med_channel_default"
    const val CHANNEL_ALARM = "med_channel_alarm"
    const val CHANNEL_RINGTONE = "med_channel_ringtone"
    const val CHANNEL_SILENT = "med_channel_silent"

    fun getPillBottleSoundUri(context: Context): Uri {
        return Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.pill_bottle_shake}")
    }

    fun playPillShakeSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.pill_bottle_shake)
            mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Gracefully ignore audio hardware errors
        }
    }

    /**
     * Previews the selected sound tone directly in the UI
     */
    fun playSoundPreview(context: Context, soundTone: String) {
        try {
            when (soundTone) {
                SOUND_PILL_SHAKE -> playPillShakeSound(context)
                SOUND_DEFAULT -> {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    ringtone?.play()
                }
                SOUND_ALARM -> {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    ringtone?.play()
                }
                SOUND_RINGTONE -> {
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    ringtone?.play()
                }
                else -> playPillShakeSound(context)
            }
        } catch (e: Exception) {
            playPillShakeSound(context)
        }
    }

    fun getChannelId(soundEnabled: Boolean, soundTone: String): String {
        if (!soundEnabled) return CHANNEL_SILENT
        return when (soundTone) {
            SOUND_DEFAULT -> CHANNEL_DEFAULT
            SOUND_ALARM -> CHANNEL_ALARM
            SOUND_RINGTONE -> CHANNEL_RINGTONE
            else -> CHANNEL_PILL_SHAKE
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val audioAttributesNotification = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val audioAttributesAlarm = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 1. Channel Pill Bottle Shake
            val pillSound = getPillBottleSoundUri(context)
            val channelPill = NotificationChannel(
                CHANNEL_PILL_SHAKE,
                "Promemoria (Flacone pastiglie 💊)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Suono realistico del flacone di pillole scosse"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setSound(pillSound, audioAttributesNotification)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channelPill)

            // 2. Channel Default System Notification
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channelDefault = NotificationChannel(
                CHANNEL_DEFAULT,
                "Promemoria (Campanello standard 🔔)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica con suono standard del telefono"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setSound(defaultSound, audioAttributesNotification)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channelDefault)

            // 3. Channel Alarm Chime
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channelAlarm = NotificationChannel(
                CHANNEL_ALARM,
                "Promemoria (Sveglia medica forte ⏰)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica persistente con tono sveglia"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(alarmSound, audioAttributesAlarm)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channelAlarm)

            // 4. Channel Ringtone
            val ringtoneSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channelRingtone = NotificationChannel(
                CHANNEL_RINGTONE,
                "Promemoria (Suoneria continua 📞)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifica con tono suoneria"
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(ringtoneSound, audioAttributesAlarm)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channelRingtone)

            // 5. Channel Silent
            val channelSilent = NotificationChannel(
                CHANNEL_SILENT,
                "Promemoria (Silenzioso / Solo Vibrazione)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifica senza emissione di suono"
                enableLights(true)
                enableVibration(true)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channelSilent)
        }
    }

    fun showReminderNotification(
        context: Context,
        notificationId: Int,
        medicationId: Long,
        scheduleId: Long,
        medicationName: String,
        dosage: String,
        instructions: String,
        customMemo: String,
        vibrationEnabled: Boolean,
        soundEnabled: Boolean,
        soundTone: String = SOUND_PILL_SHAKE
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure channels are created
        createNotificationChannels(context)

        // Tap content intent -> opens MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NOTIFICATION_MED_ID", medicationId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "Segna come preso"
        val takenIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_MARK_AS_TAKEN
            putExtra("MEDICATION_ID", medicationId)
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("MEDICATION_NAME", medicationName)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "Posticipa 15 min"
        val snoozeIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = MedicationReminderReceiver.ACTION_SNOOZE
            putExtra("MEDICATION_ID", medicationId)
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("MEDICATION_NAME", medicationName)
            putExtra("DOSAGE", dosage)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtext = if (customMemo.isNotBlank()) "Promemoria: $customMemo" else instructions
        val channelId = getChannelId(soundEnabled, soundTone)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("È ora di prendere: $medicationName")
            .setContentText("$dosage • $subtext")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Dose: $dosage\n$instructions" + if (customMemo.isNotBlank()) "\nNota: $customMemo" else "")
                    .setSummaryText("Promemoria Farmaco")
            )
            .setColor(0xFF0284C7.toInt())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✓ Segna come preso",
                takenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_popup_sync,
                "⏱ Posticipa 15 min",
                snoozePendingIntent
            )

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 300, 150, 300))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        if (soundEnabled) {
            when (soundTone) {
                SOUND_DEFAULT -> builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                SOUND_ALARM -> builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                SOUND_RINGTONE -> builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                else -> builder.setSound(getPillBottleSoundUri(context))
            }
        } else {
            builder.setSound(null)
        }

        manager.notify(notificationId, builder.build())
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}
