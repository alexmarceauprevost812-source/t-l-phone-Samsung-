#!/usr/bin/env bash
# Installe le SDK Android léger sur LexOS (Linux), sans Android Studio.
# À lancer UNE fois :  bash installer-sdk.sh

set -e
echo "======================================================"
echo "   Installation du SDK Android pour LexOS"
echo "======================================================"
echo ""

# 1. Java (obligatoire)
if ! command -v java >/dev/null 2>&1; then
  echo "[1/4] Installation de Java..."
  sudo apt update
  sudo apt install -y default-jdk unzip wget
else
  echo "[1/4] Java déjà présent : $(java -version 2>&1 | head -1)"
fi

# 2. Dossier du SDK
SDK="$HOME/Android/Sdk"
mkdir -p "$SDK/cmdline-tools"
echo "[2/4] SDK dans : $SDK"

# 3. Télécharger les command-line tools
if [ ! -d "$SDK/cmdline-tools/latest" ]; then
  echo "[3/4] Téléchargement des outils Android..."
  cd /tmp
  wget -q -O cmdtools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  unzip -q -o cmdtools.zip -d "$SDK/cmdline-tools"
  mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm cmdtools.zip
else
  echo "[3/4] Outils déjà présents."
fi

# 4. Variables d'environnement
export ANDROID_HOME="$SDK"
export PATH="$PATH:$SDK/cmdline-tools/latest/bin:$SDK/platform-tools"

# Ajoute au .bashrc pour les prochaines fois
if ! grep -q "ANDROID_HOME" "$HOME/.bashrc"; then
  {
    echo ""
    echo "# Android SDK (LexOS)"
    echo "export ANDROID_HOME=\"$SDK\""
    echo "export PATH=\"\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools\""
  } >> "$HOME/.bashrc"
fi

# 5. Accepter les licences + installer les paquets nécessaires
echo "[4/4] Installation des paquets Android (licences, platform-tools, build-tools)..."
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo ""
echo "======================================================"
echo "   TERMINÉ !"
echo "======================================================"
echo "Ferme ce terminal, rouvre-en un neuf, puis lance :"
echo "   bash construire-apk.sh"
echo ""
