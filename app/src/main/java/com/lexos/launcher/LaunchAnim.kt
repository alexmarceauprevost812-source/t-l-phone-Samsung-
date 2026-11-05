package com.lexos.launcher

import android.app.Activity
import android.view.View
import android.view.animation.OvershootInterpolator

// Animation "pop" a l'ouverture : l'icone rapetisse, puis grossit en s'estompant,
// puis l'app s'ouvre avec un fondu.
object LaunchAnim {

    fun openWithAnimation(activity: Activity, clicked: View, packageName: String) {
        clicked.isEnabled = false
        clicked.animate()
            .scaleX(0.82f).scaleY(0.82f)
            .setDuration(90)
            .withEndAction {
                clicked.animate()
                    .scaleX(1.5f).scaleY(1.5f)
                    .alpha(0f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator(1.4f))
                    .withEndAction {
                        AppRepository.launch(activity, packageName)
                        activity.overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        // Remet l'icone normale pour le retour a l'accueil
                        clicked.postDelayed({
                            clicked.scaleX = 1f
                            clicked.scaleY = 1f
                            clicked.alpha = 1f
                            clicked.isEnabled = true
                        }, 300)
                    }
                    .start()
            }
            .start()
    }
}
