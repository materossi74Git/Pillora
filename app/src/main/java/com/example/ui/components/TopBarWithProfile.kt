package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity

@Composable
fun TopBarWithProfile(
    currentProfile: UserProfileEntity?,
    isAllProfilesSelected: Boolean,
    profiles: List<UserProfileEntity>,
    onSelectProfile: (UserProfileEntity) -> Unit,
    onSelectAllProfiles: () -> Unit,
    onAddProfile: (name: String, relationship: String, colorHex: String) -> Unit,
    onEditProfile: (UserProfileEntity) -> Unit,
    onDeleteProfile: (UserProfileEntity) -> Unit,
    modifier: Modifier = Modifier,
    onShareSchedule: (() -> Unit)? = null
) {
    var showProfileManagerDialog by remember { mutableStateOf(false) }

    val avatarColor = remember(currentProfile, isAllProfilesSelected) {
        if (isAllProfilesSelected) {
            Color(0xFF8B5CF6)
        } else {
            val hex = currentProfile?.avatarColorHex ?: "#0284C7"
            try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                Color(0xFF0284C7)
            }
        }
    }

    val displayName = remember(currentProfile, isAllProfilesSelected) {
        if (isAllProfilesSelected) "Tutti i profili"
        else currentProfile?.name ?: "Paziente"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF072744)) // Deep sleek medical blue header
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile selector chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0C3860))
                    .clickable { showProfileManagerDialog = true }
                    .padding(horizontal = 7.dp, vertical = 3.dp)
                    .testTag("profile_selector_btn")
            ) {
                // Avatar circle with letter or icon
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAllProfilesSelected) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Tutti i profili",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        Text(
                            text = displayName.trim().take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Cambia utente",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!isAllProfilesSelected && currentProfile?.relationship?.isNotBlank() == true) {
                        Text(
                            text = currentProfile.relationship,
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (onShareSchedule != null) {
                IconButton(
                    onClick = onShareSchedule,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("share_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Condividi piano terapeutico",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showProfileManagerDialog) {
        ProfileManagerDialog(
            currentProfile = currentProfile,
            isAllProfilesSelected = isAllProfilesSelected,
            profiles = profiles,
            onDismiss = { showProfileManagerDialog = false },
            onSelectProfile = { profile ->
                onSelectProfile(profile)
                showProfileManagerDialog = false
            },
            onSelectAllProfiles = {
                onSelectAllProfiles()
                showProfileManagerDialog = false
            },
            onAddProfile = { name, rel, col ->
                onAddProfile(name, rel, col)
            },
            onEditProfile = { profile ->
                onEditProfile(profile)
            },
            onDeleteProfile = { profile ->
                onDeleteProfile(profile)
            }
        )
    }
}

@Composable
private fun ProfileManagerDialog(
    currentProfile: UserProfileEntity?,
    isAllProfilesSelected: Boolean,
    profiles: List<UserProfileEntity>,
    onDismiss: () -> Unit,
    onSelectProfile: (UserProfileEntity) -> Unit,
    onSelectAllProfiles: () -> Unit,
    onAddProfile: (String, String, String) -> Unit,
    onEditProfile: (UserProfileEntity) -> Unit,
    onDeleteProfile: (UserProfileEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<UserProfileEntity?>(null) }
    var profileToDelete by remember { mutableStateOf<UserProfileEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gestione Utenti Posologia",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Seleziona la persona per vedere i suoi farmaci o aggiungi altri utenti della famiglia:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Profile list
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles, key = { it.id }) { prof ->
                        val isCurrent = !isAllProfilesSelected && currentProfile?.id == prof.id
                        val color = try {
                            Color(android.graphics.Color.parseColor(prof.avatarColorHex))
                        } catch (e: Exception) {
                            Color(0xFF0284C7)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectProfile(prof) }
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) color else Color(0xFF263547),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFF142436) else Color(0xFF101924)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prof.name.trim().take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = prof.name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF0284C7))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Attivo",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = prof.relationship,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }

                                // Edit button
                                IconButton(
                                    onClick = { editingProfile = prof },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Modifica profilo",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete button (only if more than 1 profile)
                                if (profiles.size > 1) {
                                    IconButton(
                                        onClick = { profileToDelete = prof },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Elimina profilo",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Combined "Tutti i profili" option
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectAllProfiles() }
                                .border(
                                    width = if (isAllProfilesSelected) 2.dp else 1.dp,
                                    color = if (isAllProfilesSelected) Color(0xFF8B5CF6) else Color(0xFF263547),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAllProfilesSelected) Color(0xFF201738) else Color(0xFF101924)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8B5CF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tutti i profili",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Vista combinata per tutta la famiglia",
                                        color = Color(0xFFA78BFA),
                                        fontSize = 12.sp
                                    )
                                }

                                if (isAllProfilesSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button to add new profile
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_add_new_profile"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aggiungi un altro utente / familiare")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )

    // Add Profile Dialog
    if (showAddDialog) {
        AddEditProfileModal(
            initialProfile = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, rel, col ->
                onAddProfile(name, rel, col)
                showAddDialog = false
            }
        )
    }

    // Edit Profile Dialog
    editingProfile?.let { prof ->
        AddEditProfileModal(
            initialProfile = prof,
            onDismiss = { editingProfile = null },
            onSave = { name, rel, col ->
                onEditProfile(prof.copy(name = name, relationship = rel, avatarColorHex = col))
                editingProfile = null
            }
        )
    }

    // Delete Confirmation Dialog
    profileToDelete?.let { prof ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Eliminare il profilo?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Sei sicuro di voler eliminare il profilo di ${prof.name}? I suoi farmaci associati verranno rimossi.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProfile(prof)
                        profileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun AddEditProfileModal(
    initialProfile: UserProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, relationship: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    val relationshipOptions = listOf("Personale", "Mamma", "Papà", "Figlio/a", "Nonno/a", "Paziente assistito", "Altro")
    var selectedRelationship by remember { mutableStateOf(initialProfile?.relationship ?: "Personale") }

    val colorPalette = listOf(
        "#0284C7" to "Blu Medico",
        "#06B6D4" to "Turchese",
        "#10B981" to "Smeraldo",
        "#8B5CF6" to "Viola",
        "#F43F5E" to "Corallo",
        "#F59E0B" to "Ambra"
    )
    var selectedColorHex by remember { mutableStateOf(initialProfile?.avatarColorHex ?: "#0284C7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProfile == null) "Nuovo Profilo Utente" else "Modifica Profilo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome della persona *") },
                    placeholder = { Text("es. Marco, Maria, Luca...") },
                    modifier = Modifier.fillMaxWidth().testTag("add_profile_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Ruolo / Relazione:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relationshipOptions.take(4).forEach { rel ->
                        val isSelected = selectedRelationship == rel
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B))
                                .clickable { selectedRelationship = rel }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = rel,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relationshipOptions.drop(4).forEach { rel ->
                        val isSelected = selectedRelationship == rel
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B))
                                .clickable { selectedRelationship = rel }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = rel,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Colore distintivo:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colorPalette.forEach { (hex, _) ->
                        val col = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedRelationship, selectedColorHex)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
