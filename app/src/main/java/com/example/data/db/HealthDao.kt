package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppointmentEntity
import com.example.data.model.DiaryNoteEntity
import com.example.data.model.DoctorContactEntity
import com.example.data.model.HealthMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    // Health measurements
    @Query("SELECT * FROM health_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<HealthMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(item: HealthMeasurementEntity): Long

    @Delete
    suspend fun deleteMeasurement(item: HealthMeasurementEntity)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY timestamp ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(item: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(item: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(item: AppointmentEntity)

    // Doctors
    @Query("SELECT * FROM doctor_contacts ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<DoctorContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(item: DoctorContactEntity): Long

    @Update
    suspend fun updateDoctor(item: DoctorContactEntity)

    @Delete
    suspend fun deleteDoctor(item: DoctorContactEntity)

    // Diary notes
    @Query("SELECT * FROM diary_notes ORDER BY timestamp DESC")
    fun getAllDiaryNotes(): Flow<List<DiaryNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryNote(item: DiaryNoteEntity): Long

    @Delete
    suspend fun deleteDiaryNote(item: DiaryNoteEntity)
}
