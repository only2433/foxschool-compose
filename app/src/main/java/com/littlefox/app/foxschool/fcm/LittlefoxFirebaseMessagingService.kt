package com.littlefox.app.foxschool.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.littlefox.app.foxschool.main.MainActivity
import com.littlefox.app.foxschool.R
import com.littlefox.logmonitor.Log


class LittlefoxFirebaseMessagingService : FirebaseMessagingService()
{
    val TAG = "FoxschoolFirebaseMessagingService"
    override fun onNewToken(token : String)
    {
        super.onNewToken(token)
        Log.f("token : $token")
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage : RemoteMessage)
    {
        Log.f("remoteMessage data : " + remoteMessage.getData())
        Log.i("From: " + remoteMessage.getFrom())

        // Check if message contains a data payload.
        if(remoteMessage.getData().size > 0)
        {
            Log.i("Message data payload: " + remoteMessage.getData())
            showNotification(remoteMessage.getData().get("msg").toString())
        }
        else
        {
            Log.f("Data Empty.")
        }
    }

    private fun showNotification(message : String)
    {
        val CHANNEL_ID = "foxchool_channel_id"
        val CHANNEL_NAME = "foxchool_channel_name"
        Log.f("message : $message")
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent : PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notificationBuilder : NotificationCompat.Builder? = null
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableLights(true)
            notificationChannel.setLightColor(Color.BLUE)
            notificationManager.createNotificationChannel(notificationChannel)
            notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID) as NotificationCompat.Builder
        }
        else
        {
            notificationBuilder = NotificationCompat.Builder(this)
        }
        val soundUri : Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification : Notification = notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true).setSound(soundUri)
                .setContentIntent(pendingIntent)
                .build()
        notificationManager.notify(0, notification)
    }
}