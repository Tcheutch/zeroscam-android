#!/usr/bin/env bash
set -Eeuo pipefail

# ------------------------------------------------------------
# ZeroScam - Android Env (PROD)
# Single source of truth: $HOME/Android/Sdk
# - Force ANDROID_SDK_ROOT/ANDROID_HOME
# - Force PATH to prefer SDK platform-tools/emulator
# - Refuse /usr/bin/adb or /usr/bin/emulator when ambiguity exists
# - Provide stable wrappers: gradle, adb, emulator, doctor
# ------------------------------------------------------------

SCRIPT_NAME="$(basename "$0")"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_ROOT_DEFAULT="$HOME/Android/Sdk"

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$SDK_ROOT_DEFAULT}"
ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"

ADB_BIN="$ANDROID_SDK_ROOT/platform-tools/adb"
EMU_BIN="$ANDROID_SDK_ROOT/emulator/emulator"
JAVA_BIN="${JAVA_HOME:-}/bin/java"

# Prepend SDK tools FIRST (beats /usr/bin)
export ANDROID_SDK_ROOT ANDROID_HOME
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/tools/bin:$PATH"

# Hard refresh shell cache if available (safe no-op in non-interactive)
hash -r 2>/dev/null || true

die() { echo "[$SCRIPT_NAME] ERROR: $*" >&2; exit 1; }
info() { echo "[$SCRIPT_NAME] $*" >&2; }

require_file() {
  local f="$1"
  [[ -f "$f" ]] || die "Missing file: $f (SDK root is '$ANDROID_SDK_ROOT')"
}

require_exec() {
  local f="$1"
  [[ -x "$f" ]] || die "Not executable: $f"
}

ensure_sdk_layout() {
  require_file "$ANDROID_SDK_ROOT/platform-tools/adb"
  require_file "$ANDROID_SDK_ROOT/emulator/emulator"
}

ensure_binaries_are_sdk_ones() {
  # command -v resolves the first binary in PATH
  local adb_resolved emu_resolved
  adb_resolved="$(command -v adb || true)"
  emu_resolved="$(command -v emulator || true)"

  [[ "$adb_resolved" == "$ADB_BIN" ]] || die "adb resolves to '$adb_resolved' (expected '$ADB_BIN'). Fix PATH/aliases."
  [[ "$emu_resolved" == "$EMU_BIN" ]] || die "emulator resolves to '$emu_resolved' (expected '$EMU_BIN'). Fix PATH/aliases."
}

doctor() {
  info "Repo root       : $REPO_ROOT"
  info "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
  info "ANDROID_HOME    : $ANDROID_HOME"
  info "Resolved adb    : $(command -v adb || true)"
  info "Resolved emulator: $(command -v emulator || true)"
  info "adb version:"
  adb version || die "adb failed to run"
  info "emulator version:"
  emulator -version || die "emulator failed to run"
}

ensure_sdk_layout
ensure_binaries_are_sdk_ones

# -----------------------------
# Commands
# -----------------------------

cmd="${1:-doctor}"
shift || true

case "$cmd" in
  doctor)
    doctor
    ;;
  adb)
    exec "$ADB_BIN" "$@"
    ;;
  emulator)
    exec "$EMU_BIN" "$@"
    ;;
  gradle)
    # Always run gradle from repo root for consistency
    cd "$REPO_ROOT"
    exec ./gradlew "$@"
    ;;
  *)
    cat >&2 <<EOF
Usage:
  $SCRIPT_NAME doctor
  $SCRIPT_NAME adb <args...>
  $SCRIPT_NAME emulator <args...>
  $SCRIPT_NAME gradle <gradle tasks...>

Examples:
  $SCRIPT_NAME doctor
  $SCRIPT_NAME gradle :app:assembleRelease --no-daemon
  $SCRIPT_NAME adb devices -l
  $SCRIPT_NAME emulator -list-avds
EOF
    exit 2
    ;;
esac
