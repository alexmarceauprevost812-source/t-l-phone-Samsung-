package com.lexos.launcher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

// Service qui ecoute les notifications du telephone et garde les titres recents.
// Necessite que l'utilisateur active "Acces aux notifications" pour LexOS.
class LexNotifService : NotificationListenerService() {

    override fun onListenerConnected() {
        snapshot()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        snapshot()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        snapshot()
    }

    private fun snapshot() {
        try {
            val notifs = activeNotifications ?: return
            val titles = notifs.mapNotNull { sbn ->
                val extras = sbn.notification?.extras ?: return@mapNotNull null
                extras.getCharSequence("android.title")?.toString()
            }.filter { it.isNotBlank() }
            recent = titles.distinct().takeLast(6)
            count = notifs.size
        } catch (e: Exception) {
            // ignore
        }
    }

    companion object {
        var recent: List<String> = emptyList()
        var count: Int = 0
    }
}
