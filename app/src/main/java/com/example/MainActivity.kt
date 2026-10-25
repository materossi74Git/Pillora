package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.MedicationWithSchedules
import com.example.ui.AppNavTab
import com.example.ui.MedicationViewModel
import com.example.ui.MedicationViewModelFactory
import com.example.ui.SubScreen
import com.example.ui.components.TopBarWithProfile
import com.example.ui.components.WeeklyDateStrip
import com.example.ui.screens.AddEditMedicationDialog
import com.example.ui.screens.AppUpdateScreen
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.BackupInfoScreen
import com.example.ui.screens.DailyScheduleScreen
import com.example.ui.screens.DiaryNotesScreen
import com.example.ui.screens.DoctorsScreen
import com.example.ui.screens.HealthMenuScreen
import com.example.ui.screens.HealthTrackerScreen
import com.example.ui.screens.MedicationsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.StockRefillScreen
import com.example.ui.screens.WeeklyScheduleScreen
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.components.FirstLaunchOnboardingDialog

class MainActivity : ComponentActivity() {

    private val viewModel: MedicationViewModel by viewModels {
        val app = application as MedicationApplication
        MedicationViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel, onSharePlan = { shareTherapyPlan() })
            }
        }
    }

    private fun shareTherapyPlan() {
        val activeMeds = viewModel.activeMedications.value
        val profile = viewModel.currentProfileName.value
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(Date())

        val sb = StringBuilder()
        sb.append("📋 Piano Terapeutico di $profile - $todayStr\n\n")

        if (activeMeds.isEmpty()) {
            sb.append("Nessun farmaco attivo al momento.\n")
        } else {
            activeMeds.forEach { medWithSched ->
                val med = medWithSched.medication
                sb.append("💊 ${med.name} (${med.dosage})\n")
                sb.append("• Tipo: ${med.type.displayName}\n")
                if (med.instructions.isNotBlank()) {
                    sb.append("• Istruzioni: ${med.instructions}\n")
                }
                sb.append("• Orari: ")
                val times = medWithSched.schedules.joinToString(", ") {
                    String.format("%02d:%02d", it.timeHour, it.timeMinute)
                }
                sb.append("$times\n")
                sb.append("• Scorta residua: ${med.stockRemaining} dosi\n\n")
            }
        }

        sb.append("Inviato tramite Pillora.")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Condividi piano terapeutico con medico o familiari")
        startActivity(shareIntent)
    }
}

@Composable
fun MainContent(
    viewModel: MedicationViewModel,
    onSharePlan: () -> Unit
) {
    val context = LocalContext.current

    // Notification Permission Launcher (Android 13+) - requested on demand, non-blocking
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notifiche attivate con successo! 🔔", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Promemoria attivi nell'app (puoi abilitare le notifiche di sistema nelle impostazioni)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // State collections
    val currentTab by viewModel.currentTab.collectAsState()
    val currentSubScreen by viewModel.currentSubScreen.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val isAllProfilesSelected by viewModel.isAllProfilesSelected.collectAsState()
    val currentProfileName by viewModel.currentProfileName.collectAsState()
    val showFirstLaunchDialog by viewModel.showFirstLaunchDialog.collectAsState()
    val selectedCalendar by viewModel.selectedCalendar.collectAsState()
    val weekDays by viewModel.weekDays.collectAsState()
    val weekOffset by viewModel.weekOffset.collectAsState()
    val dailyDoses by viewModel.dailyDoses.collectAsState()
    val activeMedications by viewModel.activeMedications.collectAsState()
    val measurements by viewModel.healthMeasurements.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val diaryNotes by viewModel.diaryNotes.collectAsState()
    val weeklyStatistics by viewModel.weeklyStatistics.collectAsState()

    // Add / Edit Medication Dialog state
    var showAddMedDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<MedicationWithSchedules?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentSubScreen == SubScreen.NONE) {
                TopBarWithProfile(
                    currentProfile = selectedProfile,
                    isAllProfilesSelected = isAllProfilesSelected,
                    profiles = profiles,
                    onSelectProfile = { viewModel.selectProfile(it) },
                    onSelectAllProfiles = { viewModel.selectAllProfiles() },
                    onAddProfile = { name, rel, col -> viewModel.addNewProfile(name, rel, col) },
                    onEditProfile = { viewModel.updateProfile(it) },
                    onDeleteProfile = { viewModel.deleteProfile(it) },
                    onShareSchedule = onSharePlan
                )
            }
        },
        bottomBar = {
            if (currentSubScreen == SubScreen.NONE) {
                NavigationBar(
                    containerColor = Color(0xFF0F1E2E), // Dark navy bottom bar matching photos
                    contentColor = Color.White
                ) {
                    // 1. Casa (Home / Daily schedule)
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.CASA,
                        onClick = { viewModel.selectTab(AppNavTab.CASA) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.CASA) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Casa"
                            )
                        },
                        label = { Text("Casa", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00B4D8),
                            selectedTextColor = Color(0xFF00B4D8),
                            indicatorColor = Color(0xFF162E47),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_casa")
                    )

                    // 2. Settimana (Weekly overview)
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.SETTIMANA,
                        onClick = { viewModel.selectTab(AppNavTab.SETTIMANA) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.SETTIMANA) Icons.Filled.ViewWeek else Icons.Filled.CalendarMonth,
                                contentDescription = "Settimana"
                            )
                        },
                        label = { Text("Settimana", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00B4D8),
                            selectedTextColor = Color(0xFF00B4D8),
                            indicatorColor = Color(0xFF162E47),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_settimana")
                    )

                    // 3. Medicine (Active medications catalog)
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.MEDICINE,
                        onClick = { viewModel.selectTab(AppNavTab.MEDICINE) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.MEDICINE) Icons.Filled.Medication else Icons.Outlined.Medication,
                                contentDescription = "Medicine"
                            )
                        },
                        label = { Text("Medicine", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00B4D8),
                            selectedTextColor = Color(0xFF00B4D8),
                            indicatorColor = Color(0xFF162E47),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_medicine")
                    )

                    // 4. Ulteriori info (Hub)
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.ULTERIORI_INFO,
                        onClick = { viewModel.selectTab(AppNavTab.ULTERIORI_INFO) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.ULTERIORI_INFO) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz,
                                contentDescription = "Ulteriori info"
                            )
                        },
                        label = { Text("Ulteriori info", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00B4D8),
                            selectedTextColor = Color(0xFF00B4D8),
                            indicatorColor = Color(0xFF162E47),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_info")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B131E))
        ) {
            // Check if user is navigating a detail SubScreen
            when (currentSubScreen) {
                SubScreen.TRACKER_SALUTE -> {
                    HealthTrackerScreen(
                        measurements = measurements,
                        onAddMeasurement = { viewModel.addMeasurement(it) },
                        onDeleteMeasurement = { viewModel.deleteMeasurement(it) },
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.APPUNTAMENTI -> {
                    AppointmentsScreen(
                        appointments = appointments,
                        onAddAppointment = { viewModel.addAppointment(it) },
                        onDeleteAppointment = { viewModel.deleteAppointment(it) },
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.MEDICI -> {
                    DoctorsScreen(
                        doctors = doctors,
                        onAddDoctor = { viewModel.addDoctor(it) },
                        onDeleteDoctor = { viewModel.deleteDoctor(it) },
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.NOTE_DIARIO -> {
                    DiaryNotesScreen(
                        notes = diaryNotes,
                        onAddNote = { viewModel.addDiaryNote(it) },
                        onDeleteNote = { viewModel.deleteDiaryNote(it) },
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.RICARICHE -> {
                    StockRefillScreen(
                        medications = activeMedications,
                        onUpdateStock = { id, stock -> viewModel.updateStock(id, stock) },
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.BACKUP_INFO -> {
                    BackupInfoScreen(
                        medications = activeMedications,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.STATISTICHE -> {
                    StatisticsScreen(
                        statisticsState = weeklyStatistics,
                        patientName = currentProfileName,
                        onPreviousWeek = { viewModel.previousWeek() },
                        onNextWeek = { viewModel.nextWeek() },
                        onResetToday = { viewModel.resetToToday() },
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.AGGIORNAMENTI -> {
                    AppUpdateScreen(
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.NONE -> {
                    // Normal main tabs
                    when (currentTab) {
                        AppNavTab.CASA -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Top Weekly Date Strip matching screenshot 4
                                WeeklyDateStrip(
                                    weekDays = weekDays,
                                    selectedCalendar = selectedCalendar,
                                    onDaySelected = { dateStr -> viewModel.selectDay(dateStr) },
                                    onPreviousWeek = { viewModel.previousWeek() },
                                    onNextWeek = { viewModel.nextWeek() },
                                    onResetToday = { viewModel.resetToToday() },
                                    isCurrentWeek = weekOffset == 0
                                )

                                DailyScheduleScreen(
                                    doses = dailyDoses,
                                    onMarkStatus = { item, status -> viewModel.markDoseStatus(item, status) },
                                    onSnooze = { item -> viewModel.snoozeDose(context, item) },
                                    onTestNotification = { item ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                            if (!granted) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        }
                                        viewModel.testPushNotification(context, item)
                                        Toast.makeText(context, "Promemoria inviato per ${item.medication.name}! 🔔", Toast.LENGTH_SHORT).show()
                                    },
                                    onUpdateSchedule = { schedule -> viewModel.saveMedication(context, itemToMed(schedule, activeMedications), listOf(schedule)) },
                                    onAddMedicationClick = {
                                        editingMedication = null
                                        showAddMedDialog = true
                                    },
                                    onViewWeeklySchedule = { viewModel.selectTab(AppNavTab.SETTIMANA) },
                                    onViewStatistics = { viewModel.openSubScreen(SubScreen.STATISTICHE) }
                                )
                            }
                        }

                        AppNavTab.SETTIMANA -> {
                            val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                            WeeklyScheduleScreen(
                                weekDays = weekDays,
                                activeMedications = activeMedications,
                                selectedDayString = selectedDateStr,
                                onSelectDay = { dateStr ->
                                    viewModel.selectDay(dateStr)
                                    viewModel.selectTab(AppNavTab.CASA) // Switch to daily view for that day
                                },
                                onPreviousWeek = { viewModel.previousWeek() },
                                onNextWeek = { viewModel.nextWeek() },
                                onViewStatistics = { viewModel.openSubScreen(SubScreen.STATISTICHE) }
                            )
                        }

                        AppNavTab.MEDICINE -> {
                            MedicationsScreen(
                                medications = activeMedications,
                                onAddMedication = {
                                    editingMedication = null
                                    showAddMedDialog = true
                                },
                                onEditMedication = { item ->
                                    editingMedication = item
                                    showAddMedDialog = true
                                },
                                onDeleteMedication = { item ->
                                    viewModel.deleteMedication(context, item.medication)
                                }
                            )
                        }

                        AppNavTab.ULTERIORI_INFO -> {
                            HealthMenuScreen(
                                onNavigateSubScreen = { sub -> viewModel.openSubScreen(sub) },
                                onNavigateFarmaci = { viewModel.selectTab(AppNavTab.MEDICINE) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Medication Dialog
    if (showAddMedDialog) {
        AddEditMedicationDialog(
            initialMedication = editingMedication,
            profiles = profiles,
            defaultProfileId = selectedProfile?.id ?: 0L,
            onDismiss = {
                showAddMedDialog = false
                editingMedication = null
            },
            onSave = { medication, schedules ->
                viewModel.saveMedication(context, medication, schedules)
                showAddMedDialog = false
                editingMedication = null
                Toast.makeText(context, "${medication.name} salvato con successo!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // First Launch Setup Dialog: asks for posology owner name and informs about multi-user support
    if (showFirstLaunchDialog) {
        FirstLaunchOnboardingDialog(
            onConfirmProfile = { name, relationship, colorHex ->
                viewModel.createInitialProfile(name, relationship, colorHex)
            }
        )
    }
}

private fun itemToMed(
    schedule: com.example.data.model.DoseScheduleEntity,
    medList: List<MedicationWithSchedules>
): com.example.data.model.MedicationEntity {
    return medList.firstOrNull { it.medication.id == schedule.medicationId }?.medication
        ?: com.example.data.model.MedicationEntity(
            id = schedule.medicationId,
            name = "Farmaco",
            dosage = schedule.dosageQuantity,
            type = com.example.data.model.MedicationType.COMPRESSA,
            instructions = ""
        )
}
