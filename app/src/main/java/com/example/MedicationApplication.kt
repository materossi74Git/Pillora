package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.MedicationRepository
import com.example.notification.NotificationHelper

class MedicationApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: MedicationRepository by lazy {
        MedicationRepository(
            database.medicationDao(),
            database.healthDao(),
            database.profileDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
