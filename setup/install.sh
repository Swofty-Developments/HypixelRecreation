#!/usr/bin/env bash
set -euo pipefail

if [[ ! -t 0 && -z "${SKYBLOCK_INSTALLER_REEXEC:-}" ]]; then
  tmpdir="$(mktemp -d /tmp/skyblock-installer-XXXXXX)"
  {
    echo '#!/usr/bin/env bash'
    echo 'set -euo pipefail'
    cat
  } > "$tmpdir/install.sh"
  chmod +x "$tmpdir/install.sh"
  if { : </dev/tty; } 2>/dev/null; then
    exec env SKYBLOCK_INSTALLER_REEXEC=1 bash "$tmpdir/install.sh" "$@" </dev/tty
  fi
  exec env SKYBLOCK_INSTALLER_REEXEC=1 bash "$tmpdir/install.sh" "$@"
fi

REPO="${SKYBLOCK_INSTALLER_REPO:-Swofty-Developments/HypixelRecreation}"
VERSION="${SKYBLOCK_INSTALLER_VERSION:-latest}"
INSTALLER_NAME="skyblock-installer"
CACHE_DIR="${XDG_CACHE_HOME:-$HOME/.cache}/skyblock-installer"

if [[ -n "${SKYBLOCK_INSTALLER_BIN:-}" && -x "${SKYBLOCK_INSTALLER_BIN}" ]]; then
  exec "${SKYBLOCK_INSTALLER_BIN}" "$@"
fi

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" 2>/dev/null && pwd || true)"
if [[ -n "$script_dir" && -x "$script_dir/.bin/$INSTALLER_NAME" ]]; then
  exec "$script_dir/.bin/$INSTALLER_NAME" "$@"
fi

os="$(uname -s | tr '[:upper:]' '[:lower:]')"
arch="$(uname -m)"
case "$arch" in
  x86_64|amd64) arch="amd64" ;;
  aarch64|arm64) arch="arm64" ;;
  *) echo "Unsupported architecture: $arch" >&2; exit 1 ;;
esac

case "$os" in
  linux|darwin) ;;
  *) echo "Unsupported OS: $os" >&2; exit 1 ;;
esac

asset="${INSTALLER_NAME}_${os}_${arch}.tar.gz"
mkdir -p "$CACHE_DIR"
bin="$CACHE_DIR/${INSTALLER_NAME}-${VERSION}-${os}-${arch}"

if [[ "$VERSION" == "latest" ]]; then
  rm -f "$bin"
fi

if [[ ! -x "$bin" ]]; then
  tmp="$(mktemp -d)"
  trap 'rm -rf "$tmp"' EXIT

  if [[ "$VERSION" == "latest" ]]; then
    url="https://github.com/${REPO}/releases/latest/download/${asset}"
  else
    url="https://github.com/${REPO}/releases/download/${VERSION}/${asset}"
  fi

  echo "Downloading ${INSTALLER_NAME} (${os}/${arch})..." >&2
  curl -fsSL "$url" -o "$tmp/$asset"
  tar -xzf "$tmp/$asset" -C "$tmp"
  chmod +x "$tmp/$INSTALLER_NAME"
  mv "$tmp/$INSTALLER_NAME" "$bin"
fi

exec "$bin" "$@"
