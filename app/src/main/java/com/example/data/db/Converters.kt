package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.FrequencyType
import com.example.data.model.IntakeStatus
import com.example.data.model.MedicationType

class Converters {
    @TypeConverter
    fun fromFrequencyType(value: FrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String): FrequencyType =
        try {
            FrequencyType.valueOf(value)
        } catch (e: Exception) {
            FrequencyType.DAILY
        }

    @TypeConverter
    fun fromMedicationType(value: MedicationType): String = value.name

    @TypeConverter
    fun toMedicationType(value: String): MedicationType =
        try {
            MedicationType.valueOf(value)
        } catch (e: Exception) {
            MedicationType.COMPRESSA
        }

    @TypeConverter
    fun fromIntakeStatus(value: IntakeStatus): String = value.name

    @TypeConverter
    fun toIntakeStatus(value: String): IntakeStatus =
        try {
            IntakeStatus.valueOf(value)
        } catch (e: Exception) {
            IntakeStatus.PENDING
        }
}
