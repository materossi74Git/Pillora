package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class MedicationType(val displayName: String, val iconResName: String) {
    COMPRESSA("Compressa", "pill"),
    CAPSULA("Capsula", "capsule"),
    GOCCE("Gocce", "dropper"),
    INIEZIONE("Iniezione", "syringe"),
    SCIROPPO("Sciroppo", "syrup"),
    INALATORE("Inalatore", "inhaler"),
    BUSTINA("Bustina", "sachet"),
    POMATA("Pomata", "ointment"),
    ALTRO("Altro", "medication")
}

enum class IntakeStatus {
    PENDING,
    TAKEN,
    SNOOZED,
    SKIPPED
}

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val type: MedicationType,
    val instructions: String,
    val colorHex: String = "#0284C7",
    val stockRemaining: Int = 30,
    val stockThresholdAlert: Int = 5,
    val notes: String = "",
    val isActive: Boolean = true,
    val profileId: Long = 1L,
    val profileName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class FrequencyType(val displayName: String) {
    DAILY("Tutti i giorni"),
    DAYS_OF_WEEK("Giorni della settimana"),
    INTERVAL_DAYS("Ogni X giorni (intervallo)")
}

@Entity(
    tableName = "dose_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"])]
)
data class DoseScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val timeHour: Int, // 0..23
    val timeMinute: Int, // 0..59
    val dosageQuantity: String, // e.g. "1 Compressa" or "25 mg"
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val daysOfWeekMask: Int = 127, // Bitmask: Lun=1, Mar=2, Mer=4, Gio=8, Ven=16, Sab=32, Dom=64 (127 = every day)
    val intervalDays: Int = 1, // e.g. 28 for "ogni 28 giorni" (puntura/iniezione), 15 for 15 gg, 2 for a giorni alterni
    val startDateString: String = "", // Format "YYYY-MM-DD" anchor date for interval calculation. Empty = today
    val customNotificationEnabled: Boolean = true,
    val advanceNoticeMinutes: Int = 0, // 0 = exact time, 5, 10, 15, 30
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val soundTone: String = "PILL_SHAKE", // "PILL_SHAKE", "DEFAULT_NOTIFICATION", "ALARM_CLOCK", "CHIME_RINGTONE"
    val customMessage: String = "", // custom memo per dose
    val snoozeMinutes: Int = 15
) {
    fun isScheduledForDayOfWeek(dayOfWeekIndex1To7: Int): Boolean {
        // 1=Monday (1<<0 = 1), 2=Tuesday (1<<1 = 2)... 7=Sunday (1<<6 = 64)
        val bit = 1 shl (dayOfWeekIndex1To7 - 1)
        return (daysOfWeekMask and bit) != 0
    }

    fun isScheduledForDate(dateString: String, dayOfWeekIndex1To7: Int): Boolean {
        return when (frequencyType) {
            FrequencyType.DAILY -> true
            FrequencyType.DAYS_OF_WEEK -> isScheduledForDayOfWeek(dayOfWeekIndex1To7)
            FrequencyType.INTERVAL_DAYS -> {
                if (intervalDays <= 1) return true
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(java.util.Date())
                val anchor = if (startDateString.isNotBlank()) startDateString else todayStr
                try {
                    val anchorDate = sdf.parse(anchor) ?: return false
                    val targetDate = sdf.parse(dateString) ?: return false

                    val calAnchor = Calendar.getInstance().apply {
                        time = anchorDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val calTarget = Calendar.getInstance().apply {
                        time = targetDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val diffMillis = calTarget.timeInMillis - calAnchor.timeInMillis
                    val diffDays = Math.round(diffMillis.toDouble() / (24.0 * 3600 * 1000)).toInt()
                    diffDays >= 0 && (diffDays % intervalDays == 0)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    fun getFrequencyDescription(): String {
        return when (frequencyType) {
            FrequencyType.DAILY -> "Tutti i giorni"
            FrequencyType.DAYS_OF_WEEK -> {
                val dayNames = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
                val activeDays = (1..7).filter { isScheduledForDayOfWeek(it) }.map { dayNames[it - 1] }
                if (activeDays.size == 7) "Tutti i giorni"
                else if (activeDays.isEmpty()) "Nessun giorno"
                else activeDays.joinToString(", ")
            }
            FrequencyType.INTERVAL_DAYS -> {
                when (intervalDays) {
                    1 -> "Tutti i giorni"
                    2 -> "A giorni alterni (ogni 2 gg)"
                    28 -> "Ogni 28 giorni (puntura/iniezione)" + if (startDateString.isNotBlank()) " (dal $startDateString)" else ""
                    else -> "Ogni $intervalDays giorni" + if (startDateString.isNotBlank()) " (dal $startDateString)" else ""
                }
            }
        }
    }
}

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medicationId"]),
        Index(value = ["scheduleId", "dateString"], unique = true)
    ]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val dateString: String, // Format "YYYY-MM-DD"
    val scheduledTime: String, // "07:00"
    val status: IntakeStatus = IntakeStatus.PENDING,
    val actionTimestamp: Long? = null,
    val notes: String? = null
)

data class MedicationWithSchedules(
    @Embedded val medication: MedicationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val schedules: List<DoseScheduleEntity>
)

data class ScheduledDoseItem(
    val medication: MedicationEntity,
    val schedule: DoseScheduleEntity,
    val log: DoseLogEntity?,
    val dateString: String
) {
    val status: IntakeStatus
        get() = log?.status ?: IntakeStatus.PENDING

    val timeFormatted: String
        get() = String.format("%02d:%02d", schedule.timeHour, schedule.timeMinute)
}

@Entity(tableName = "health_measurements")
data class HealthMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "PRESSIONE", "GLICEMIA", "BATTITI", "PESO", "TEMPERATURA"
    val valuePrimary: Float,
    val valueSecondary: Float? = null,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val doctorOrSpecialty: String,
    val timestamp: Long,
    val location: String = "",
    val notes: String = ""
)

@Entity(tableName = "doctor_contacts")
data class DoctorContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val specialty: String,
    val phone: String,
    val email: String = "",
    val notes: String = ""
)

@Entity(tableName = "diary_notes")
data class DiaryNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,
    val title: String,
    val symptoms: String,
    val mood: String, // "Ottimo", "Buono", "Così così", "Male"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
