package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicationType
import com.example.data.model.MedicationWithSchedules

@Composable
fun MedicationsScreen(
    medications: List<MedicationWithSchedules>,
    onAddMedication: () -> Unit,
    onEditMedication: (MedicationWithSchedules) -> Unit,
    onDeleteMedication: (MedicationWithSchedules) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = medications.filter {
        it.medication.name.contains(searchQuery, ignoreCase = true) ||
                it.medication.dosage.contains(searchQuery, ignoreCase = true) ||
                it.medication.notes.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B131E))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Screen Header
            item {
                Text(
                    text = "Medicinali attivi",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cerca farmaco...", color = Color(0xFF64748B), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cerca",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_med_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF16212E),
                        unfocusedContainerColor = Color(0xFF16212E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color(0xFF0284C7),
                        unfocusedIndicatorColor = Color(0xFF334155)
                    ),
                    singleLine = true
                )
            }

            // Medications List
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2636)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Nessun medicinale trovato",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Tocca il pulsante in basso per aggiungere una nuova terapia",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.medication.id }) { item ->
                    MedicationListItem(
                        item = item,
                        onClick = { onEditMedication(item) },
                        onDelete = { onDeleteMedication(item) }
                    )
                }
            }
        }

        // Bottom CTA Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF0B131E).copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Button(
                onClick = onAddMedication,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0284C7),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("btn_aggiungi_farmaco_screen")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aggiungi un farmaco",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MedicationListItem(
    item: MedicationWithSchedules,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val med = item.medication
    val schedules = item.schedules

    // Determine next reminder label
    val nextReminderText = if (schedules.isNotEmpty()) {
        val first = schedules.first()
        "${first.getFrequencyDescription()} • ore ${String.format("%02d:%02d", first.timeHour, first.timeMinute)}"
    } else {
        "Nessun orario impostato"
    }

    val isStockLow = med.stockRemaining <= med.stockThresholdAlert

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF263547), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("med_card_${med.name}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16212E))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Form Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1E2E)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (med.type) {
                        MedicationType.GOCCE -> Icons.Default.InvertColors
                        MedicationType.INIEZIONE -> Icons.Default.Vaccines
                        else -> Icons.Default.Medication
                    }
                    val iconTint = when (med.type) {
                        MedicationType.GOCCE -> Color(0xFF38BDF8)
                        MedicationType.INIEZIONE -> Color(0xFFA78BFA)
                        else -> Color(0xFFFFFFFF)
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = med.name,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (med.dosage.isNotBlank()) "${med.name}  ${med.dosage}" else med.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = nextReminderText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_med_${med.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Stock and Schedules pill badges
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Schedules chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    schedules.forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F2033))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${String.format("%02d:%02d", s.timeHour, s.timeMinute)} • ${s.getFrequencyDescription()}",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Scorte rimanenti
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isStockLow) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Scorte in esaurimento",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = "Scorta: ${med.stockRemaining}",
                        fontSize = 11.sp,
                        color = if (isStockLow) Color(0xFFF59E0B) else Color(0xFF64748B),
                        fontWeight = if (isStockLow) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
