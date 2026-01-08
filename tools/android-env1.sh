#!/usr/bin/env bash
set -Eeuo pipefail

# ==============================================================================
# android-env.sh (Prod "Béton Armé")
# - Force l'usage du SDK utilisateur (local.properties / $ANDROID_SDK_ROOT)
# - Évite les collisions avec /usr/bin/adb, /usr/bin/emulator
# - Vérifie la présence des binaires essentiels
# - Fournit des commandes sûres: test, assembleRelease, clean, doctor
# ==============================================================================

# ---------- Logging helpers ----------
log()  { printf '%s\n' "[android-env] $*"; }
warn() { printf '%s\n' "[android-env][WARN] $*" >&2; }
die()  { printf '%s\n' "[android-env][ERROR] $*" >&2; exit 1; }

# ---------- Resolve project root ----------
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

# ---------- SDK resolution ----------
# Priority:
# 1) sdk.dir from local.properties (recommended when running from project root)
# 2) ANDROID_SDK_ROOT
# 3) ANDROID_HOME
read_sdk_dir_from_local_properties() {
  local lp="${PROJECT_ROOT}/local.properties"
  [[ -f "$lp" ]] || return 1
  local val
  val="$(grep -E '^[[:space:]]*sdk\.dir=' "$lp" | tail -n1 | cut -d'=' -f2- || true)"
  [[ -n "${val:-}" ]] || return 1
  # trim whitespace
  val="${val#"${val%%[![:space:]]*}"}"
  val="${val%"${val##*[![:space:]]}"}"
  printf '%s' "$val"
  return 0
}

SDK_DIR=""
if SDK_DIR="$(read_sdk_dir_from_local_properties)"; then
  :
elif [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
  SDK_DIR="$ANDROID_SDK_ROOT"
elif [[ -n "${ANDROID_HOME:-}" ]]; then
  SDK_DIR="$ANDROID_HOME"
else
  die "Impossible de déterminer le SDK. Ajoute local.properties (sdk.dir=...) ou export ANDROID_SDK_ROOT."
fi

[[ -d "$SDK_DIR" ]] || die "SDK_DIR introuvable: $SDK_DIR"

export ANDROID_SDK_ROOT="$SDK_DIR"
export ANDROID_HOME="$SDK_DIR"

# Force PATH (SDK first) + neutralise caches shell
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/tools/bin:$PATH"
hash -r || true

# ---------- Hard pin binaries (avoid system ones) ----------
ADB_BIN="$ANDROID_SDK_ROOT/platform-tools/adb"
EMU_BIN="$ANDROID_SDK_ROOT/emulator/emulator"
SDKMANAGER_BIN="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"

[[ -x "$ADB_BIN" ]] || die "adb introuvable/exécutable manquant: $ADB_BIN"
[[ -x "$EMU_BIN" ]] || warn "emulator introuvable/exécutable manquant: $EMU_BIN (ok si tu ne lances pas d'AVD ici)"
if [[ ! -x "$SDKMANAGER_BIN" ]]; then
  warn "sdkmanager introuvable: $SDKMANAGER_BIN (cmdline-tools 'latest' non installé)."
fi

# ---------- Gradle wrapper ----------
GRADLEW="$PROJECT_ROOT/gradlew"
[[ -x "$GRADLEW" ]] || die "gradlew introuvable ou non exécutable: $GRADLEW"

# ---------- Default Gradle flags (safe, CI-friendly) ----------
GRADLE_FLAGS=(
  "--no-daemon"
  "--stacktrace"
)

# ---------- Utilities ----------
print_env() {
  log "PROJECT_ROOT=$PROJECT_ROOT"
  log "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
  log "ANDROID_HOME=$ANDROID_HOME"
  log "adb=$(command -v adb || true)"
  log "emulator=$(command -v emulator || true)"
  log "adb_version=$("$ADB_BIN" version | head -n 2 | tr '\n' ' ' || true)"
  log "emu_version=$("$EMU_BIN" -version 2>/dev/null | head -n 1 || true)"
  log "java=$(command -v java || true)"
  log "gradle_wrapper=$GRADLEW"
}

ensure_adb_server() {
  # Certains environnements ont un adb daemon instable si collisions; on force notre adb.
  "$ADB_BIN" kill-server >/dev/null 2>&1 || true
  "$ADB_BIN" start-server >/dev/null
}

require_device_or_emulator() {
  ensure_adb_server
  local count
  count="$("$ADB_BIN" devices | awk 'NR>1 && $2=="device"{c++} END{print c+0}')"
  if [[ "$count" -lt 1 ]]; then
    die "Aucun device/emulator en état 'device'. Lance un AVD ou branche un téléphone, puis réessaie."
  fi
}

# Start emulator if requested (optional)
start_emulator() {
  local avd="${1:-}"
  [[ -n "$avd" ]] || die "Usage: $0 emulator <AVD_NAME>"
  [[ -x "$EMU_BIN" ]] || die "emulator non disponible: $EMU_BIN"

  ensure_adb_server

  log "Démarrage AVD: $avd"
  # Mode stable (évite quickboot/snapshots si corrompus)
  "$EMU_BIN" \
    -avd "$avd" \
    -no-snapshot-load -no-snapshot-save \
    -netdelay none -netspeed full \
    -gpu swiftshader_indirect \
    -no-boot-anim \
    >/tmp/android-emulator-"$avd".log 2>&1 &

  log "Logs: /tmp/android-emulator-$avd.log"
  log "Attente device..."
  "$ADB_BIN" wait-for-device

  # attend sys.boot_completed=1 (timeout ~ 180s)
  local i
  for i in $(seq 1 180); do
    if [[ "$("$ADB_BIN" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
      log "Boot completed."
      return 0
    fi
    sleep 1
  done

  die "Timeout boot AVD. Voir /tmp/android-emulator-$avd.log"
}

doctor() {
  print_env
  log "local.properties sdk.dir=$(grep -E '^[[:space:]]*sdk\.dir=' "$PROJECT_ROOT/local.properties" 2>/dev/null || echo '<absent>')"
  log "adb devices:"
  ensure_adb_server
  "$ADB_BIN" devices -l || true

  if [[ -x "$SDKMANAGER_BIN" ]]; then
    log "sdkmanager --list (extrait):"
    "$SDKMANAGER_BIN" --version || true
  fi
}

run_gradle() {
  (cd "$PROJECT_ROOT" && "$GRADLEW" "${GRADLE_FLAGS[@]}" "$@")
}

usage() {
  cat <<EOF
Usage:
  $0 doctor
  $0 clean
  $0 assembleRelease
  $0 test                # connectedDebugAndroidTest (requires device/emulator)
  $0 unitTest            # testDebugUnitTest
  $0 compileDebug
  $0 emulator <AVD_NAME> # optional helper

Examples:
  $0 doctor
  $0 assembleRelease
  $0 emulator Medium_Phone_API_36.1
  $0 test
EOF
}

# ---------- Main ----------
cmd="${1:-}"
shift || true

case "$cmd" in
  doctor)
    doctor
    ;;
  clean)
    run_gradle :app:clean
    ;;
  compileDebug)
    run_gradle :app:compileDebugKotlin
    ;;
  unitTest)
    run_gradle :app:testDebugUnitTest
    ;;
  assembleRelease)
    run_gradle :app:assembleRelease
    ;;
  test)
    require_device_or_emulator
    run_gradle :app:connectedDebugAndroidTest
    ;;
  emulator)
    start_emulator "${1:-}"
    ;;
  ""|-h|--help|help)
    usage
    ;;
  *)
    die "Commande inconnue: '$cmd'. Utilise '$0 --help'."
    ;;
esac
