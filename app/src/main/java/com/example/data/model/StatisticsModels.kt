package com.example.data.model

import com.example.ui.DayItem

data class DayAdherence(
    val dayItem: DayItem,
    val scheduledCount: Int,
    val takenCount: Int,
    val percentage: Int // 0..100
)

data class MedicationAdherence(
    val medication: MedicationEntity,
    val scheduledCount: Int,
    val takenCount: Int,
    val percentage: Int
)

data class TimeSlotAdherence(
    val slotName: String, // "Mattina (06:00 - 12:00)", "Pomeriggio (12:00 - 18:00)", "Sera (18:00 - 24:00)"
    val scheduledCount: Int,
    val takenCount: Int,
    val percentage: Int
)

data class WeeklyStatisticsState(
    val weekOffset: Int = 0,
    val weekRangeLabel: String = "",
    val totalScheduled: Int = 0,
    val totalTaken: Int = 0,
    val overallPercentage: Int = 0,
    val dayAdherences: List<DayAdherence> = emptyList(),
    val medicationAdherences: List<MedicationAdherence> = emptyList(),
    val timeSlotAdherences: List<TimeSlotAdherence> = emptyList(),
    val consecutiveStreakDays: Int = 0
)
