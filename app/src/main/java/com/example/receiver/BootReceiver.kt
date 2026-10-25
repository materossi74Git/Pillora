package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MedicationApplication
import com.example.notification.MedicationAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? MedicationApplication ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val medWithSchedules = app.repository.activeMedicationsWithSchedules.first()
                for (item in medWithSchedules) {
                    for (schedule in item.schedules) {
                        if (schedule.customNotificationEnabled) {
                            MedicationAlarmScheduler.scheduleDoseReminder(
                                context,
                                schedule,
                                item.medication
                            )
                        }
                    }
                }
            }
        }
    }
}
