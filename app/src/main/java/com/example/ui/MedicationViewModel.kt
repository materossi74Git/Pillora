package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppointmentEntity
import com.example.data.model.DiaryNoteEntity
import com.example.data.model.DoctorContactEntity
import com.example.data.model.DoseScheduleEntity
import com.example.data.model.HealthMeasurementEntity
import com.example.data.model.IntakeStatus
import com.example.data.model.MedicationEntity
import com.example.data.model.MedicationWithSchedules
import com.example.data.model.ScheduledDoseItem
import com.example.data.repository.MedicationRepository
import com.example.notification.MedicationAlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.model.DayAdherence
import com.example.data.model.MedicationAdherence
import com.example.data.model.TimeSlotAdherence
import com.example.data.model.WeeklyStatisticsState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

import com.example.data.model.UserProfileEntity

data class DayItem(
    val dayNameShort: String, // "lun", "mar", etc.
    val dayNumber: Int,       // 5, 6, 7, 8...
    val dateString: String,   // "2026-10-08"
    val dayOfWeek1To7: Int,   // 1=Mon .. 7=Sun
    val isToday: Boolean,
    val isSelected: Boolean
)

enum class AppNavTab(val label: String) {
    CASA("Casa"),
    SETTIMANA("Settimana"),
    MEDICINE("Medicine"),
    ULTERIORI_INFO("Ulteriori info")
}

enum class SubScreen {
    NONE,
    TRACKER_SALUTE,
    APPUNTAMENTI,
    NOTE_DIARIO,
    MEDICI,
    RICARICHE,
    BACKUP_INFO,
    STATISTICHE,
    AGGIORNAMENTI
}

class MedicationViewModel(
    private val repository: MedicationRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // All registered profiles
    val profiles: StateFlow<List<UserProfileEntity>> =
        repository.allProfiles
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected profile (null if not initialized or all selected)
    private val _selectedProfile = MutableStateFlow<UserProfileEntity?>(null)
    val selectedProfile: StateFlow<UserProfileEntity?> = _selectedProfile.asStateFlow()

    // Mode: view all profiles combined
    private val _isAllProfilesSelected = MutableStateFlow(false)
    val isAllProfilesSelected: StateFlow<Boolean> = _isAllProfilesSelected.asStateFlow()

    // First launch onboarding flag (true if 0 profiles exist)
    private val _showFirstLaunchDialog = MutableStateFlow(false)
    val showFirstLaunchDialog: StateFlow<Boolean> = _showFirstLaunchDialog.asStateFlow()

    // Display profile name
    val currentProfileName: StateFlow<String> = combine(_selectedProfile, _isAllProfilesSelected) { prof, all ->
        if (all) "Tutti i profili" else prof?.name ?: "Paziente"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Paziente")

    init {
        viewModelScope.launch {
            val count = repository.getProfileCount()
            if (count == 0) {
                createInitialProfile("Mio Profilo", "Personale", "#0284C7")
            } else {
                val first = repository.getFirstProfile()
                _selectedProfile.value = first
                _showFirstLaunchDialog.value = false
            }
        }
    }

    // Current selected date
    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedCalendar: StateFlow<Calendar> = _selectedCalendar.asStateFlow()

    // Week offset (0 = current week, +1 = next week, -1 = previous week)
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    // Current Navigation Tab & Subscreen
    private val _currentTab = MutableStateFlow(AppNavTab.CASA)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _currentSubScreen = MutableStateFlow(SubScreen.NONE)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    // Active medications (filtered by selected profile or all)
    val activeMedications: StateFlow<List<MedicationWithSchedules>> =
        combine(_selectedProfile, _isAllProfilesSelected) { prof, isAll ->
            if (isAll || prof == null) 0L else prof.id
        }.flatMapLatest { profileId ->
            repository.getActiveMedicationsWithSchedules(profileId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily doses for currently selected day (filtered by profile or all)
    val dailyDoses: StateFlow<List<ScheduledDoseItem>> =
        combine(_selectedCalendar, _selectedProfile, _isAllProfilesSelected) { cal, prof, isAll ->
            Triple(cal, if (isAll || prof == null) 0L else prof.id, Unit)
        }.flatMapLatest { (cal, profileId, _) ->
            val dateStr = dateFormat.format(cal.time)
            val dayOfWeek = getDayOfWeek1To7(cal)
            repository.getScheduledDosesForDay(dateStr, dayOfWeek, profileId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly date strip items
    val weekDays: StateFlow<List<DayItem>> = combine(
        _selectedCalendar,
        _weekOffset
    ) { selectedCal, offset ->
        calculateWeekDays(selectedCal, offset)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly statistics and medication adherence
    val weeklyStatistics: StateFlow<WeeklyStatisticsState> = combine(
        weekDays,
        activeMedications,
        _weekOffset
    ) { days, activeMeds, offset ->
        Triple(days, activeMeds, offset)
    }.flatMapLatest { (days, activeMeds, offset) ->
        if (days.isEmpty()) {
            flowOf(WeeklyStatisticsState())
        } else {
            val startDate = days.first().dateString
            val endDate = days.last().dateString
            repository.getLogsForDateRange(startDate, endDate).map { logs ->
                calculateWeeklyStatistics(days, activeMeds, logs, offset)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyStatisticsState())

    // Health items
    val healthMeasurements: StateFlow<List<HealthMeasurementEntity>> =
        repository.healthMeasurements
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> =
        repository.appointments
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors: StateFlow<List<DoctorContactEntity>> =
        repository.doctors
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diaryNotes: StateFlow<List<DiaryNoteEntity>> =
        repository.diaryNotes
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun selectDay(dateString: String) {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            }
            _selectedCalendar.value = cal
        }
    }

    fun nextWeek() {
        _weekOffset.value += 1
    }

    fun previousWeek() {
        _weekOffset.value -= 1
    }

    fun resetToToday() {
        _weekOffset.value = 0
        _selectedCalendar.value = Calendar.getInstance()
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
        _currentSubScreen.value = SubScreen.NONE
    }

    fun openSubScreen(sub: SubScreen) {
        _currentSubScreen.value = sub
    }

    fun closeSubScreen() {
        _currentSubScreen.value = SubScreen.NONE
    }

    // Profile Management
    fun createInitialProfile(name: String, relationship: String, colorHex: String) {
        viewModelScope.launch {
            val profile = UserProfileEntity(
                name = name.trim(),
                relationship = relationship,
                avatarColorHex = colorHex
            )
            val id = repository.insertProfile(profile)
            val created = profile.copy(id = id)
            _selectedProfile.value = created
            _isAllProfilesSelected.value = false
            _showFirstLaunchDialog.value = false
            repository.assignOrphanedMedicationsToProfile(id, created.name)
        }
    }

    fun addNewProfile(name: String, relationship: String, colorHex: String) {
        viewModelScope.launch {
            val profile = UserProfileEntity(
                name = name.trim(),
                relationship = relationship,
                avatarColorHex = colorHex
            )
            val id = repository.insertProfile(profile)
            val created = profile.copy(id = id)
            _selectedProfile.value = created
            _isAllProfilesSelected.value = false
        }
    }

    fun selectProfile(profile: UserProfileEntity) {
        _selectedProfile.value = profile
        _isAllProfilesSelected.value = false
    }

    fun selectAllProfiles() {
        _isAllProfilesSelected.value = true
    }

    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            if (_selectedProfile.value?.id == profile.id) {
                _selectedProfile.value = profile
            }
        }
    }

    fun deleteProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (_selectedProfile.value?.id == profile.id) {
                val first = repository.getFirstProfile()
                _selectedProfile.value = first
                if (first == null) {
                    _showFirstLaunchDialog.value = true
                }
            }
        }
    }

    fun markDoseStatus(item: ScheduledDoseItem, newStatus: IntakeStatus) {
        viewModelScope.launch {
            repository.markDoseStatus(
                medicationId = item.medication.id,
                scheduleId = item.schedule.id,
                dateString = item.dateString,
                scheduledTime = item.timeFormatted,
                newStatus = newStatus
            )
        }
    }

    fun saveMedication(
        context: Context,
        medication: MedicationEntity,
        schedules: List<DoseScheduleEntity>
    ) {
        viewModelScope.launch {
            val currentProf = _selectedProfile.value
            val medToSave = if (medication.profileId <= 0 && currentProf != null) {
                medication.copy(profileId = currentProf.id, profileName = currentProf.name)
            } else {
                medication
            }
            val medId = repository.saveMedicationWithSchedules(medToSave, schedules)
            // Schedule alarms for each dose
            val savedMed = repository.getMedicationById(medId) ?: medToSave.copy(id = medId)
            val updatedWithSched = repository.getMedicationWithSchedulesById(medId)
            updatedWithSched?.schedules?.forEach { schedule ->
                MedicationAlarmScheduler.scheduleDoseReminder(context, schedule, savedMed)
            }
        }
    }

    fun deleteMedication(context: Context, medication: MedicationEntity) {
        viewModelScope.launch {
            val medWithSched = repository.getMedicationWithSchedulesById(medication.id)
            medWithSched?.schedules?.forEach { schedule ->
                MedicationAlarmScheduler.cancelDoseReminder(context, schedule.id)
            }
            repository.deleteMedication(medication)
        }
    }

    fun updateStock(medicationId: Long, newStock: Int) {
        viewModelScope.launch {
            repository.updateStock(medicationId, newStock)
        }
    }

    fun testPushNotification(context: Context, item: ScheduledDoseItem) {
        NotificationHelper.showReminderNotification(
            context = context,
            notificationId = item.schedule.id.toInt(),
            medicationId = item.medication.id,
            scheduleId = item.schedule.id,
            medicationName = item.medication.name,
            dosage = item.schedule.dosageQuantity,
            instructions = item.medication.instructions,
            customMemo = item.schedule.customMessage,
            vibrationEnabled = item.schedule.vibrationEnabled,
            soundEnabled = item.schedule.soundEnabled,
            soundTone = item.schedule.soundTone
        )
    }

    fun snoozeDose(context: Context, item: ScheduledDoseItem) {
        markDoseStatus(item, IntakeStatus.SNOOZED)
        MedicationAlarmScheduler.scheduleSnoozeReminder(
            context = context,
            scheduleId = item.schedule.id,
            medicationId = item.medication.id,
            medicationName = item.medication.name,
            dosage = item.schedule.dosageQuantity,
            snoozeMinutes = item.schedule.snoozeMinutes
        )
    }

    // Health items actions
    fun addMeasurement(item: HealthMeasurementEntity) {
        viewModelScope.launch { repository.addMeasurement(item) }
    }
    fun deleteMeasurement(item: HealthMeasurementEntity) {
        viewModelScope.launch { repository.deleteMeasurement(item) }
    }

    fun addAppointment(item: AppointmentEntity) {
        viewModelScope.launch { repository.addAppointment(item) }
    }
    fun deleteAppointment(item: AppointmentEntity) {
        viewModelScope.launch { repository.deleteAppointment(item) }
    }

    fun addDoctor(item: DoctorContactEntity) {
        viewModelScope.launch { repository.addDoctor(item) }
    }
    fun deleteDoctor(item: DoctorContactEntity) {
        viewModelScope.launch { repository.deleteDoctor(item) }
    }

    fun addDiaryNote(item: DiaryNoteEntity) {
        viewModelScope.launch { repository.addDiaryNote(item) }
    }
    fun deleteDiaryNote(item: DiaryNoteEntity) {
        viewModelScope.launch { repository.deleteDiaryNote(item) }
    }

    private fun calculateWeekDays(selectedCal: Calendar, offset: CalendarOffset): List<DayItem> {
        val today = Calendar.getInstance()
        val todayStr = dateFormat.format(today.time)
        val selectedStr = dateFormat.format(selectedCal.time)

        val cal = (today.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, offset)
            firstDayOfWeek = Calendar.MONDAY
            // Move to Monday of this week
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
            add(Calendar.DAY_OF_MONTH, diff)
        }

        val list = mutableListOf<DayItem>()
        val dayNames = listOf("lun", "mar", "mer", "gio", "ven", "sab", "dom")

        for (i in 0..6) {
            val dateStr = dateFormat.format(cal.time)
            list.add(
                DayItem(
                    dayNameShort = dayNames[i],
                    dayNumber = cal.get(Calendar.DAY_OF_MONTH),
                    dateString = dateStr,
                    dayOfWeek1To7 = i + 1,
                    isToday = dateStr == todayStr,
                    isSelected = dateStr == selectedStr
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return list
    }

    private fun getDayOfWeek1To7(cal: Calendar): Int {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun calculateWeeklyStatistics(
        days: List<DayItem>,
        activeMeds: List<MedicationWithSchedules>,
        logs: List<com.example.data.model.DoseLogEntity>,
        offset: Int
    ): WeeklyStatisticsState {
        if (days.isEmpty()) return WeeklyStatisticsState()

        val logsByScheduleAndDate = logs.associateBy { "${it.scheduleId}_${it.dateString}" }

        var totalScheduled = 0
        var totalTaken = 0

        val dayAdherences = days.map { day ->
            var dayScheduled = 0
            var dayTaken = 0
            for (med in activeMeds) {
                for (sched in med.schedules) {
                    if (sched.isScheduledForDate(day.dateString, day.dayOfWeek1To7)) {
                        dayScheduled++
                        val key = "${sched.id}_${day.dateString}"
                        val log = logsByScheduleAndDate[key]
                        if (log?.status == IntakeStatus.TAKEN) {
                            dayTaken++
                        }
                    }
                }
            }
            totalScheduled += dayScheduled
            totalTaken += dayTaken
            val pct = if (dayScheduled > 0) (dayTaken * 100 / dayScheduled) else 100
            DayAdherence(
                dayItem = day,
                scheduledCount = dayScheduled,
                takenCount = dayTaken,
                percentage = pct
            )
        }

        // Per-medication adherence
        val medAdherences = activeMeds.map { medWithSched ->
            val med = medWithSched.medication
            var medScheduledCount = 0
            var medTakenCount = 0

            days.forEach { day ->
                medWithSched.schedules.forEach { sched ->
                    if (sched.isScheduledForDate(day.dateString, day.dayOfWeek1To7)) {
                        medScheduledCount++
                        val key = "${sched.id}_${day.dateString}"
                        if (logsByScheduleAndDate[key]?.status == IntakeStatus.TAKEN) {
                            medTakenCount++
                        }
                    }
                }
            }
            val pct = if (medScheduledCount > 0) (medTakenCount * 100 / medScheduledCount) else 100
            MedicationAdherence(
                medication = med,
                scheduledCount = medScheduledCount,
                takenCount = medTakenCount,
                percentage = pct
            )
        }

        // Time slots adherence: Mattina (06-12), Pomeriggio (12-18), Sera (18-24)
        val slots: List<Pair<String, IntRange>> = listOf(
            "Mattina (06:00 - 12:00)" to (6..11),
            "Pomeriggio (12:00 - 18:00)" to (12..17),
            "Sera e Notte (18:00 - 24:00)" to (18..23)
        )
        val timeSlotAdherences = slots.map { (name, hourRange) ->
            var slotScheduled = 0
            var slotTaken = 0
            days.forEach { day ->
                activeMeds.forEach { med ->
                    med.schedules.filter { it.timeHour in hourRange || (hourRange.start == 18 && it.timeHour in 0..5) }.forEach { sched ->
                        if (sched.isScheduledForDate(day.dateString, day.dayOfWeek1To7)) {
                            slotScheduled++
                            val key = "${sched.id}_${day.dateString}"
                            if (logsByScheduleAndDate[key]?.status == IntakeStatus.TAKEN) {
                                slotTaken++
                            }
                        }
                    }
                }
            }
            val pct = if (slotScheduled > 0) (slotTaken * 100 / slotScheduled) else 100
            TimeSlotAdherence(
                slotName = name,
                scheduledCount = slotScheduled,
                takenCount = slotTaken,
                percentage = pct
            )
        }

        // Streak calculation (consecutive days with 100% adherence up to today)
        var streak = 0
        for (da in dayAdherences) {
            if (da.scheduledCount > 0 && da.takenCount >= da.scheduledCount) {
                streak++
            } else if (da.dayItem.isToday && da.takenCount > 0) {
                streak++
                break
            } else if (da.dayItem.isToday) {
                break
            }
        }

        val overallPct = if (totalScheduled > 0) (totalTaken * 100 / totalScheduled) else 100
        val weekLabel = when (offset) {
            0 -> "Questa settimana"
            -1 -> "Settimana scorsa"
            1 -> "Prossima settimana"
            else -> if (offset < 0) "$offset settimane fa" else "Tra $offset settimane"
        }

        return WeeklyStatisticsState(
            weekOffset = offset,
            weekRangeLabel = "${days.first().dayNumber} ${days.first().dayNameShort} - ${days.last().dayNumber} ${days.last().dayNameShort} ($weekLabel)",
            totalScheduled = totalScheduled,
            totalTaken = totalTaken,
            overallPercentage = overallPct,
            dayAdherences = dayAdherences,
            medicationAdherences = medAdherences,
            timeSlotAdherences = timeSlotAdherences,
            consecutiveStreakDays = streak
        )
    }
}

typealias CalendarOffset = Int

class MedicationViewModelFactory(private val repository: MedicationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
