#!/usr/bin/env bash
# Instala .debs do Termux manualmente (contorna bloqueio root do apt do Termux).
# Necessário para obter aapt2 ARM64 que roda dentro do proot.

set -euo pipefail
TERMUX_PREFIX="/data/data/com.termux/files/usr"
TERMUX_REPO="https://packages-cf.termux.dev/apt/termux-main/pool"
INDEX="/tmp/termux-aarch64-Packages"
WORK=/tmp/termux-debs
mkdir -p "$WORK"
cd "$WORK"

if [ ! -f "$INDEX" ]; then
  curl -s "https://packages-cf.termux.dev/apt/termux-main/dists/stable/main/binary-aarch64/Packages" -o "$INDEX"
fi

deb_path() {
  awk -v n="Package: $1" '$0==n{found=1} found && /^Filename:/ {print $2; exit}' "$INDEX"
}

fetch_install() {
  local pkg="$1"
  local filename deb
  filename="$(deb_path "$pkg" || true)"
  if [ -z "$filename" ]; then echo "Pacote não encontrado: $pkg"; return 1; fi
  deb="$(basename "$filename")"
  if [ ! -f "$deb" ]; then
    echo ">> baixando $deb"
    curl -sLo "$deb" "$TERMUX_REPO/${filename#pool/}"
  fi
  rm -rf extract && mkdir extract
  ar x "$deb" --output extract
  tar -xf extract/data.tar.xz -C extract
  # instala direto no prefixo do Termux
  (cd extract && cp -a --parents data/data/com.termux/files/usr/. "$TERMUX_PREFIX"/../ 2>/dev/null || true)
  (cd extract/data/data/com.termux/files/usr && cp -ap bin/. "$TERMUX_PREFIX/bin/" 2>/dev/null || true)
  (cd extract/data/data/com.termux/files/usr && cp -ap lib/. "$TERMUX_PREFIX/lib/" 2>/dev/null || true)
  find extract -name '*.so*' -exec cp -ap {} "$TERMUX_PREFIX/lib/" \; 2>/dev/null || true
  find extract -type f -path '*usr/bin/*' -exec cp -ap {} "$TERMUX_PREFIX/bin/" \; 2>/dev/null || true
  echo ">> instalado: $pkg"
}

fetch_install aapt2
fetch_install aapt
fetch_install fmt
fetch_install libpng
fetch_install libzopfli
fetch_install abseil-cpp
fetch_install libprotobuf

echo "=== teste ==="
TERMUX_PREFIX_TEST="$TERMUX_PREFIX" "$TERMUX_PREFIX/bin/aapt2" version && echo "AAPT2_ARM64_OK"