package com.lexos.launcher

import android.app.Activity
import android.view.View
import android.view.animation.AccelerateInterpolator

// Anime l'icone (petit zoom + fondu) puis lance l'app, avec une transition systeme.
object LaunchAnim {

    fun openWithAnimation(activity: Activity, clicked: View, packageName: String) {
        clicked.isEnabled = false
        clicked.animate()
            .scaleX(1.25f)
            .scaleY(1.25f)
            .alpha(0.4f)
            .setDuration(170)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                AppRepository.launch(activity, packageName)
                // Transition d'ouverture (zoom depuis l'icone sur Android recent)
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
                }, 350)
            }
            .start()
    }
}
