#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_NAME="$(basename "$0")"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_ROOT_DEFAULT="$HOME/Android/Sdk"

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$SDK_ROOT_DEFAULT}"
ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"

export ANDROID_SDK_ROOT ANDROID_HOME
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
hash -r 2>/dev/null || true

die() { echo "[$SCRIPT_NAME] ERROR: $*" >&2; exit 1; }
info() { echo "[$SCRIPT_NAME] $*" >&2; }

ADB_BIN="$ANDROID_SDK_ROOT/platform-tools/adb"

require_exec() { [[ -x "$1" ]] || die "Not executable: $1"; }
require_exec "$ADB_BIN"

# CI strict: refuse emulator usage
if [[ "${1:-}" == "emulator" ]]; then
  die "CI strict forbids launching emulator. Provide a real device/managed device in CI, or run local android-test."
fi

has_device_connected() {
  # Count devices in "device" state, excluding header
  local n
  n="$("$ADB_BIN" devices 2>/dev/null | awk 'NR>1 && $2=="device"{c++} END{print c+0}')"
  [[ "$n" -ge 1 ]]
}

cmd="${1:-}"
shift || true

case "$cmd" in
  doctor)
    info "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
    info "adb: $("$ADB_BIN" version | head -n 1)"
    info "devices:"
    "$ADB_BIN" devices -l || true
    ;;
  gradle)
    cd "$REPO_ROOT"

    # If user tries connected tests with no device => fail clearly
    if printf '%s ' "$@" | grep -qE 'connected(AndroidTest|DebugAndroidTest)|deviceAndroidTest'; then
      if ! has_device_connected; then
        die "No connected devices. CI strict forbids skipping connected tests."
      fi
    fi

    exec ./gradlew "$@"
    ;;
  adb)
    exec "$ADB_BIN" "$@"
    ;;
  *)
    cat >&2 <<EOF
Usage:
  $SCRIPT_NAME doctor
  $SCRIPT_NAME adb <args...>
  $SCRIPT_NAME gradle <tasks...>

CI strict behavior:
- Never launches emulator
- Fails if connected tests are requested without a device
EOF
    exit 2
    ;;
esac
