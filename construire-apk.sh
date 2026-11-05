#!/usr/bin/env bash
# Compile l'APK du launcher LexOS et l'installe sur le téléphone branché.
# À lancer APRÈS installer-sdk.sh :  bash construire-apk.sh

set -e
cd "$(dirname "$0")"

echo "=============================================="
echo "   LexOS Launcher - construction de l'APK"
echo "=============================================="
echo ""

# Charge les variables si pas déjà dans l'environnement
SDK="$HOME/Android/Sdk"
GRADLE_DIR="$HOME/gradle-8.7"
export ANDROID_HOME="${ANDROID_HOME:-$SDK}"
export PATH="$GRADLE_DIR/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# Vérifs
if ! command -v java >/dev/null 2>&1; then
  echo "[X] Java manquant. Lance d'abord :  bash installer-sdk.sh"
  exit 1
fi
if ! command -v gradle >/dev/null 2>&1 && [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
  echo "[X] Gradle manquant. Lance d'abord :  bash installer-sdk.sh"
  exit 1
fi
GRADLE_BIN="$(command -v gradle || echo "$GRADLE_DIR/bin/gradle")"

# Indique où est le SDK au projet
echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "[i] SDK : $ANDROID_HOME"

# Génère le wrapper si absent (nécessite internet, une seule fois)
if [ ! -f gradlew ]; then
  echo "[i] Génération du wrapper Gradle..."
  "$GRADLE_BIN" wrapper --gradle-version 8.7
fi
chmod +x gradlew 2>/dev/null || true

# Compile
echo "[1/2] Compilation de l'APK (1-3 minutes la première fois)..."
if [ -f gradlew ]; then
  ./gradlew assembleDebug
else
  "$GRADLE_BIN" assembleDebug
fi

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK" ]; then
  echo "[X] L'APK n'a pas été généré. Copie-moi les erreurs ci-dessus."
  exit 1
fi
echo "[OK] APK prêt : $APK"
echo ""

# Installe si un téléphone est branché
if command -v adb >/dev/null 2>&1 && adb get-state >/dev/null 2>&1; then
  echo "[2/2] Téléphone détecté, installation..."
  adb install -r "$APK"
  echo ""
  echo "[OK] Installé ! Appuie sur Accueil sur ton téléphone et choisis LexOS."
else
  echo "[i] Aucun téléphone branché. L'APK est ici :"
  echo "    $(pwd)/$APK"
  echo "    Copie-le sur ton téléphone et ouvre-le pour l'installer."
fi
echo ""
echo "Terminé."
