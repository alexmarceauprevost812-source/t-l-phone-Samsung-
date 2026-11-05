#!/usr/bin/env bash
# Raccourci LexOS : compile l'APK du launcher et l'installe sur le telephone branche.
# Double-clic, ou dans un terminal : ./construire-apk.sh

set -e
cd "$(dirname "$0")"

echo "=============================================="
echo "   LexOS Launcher - construction de l'APK"
echo "=============================================="
echo ""

# 1. Verifier Java
if ! command -v java >/dev/null 2>&1; then
  echo "[X] Java n'est pas installe."
  echo "    Installe-le :  sudo apt install default-jdk"
  exit 1
fi

# 2. S'assurer que gradlew est executable
if [ ! -f gradlew ]; then
  echo "[i] gradlew absent, generation du wrapper..."
  gradle wrapper || {
    echo "[X] Gradle introuvable. Ouvre le projet une fois dans Android Studio,"
    echo "    puis relance ce script."
    exit 1
  }
fi
chmod +x gradlew

# 3. Compiler
echo "[1/2] Compilation de l'APK (ca peut prendre 1-2 minutes)..."
./gradlew assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK" ]; then
  echo "[X] L'APK n'a pas ete genere. Regarde les erreurs plus haut."
  exit 1
fi
echo "[OK] APK pret : $APK"
echo ""

# 4. Installer si un telephone est branche
if command -v adb >/dev/null 2>&1; then
  if adb get-state >/dev/null 2>&1; then
    echo "[2/2] Telephone detecte, installation..."
    adb install -r "$APK"
    echo ""
    echo "[OK] Installe ! Appuie sur Accueil sur ton telephone et choisis LexOS."
  else
    echo "[i] Aucun telephone branche. Pour installer plus tard :"
    echo "    adb install -r $APK"
  fi
else
  echo "[i] adb absent. Pour l'installer, copie ce fichier sur ton telephone :"
  echo "    $APK"
fi

echo ""
echo "Termine."
