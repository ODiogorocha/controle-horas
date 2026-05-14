#!/bin/bash

# =============================================================
# INSTALADOR - CONTROLE DE HORAS
# =============================================================

set -e

APP_NAME="Controle de Horas"
APP_VERSION="1.5.0"

GITHUB_USER="ODiogorocha"
GITHUB_REPO="controle-de-horas"

BASE_URL="https://github.com/$GITHUB_USER/$GITHUB_REPO/releases/latest/download"

INSTALL_DIR="$HOME/.local/share/controle-horas"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="$HOME/.local/share/applications"

JAR_URL="$BASE_URL/controle-horas.jar"
ICON_URL="$BASE_URL/controle-horas.png"

JAR_LOCAL="$INSTALL_DIR/controle-horas.jar"
ICON_LOCAL="$INSTALL_DIR/controle-horas.png"

# =============================================================
# CORES
# =============================================================

VERDE='\033[0;32m'
AZUL='\033[0;34m'
VERMELHO='\033[0;31m'
AMARELO='\033[1;33m'
RESET='\033[0m'
NEGRITO='\033[1m'

clear

echo ""
echo -e "${AZUL}${NEGRITO}====================================================${RESET}"
echo -e "${AZUL}${NEGRITO}         SISTEMA DE CONTROLE DE HORAS               ${RESET}"
echo -e "${AZUL}${NEGRITO}====================================================${RESET}"
echo ""

# =============================================================
# JAVA
# =============================================================

echo -e "${AZUL}[1/7]${RESET} Verificando Java..."

if ! command -v java &>/dev/null; then
    echo ""
    echo -e "${VERMELHO}Java nao encontrado.${RESET}"
    echo ""
    echo "Instale Java 21:"
    echo "https://adoptium.net/"
    echo ""
    exit 1
fi

echo -e "${VERDE}[OK]${RESET} Java encontrado."

# =============================================================
# PASTAS
# =============================================================

echo ""
echo -e "${AZUL}[2/7]${RESET} Criando diretorios..."

mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$DESKTOP_DIR"

echo -e "${VERDE}[OK]${RESET} Diretorios criados."

# =============================================================
# REMOVE VERSAO ANTIGA
# =============================================================

echo ""
echo -e "${AZUL}[3/7]${RESET} Removendo versao antiga..."

rm -f "$JAR_LOCAL"

echo -e "${VERDE}[OK]${RESET} Versao antiga removida."

# =============================================================
# DOWNLOAD JAR
# =============================================================

echo ""
echo -e "${AZUL}[4/7]${RESET} Baixando sistema..."

DOWNLOAD_OK=false

if command -v wget &>/dev/null; then

    wget \
        --no-cache \
        --no-cookies \
        -O "$JAR_LOCAL" \
        "$JAR_URL"

    DOWNLOAD_OK=true

elif command -v curl &>/dev/null; then

    curl \
        -L \
        -H 'Cache-Control: no-cache, no-store' \
        -o "$JAR_LOCAL" \
        "$JAR_URL"

    DOWNLOAD_OK=true
fi

if [ "$DOWNLOAD_OK" = false ]; then
    echo -e "${VERMELHO}Nem wget nem curl encontrados.${RESET}"
    exit 1
fi

if [ ! -f "$JAR_LOCAL" ]; then
    echo ""
    echo -e "${VERMELHO}Falha ao baixar o sistema.${RESET}"
    exit 1
fi

if [ ! -s "$JAR_LOCAL" ]; then
    echo ""
    echo -e "${VERMELHO}Arquivo baixado esta vazio.${RESET}"
    exit 1
fi

echo -e "${VERDE}[OK]${RESET} Sistema atualizado."

# =============================================================
# ICONE
# =============================================================

echo ""
echo -e "${AZUL}[5/7]${RESET} Baixando icone..."

if command -v wget &>/dev/null; then
    wget -q -O "$ICON_LOCAL" "$ICON_URL" || true
else
    curl -L -o "$ICON_LOCAL" "$ICON_URL" || true
fi

echo -e "${VERDE}[OK]${RESET} Icone atualizado."

# =============================================================
# LAUNCHER
# =============================================================

echo ""
echo -e "${AZUL}[6/7]${RESET} Criando launcher..."

cat > "$BIN_DIR/controle-horas" << EOF
#!/bin/bash
java -jar "$JAR_LOCAL"
EOF

chmod +x "$BIN_DIR/controle-horas"

echo -e "${VERDE}[OK]${RESET} Launcher criado."

# =============================================================
# MENU
# =============================================================

echo ""
echo -e "${AZUL}[7/7]${RESET} Criando atalho..."

cat > "$DESKTOP_DIR/controle-horas.desktop" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=Controle de Horas
Comment=Sistema de Controle de Horas
Exec=$BIN_DIR/controle-horas
Icon=$ICON_LOCAL
Terminal=false
Categories=Office;
StartupNotify=true
EOF

chmod +x "$DESKTOP_DIR/controle-horas.desktop"

if command -v update-desktop-database &>/dev/null; then
    update-desktop-database "$DESKTOP_DIR" 2>/dev/null || true
fi

echo -e "${VERDE}[OK]${RESET} Atalho criado."

# =============================================================
# FINAL
# =============================================================

echo ""
echo -e "${VERDE}${NEGRITO}====================================================${RESET}"
echo -e "${VERDE}${NEGRITO}         INSTALACAO CONCLUIDA COM SUCESSO           ${RESET}"
echo -e "${VERDE}${NEGRITO}====================================================${RESET}"
echo ""

echo "Execute com:"
echo ""
echo "  controle-horas"
echo ""

read -p "Abrir agora? [s/N]: " abrir

if [[ "$abrir" =~ ^[Ss]$ ]]; then
    nohup java -jar "$JAR_LOCAL" >/dev/null 2>&1 &
fi