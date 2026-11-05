package com.lexos.launcher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

// Service qui ecoute les notifications du telephone.
// Garde les titres recents + les apps (paquets) qui en ont.
// Necessite que l'utilisateur active "Acces aux notifications" pour LexOS.
class LexNotifService : NotificationListenerService() {

    override fun onListenerConnected() { snapshot() }
    override fun onNotificationPosted(sbn: StatusBarNotification?) { snapshot() }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) { snapshot() }

    private fun snapshot() {
        try {
            val notifs = activeNotifications ?: return
            // Ignore les notifications "silencieuses" / permanentes (ongoing) pour ne garder que l'utile
            val useful = notifs.filter { !it.isOngoing }
            recent = useful.mapNotNull { sbn ->
                sbn.notification?.extras?.getCharSequence("android.title")?.toString()
            }.filter { it.isNotBlank() }.distinct().takeLast(6)
            packages = useful.map { it.packageName }.distinct()
            count = useful.size
            listener?.invoke()
        } catch (e: Exception) {
            // ignore
        }
    }

    companion object {
        var recent: List<String> = emptyList()
        var packages: List<String> = emptyList()
        var count: Int = 0
        // L'activite s'abonne ici pour se rafraichir quand une notif arrive
        var listener: (() -> Unit)? = null
    }
}
