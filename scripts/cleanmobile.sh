#!/usr/bin/env bash
# cleanyourmobile - OnePlus 6T debloat/tune over ADB (no root)
set -u
DIR="$(cd "$(dirname "$0")/.." && pwd)"
CMD="${1:-help}"

command -v adb >/dev/null || { echo "adb not found. macOS: brew install android-platform-tools"; exit 1; }
adb get-state >/dev/null 2>&1 || { echo "No authorised device. Enable USB debugging and accept the prompt."; exit 1; }

MODEL=$(adb shell getprop ro.product.model | tr -d '\r')
case "$MODEL" in
  *A6013*|*A6010*) ;;
  *) echo "Warning: device is '$MODEL', not a OnePlus 6T"; read -r -p "Continue? [y/N] " a; [ "$a" = y ] || exit 1 ;;
esac

pkgs() { grep -v '^[[:space:]]*#' "$1" | sed 's/#.*//' | awk 'NF{print $1}'; }

backup() {
  mkdir -p "$DIR/backups"
  adb shell pm list packages --user 0 | tr -d '\r' > "$DIR/backups/packages-$(date +%Y%m%d-%H%M%S).txt"
}

debloat() {
  backup
  INSTALLED=$(adb shell pm list packages --user 0 | tr -d '\r')
  for f in "$@"; do
    echo "== $f"
    for p in $(pkgs "$f"); do
      if echo "$INSTALLED" | grep -qx "package:$p"; then
        r=$(adb shell pm uninstall -k --user 0 "$p" | tr -d '\r')
        echo "$p -> $r"
      fi
    done
  done
}

restore() {
  for p in $(pkgs "$1"); do
    echo "$p -> $(adb shell cmd package install-existing "$p" | tr -d '\r')"
  done
}

tune() {
  echo "Animations 0.5x"
  for s in window_animation_scale transition_animation_scale animator_duration_scale; do
    adb shell settings put global $s 0.5
  done
  echo "Battery standby features on"
  adb shell settings put global app_standby_enabled 1
  adb shell settings put global adaptive_battery_management_enabled 1
  echo "Private DNS ad blocking (AdGuard)"
  adb shell settings put global private_dns_mode hostname
  adb shell settings put global private_dns_specifier dns.adguard-dns.com
  echo "Trimming caches"
  adb shell pm trim-caches 999G
  echo "Killing background apps"
  adb shell cmd activity kill-all
  echo "Background dexopt (may take a few minutes)"
  adb shell cmd package bg-dexopt-job
}

undo_tune() {
  for s in window_animation_scale transition_animation_scale animator_duration_scale; do
    adb shell settings put global $s 1.0
  done
  adb shell settings put global private_dns_mode opportunistic
  adb shell settings delete global private_dns_specifier
}

case "$CMD" in
  debloat-safe)    debloat "$DIR/lists/oneplus_safe.txt" ;;
  debloat-google)  debloat "$DIR/lists/google_optional.txt" ;;
  debloat-caution) debloat "$DIR/lists/oneplus_caution.txt" ;;
  restore)         restore "${2:?usage: restore <list-file>}" ;;
  tune)            tune ;;
  undo-tune)       undo_tune ;;
  all)             debloat "$DIR/lists/oneplus_safe.txt"; tune ;;
  *) echo "Usage: $0 {debloat-safe|debloat-google|debloat-caution|restore <list>|tune|undo-tune|all}" ;;
esac
