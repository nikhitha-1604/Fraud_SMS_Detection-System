package com.example.fraudsms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Give us more time for the network call
        val pendingResult = goAsync()

        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val fullMessage = messages.joinToString(separator = "") { it.messageBody ?: "" }.trim()

            if (fullMessage.isEmpty()) {
                pendingResult.finish()
                return
            }

            ensureNotificationChannel(context)

            ApiClient.service.predictSms(SmsRequest(fullMessage))
                .enqueue(object : Callback<SmsResponse> {
                    override fun onResponse(call: Call<SmsResponse>, response: Response<SmsResponse>) {
                        try {
                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body != null) {
                                    showNotification(
                                        context = context,
                                        label = body.label,
                                        spamProbability = body.spamProbability,
                                        smsText = fullMessage
                                    )
                                }
                            } else {
                                val errBody = try {
                                    response.errorBody()?.string()
                                } catch (_: Exception) {
                                    null
                                }
                                Log.e(
                                    "SmsReceiver",
                                    "API error: ${response.code()} body=$errBody"
                                )
                                showFallbackNotification(context, fullMessage)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }

                    override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                        try {
                            Log.e("SmsReceiver", "API failure", t)
                            showFallbackNotification(context, fullMessage)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                })
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Receiver exception", e)
            pendingResult.finish()
        }
    }

    private fun showFallbackNotification(context: Context, smsText: String) {
        // If the API fails, still alert the user that an SMS was received.
        showNotification(
            context = context,
            label = "unknown",
            spamProbability = 0.0,
            smsText = smsText
        )
    }

    private fun showNotification(
        context: Context,
        label: String,
        spamProbability: Double,
        smsText: String
    ) {
        // On Android 13+, POST_NOTIFICATIONS is required.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val title = when (label.lowercase()) {
            "spam" -> "Suspicious SMS detected"
            "genuine" -> "Safe SMS"
            else -> "SMS received"
        }

        val content = when (label.lowercase()) {
            "spam" -> "Risk: ${"%.2f".format(spamProbability)} (spam probability)"
            "genuine" -> "Risk: ${"%.2f".format(spamProbability)} (spam probability)"
            else -> "We could not classify the message."
        }

        val channelId = CHANNEL_ID

        val truncatedSms = if (smsText.length > 220) smsText.substring(0, 220) + "..." else smsText

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$content\n\nMessage:\n$truncatedSms")
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "fraud_sms_channel"
        private const val CHANNEL_NAME = "Fraud SMS Detector"

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing != null) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}

