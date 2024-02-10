package com.loopcreations.iclimate.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.loopcreations.iclimate.R
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class SettingsFragment : PreferenceFragmentCompat() {

    private val requestPermission = activityResultLauncher()
    private lateinit var alarmManager: AlarmManager
    private lateinit var forecastSpeechSwitch: SwitchPreferenceCompat
    private lateinit var startIntent: Intent
    private lateinit var workManager: WorkManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundColor(resources.getColor(R.color.night, null))
        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val unitPrefCategory = findPreference<PreferenceCategory>("unit_title")
        val notificationPrefCategory = findPreference<PreferenceCategory>("notification_title")

        workManager = WorkManager.getInstance(requireContext())

        unitPrefCategory?.layoutResource = R.layout.preference_category_layout
        notificationPrefCategory?.layoutResource = R.layout.preference_category_layout

        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        forecastSpeechSwitch = findPreference("textToSpeech")!!
        forecastSpeechSwitch.setOnPreferenceChangeListener { _, newValue ->
            var result = true
//            var res = true
//            if (newValue == true) {
//                res = forecastServices()
//            } else {
//                workManager.cancelAllWorkByTag("Announcement")
//            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!isNotificationEnabled()) {
                    result = requestNotification()
                    if (newValue == true) {
                        createAnnouncementChannel()
                        val res = forecastServices()
                        if(res){
                            Toast.makeText(requireContext(),"scheduled",Toast.LENGTH_LONG).show()
                        }
                    } else {
                        workManager.cancelAllWorkByTag("Announcement")
                    }
                } else {
                    if (newValue == true) {
                        createAnnouncementChannel()
                        val res = forecastServices()
                        if(res){
                            Toast.makeText(requireContext(),"scheduled",Toast.LENGTH_LONG).show()
                        }
                    } else {
                        workManager.cancelAllWorkByTag("Announcement")
                    }
                }
                result
            } else {
                if (newValue == true) {
                    createAnnouncementChannel()
                    val res = forecastServices()
                    if(res){
                        Toast.makeText(requireContext(),"scheduled",Toast.LENGTH_LONG).show()
                    }
                } else {
                    workManager.cancelAllWorkByTag("Announcement")
                }
                true
            }


//            res
        }

        val notificationSwitch = findPreference<SwitchPreferenceCompat>("notification")
        notificationSwitch?.setOnPreferenceChangeListener { _, newValue ->
            var result = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!isNotificationEnabled()) {
                    result = requestNotification()
                    if (newValue == true) {
                        createNotificationChannel()
                        scheduleNotification()
                    } else {
                        workManager.cancelAllWorkByTag("Notifications")
                    }
                } else {
                    if (newValue == true) {
                        createNotificationChannel()
                        scheduleNotification()
                    } else {
                        workManager.cancelAllWorkByTag("Notifications")
                    }
                }
                result
            } else {
                if (newValue == true) {
                    createNotificationChannel()
                    scheduleNotification()
                } else {
                    workManager.cancelAllWorkByTag("Notifications")
                }
                true
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun forecastServices(): Boolean {
        val time = getTime()

        startIntent = Intent(activity, AnnouncementReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            100,
            startIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return when {
                alarmManager.canScheduleExactAlarms() -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                    true
                }

                else -> {
                    startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    false
                }
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            return true
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "forecast_channel",
            "Forecast Notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Notification Settings For Forecast"

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createAnnouncementChannel() {
        val channel = NotificationChannel(
            "ttsChannel",
            "TTS Notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Notification for TTS service"

        val notificationManager = requireContext().getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun getTime(): Long {
//        val hour = 21
        val minute = 0
//        val second = 0

        val currentDate = Date()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMin = Calendar.getInstance().get(Calendar.MINUTE)

        if (currentHour >= 8 && currentMin > 0) {
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 8)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            return calendar.timeInMillis
        } else {
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.set(Calendar.HOUR_OF_DAY, 8)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            return calendar.timeInMillis
        }
    }

    private fun isNotificationEnabled(): Boolean {
        val notificationManager =
            activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    private fun requestNotification(): Boolean {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                return true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                view?.let {
                    Snackbar.make(
                        requireContext(),
                        it,
                        "Please enable notification to get climate details for your current location",
                        Snackbar.LENGTH_LONG
                    )
                        .setBackgroundTint(Color.BLACK)
                        .setTextColor(Color.WHITE)
                        .setAction("Settings") {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity?.packageName)
                            startActivity(intent)
                        }
                        .show()
                    return false
                }
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                return false
            }
        }
        return true
    }

    private fun activityResultLauncher() =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { status ->
            if (status) {
                Log.i("noti_status", status.toString())
            } else {
                Log.i("noti_status", status.toString())
            }
        }

    private fun scheduleNotification() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval = 6, // Repeat interval in hours
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .addTag("Notifications")
            .build()

        workManager.enqueue(workRequest)
    }
}