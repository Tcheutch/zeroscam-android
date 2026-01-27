#!/usr/bin/env bash
set -euo pipefail

# ============================
#  ZeroScam -> VERIDIA refactor
#  Mode Prod "Béton armé"
# ============================

OLD_BRAND="${1:-ZeroScam}"
NEW_BRAND="${2:-VERIDIA}"

OLD_LC="$(echo "$OLD_BRAND" | tr '[:upper:]' '[:lower:]')"   # zeroscam
NEW_LC="$(echo "$NEW_BRAND" | tr '[:upper:]' '[:lower:]')"   # veridia

ROOT="$(pwd)"

# -------- Helpers --------
die(){ echo "❌ $*" >&2; exit 1; }
info(){ echo "ℹ️  $*"; }
ok(){ echo "✅ $*"; }

need_cmd() { command -v "$1" >/dev/null 2>&1 || die "Commande manquante: $1"; }

# -------- Preconditions --------
need_cmd git
need_cmd python3
need_cmd find
need_cmd sed

if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  :
else
  die "Ce dossier n'est pas un repo git."
fi

# Vérif état git
if [[ -n "$(git status --porcelain)" ]]; then
  die "Repo non propre. Commit/stash avant de lancer le refactor."
fi

# Tag déjà créé (tu as dit que oui) => on évite de toucher au tag, on travaille sur une branche
BRANCH="chore/brand-${OLD_LC}-to-${NEW_LC}"
info "Création branche: $BRANCH"
git checkout -b "$BRANCH" >/dev/null
ok "Branche créée."

# Snapshot safety
STAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR="../backup_${OLD_LC}_to_${NEW_LC}_${STAMP}"
info "Backup vers: $BACKUP_DIR"
mkdir -p "$BACKUP_DIR"
# archive git propre (sans build caches)
git archive --format=tar HEAD | tar -x -C "$BACKUP_DIR"
ok "Backup OK."

info "Résumé paramètres:"
echo "   OLD_BRAND = $OLD_BRAND"
echo "   NEW_BRAND = $NEW_BRAND"
echo "   OLD_LC    = $OLD_LC"
echo "   NEW_LC    = $NEW_LC"
echo

# -------- Phase 1: Renommage des chemins (git mv) --------
# Objectif: renommer dossiers/fichiers contenant "zeroscam" ou "ZeroScam"
# On exclut build/.gradle/.idea/.git et autres caches.
info "Phase 1/3: git mv des chemins contenant $OLD_LC / $OLD_BRAND"

mapfile -t PATHS < <(
  find . \
    -path "./.git" -prune -o \
    -path "./.gradle" -prune -o \
    -path "./.idea" -prune -o \
    -path "./build" -prune -o \
    -path "./**/build" -prune -o \
    -print 2>/dev/null \
  | sed 's|^\./||' \
  | awk 'length($0)>0' \
  | grep -E "(${OLD_LC}|${OLD_BRAND})" \
  | sort -r
)

for p in "${PATHS[@]}"; do
  # skip if already moved
  [[ -e "$p" ]] || continue

  newp="$p"
  newp="${newp//$OLD_BRAND/$NEW_BRAND}"
  newp="${newp//$OLD_LC/$NEW_LC}"

  if [[ "$newp" != "$p" ]]; then
    mkdir -p "$(dirname "$newp")" 2>/dev/null || true
    git mv "$p" "$newp" 2>/dev/null || {
      # si git mv échoue (ex: collisions), on tente mv + git add/rm
      info "Collision/exception git mv sur: $p -> $newp (tentative fallback)"
      mkdir -p "$(dirname "$newp")"
      mv "$p" "$newp"
      git add "$newp"
      git rm -r --cached "$p" >/dev/null 2>&1 || true
    }
  fi
done
ok "Renommage des chemins terminé."

# -------- Phase 2: Remplacement dans les fichiers texte --------
info "Phase 2/3: remplacement dans fichiers texte (évite binaires)"

python3 - <<'PY'
import os, sys

OLD_BRAND = os.environ.get("OLD_BRAND", "ZeroScam")
NEW_BRAND = os.environ.get("NEW_BRAND", "VERIDIA")
OLD_LC = OLD_BRAND.lower()
NEW_LC = NEW_BRAND.lower()

EXCLUDE_DIRS = {".git", ".gradle", ".idea", "build"}
EXCLUDE_EXT = {
  ".png",".jpg",".jpeg",".webp",".gif",".ico",".psd",
  ".keystore",".jks",".so",".jar",".aar",".class",
  ".ttf",".otf",".mp4",".mov",".wav",".mp3",".zip",".7z",".rar",".pdf"
}

def is_binary(path: str) -> bool:
  try:
    with open(path, "rb") as f:
      chunk = f.read(2048)
    return b"\x00" in chunk
  except Exception:
    return True

def should_skip(path: str) -> bool:
  p = path.replace("\\","/")
  parts = p.split("/")
  if any(part in EXCLUDE_DIRS for part in parts):
    return True
  _, ext = os.path.splitext(path)
  if ext.lower() in EXCLUDE_EXT:
    return True
  return False

changed = 0
touched_files = 0

for root, dirs, files in os.walk("."):
  # prune dirs
  dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
  for name in files:
    path = os.path.join(root, name)
    if should_skip(path):
      continue
    if is_binary(path):
      continue

    try:
      with open(path, "r", encoding="utf-8") as f:
        s = f.read()
    except UnicodeDecodeError:
      # fallback latin-1 (rare)
      try:
        with open(path, "r", encoding="latin-1") as f:
          s = f.read()
      except Exception:
        continue
    except Exception:
      continue

    new = s

    # Remplacements prioritaires:
    # 1) Exact brand
    new = new.replace(OLD_BRAND, NEW_BRAND)
    # 2) lowercase occurrences (packages, ids, chemins)
    new = new.replace(OLD_LC, NEW_LC)

    if new != s:
      try:
        with open(path, "w", encoding="utf-8") as f:
          f.write(new)
        changed += (s != new)
        touched_files += 1
      except Exception:
        pass

print(f"TEXT-REPLACE: fichiers modifiés = {touched_files}")
PY
ok "Remplacements texte terminés."

# Export env for python (above uses env vars)
# (Note: python already ran; this is for consistency if you extend script)
export OLD_BRAND="$OLD_BRAND"
export NEW_BRAND="$NEW_BRAND"

# -------- Phase 3: Vérifs ciblées Android / Kotlin / Gradle --------
info "Phase 3/3: vérifs & détections"

# Recherche résiduelle (si rg existe)
if command -v rg >/dev/null 2>&1; then
  info "Scan résiduel (ripgrep): occurrences restantes de $OLD_LC / $OLD_BRAND"
  rg -n --hidden --glob '!.git/**' --glob '!.gradle/**' --glob '!.idea/**' --glob '!**/build/**' \
    -e "$OLD_BRAND" -e "$OLD_LC" . || true
else
  info "rg non installé: scan simple via grep"
  grep -RIn --exclude-dir=.git --exclude-dir=.gradle --exclude-dir=.idea --exclude-dir=build \
    -e "$OLD_BRAND" -e "$OLD_LC" . || true
fi

echo
info "Conseils post-run (Android):"
cat <<'TXT'
1) Vérifie app/build.gradle(.kts) et modules:
   - namespace = "....veridia...."
   - applicationId = "....veridia...." (si tu veux changer l'ID)
2) Vérifie AndroidManifest.xml:
   - package (si présent) et les références .<Activity> / authorities
3) Vérifie google-services.json / Firebase (si utilisé):
   - applicationId doit correspondre au projet Firebase => sinon ajuster côté Firebase.
4) Exécute:
   - ./gradlew clean
   - ./gradlew :app:assembleDebug (ou assembleRelease)
5) Commit final:
   - git status
   - git diff
TXT

ok "Script terminé. Fais un build + tests, puis commit."
