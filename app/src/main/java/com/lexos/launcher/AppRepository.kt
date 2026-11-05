package com.lexos.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppRepository {

    // Les paquets mis en avant dans le carrousel des favoris, dans l'ordre.
    // Remplace par les vrais noms de paquets de tes apps une fois connus.
    val FAVORITE_PACKAGES = listOf(
        "com.lexos.codexlocal",
        "com.lexos.marceau",
        "com.lexos.tva"
    )

    // Les paquets de la barre d'accueil fixe (max 5).
    val DOCK_PACKAGES = listOf(
        "dialer",      // telephone
        "sms",         // messages
        "camera",      // appareil photo
        "browser",     // navigateur
        "settings"     // reglages
    )

    fun loadAll(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val self = context.packageName
        return pm.queryIntentActivities(intent, 0)
            .mapNotNull { ri ->
                val pkg = ri.activityInfo.packageName
                if (pkg == self) return@mapNotNull null
                AppInfo(
                    label = ri.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = ri.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    // Trouve les favoris existants, dans l'ordre demande.
    fun favorites(all: List<AppInfo>): List<AppInfo> {
        val byPkg = all.associateBy { it.packageName }
        return FAVORITE_PACKAGES.mapNotNull { byPkg[it] }
    }

    // Resout un role de la barre d'accueil (telephone, camera...) vers une vraie app.
    fun resolveDock(context: Context, role: String): AppInfo? {
        val pm = context.packageManager
        val intent: Intent? = when (role) {
            "dialer" -> Intent(Intent.ACTION_DIAL)
            "sms" -> Intent(Intent.ACTION_MAIN).apply { addCategory("android.intent.category.APP_MESSAGING") }
            "camera" -> Intent("android.media.action.STILL_IMAGE_CAMERA")
            "browser" -> Intent(Intent.ACTION_MAIN).apply { addCategory("android.intent.category.APP_BROWSER") }
            "settings" -> Intent(android.provider.Settings.ACTION_SETTINGS)
            else -> null
        } ?: return null

        val ri = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) ?: return null
        val ai = ri.activityInfo ?: return null
        return try {
            AppInfo(
                label = ri.loadLabel(pm).toString(),
                packageName = ai.packageName,
                icon = ri.loadIcon(pm)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun launch(context: Context, packageName: String) {
        val pm = context.packageManager
        val launch = pm.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        }
    }
}
