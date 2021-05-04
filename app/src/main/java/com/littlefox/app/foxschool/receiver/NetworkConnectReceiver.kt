package com.littlefox.app.foxschool.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.widget.Toast
import com.littlefox.app.foxschool.R
import com.littlefox.app.foxschool.common.NetworkUtil


class NetworkConnectReceiver : BroadcastReceiver()
{
    override fun onReceive(context : Context, intent : Intent)
    {
        if(NetworkUtil.isConnectNetwork(context) === false)
        {
            Toast.makeText(context, context.resources.getString(R.string.message_toast_network_error), Toast.LENGTH_LONG).show()
        }
    }

    fun register(context : Context)
    {
        context.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    fun unregister(context : Context)
    {
        context.unregisterReceiver(this)
    }
}