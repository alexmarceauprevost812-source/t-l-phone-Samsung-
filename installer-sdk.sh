#!/usr/bin/env bash
# Installe TOUT ce qu'il faut pour compiler l'APK sur LexOS : Java, Gradle, SDK Android.
# À lancer UNE fois :  bash installer-sdk.sh

set -e
echo "======================================================"
echo "   Installation des outils Android pour LexOS"
echo "======================================================"
echo ""

# 1. Java + utilitaires
if ! command -v java >/dev/null 2>&1; then
  echo "[1/5] Installation de Java..."
  sudo apt update
  sudo apt install -y default-jdk unzip wget curl
else
  echo "[1/5] Java déjà présent : $(java -version 2>&1 | head -1)"
  sudo apt install -y unzip wget curl >/dev/null 2>&1 || true
fi

# 2. Gradle 8.7 (téléchargement direct, version compatible avec le projet)
GRADLE_DIR="$HOME/gradle-8.7"
if [ ! -x "$GRADLE_DIR/bin/gradle" ]; then
  echo "[2/5] Téléchargement de Gradle 8.7..."
  cd /tmp
  wget -q -O gradle.zip "https://services.gradle.org/distributions/gradle-8.7-bin.zip"
  unzip -q -o gradle.zip -d "$HOME"
  rm gradle.zip
else
  echo "[2/5] Gradle déjà présent."
fi

# 3. SDK Android (command-line tools)
SDK="$HOME/Android/Sdk"
mkdir -p "$SDK/cmdline-tools"
if [ ! -d "$SDK/cmdline-tools/latest" ]; then
  echo "[3/5] Téléchargement du SDK Android..."
  cd /tmp
  wget -q -O cmdtools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  unzip -q -o cmdtools.zip -d "$SDK/cmdline-tools"
  mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm cmdtools.zip
else
  echo "[3/5] SDK déjà présent."
fi

# 4. Variables d'environnement (dans .bashrc pour les prochaines fois)
export ANDROID_HOME="$SDK"
export PATH="$GRADLE_DIR/bin:$SDK/cmdline-tools/latest/bin:$SDK/platform-tools:$PATH"

if ! grep -q "ANDROID_HOME" "$HOME/.bashrc"; then
  {
    echo ""
    echo "# Outils Android (LexOS)"
    echo "export ANDROID_HOME=\"$SDK\""
    echo "export PATH=\"$GRADLE_DIR/bin:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH\""
  } >> "$HOME/.bashrc"
fi

# 5. Licences + paquets Android nécessaires
echo "[4/5] Acceptation des licences Android..."
yes | sdkmanager --licenses >/dev/null 2>&1 || true
echo "[5/5] Installation des paquets Android (peut prendre 2-3 min)..."
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo ""
echo "======================================================"
echo "   TERMINÉ !"
echo "======================================================"
echo "Maintenant, lance la compilation :"
echo "   bash construire-apk.sh"
echo ""
echo "(Si 'gradle' n'est pas reconnu, ferme ce terminal,"
echo " rouvre-en un neuf, reviens dans le dossier et relance.)"
echo ""
