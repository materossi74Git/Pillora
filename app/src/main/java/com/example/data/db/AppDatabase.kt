package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppointmentEntity
import com.example.data.model.DiaryNoteEntity
import com.example.data.model.DoctorContactEntity
import com.example.data.model.DoseLogEntity
import com.example.data.model.DoseScheduleEntity
import com.example.data.model.FrequencyType
import com.example.data.model.HealthMeasurementEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        MedicationEntity::class,
        DoseScheduleEntity::class,
        DoseLogEntity::class,
        HealthMeasurementEntity::class,
        AppointmentEntity::class,
        DoctorContactEntity::class,
        DiaryNoteEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun healthDao(): HealthDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medication_reminder_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val profileDao = db.profileDao()
            val medDao = db.medicationDao()
            val healthDao = db.healthDao()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val defaultProfileId = profileDao.insertProfile(
                UserProfileEntity(
                    name = "Mio Profilo",
                    relationship = "Personale",
                    avatarColorHex = "#0284C7"
                )
            )

            // 1. Exemestane (from user screenshot)
            val med1Id = medDao.insertMedication(
                MedicationEntity(
                    profileId = defaultProfileId,
                    name = "Exemestane",
                    dosage = "25 mg",
                    type = MedicationType.COMPRESSA,
                    instructions = "Prendi 1 Compressa/e preferibilmente dopo colazione",
                    colorHex = "#0284C7",
                    stockRemaining = 28,
                    stockThresholdAlert = 5,
                    notes = "Trattamento ormonale adiuvante"
                )
            )
            val sched1Id = medDao.insertSchedule(
                DoseScheduleEntity(
                    medicationId = med1Id,
                    timeHour = 7,
                    timeMinute = 0,
                    dosageQuantity = "1 Compressa (25 mg)",
                    daysOfWeekMask = 127,
                    customNotificationEnabled = true,
                    advanceNoticeMinutes = 0,
                    customMessage = "Da prendere con un bicchiere d'acqua"
                )
            )

            // 2. Calcium Phosphate Dibasic (from user screenshot)
            val med2Id = medDao.insertMedication(
                MedicationEntity(
                    name = "Calcium Phosphate Dibasic",
                    dosage = "500 mg",
                    type = MedicationType.GOCCE,
                    instructions = "Prendi con acqua durante i pasti",
                    colorHex = "#06B6D4",
                    stockRemaining = 35,
                    stockThresholdAlert = 7,
                    notes = "Integrazione di calcio e fosforo"
                )
            )
            val sched2Id = medDao.insertSchedule(
                DoseScheduleEntity(
                    medicationId = med2Id,
                    timeHour = 8,
                    timeMinute = 0,
                    dosageQuantity = "1 dose / 500 mg",
                    daysOfWeekMask = 127,
                    customNotificationEnabled = true,
                    advanceNoticeMinutes = 5,
                    customMessage = "Non assumere a stomaco vuoto"
                )
            )

            // 3. Decapeptyl (from user screenshot)
            val med3Id = medDao.insertMedication(
                MedicationEntity(
                    name = "Decapeptyl",
                    dosage = "3.75 mg",
                    type = MedicationType.INIEZIONE,
                    instructions = "Iniezione sottocutanea/intramuscolare programmata",
                    colorHex = "#8B5CF6",
                    stockRemaining = 3,
                    stockThresholdAlert = 1,
                    notes = "Somministrazione periodica"
                )
            )
            val sched3Id = medDao.insertSchedule(
                DoseScheduleEntity(
                    medicationId = med3Id,
                    timeHour = 8,
                    timeMinute = 0,
                    dosageQuantity = "1 fiala (3.75 mg)",
                    frequencyType = FrequencyType.INTERVAL_DAYS,
                    intervalDays = 15,
                    startDateString = todayStr,
                    customNotificationEnabled = true,
                    advanceNoticeMinutes = 15,
                    customMessage = "Preparare il sito di iniezione"
                )
            )

            // Seed doctors
            healthDao.insertDoctor(
                DoctorContactEntity(
                    name = "Dott.ssa Bianchi",
                    specialty = "Oncologia Medica",
                    phone = "+39 030 39951",
                    email = "bianchi.oncologia@ospedale.it",
                    notes = "Riceve il martedì mattina su appuntamento"
                )
            )
            healthDao.insertDoctor(
                DoctorContactEntity(
                    name = "Dott. Marco Rossi",
                    specialty = "Medico di Medicina Generale",
                    phone = "+39 030 248911",
                    email = "dott.rossi@medicinagenerale.it",
                    notes = "Ambulatorio Lun-Ven ore 9:00 - 12:30"
                )
            )

            // Seed sample appointment
            val nextWeek = System.currentTimeMillis() + 86400000L * 7
            healthDao.insertAppointment(
                AppointmentEntity(
                    title = "Controllo Oncologico Periodico",
                    doctorOrSpecialty = "Dott.ssa Bianchi (Oncologia)",
                    timestamp = nextWeek,
                    location = "Spedali Civili di Brescia, Ambulatorio 3",
                    notes = "Portare esami del sangue recenti e diario delle assunzioni"
                )
            )

            // Seed sample health measurements
            healthDao.insertMeasurement(
                HealthMeasurementEntity(
                    type = "PRESSIONE",
                    valuePrimary = 120f,
                    valueSecondary = 78f,
                    unit = "mmHg",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    note = "Misurazione serale a riposo"
                )
            )
            healthDao.insertMeasurement(
                HealthMeasurementEntity(
                    type = "BATTITI",
                    valuePrimary = 72f,
                    unit = "bpm",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    note = "Regolari"
                )
            )

            // Seed sample diary note
            healthDao.insertDiaryNote(
                DiaryNoteEntity(
                    dateString = todayStr,
                    title = "Nessun effetto collaterale",
                    symptoms = "Tutto nella norma, energia buona",
                    mood = "Buono",
                    content = "Ho assunto Exemestane alle 07:00 come previsto dopo la colazione."
                )
            )

            // Seed past days dose logs for the current week so statistics charts are populated
            val cal = Calendar.getInstance()
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val daysFromMon = if (dayOfWeek == Calendar.SUNDAY) 6 else (dayOfWeek - Calendar.MONDAY).coerceAtLeast(0)

            for (i in 0 until daysFromMon) {
                val pastCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -(daysFromMon - i))
                }
                val pastDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(pastCal.time)
                // Exemestane taken
                medDao.insertOrUpdateLog(
                    DoseLogEntity(
                        medicationId = med1Id,
                        scheduleId = sched1Id,
                        dateString = pastDateStr,
                        scheduledTime = "07:00",
                        status = IntakeStatus.TAKEN,
                        actionTimestamp = pastCal.timeInMillis + 7 * 3600000L
                    )
                )
                // Calcium Phosphate taken
                medDao.insertOrUpdateLog(
                    DoseLogEntity(
                        medicationId = med2Id,
                        scheduleId = sched2Id,
                        dateString = pastDateStr,
                        scheduledTime = "08:00",
                        status = IntakeStatus.TAKEN,
                        actionTimestamp = pastCal.timeInMillis + 8 * 3600000L + 120000L
                    )
                )
            }
        }
    }
}
