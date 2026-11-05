# ☀️ À lire au réveil — Alex

Salut Alex ! Pendant que tu dormais, j'ai mis tout le code du launcher LexOS à jour
avec le design final qu'on a validé cette nuit. Tout est poussé sur GitHub.

## Ce qui est fait (design final)

- **Page d'accueil** : apps officielles Samsung (téléphone, contacts, messages,
  photo, navigateur…), horloge en haut, effet Matrix orange en fond.
- **Glisser vers la droite** : tes favoris en gros plan, avec halo fluo + Matrix.
- **Glisser vers le bas** (sur l'horloge) : toutes les apps.
- **Appui long sur l'horloge** : ouvre les paramètres.
- **Paramètres** : mode jour/soir, couleur boutons/texte/fond, photo de fond + voile,
  **choix de police** (Normale, Monospace, Arrondie, Techno), **effet Matrix on/off**.
- **Animation** d'ouverture d'app (zoom/fondu).

## Ce qu'il reste à faire (3 étapes, ~15 min)

Je ne pouvais pas compiler l'APK moi-même (l'outil Google est bloqué dans mon
environnement). Mais sur ton Alienware c'est simple :

### 1. Récupérer le code à jour
Ouvre un terminal dans le dossier du projet et tape :
```
git pull origin supabase
```

### 2. Compiler
- Ouvre le dossier dans **Android Studio**.
- Attends qu'il télécharge Gradle (1re fois seulement).
- Menu **Build > Build APK(s)**.
- L'APK sort dans `app/build/outputs/apk/debug/app-debug.apk`.

Ou en terminal : `./gradlew assembleDebug`

### 3. Installer sur le A55 (ou S25 Ultra)
Branche le téléphone (débogage USB activé), puis :
```
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Appuie sur Accueil → choisis **LexOS**.

## Important

Ce code est écrit mais **pas encore testé/compilé** (je n'ai pas pu le faire ici).
S'il y a une petite erreur de compilation au premier essai, c'est normal — copie-moi
le message d'erreur et je te le corrige en deux minutes. On est pas loin du but. 💪

Bonne journée Alex !
