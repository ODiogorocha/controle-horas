#!/bin/bash

# =============================================================
#  Instalador do Sistema de Controle de Horas
#  Linux (Ubuntu, Debian, Fedora, Arch)
# =============================================================

set -e

VERDE='\033[0;32m'
AZUL='\033[0;34m'
AMARELO='\033[1;33m'
VERMELHO='\033[0;31m'
RESET='\033[0m'
NEGRITO='\033[1m'

APP_NAME="controle-horas"
APP_VERSION="1.0.0"
INSTALL_DIR="$HOME/.local/share/controle-horas"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="$HOME/.local/share/applications"
GITHUB_REPO="SEU_USUARIO/controle-horas"
JAR_URL="https://github.com/$GITHUB_REPO/releases/latest/download/controle-horas.jar"

echo ""
echo -e "${AZUL}${NEGRITO}=============================================${RESET}"
echo -e "${AZUL}${NEGRITO}   Sistema de Controle de Horas v$APP_VERSION   ${RESET}"
echo -e "${AZUL}${NEGRITO}   Instalador para Linux                    ${RESET}"
echo -e "${AZUL}${NEGRITO}=============================================${RESET}"
echo ""

# ── Funcoes auxiliares ────────────────────────────────────────
info()    { echo -e "${AZUL}[INFO]${RESET} $1"; }
sucesso() { echo -e "${VERDE}[OK]${RESET}   $1"; }
aviso()   { echo -e "${AMARELO}[AVISO]${RESET} $1"; }
erro()    { echo -e "${VERMELHO}[ERRO]${RESET} $1"; exit 1; }

# ── 1. Verifica Java ──────────────────────────────────────────
info "Verificando se o Java esta instalado..."

if command -v java &>/dev/null; then
    JAVA_VER=$(java -version 2>&1 | head -1 | grep -oP '(?<=version ")[\d.]+' | cut -d. -f1)
    if [ "$JAVA_VER" -ge 17 ] 2>/dev/null; then
        sucesso "Java $JAVA_VER encontrado."
    else
        aviso "Java encontrado mas e versao $JAVA_VER. Precisa da versao 17 ou superior."
        instalar_java
    fi
else
    aviso "Java nao encontrado. Instalando automaticamente..."
    instalar_java
fi

instalar_java() {
    info "Detectando gerenciador de pacotes..."

    if command -v apt-get &>/dev/null; then
        info "Instalando Java 21 via apt (Ubuntu/Debian)..."
        sudo apt-get update -qq
        sudo apt-get install -y openjdk-21-jre-headless
    elif command -v dnf &>/dev/null; then
        info "Instalando Java 21 via dnf (Fedora/RHEL)..."
        sudo dnf install -y java-21-openjdk-headless
    elif command -v pacman &>/dev/null; then
        info "Instalando Java 21 via pacman (Arch)..."
        sudo pacman -Sy --noconfirm jre21-openjdk-headless
    elif command -v zypper &>/dev/null; then
        info "Instalando Java 21 via zypper (openSUSE)..."
        sudo zypper install -y java-21-openjdk-headless
    else
        erro "Nao foi possivel instalar o Java automaticamente.\nInstale manualmente: https://adoptium.net\nDepois rode este instalador novamente."
    fi

    sucesso "Java instalado com sucesso!"
}

# ── 2. Cria pastas de instalacao ─────────────────────────────
info "Criando pastas de instalacao em $INSTALL_DIR ..."
mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$DESKTOP_DIR"
sucesso "Pastas criadas."

# ── 3. Baixa o JAR ───────────────────────────────────────────
JAR_LOCAL="$INSTALL_DIR/controle-horas.jar"

if [ -f "$JAR_LOCAL" ]; then
    aviso "Versao anterior encontrada. Atualizando..."
    rm -f "$JAR_LOCAL"
fi

info "Baixando o sistema ($JAR_URL)..."

if command -v wget &>/dev/null; then
    wget -q --show-progress -O "$JAR_LOCAL" "$JAR_URL"
elif command -v curl &>/dev/null; then
    curl -L --progress-bar -o "$JAR_LOCAL" "$JAR_URL"
else
    erro "wget ou curl nao encontrado. Instale um deles e tente novamente."
fi

if [ ! -f "$JAR_LOCAL" ]; then
    erro "Falha ao baixar o arquivo. Verifique sua conexao com a internet."
fi

sucesso "Sistema baixado com sucesso."

# ── 4. Cria script de inicializacao ──────────────────────────
info "Criando comando de inicializacao..."

cat > "$BIN_DIR/controle-horas" << 'SCRIPT'
#!/bin/bash
JAR="$HOME/.local/share/controle-horas/controle-horas.jar"

if [ ! -f "$JAR" ]; then
    echo "ERRO: Arquivo do sistema nao encontrado em $JAR"
    echo "Tente reinstalar com: curl -sSL https://github.com/SEU_USUARIO/controle-horas/releases/latest/download/instalar-linux.sh | bash"
    exit 1
fi

# Configura JAVA_HOME se necessario
if [ -z "$JAVA_HOME" ]; then
    for dir in /usr/lib/jvm/java-21* /usr/lib/jvm/java-17* /usr/lib/jvm/default-java; do
        if [ -d "$dir" ]; then
            export JAVA_HOME="$dir"
            break
        fi
    done
fi

echo "Iniciando Sistema de Controle de Horas..."
java -jar "$JAR" "$@"
SCRIPT

chmod +x "$BIN_DIR/controle-horas"
sucesso "Comando 'controle-horas' criado."

# ── 5. Cria icone na area de trabalho ────────────────────────
info "Criando atalho no menu de aplicativos..."

cat > "$DESKTOP_DIR/controle-horas.desktop" << DESKTOP
[Desktop Entry]
Version=1.0
Type=Application
Name=Controle de Horas
Comment=Sistema de controle de ponto e horas extras
Exec=$BIN_DIR/controle-horas
Icon=accessories-clock
Terminal=false
Categories=Office;Finance;
Keywords=ponto;horas;funcionarios;trabalho;
StartupNotify=true
DESKTOP

chmod +x "$DESKTOP_DIR/controle-horas.desktop"

# Atualiza banco de dados de aplicativos (se disponivel)
if command -v update-desktop-database &>/dev/null; then
    update-desktop-database "$DESKTOP_DIR" 2>/dev/null || true
fi

sucesso "Atalho criado no menu de aplicativos."

# ── 6. Adiciona ao PATH se necessario ────────────────────────
SHELL_RC=""
if [ -f "$HOME/.bashrc" ]; then
    SHELL_RC="$HOME/.bashrc"
elif [ -f "$HOME/.zshrc" ]; then
    SHELL_RC="$HOME/.zshrc"
fi

if [ -n "$SHELL_RC" ] && ! grep -q "$BIN_DIR" "$SHELL_RC"; then
    echo "" >> "$SHELL_RC"
    echo "# Sistema de Controle de Horas" >> "$SHELL_RC"
    echo "export PATH=\"\$PATH:$BIN_DIR\"" >> "$SHELL_RC"
    aviso "Adicionado $BIN_DIR ao PATH em $SHELL_RC"
    aviso "Feche e abra o terminal para usar o comando 'controle-horas'."
fi

# ── 7. Conclusao ──────────────────────────────────────────────
echo ""
echo -e "${VERDE}${NEGRITO}=============================================${RESET}"
echo -e "${VERDE}${NEGRITO}   Instalacao concluida com sucesso!        ${RESET}"
echo -e "${VERDE}${NEGRITO}=============================================${RESET}"
echo ""
echo -e "  Como abrir o sistema:"
echo -e "  ${NEGRITO}Opcao 1:${RESET} Procure 'Controle de Horas' no menu de aplicativos"
echo -e "  ${NEGRITO}Opcao 2:${RESET} Digite no terminal: ${AZUL}controle-horas${RESET}"
echo ""
echo -e "  Para desinstalar:"
echo -e "  ${AZUL}curl -sSL https://github.com/$GITHUB_REPO/releases/latest/download/desinstalar-linux.sh | bash${RESET}"
echo ""

# Pergunta se quer abrir agora
read -p "Deseja abrir o sistema agora? [s/N] " resposta
if [[ "$resposta" =~ ^[Ss]$ ]]; then
    info "Abrindo o sistema..."
    nohup java -jar "$JAR_LOCAL" &>/dev/null &
    sucesso "Sistema iniciado!"
fi