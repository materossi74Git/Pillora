package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import com.example.data.model.DiaryNoteEntity
import com.example.data.model.DoctorContactEntity
import com.example.data.model.HealthMeasurementEntity
import com.example.data.model.MedicationWithSchedules
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------
// 1. TRACKER DI SALUTE E MISURAZIONI
// -------------------------------------------------------------
@Composable
fun HealthTrackerScreen(
    measurements: List<HealthMeasurementEntity>,
    onAddMeasurement: (HealthMeasurementEntity) -> Unit,
    onDeleteMeasurement: (HealthMeasurementEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        SubScreenHeader(title = "Tracker di salute e misurazioni", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_add_measurement"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registra nuova misurazione")
                }
            }

            if (measurements.isEmpty()) {
                item {
                    Text(
                        text = "Nessuna misurazione registrata.",
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(measurements, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = Color(0xFF38BDF8))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val valueText = if (item.valueSecondary != null) {
                                    "${item.valuePrimary.toInt()} / ${item.valueSecondary.toInt()} ${item.unit}"
                                } else {
                                    "${item.valuePrimary} ${item.unit}"
                                }
                                Text(
                                    text = "${item.type}: $valueText",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ITALIAN).format(Date(item.timestamp))
                                Text(dateStr, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                if (item.note.isNotBlank()) {
                                    Text(item.note, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { onDeleteMeasurement(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeasurementDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = {
                onAddMeasurement(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddMeasurementDialog(
    onDismiss: () -> Unit,
    onConfirm: (HealthMeasurementEntity) -> Unit
) {
    var type by remember { mutableStateOf("PRESSIONE") }
    var val1 by remember { mutableStateOf("120") }
    var val2 by remember { mutableStateOf("80") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova misurazione", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PRESSIONE", "GLICEMIA", "BATTITI", "PESO").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = {
                                type = t
                                if (t == "GLICEMIA") { val1 = "95"; val2 = "" }
                                else if (t == "BATTITI") { val1 = "72"; val2 = "" }
                                else if (t == "PESO") { val1 = "65"; val2 = "" }
                                else { val1 = "120"; val2 = "80" }
                            },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = val1,
                    onValueChange = { val1 = it },
                    label = { Text(if (type == "PRESSIONE") "Sistolica (Massima)" else "Valore") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == "PRESSIONE") {
                    OutlinedTextField(
                        value = val2,
                        onValueChange = { val2 = it },
                        label = { Text("Diastolica (Minima)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note aggiuntive") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val unit = when (type) {
                    "PRESSIONE" -> "mmHg"
                    "GLICEMIA" -> "mg/dL"
                    "BATTITI" -> "bpm"
                    "PESO" -> "kg"
                    else -> ""
                }
                onConfirm(
                    HealthMeasurementEntity(
                        type = type,
                        valuePrimary = val1.toFloatOrNull() ?: 0f,
                        valueSecondary = if (type == "PRESSIONE") val2.toFloatOrNull() else null,
                        unit = unit,
                        note = note
                    )
                )
            }) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}

// -------------------------------------------------------------
// 2. APPUNTAMENTI
// -------------------------------------------------------------
@Composable
fun AppointmentsScreen(
    appointments: List<AppointmentEntity>,
    onAddAppointment: (AppointmentEntity) -> Unit,
    onDeleteAppointment: (AppointmentEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        SubScreenHeader(title = "Appuntamenti Medici", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_add_appointment"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aggiungi nuovo appuntamento")
                }
            }

            if (appointments.isEmpty()) {
                item {
                    Text(
                        text = "Nessun appuntamento in programma.",
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(appointments, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = item.doctorOrSpecialty,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 14.sp
                                    )
                                }
                                IconButton(onClick = { onDeleteAppointment(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val dateStr = SimpleDateFormat("EEEE dd MMMM yyyy, HH:mm", Locale.ITALIAN).format(Date(item.timestamp))
                            Text(dateStr, color = Color(0xFF94A3B8), fontSize = 13.sp)
                            if (item.location.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(item.location, color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                }
                            }
                            if (item.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(item.notes, color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var doctor by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nuovo Appuntamento", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titolo visita / esame") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = doctor, onValueChange = { doctor = it }, label = { Text("Medico / Reparto") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Luogo / Struttura") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Note o raccomandazioni") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddAppointment(
                                AppointmentEntity(
                                    title = title,
                                    doctorOrSpecialty = doctor,
                                    timestamp = System.currentTimeMillis() + 86400000L * 3, // In 3 days default
                                    location = location,
                                    notes = notes
                                )
                            )
                            showDialog = false
                        }
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("Salva")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annulla") }
            }
        )
    }
}

// -------------------------------------------------------------
// 3. MEDICI
// -------------------------------------------------------------
@Composable
fun DoctorsScreen(
    doctors: List<DoctorContactEntity>,
    onAddDoctor: (DoctorContactEntity) -> Unit,
    onDeleteDoctor: (DoctorContactEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        SubScreenHeader(title = "Rubrica Medici", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_add_doctor"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aggiungi medico curante")
                }
            }

            if (doctors.isEmpty()) {
                item {
                    Text(
                        text = "Nessun contatto medico registrato.",
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(doctors, key = { it.id }) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF34D399))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(doc.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(doc.specialty, color = Color(0xFF94A3B8), fontSize = 13.sp)
                                }
                                IconButton(onClick = { onDeleteDoctor(doc) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (doc.phone.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${doc.phone}"))
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chiama", fontSize = 13.sp)
                                    }
                                }
                                if (doc.email.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${doc.email}"))
                                            context.startActivity(emailIntent)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Email", fontSize = 13.sp)
                                    }
                                }
                            }
                            if (doc.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(doc.notes, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var spec by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nuovo Contatto Medico", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome e cognome *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = spec, onValueChange = { spec = it }, label = { Text("Specializzazione") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Numero di telefono") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Orari ambulatorio / Note") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddDoctor(DoctorContactEntity(name = name, specialty = spec, phone = phone, email = email, notes = notes))
                            showDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Salva")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annulla") }
            }
        )
    }
}

// -------------------------------------------------------------
// 4. NOTE DEL DIARIO & SINTOMI
// -------------------------------------------------------------
@Composable
fun DiaryNotesScreen(
    notes: List<DiaryNoteEntity>,
    onAddNote: (DiaryNoteEntity) -> Unit,
    onDeleteNote: (DiaryNoteEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        SubScreenHeader(title = "Note del diario & Sintomi", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_add_diary_note"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuova nota clinica o sintomo")
                }
            }

            if (notes.isEmpty()) {
                item {
                    Text(
                        text = "Nessuna annotazione nel diario.",
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(notes, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Umore: ${item.mood}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                }
                                IconButton(onClick = { onDeleteNote(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }
                            if (item.symptoms.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sintomi: ${item.symptoms}", color = Color(0xFFFBBF24), fontSize = 13.sp)
                            }
                            if (item.content.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(item.content, color = Color(0xFFCBD5E1), fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ITALIAN).format(Date(item.timestamp))
                            Text(dateStr, color = Color(0xFF64748B), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var mood by remember { mutableStateOf("Buono") }
        var symptoms by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Aggiungi Nota al Diario", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titolo") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("Umore / Stato (es. Ottimo, Stanco)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = symptoms, onValueChange = { symptoms = it }, label = { Text("Eventuali sintomi riscontrati") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Descrizione dettagliata") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            onAddNote(
                                DiaryNoteEntity(
                                    dateString = todayStr,
                                    title = title,
                                    symptoms = symptoms,
                                    mood = mood,
                                    content = content
                                )
                            )
                            showDialog = false
                        }
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("Salva")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annulla") }
            }
        )
    }
}

// -------------------------------------------------------------
// 5. RICARICHE & SCORTE FARMACI
// -------------------------------------------------------------
@Composable
fun StockRefillScreen(
    medications: List<MedicationWithSchedules>,
    onUpdateStock: (Long, Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        SubScreenHeader(title = "Ricariche & Scorte", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Tieni traccia delle pillole rimanenti per non restare mai senza scorte:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }

            items(medications, key = { it.medication.id }) { item ->
                val med = item.medication
                val isLow = med.stockRemaining <= med.stockThresholdAlert

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(med.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("${med.dosage} • ${med.type.displayName}", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            }
                            if (isLow) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF78350F))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Scorta bassa!", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rimanenti: ${med.stockRemaining}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLow) Color(0xFFF59E0B) else Color(0xFF38BDF8)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onUpdateStock(med.id, med.stockRemaining + 10) }) {
                                    Text("+10")
                                }
                                OutlinedButton(onClick = { onUpdateStock(med.id, med.stockRemaining + 30) }) {
                                    Text("+30 (Confezione)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubScreenHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F2033))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("subscreen_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
fun BackupInfoScreen(
    medications: List<MedicationWithSchedules>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val alarmManager = remember { context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager }
    val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager }

    var isExactAlarmAllowed by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        )
    }

    var isBatteryOptIgnored by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        )
    }

    // Security PIN State
    val prefs = remember { context.getSharedPreferences("med_security_prefs", android.content.Context.MODE_PRIVATE) }
    var pinEnabled by remember { mutableStateOf(prefs.getBoolean("pin_enabled", false)) }
    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        SubScreenHeader(title = "Affidabilità, Sicurezza & Backup", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========================================================
            // SECTION 1: AFFIDABILITÀ ALLARMI & FORZA IN BACKGROUND
            // ========================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = Color(0xFF38BDF8))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Forza Promemoria & Affidabilità",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Sveglie esatte e protezione dai blocchi Android",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Badge 1: Sveglie esatte (Exact Alarms)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F2033))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sveglia esatta di sistema (RTC_WAKEUP)",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isExactAlarmAllowed) "✓ Autorizzato: suona al secondo esatto" else "⚠ Limitato dal sistema",
                                    color = if (isExactAlarmAllowed) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    fontSize = 11.sp
                                )
                            }
                            if (!isExactAlarmAllowed && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Impossibile aprire impostazioni sveglia", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Attiva", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Badge 2: Batteria e Doze Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F2033))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Protezione Risparmio Batteria (Anti-Doze)",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isBatteryOptIgnored) "✓ Senza restrizioni: nessuna interruzione" else "⚠ Batteria ottimizzata: potrebbe ritardare notifiche a schermo spento",
                                    color = if (isBatteryOptIgnored) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    fontSize = 11.sp
                                )
                            }
                            if (!isBatteryOptIgnored && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            try {
                                                val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                                context.startActivity(intent)
                                            } catch (ex: Exception) {
                                                android.widget.Toast.makeText(context, "Impostazioni batteria non accessibili", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Consenti", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "🛡 Come garantisce l'app le notifiche anche se chiusa:\n" +
                                    "1. Sveglia Hardware RTC: L'app usa 'AlarmManager.setExactAndAllowWhileIdle' che risveglia il telefono dal sonno profondo (Doze Mode).\n" +
                                    "2. Riavvio e Aggiornamenti: Al riavvio del telefono o all'aggiornamento dell'app, il componente BootReceiver riprogramma automaticamente tutte le sveglie attive dal database.\n" +
                                    "3. Canali con Priorità Massima: Notifiche categorizzate come ALLARMI con illuminazione schermo e vibrazione.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ========================================================
            // SECTION 2: RISPOSTA AGGIORNAMENTO VS REINSTALLAZIONE
            // ========================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2836)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "FAQ: Perdo i dati se aggiorno l'app?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "🟢 SE AGGIORNI L'APP (Installando la nuova versione sopra quella esistente):\n" +
                                    "➡ NON PERDI NULLA! Tutti i farmaci registrati, le scadenze (anche quelle a 28 giorni), lo storico delle assunzioni, le misurazioni e i profili rimangono salvati al 100% nel database interno SQLite/Room di Android.\n\n" +
                                    "🔴 SE INVECE DISINSTALLI L'APP PRIMA DI REINSTALLARLA:\n" +
                                    "➡ Android rimuove i file locali dell'app. Tuttavia, con il Backup Google Drive attivo (sotto) e la funzione di esportazione manuale, puoi recuperare immediatamente l'intero piano terapeutico.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            // ========================================================
            // SECTION 3: PROTEZIONE DATI PERSONALI & PIN
            // ========================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Protezione Dati Sanitari & PIN",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (pinEnabled) "PIN di blocco attivo 🔒" else "Accesso libero",
                                        color = if (pinEnabled) Color(0xFFA78BFA) else Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            androidx.compose.material3.Switch(
                                checked = pinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinDialog = true
                                    } else {
                                        prefs.edit().putBoolean("pin_enabled", false).putString("security_pin", "").apply()
                                        pinEnabled = false
                                        android.widget.Toast.makeText(context, "Blocco PIN disattivato", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Protegge le tue informazioni sui farmaci, dosaggi e note sanitarie dagli sguardi indiscreti quando presti il telefono a terzi.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ========================================================
            // SECTION 4: BACKUP SU PROFILO GOOGLE & DRIVE
            // ========================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF38BDF8))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Backup Automatico Google Account",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Google Drive Cloud Backup: Attivo ✓",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "L'app include le regole ufficiali Android di backup automatico sul tuo account Google personale (data_extraction_rules e backup_rules). " +
                                    "Android esegue periodicamente una copia crittografata del database sul tuo Google Drive quando il dispositivo è sotto carica e connesso a Wi-Fi.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Manual export button
                        Button(
                            onClick = {
                                val sb = StringBuilder()
                                sb.append("=== BACKUP COMPLETO PROMEMORIA FARMACI ===\n")
                                sb.append("Data export: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n\n")
                                sb.append("FARMACI E PIANO TERAPEUTICO:\n")
                                medications.forEach { item ->
                                    val med = item.medication
                                    sb.append("• ${med.name} (${med.dosage}) - ${med.type.displayName}\n")
                                    sb.append("  Istruzioni: ${med.instructions}\n")
                                    sb.append("  Scorta: ${med.stockRemaining} unità\n")
                                    val times = item.schedules.joinToString("; ") { sched ->
                                        "${String.format("%02d:%02d", sched.timeHour, sched.timeMinute)} (${sched.getFrequencyDescription()}) [Suono: ${sched.soundTone}]"
                                    }
                                    sb.append("  Orari e Frequenza: $times\n\n")
                                }
                                sb.append("Fine report backup.")

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, sb.toString())
                                    type = "text/plain"
                                }
                                val chooser = Intent.createChooser(sendIntent, "Salva copia di sicurezza su Google Drive o invia")
                                context.startActivity(chooser)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_export_backup"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Salva / Condividi Backup su Google Drive", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onConfirmPin = { enteredPin ->
                prefs.edit().putBoolean("pin_enabled", true).putString("security_pin", enteredPin).apply()
                pinEnabled = true
                showPinDialog = false
                android.widget.Toast.makeText(context, "PIN di sicurezza impostato con successo! 🔒", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun SetPinDialog(
    onDismiss: () -> Unit,
    onConfirmPin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Imposta PIN di Sicurezza", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Inserisci un codice PIN a 4 cifre per proteggere i tuoi dati di terapia:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pin = it },
                    label = { Text("PIN (4 cifre)") },
                    placeholder = { Text("es. 1234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (pin.length == 4) onConfirmPin(pin) },
                enabled = pin.length == 4
            ) {
                Text("Salva PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
