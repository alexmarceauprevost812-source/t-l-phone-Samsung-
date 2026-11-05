# LexOS Launcher

Écran d'accueil LexOS pour Android (Galaxy A55, S25 Ultra, et tout téléphone Android 8+).
Sans root. Thème noir/orange, favoris en carrousel, tiroir d'apps, réglages complets.

## Ce qu'il fait

- **Carrousel de favoris** : glisse vers la droite pour tes apps favorites en grand, chacune pleine page.
- **Tiroir d'apps** : tire vers le haut (poignée orange) pour voir toutes les apps installées en grille.
- **Barre d'accueil** fixe : téléphone, messages, appareil photo, navigateur, réglages.
- **Animation d'ouverture** : l'icône fait un zoom/fondu quand tu lances une app.
- **Paramètres LexOS** (maintenir le mot « LexOS » en haut de l'écran) :
  - Mode jour / soir
  - Couleur des boutons (8 choix)
  - Couleur du texte
  - Couleur de fond
  - Ta propre photo en fond d'écran + voile sombre réglable
  - Aperçu en direct, et bouton réinitialiser

## Personnaliser tes favoris

Ouvre `app/src/main/java/com/lexos/launcher/AppRepository.kt` et remplace les
noms de paquets dans `FAVORITE_PACKAGES` par ceux de tes vraies apps, p. ex. :

```
val FAVORITE_PACKAGES = listOf(
    "com.lexos.codexlocal",
    "com.lexos.marceau",
    "com.lexos.tva"
)
```

Pour trouver le nom de paquet d'une app installée, depuis ton PC LexOS :
`adb shell pm list packages | grep <mot>`

## Compiler l'APK

1. Ouvre le dossier dans **Android Studio** (sur ton Alienware).
2. Laisse-le télécharger Gradle et les dépendances.
3. Menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. L'APK sort dans `app/build/outputs/apk/debug/app-debug.apk`.

Ou en ligne de commande depuis le dossier :
```
./gradlew assembleDebug
```

## Installer sur le téléphone

```
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Puis appuie sur le bouton Accueil du téléphone → choisis **LexOS** → « Toujours ».

## Le mettre par défaut / le retirer

- Par défaut : Paramètres Android > Applications > Applis par défaut > Application d'accueil > LexOS.
- Pour revenir à Samsung : même menu, choisis One UI Home.

Rien n'est modifié dans le système : désinstaller l'APK remet tout comme avant.
