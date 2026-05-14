#!/bin/bash

# =============================================================
#  DESINSTALADOR - SISTEMA DE CONTROLE DE HORAS
# =============================================================

set -e

# =============================================================
# CORES
# =============================================================

VERDE='\033[0;32m'
AZUL='\033[0;34m'
VERMELHO='\033[0;31m'
AMARELO='\033[1;33m'
RESET='\033[0m'
NEGRITO='\033[1m'

# =============================================================
# PASTAS
# =============================================================

INSTALL_DIR="$HOME/.local/share/controle-horas"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="$HOME/.local/share/applications"

JAR_FILE="$INSTALL_DIR/controle-horas.jar"
ICON_FILE="$INSTALL_DIR/controle-horas.png"

LAUNCHER_FILE="$BIN_DIR/controle-horas"
DESKTOP_FILE="$DESKTOP_DIR/controle-horas.desktop"

# =============================================================
# CABECALHO
# =============================================================

clear

echo ""
echo -e "${VERMELHO}${NEGRITO}====================================================${RESET}"
echo -e "${VERMELHO}${NEGRITO}        DESINSTALADOR - CONTROLE DE HORAS           ${RESET}"
echo -e "${VERMELHO}${NEGRITO}====================================================${RESET}"
echo ""

echo "Este processo ira remover:"
echo ""
echo "  • Aplicacao"
echo "  • Atalho do menu"
echo "  • Launcher do terminal"
echo ""

# =============================================================
# CONFIRMACAO
# =============================================================

read -p "Deseja continuar? [s/N]: " resposta

if [[ ! "$resposta" =~ ^[Ss]$ ]]; then
    echo ""
    echo -e "${AMARELO}Desinstalacao cancelada.${RESET}"
    echo ""
    exit 0
fi

echo ""

# =============================================================
# DADOS
# =============================================================

read -p "Deseja REMOVER tambem os dados do sistema? [s/N]: " remover_dados

echo ""

# =============================================================
# REMOVENDO ARQUIVOS
# =============================================================

echo -e "${AZUL}[1/5]${RESET} Removendo launcher..."

rm -f "$LAUNCHER_FILE"

echo -e "${VERDE}[OK]${RESET} Launcher removido."

echo ""

echo -e "${AZUL}[2/5]${RESET} Removendo atalho do menu..."

rm -f "$DESKTOP_FILE"

echo -e "${VERDE}[OK]${RESET} Atalho removido."

echo ""

echo -e "${AZUL}[3/5]${RESET} Removendo arquivos da aplicacao..."

rm -f "$JAR_FILE"
rm -f "$ICON_FILE"

echo -e "${VERDE}[OK]${RESET} Arquivos removidos."

echo ""

# =============================================================
# REMOVER DADOS
# =============================================================

if [[ "$remover_dados" =~ ^[Ss]$ ]]; then

    echo -e "${AZUL}[4/5]${RESET} Removendo dados..."

    rm -rf "$INSTALL_DIR"

    echo -e "${VERDE}[OK]${RESET} Dados removidos."

else

    echo -e "${AZUL}[4/5]${RESET} Mantendo dados do usuario..."

    mkdir -p "$INSTALL_DIR"

    echo -e "${VERDE}[OK]${RESET} Dados preservados em:"
    echo "      $INSTALL_DIR"

fi

echo ""

# =============================================================
# ATUALIZA MENU
# =============================================================

echo -e "${AZUL}[5/5]${RESET} Atualizando sistema..."

if command -v update-desktop-database &>/dev/null; then
    update-desktop-database "$DESKTOP_DIR" 2>/dev/null || true
fi

echo -e "${VERDE}[OK]${RESET} Sistema atualizado."

echo ""

# =============================================================
# FINAL
# =============================================================

echo -e "${VERDE}${NEGRITO}====================================================${RESET}"
echo -e "${VERDE}${NEGRITO}         SISTEMA REMOVIDO COM SUCESSO               ${RESET}"
echo -e "${VERDE}${NEGRITO}====================================================${RESET}"
echo ""