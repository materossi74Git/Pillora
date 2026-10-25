package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DoseLogEntity
import com.example.data.model.DoseScheduleEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Transaction
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>>

    @Transaction
    @Query("SELECT * FROM medications WHERE isActive = 1 AND (:profileId <= 0 OR profileId = :profileId) ORDER BY name ASC")
    fun getActiveMedicationsWithSchedulesForProfile(profileId: Long): Flow<List<MedicationWithSchedules>>

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>>

    @Transaction
    @Query("SELECT * FROM medications WHERE (:profileId <= 0 OR profileId = :profileId) ORDER BY name ASC")
    fun getAllMedicationsWithSchedulesForProfile(profileId: Long): Flow<List<MedicationWithSchedules>>

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :medicationId LIMIT 1")
    suspend fun getMedicationWithSchedulesById(medicationId: Long): MedicationWithSchedules?

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DoseScheduleEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DoseScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: DoseScheduleEntity)

    @Query("DELETE FROM dose_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)

    @Query("DELETE FROM dose_schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Long)

    @Query("SELECT * FROM dose_schedules WHERE id = :scheduleId LIMIT 1")
    suspend fun getScheduleById(scheduleId: Long): DoseScheduleEntity?

    @Query("SELECT * FROM dose_schedules")
    suspend fun getAllSchedules(): List<DoseScheduleEntity>

    // Logs
    @Query("SELECT * FROM dose_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE dateString BETWEEN :startDate AND :endDate")
    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE scheduleId = :scheduleId AND dateString = :dateString LIMIT 1")
    suspend fun getLogForScheduleAndDate(scheduleId: Long, dateString: String): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: DoseLogEntity): Long

    @Query("UPDATE dose_logs SET status = :status, actionTimestamp = :timestamp WHERE scheduleId = :scheduleId AND dateString = :dateString")
    suspend fun updateLogStatus(scheduleId: Long, dateString: String, status: IntakeStatus, timestamp: Long?): Int

    @Query("UPDATE medications SET stockRemaining = MAX(0, stockRemaining - 1) WHERE id = :medicationId")
    suspend fun decrementStock(medicationId: Long)

    @Query("UPDATE medications SET stockRemaining = :newStock WHERE id = :medicationId")
    suspend fun updateStock(medicationId: Long, newStock: Int)

    @Query("UPDATE medications SET profileId = :profileId, profileName = :profileName WHERE profileId <= 0 OR profileId = 1")
    suspend fun assignOrphanedMedicationsToProfile(profileId: Long, profileName: String)

    @Query("DELETE FROM medications WHERE profileId = :profileId")
    suspend fun deleteMedicationsForProfile(profileId: Long)
}
