package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String = "Personale", // "Personale", "Mamma", "Papà", "Figlio/a", "Nonno/a", "Paziente assistito", "Altro"
    val avatarColorHex: String = "#0284C7",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
