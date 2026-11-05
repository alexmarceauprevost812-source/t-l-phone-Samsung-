# 📱 Guide LexOS Launcher

Ton écran d'accueil LexOS pour Samsung (A55, S25 Ultra et tout Android 8+).
Sans root. Noir et orange, effet Matrix, favoris en gros plan, tout personnalisable.

---

## 🧭 Les 3 écrans

| Geste | Ce que ça fait |
|---|---|
| **Accueil** (page de départ) | Tes apps choisies, l'heure, la date, la météo, les notifications |
| **Glisser → à droite** ou tap sur **« Favoris → »** | Tes favoris en **gros plan**, un par écran, avec halo fluo |
| **Glisser ↓ vers le bas** (sur l'horloge) ou **tap sur l'horloge** | **Toutes** les apps du téléphone |

---

## 🏠 Mettre une app sur l'accueil, en favori ou dans le dock

1. Glisse vers le bas (ou tape sur l'horloge) pour voir toutes les apps.
2. **Appuie ~1 seconde** sur une app.
3. Choisis :
   - **🏠 Ajouter à l'accueil** → elle apparaît sur la page d'accueil
   - **⭐ Ajouter aux favoris** → elle apparaît en gros plan (page Favoris)
   - **⬇ Ajouter au dock** → elle apparaît dans la barre du bas

Même appui long pour **retirer**.

Au départ, l'accueil montre les apps de base (téléphone, messages, photo…) tant que tu n'as rien choisi.

---

## ⭐ Page Favoris (gros plan)

- Flèches **◀ ▶** sur les côtés pour passer d'un favori à l'autre.
- Compteur en bas (ex. « 2 / 3 ») pour te situer.
- **Tap sur l'icône** ou sur **« Ouvrir »** pour lancer l'app.
- En haut : météo à gauche, notifications à droite.

---

## 🔔 Notifications

- Sur l'accueil, à côté de la météo : une pastille avec les **icônes des apps** qui ont des notifications et le nombre.
- **Tap** dessus → un panneau LexOS **descend** par-dessus l'écran.
- Dans le panneau :
  - **Tap** sur une notification = ouvre l'app
  - **Appui long** = l'efface
  - **« Tout effacer »** en haut
  - Tap sur le fond sombre (ou bouton retour) = fermer

### Première fois : activer l'accès
La pastille affiche **« 🔔 activer »**. Tape dessus → la liste « Accès aux notifications » s'ouvre → active **LexOS** → reviens.
Sans ça, LexOS ne peut pas voir tes notifications (c'est une règle Android).

---

## 🌤️ Météo

Affichée sous la date, avec un emoji (☀️ 🌧️ ❄️ ⛈️…).
Elle se met à jour toute seule toutes les 15 minutes. Demande internet.
Par défaut c'est la météo de **Normandin, QC**.

---

## ⬇ Le dock (barre du bas)

De gauche à droite : **⚙️ Réglages LexOS**, **Téléphone**, **Contacts**, puis tes apps ajoutées.
Il **défile de gauche à droite** si tu en mets beaucoup.

---

## ⚙️ Réglages LexOS

Ouvre-les avec l'engrenage dans le dock (ou appui long sur l'horloge).

| Réglage | Ce que ça change |
|---|---|
| **Mode Soir / Jour** | Thème sombre ou clair |
| **Couleur des boutons** | 8 couleurs (orange LexOS par défaut) |
| **Couleur du texte** | Couleur des noms d'apps, titres… |
| **Couleur de fond** | Couleur de fond quand pas de photo |
| **Fond d'écran** | Choisis ta propre photo dans la galerie, ou retire-la |
| **Voile sombre** | Assombrit la photo pour mieux lire |
| **Police d'écriture** | Normale, Monospace, Arrondie, Techno — s'applique partout |
| **Effet Matrix** | Active / désactive la pluie de caractères |
| **Tous les paramètres Samsung** | Ouvre les réglages complets du téléphone |
| **Réinitialiser le thème** | Remet tout par défaut |

Aperçu en direct en bas de l'écran de réglages.

---

## ✨ Animations

- Quand tu ouvres une app : l'icône fait un petit **pop** (rapetisse, grossit, s'estompe).
- Le panneau de notifications **glisse** depuis le haut.
- Les favoris **glissent** entre eux.

---

## 🔧 Mettre LexOS par défaut / revenir à Samsung

- **Par défaut** : appuie sur Accueil → choisis **LexOS** → « Toujours ».
  Ou : Paramètres → Applications → Applis par défaut → Application d'accueil → LexOS.
- **Revenir à Samsung** : même menu → One UI Home.
- Désinstaller LexOS remet tout comme avant. Rien n'est touché dans le système.

---

## 🛠️ Recompiler après une modification (sur ton Alienware)

Dans le dossier du projet :
```
git pull origin supabase
bash construire-apk.sh
```
Si le téléphone est branché (débogage USB activé), ça l'installe tout seul.
Sinon l'APK est dans `app/build/outputs/apk/debug/app-debug.apk`.

Première fois seulement : `bash installer-sdk.sh` (installe Java, Gradle, SDK Android).

---

## ❓ Petits problèmes connus

- **« Aucun téléphone branché »** : vérifie `adb devices`. Si `unauthorized`, autorise sur le téléphone (écran déverrouillé, mode USB « Transfert de fichiers »).
- **Météo « indisponible »** : pas d'internet, ou le service est momentanément hors ligne.
- **Pas de notifications** : l'accès n'est pas activé (voir plus haut).
- **La police ne change pas** : ferme et rouvre LexOS (bouton Accueil) après le changement.

---

*Fait par Alex Marceau Prevost — Produit du Québec 🍁*
