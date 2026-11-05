package com.lexos.launcher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

// Service qui ecoute les notifications du telephone.
// Garde les titres recents + les apps (paquets) qui en ont.
// Necessite que l'utilisateur active "Acces aux notifications" pour LexOS.
class LexNotifService : NotificationListenerService() {

    override fun onListenerConnected() { instance = this; snapshot() }
    override fun onListenerDisconnected() { instance = null }
    override fun onNotificationPosted(sbn: StatusBarNotification?) { snapshot() }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) { snapshot() }

    private fun snapshot() {
        try {
            val notifs = activeNotifications ?: return
            // Ignore les notifications permanentes (ongoing) pour ne garder que l'utile
            val useful = notifs.filter { !it.isOngoing }
            items = useful.mapNotNull { sbn ->
                val ex = sbn.notification?.extras ?: return@mapNotNull null
                val title = ex.getCharSequence("android.title")?.toString() ?: ""
                val text = ex.getCharSequence("android.text")?.toString() ?: ""
                if (title.isBlank() && text.isBlank()) return@mapNotNull null
                NotifItem(sbn.packageName, title, text, sbn.postTime, sbn.key)
            }.sortedByDescending { it.time }
            recent = items.map { it.title }.filter { it.isNotBlank() }.distinct().take(6)
            packages = items.map { it.pkg }.distinct()
            count = items.size
            listener?.invoke()
        } catch (e: Exception) {
            // ignore
        }
    }

    // Supprime une notification (quand on la glisse dans LexOS)
    fun dismiss(key: String) {
        try { cancelNotification(key) } catch (_: Exception) {}
    }

    data class NotifItem(val pkg: String, val title: String, val text: String, val time: Long, val key: String)

    companion object {
        var instance: LexNotifService? = null
        var items: List<NotifItem> = emptyList()
        var recent: List<String> = emptyList()
        var packages: List<String> = emptyList()
        var count: Int = 0
        // L'activite s'abonne ici pour se rafraichir quand une notif arrive
        var listener: (() -> Unit)? = null
    }
}
