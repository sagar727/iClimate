package com.loopcreations.iclimate.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.loopcreations.iclimate.MainActivity
import com.loopcreations.iclimate.R
import java.util.Locale

class MyTtsService : Service(){

    private lateinit var tts: TextToSpeech
    private val NOTIFICATION_ID = 1234

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ttsString = intent?.getStringExtra("ttsString")
        if (ttsString != null) {
            startForegroundService(ttsString)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(ttsString: String) {
        // Create an Intent for the notification to launch the MainActivity
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        createNotificationChannel()

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(this, "ttsChannel")
            .setContentTitle("TTS Service")
            .setContentText("Text to Speech is running")
            .setSmallIcon(R.drawable.baseline_device_thermostat_24)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Start the service as a foreground service
        startForeground(NOTIFICATION_ID, notification)

        // Start the text-to-speech functionality
        forecastTextToSpeech(ttsString)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "ttsChannel",
            "TTS Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Notification for TTS service"

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun forecastTextToSpeech(currentTempText: String) {
        tts = TextToSpeech(this){status ->
            if(status == TextToSpeech.SUCCESS){
                val result = tts.setLanguage(Locale.getDefault())
                tts.setSpeechRate(0.8F)
                tts.setPitch(1.2F)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.speak(
                        "Language not supported",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                }else{
                    tts.speak(currentTempText, TextToSpeech.QUEUE_FLUSH,null,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(p0: String?) {

                        }

                        override fun onDone(p0: String?) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(p0: String?) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            tts.stop()
                            tts.shutdown()
                        }
                    })
                }
            }else{
                Log.e("MyTtsService","Initialization failed")
            }
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }
}