package com.movtery.zalithlauncher.terracotta

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.terracotta.Terracotta.mode
import com.movtery.zalithlauncher.terracotta.Terracotta.setWaiting
import com.movtery.zalithlauncher.terracotta.Terracotta.state
import net.burningtnt.terracotta.TerracottaAndroidAPI
import java.io.IOException
import kotlin.concurrent.Volatile

/**
 * [Reference FCL](https://github.com/FCL-Team/FoldCraftLauncher/blob/5926006/FCL/src/main/java/com/tungsten/fcl/terracotta/TerracottaVPNService.java)
 */
@SuppressLint("VpnServicePolicy")
class TerracottaVPNService : VpnService() {
    private var notificationManager: NotificationManager? = null
    private var currentStateText: String? = null

    @Volatile
    private var isStopping = false

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true

        val action = intent?.action
        Log.d(TAG, "onStartCommand, action = $action")

        if (notificationManager == null) {
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        }

        if (ACTION_STOP == action) {
            isStopping = true
            cleanup()
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        if (ACTION_UPDATE_STATE == action) {
            if (intent.hasExtra(EXTRA_STATE_TEXT)) {
                currentStateText = intent.getStringExtra(EXTRA_STATE_TEXT)
            }

            if (!isStopping) {
                val n = buildVpnNotification()
                notificationManager!!.notify(VPN_NOTIFICATION_ID, n)
            }
            return START_STICKY
        }

        val fromDelete = intent != null && intent.getBooleanExtra(EXTRA_FROM_DELETE, false)

        if (ACTION_REPOST == action && fromDelete && !isStopping) {
            Log.d(TAG, "Repost VPN notification after user cleared it.")
            if (intent.hasExtra(EXTRA_STATE_TEXT)) {
                currentStateText = intent.getStringExtra(EXTRA_STATE_TEXT)
            }
            val notification = buildVpnNotification() ?: return START_NOT_STICKY
            startForeground(VPN_NOTIFICATION_ID, notification)
            return START_STICKY
        }

        isStopping = false

        createNotificationChannelIfNeeded()

        val notification = buildVpnNotification() ?: return START_NOT_STICKY
        startForeground(VPN_NOTIFICATION_ID, notification)

        val vpnBuilder: Builder = Builder().setSession("Terracotta Connection")

        try {
            vpnBuilder.addDisallowedApplication(packageName)
        } catch (_: PackageManager.NameNotFoundException) {
        }

        val request = TerracottaAndroidAPI.getPendingVpnServiceRequest()
        vpnInterface = request.startVpnService(vpnBuilder)

        return START_STICKY
    }

    override fun onRevoke() {
        Log.w(TAG, "onRevoke(): preempted by another VPN or revoked by user; tearing down.")
        isStopping = true
        setWaiting(this, false)
        cleanup()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy(): vpn service finished")
        isStopping = true
        setWaiting(this, false)
        cleanup()
        super.onDestroy()
    }

    private fun createNotificationChannelIfNeeded() {
        if (notificationManager == null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Terracotta VPN",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Terracotta VPN state"
        channel.setShowBadge(false)
        notificationManager!!.createNotificationChannel(channel)
    }

    private fun buildVpnNotification(): Notification? {
        val mode = mode ?: return null

        val title: String = getString(R.string.terracotta_notification_title)
        val modeText = if (mode == Terracotta.Mode.Host) {
            getString(R.string.terracotta_player_kind_host)
        } else {
            getString(R.string.terracotta_player_kind_guest)
        }
        if (currentStateText == null) {
            val state = state.value
            if (state != null && state !is TerracottaState.Waiting) {
                currentStateText = "terracotta_status_$state" //TODO 本地化
            }
        }
        val contentText = String.format(
            getString(R.string.terracotta_notification_desc),
            modeText,
            currentStateText
        )

        val builder = Notification.Builder(this, CHANNEL_ID)

        val deleteIntent = Intent(this, TerracottaVPNService::class.java)
            .setAction(ACTION_REPOST)
            .putExtra(EXTRA_FROM_DELETE, true)
            .putExtra(EXTRA_STATE_TEXT, currentStateText)

        val deletePendingIntent = PendingIntent.getService(
            this,
            114,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder.setSmallIcon(1) //TODO icon
            .setContentTitle(title)
            .setContentText(contentText)
            .setWhen(System.currentTimeMillis())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setDeleteIntent(deletePendingIntent)

        return builder.build()
    }

    private fun cleanup() {
        Log.d(TAG, "cleanup(): close tun & cancel notification")

        if (notificationManager != null) {
            notificationManager!!.cancel(VPN_NOTIFICATION_ID)
        }

        if (vpnInterface != null) {
            try {
                vpnInterface!!.close()
            } catch (_: IOException) {
            }
            vpnInterface = null
        }

        isRunning = false
    }

    companion object {
        private const val TAG =                     "TerracottaVPNService"

        private const val CHANNEL_ID =              "terracotta_vpn_channel"
        private const val VPN_NOTIFICATION_ID = 1

        const val ACTION_START: String =            "net.burningtnt.terracotta.action.START"
        const val ACTION_STOP: String =             "net.burningtnt.terracotta.action.STOP"
        const val ACTION_REPOST: String =           "net.burningtnt.terracotta.action.REPOST"
        const val ACTION_UPDATE_STATE: String =     "net.burningtnt.terracotta.action.UPDATE_STATE"

        private const val EXTRA_FROM_DELETE =       "from_delete"
        const val EXTRA_STATE_TEXT: String =        "terracotta_state_text"

        var isRunning: Boolean = false
            private set
    }
}
