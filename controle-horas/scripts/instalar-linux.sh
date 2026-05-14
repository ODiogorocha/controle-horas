#!/bin/bash

# =============================================================
# INSTALADOR / ATUALIZADOR - CONTROLE DE HORAS
# =============================================================

set -e

APP_VERSION="1.5.1"

GITHUB_USER="ODiogorocha"
GITHUB_REPO="$GITHUB_USER/controle-de-horas"

INSTALL_DIR="$HOME/.local/share/controle-horas"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="$HOME/.local/share/applications"

JAR_URL="https://github.com/$GITHUB_REPO/releases/latest/download/controle-horas.jar"
ICON_URL="https://github.com/$GITHUB_REPO/releases/latest/download/controle-horas.png"

JAR_LOCAL="$INSTALL_DIR/controle-horas.jar"
ICON_LOCAL="$INSTALL_DIR/controle-horas.png"

PID_FILE="$INSTALL_DIR/app.pid"

echo ""
echo "=================================================="
echo "      Sistema de Controle de Horas"
echo "=================================================="
echo ""
echo "Versao: $APP_VERSION"
echo ""

# =============================================================
# 1. JAVA
# =============================================================

echo "[1/8] Verificando Java..."

if ! command -v java &>/dev/null; then
    echo ""
    echo "ERRO: Java nao encontrado."
    echo ""
    echo "Instale Java 21:"
    echo "https://adoptium.net"
    echo ""
    exit 1
fi

echo "[OK] Java encontrado."

# =============================================================
# 2. PASTAS
# =============================================================

echo ""
echo "[2/8] Criando diretorios..."

mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$DESKTOP_DIR"

echo "[OK] Diretorios criados."

# =============================================================
# 3. PARAR VERSAO ANTIGA
# =============================================================

echo ""
echo "[3/8] Verificando instancia antiga..."

if [ -f "$PID_FILE" ]; then

    OLD_PID=$(cat "$PID_FILE")

    if ps -p "$OLD_PID" > /dev/null 2>&1; then

        echo "Parando versao anterior..."

        kill "$OLD_PID" || true

        sleep 2
    fi

    rm -f "$PID_FILE"
fi

pkill -f "controle-horas.jar" || true

echo "[OK] Sistema antigo finalizado."

# =============================================================
# 4. DOWNLOAD JAR
# =============================================================

echo ""
echo "[4/8] Baixando sistema..."

TMP_JAR="$INSTALL_DIR/controle-horas-new.jar"

if command -v wget &>/dev/null; then
    wget -O "$TMP_JAR" "$JAR_URL"
else
    curl -L -o "$TMP_JAR" "$JAR_URL"
fi

if [ ! -f "$TMP_JAR" ]; then
    echo ""
    echo "ERRO: Falha ao baixar JAR."
    exit 1
fi

if [ ! -s "$TMP_JAR" ]; then
    echo ""
    echo "ERRO: JAR vazio."
    exit 1
fi

mv -f "$TMP_JAR" "$JAR_LOCAL"

echo "[OK] Sistema atualizado."

# =============================================================
# 5. DOWNLOAD ICONE
# =============================================================

echo ""
echo "[5/8] Baixando icone..."

if command -v wget &>/dev/null; then
    wget -O "$ICON_LOCAL" "$ICON_URL" || true
else
    curl -L -o "$ICON_LOCAL" "$ICON_URL" || true
fi

if [ -f "$ICON_LOCAL" ]; then
    echo "[OK] Icone atualizado."
else
    echo "[AVISO] Icone nao encontrado."
fi

# =============================================================
# 6. SCRIPT EXECUCAO
# =============================================================

echo ""
echo "[6/8] Criando launcher..."

cat > "$BIN_DIR/controle-horas" << SCRIPT
#!/bin/bash

APP_DIR="$INSTALL_DIR"
PID_FILE="\$APP_DIR/app.pid"

nohup java -jar "\$APP_DIR/controle-horas.jar" > "\$APP_DIR/app.log" 2>&1 &

echo \$! > "\$PID_FILE"
SCRIPT

chmod +x "$BIN_DIR/controle-horas"

echo "[OK] Launcher criado."

# =============================================================
# 7. MENU
# =============================================================

echo ""
echo "[7/8] Criando menu..."

cat > "$DESKTOP_DIR/controle-horas.desktop" << DESKTOP
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
DESKTOP

chmod +x "$DESKTOP_DIR/controle-horas.desktop"

echo "[OK] Atalho criado."

# =============================================================
# 8. DESINSTALADOR
# =============================================================

echo ""
echo "[8/8] Criando desinstalador..."

cat > "$INSTALL_DIR/desinstalar.sh" << REMOVE
#!/bin/bash

echo ""
echo "Desinstalando Controle de Horas..."
echo ""

PID_FILE="$INSTALL_DIR/app.pid"

if [ -f "\$PID_FILE" ]; then

    PID=\$(cat "\$PID_FILE")

    kill \$PID 2>/dev/null || true

    rm -f "\$PID_FILE"
fi

pkill -f "controle-horas.jar" || true

rm -f "$BIN_DIR/controle-horas"

rm -f "$DESKTOP_DIR/controle-horas.desktop"

rm -rf "$INSTALL_DIR"

echo ""
echo "Sistema removido com sucesso."
echo ""
REMOVE

chmod +x "$INSTALL_DIR/desinstalar.sh"

echo "[OK] Desinstalador criado."

# =============================================================
# FINAL
# =============================================================

echo ""
echo "=================================================="
echo "      Instalacao concluida com sucesso!"
echo "=================================================="
echo ""

echo "Sistema instalado em:"
echo "  $INSTALL_DIR"

echo ""
echo "Abrir:"
echo "  Menu de aplicativos -> Controle de Horas"

echo ""
echo "Ou execute:"
echo "  controle-horas"

echo ""
read -p "Abrir agora? [s/N]: " abrir

if [[ "$abrir" =~ ^[Ss]$ ]]; then
    nohup java -jar "$JAR_LOCAL" > "$INSTALL_DIR/app.log" 2>&1 &
    echo $! > "$PID_FILE"
fi

echo ""