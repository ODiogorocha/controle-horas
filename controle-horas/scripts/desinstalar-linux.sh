#!/bin/bash

# =============================================================
#  Desinstalador do Sistema de Controle de Horas - Linux
# =============================================================

VERDE='\033[0;32m'
AZUL='\033[0;34m'
VERMELHO='\033[0;31m'
RESET='\033[0m'
NEGRITO='\033[1m'

echo ""
echo -e "${VERMELHO}${NEGRITO}=============================================${RESET}"
echo -e "${VERMELHO}${NEGRITO}   Desinstalador - Controle de Horas       ${RESET}"
echo -e "${VERMELHO}${NEGRITO}=============================================${RESET}"
echo ""

read -p "Tem certeza que deseja desinstalar o Sistema de Controle de Horas? [s/N] " resposta
if [[ ! "$resposta" =~ ^[Ss]$ ]]; then
    echo "Desinstalacao cancelada."
    exit 0
fi

read -p "Deseja manter os dados salvos (banco de dados)? [S/n] " manter_dados

# Remove arquivos do sistema
rm -f "$HOME/.local/share/controle-horas/controle-horas.jar"
rm -f "$HOME/.local/bin/controle-horas"
rm -f "$HOME/.local/share/applications/controle-horas.desktop"

if [[ "$manter_dados" =~ ^[Nn]$ ]]; then
    rm -rf "$HOME/.local/share/controle-horas"
    echo -e "${AZUL}[OK]${RESET} Dados removidos."
else
    echo -e "${AZUL}[OK]${RESET} Dados mantidos em ~/.local/share/controle-horas/"
fi

# Atualiza banco de aplicativos
if command -v update-desktop-database &>/dev/null; then
    update-desktop-database "$HOME/.local/share/applications" 2>/dev/null || true
fi

echo ""
echo -e "${VERDE}${NEGRITO}Sistema desinstalado com sucesso.${RESET}"