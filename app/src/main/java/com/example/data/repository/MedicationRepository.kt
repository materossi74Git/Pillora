package com.example.data.repository

import com.example.data.db.HealthDao
import com.example.data.db.MedicationDao
import com.example.data.model.AppointmentEntity
import com.example.data.model.DiaryNoteEntity
import com.example.data.model.DoctorContactEntity
import com.example.data.model.DoseLogEntity
import com.example.data.model.DoseScheduleEntity
import com.example.data.model.HealthMeasurementEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationWithSchedules
import com.example.data.model.ScheduledDoseItem
import com.example.data.db.ProfileDao
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val healthDao: HealthDao,
    private val profileDao: ProfileDao
) {

    // Profiles
    val allProfiles: Flow<List<UserProfileEntity>> = profileDao.getAllProfiles()

    suspend fun getProfileCount(): Int = profileDao.getProfileCount()
    suspend fun getFirstProfile(): UserProfileEntity? = profileDao.getFirstProfile()
    suspend fun getProfileById(id: Long): UserProfileEntity? = profileDao.getProfileById(id)
    suspend fun insertProfile(profile: UserProfileEntity): Long = profileDao.insertProfile(profile)
    suspend fun updateProfile(profile: UserProfileEntity) = profileDao.updateProfile(profile)
    suspend fun deleteProfile(profile: UserProfileEntity) {
        medicationDao.deleteMedicationsForProfile(profile.id)
        profileDao.deleteProfile(profile)
    }
    suspend fun assignOrphanedMedicationsToProfile(profileId: Long, profileName: String) {
        medicationDao.assignOrphanedMedicationsToProfile(profileId, profileName)
    }

    val activeMedicationsWithSchedules: Flow<List<MedicationWithSchedules>> =
        medicationDao.getActiveMedicationsWithSchedules()

    fun getActiveMedicationsWithSchedules(profileId: Long = 0): Flow<List<MedicationWithSchedules>> =
        medicationDao.getActiveMedicationsWithSchedulesForProfile(profileId)

    val allMedicationsWithSchedules: Flow<List<MedicationWithSchedules>> =
        medicationDao.getAllMedicationsWithSchedules()

    fun getAllMedicationsWithSchedules(profileId: Long = 0): Flow<List<MedicationWithSchedules>> =
        medicationDao.getAllMedicationsWithSchedulesForProfile(profileId)

    fun getLogsForDate(dateString: String): Flow<List<DoseLogEntity>> =
        medicationDao.getLogsForDate(dateString)

    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<DoseLogEntity>> =
        medicationDao.getLogsForDateRange(startDate, endDate)

    /**
     * Combines active medications and schedules with logs for a specific date string (YYYY-MM-DD)
     * and day-of-week index (1=Monday, 2=Tuesday, ..., 7=Sunday), filtered by profile (0 = all profiles).
     */
    fun getScheduledDosesForDay(
        dateString: String,
        dayOfWeek1To7: Int,
        profileId: Long = 0
    ): Flow<List<ScheduledDoseItem>> {
        return combine(
            medicationDao.getActiveMedicationsWithSchedulesForProfile(profileId),
            medicationDao.getLogsForDate(dateString)
        ) { medList, logs ->
            val logMap = logs.associateBy { it.scheduleId }
            val doseItems = mutableListOf<ScheduledDoseItem>()

            for (medWithSched in medList) {
                for (sched in medWithSched.schedules) {
                    if (sched.isScheduledForDate(dateString, dayOfWeek1To7)) {
                        doseItems.add(
                            ScheduledDoseItem(
                                medication = medWithSched.medication,
                                schedule = sched,
                                log = logMap[sched.id],
                                dateString = dateString
                            )
                        )
                    }
                }
            }

            // Order by scheduled time (hour, minute)
            doseItems.sortedWith(
                compareBy(
                    { it.schedule.timeHour },
                    { it.schedule.timeMinute },
                    { it.medication.name }
                )
            )
        }
    }

    suspend fun markDoseStatus(
        medicationId: Long,
        scheduleId: Long,
        dateString: String,
        scheduledTime: String,
        newStatus: IntakeStatus
    ) {
        val existingLog = medicationDao.getLogForScheduleAndDate(scheduleId, dateString)
        val now = System.currentTimeMillis()

        if (existingLog == null) {
            medicationDao.insertOrUpdateLog(
                DoseLogEntity(
                    medicationId = medicationId,
                    scheduleId = scheduleId,
                    dateString = dateString,
                    scheduledTime = scheduledTime,
                    status = newStatus,
                    actionTimestamp = if (newStatus == IntakeStatus.TAKEN) now else null
                )
            )
        } else {
            medicationDao.insertOrUpdateLog(
                existingLog.copy(
                    status = newStatus,
                    actionTimestamp = if (newStatus == IntakeStatus.TAKEN) now else existingLog.actionTimestamp
                )
            )
        }

        // If newly marked as taken, decrement remaining stock
        if (newStatus == IntakeStatus.TAKEN && existingLog?.status != IntakeStatus.TAKEN) {
            medicationDao.decrementStock(medicationId)
        }
    }

    suspend fun saveMedicationWithSchedules(
        medication: MedicationEntity,
        schedules: List<DoseScheduleEntity>
    ): Long {
        val medId = if (medication.id == 0L) {
            medicationDao.insertMedication(medication)
        } else {
            medicationDao.updateMedication(medication)
            medication.id
        }

        // Replace schedules for this medication
        medicationDao.deleteSchedulesForMedication(medId)
        val schedulesWithId = schedules.map { it.copy(medicationId = medId) }
        medicationDao.insertSchedules(schedulesWithId)
        return medId
    }

    suspend fun updateMedication(medication: MedicationEntity) {
        medicationDao.updateMedication(medication)
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.deleteMedication(medication)
    }

    suspend fun updateDoseSchedule(schedule: DoseScheduleEntity) {
        medicationDao.updateSchedule(schedule)
    }

    suspend fun updateStock(medicationId: Long, newStock: Int) {
        medicationDao.updateStock(medicationId, newStock)
    }

    suspend fun getMedicationWithSchedulesById(id: Long) =
        medicationDao.getMedicationWithSchedulesById(id)

    suspend fun getAllSchedules() = medicationDao.getAllSchedules()

    suspend fun getScheduleById(id: Long) = medicationDao.getScheduleById(id)

    suspend fun getMedicationById(id: Long) = medicationDao.getMedicationById(id)

    // Health items
    val healthMeasurements: Flow<List<HealthMeasurementEntity>> = healthDao.getAllMeasurements()
    val appointments: Flow<List<AppointmentEntity>> = healthDao.getAllAppointments()
    val doctors: Flow<List<DoctorContactEntity>> = healthDao.getAllDoctors()
    val diaryNotes: Flow<List<DiaryNoteEntity>> = healthDao.getAllDiaryNotes()

    suspend fun addMeasurement(item: HealthMeasurementEntity) = healthDao.insertMeasurement(item)
    suspend fun deleteMeasurement(item: HealthMeasurementEntity) = healthDao.deleteMeasurement(item)

    suspend fun addAppointment(item: AppointmentEntity) = healthDao.insertAppointment(item)
    suspend fun updateAppointment(item: AppointmentEntity) = healthDao.updateAppointment(item)
    suspend fun deleteAppointment(item: AppointmentEntity) = healthDao.deleteAppointment(item)

    suspend fun addDoctor(item: DoctorContactEntity) = healthDao.insertDoctor(item)
    suspend fun updateDoctor(item: DoctorContactEntity) = healthDao.updateDoctor(item)
    suspend fun deleteDoctor(item: DoctorContactEntity) = healthDao.deleteDoctor(item)

    suspend fun addDiaryNote(item: DiaryNoteEntity) = healthDao.insertDiaryNote(item)
    suspend fun deleteDiaryNote(item: DiaryNoteEntity) = healthDao.deleteDiaryNote(item)
}
