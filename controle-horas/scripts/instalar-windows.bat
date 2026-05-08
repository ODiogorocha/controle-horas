@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

:: =============================================================
::  Instalador do Sistema de Controle de Horas - Windows
::  Compativel com Windows 10 e 11
:: =============================================================

title Instalador - Sistema de Controle de Horas

set "APP_NAME=Controle de Horas"
set "APP_VERSION=1.0.0"
set "INSTALL_DIR=%LOCALAPPDATA%\ControleHoras"
set "JAR_NAME=controle-horas.jar"
set "GITHUB_REPO=SEU_USUARIO/controle-horas"
set "JAR_URL=https://github.com/%GITHUB_REPO%/releases/latest/download/controle-horas.jar"
set "JAVA_URL=https://github.com/adoptium/temurin21-binaries/releases/latest/download/OpenJDK21U-jre_x64_windows_hotspot_21.0.5_11.msi"

echo.
echo =============================================
echo    Sistema de Controle de Horas v%APP_VERSION%
echo    Instalador para Windows
echo =============================================
echo.

:: ── 1. Verifica Java ─────────────────────────────────────────
echo [INFO] Verificando se o Java esta instalado...

java -version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VER=%%i
        set JAVA_VER=!JAVA_VER:"=!
    )
    echo [OK]   Java encontrado: !JAVA_VER!
    goto :java_ok
)

echo [AVISO] Java nao encontrado. Baixando e instalando automaticamente...
echo.
echo        Isso pode demorar alguns minutos dependendo da sua internet.
echo        Aguarde...
echo.

:: Baixa o instalador do Java usando PowerShell
set "JAVA_INSTALLER=%TEMP%\java-installer.msi"
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%JAVA_URL%' -OutFile '%JAVA_INSTALLER%' -UseBasicParsing}"

if not exist "%JAVA_INSTALLER%" (
    echo [ERRO] Falha ao baixar o Java.
    echo.
    echo Instale manualmente em: https://adoptium.net
    echo Escolha: Windows x64 - JRE 21
    echo.
    echo Depois de instalar o Java, execute este instalador novamente.
    pause
    exit /b 1
)

echo [INFO] Instalando Java (pode pedir permissao de administrador)...
msiexec /i "%JAVA_INSTALLER%" /quiet /norestart
del "%JAVA_INSTALLER%"

:: Atualiza PATH para incluir Java recem instalado
for /f "tokens=*" %%i in ('where java 2^>nul') do set "JAVA_PATH=%%i"
if not defined JAVA_PATH (
    set "JAVA_PATH=%ProgramFiles%\Eclipse Adoptium\jre-21*\bin\java.exe"
)

echo [OK]   Java instalado com sucesso!

:java_ok

:: ── 2. Cria pasta de instalacao ──────────────────────────────
echo [INFO] Criando pasta de instalacao em %INSTALL_DIR% ...
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"
echo [OK]   Pasta criada.

:: ── 3. Baixa o JAR ───────────────────────────────────────────
echo [INFO] Baixando o sistema...
echo        URL: %JAR_URL%
echo.

set "JAR_PATH=%INSTALL_DIR%\%JAR_NAME%"

if exist "%JAR_PATH%" (
    echo [AVISO] Versao anterior encontrada. Atualizando...
    del "%JAR_PATH%"
)

powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.DownloadFile('%JAR_URL%', '%JAR_PATH%') }"

if not exist "%JAR_PATH%" (
    echo [ERRO] Falha ao baixar o sistema.
    echo        Verifique sua conexao com a internet e tente novamente.
    pause
    exit /b 1
)

echo [OK]   Sistema baixado com sucesso.

:: ── 4. Cria script de inicializacao ─────────────────────────
echo [INFO] Criando atalho de inicializacao...

set "LAUNCHER=%INSTALL_DIR%\iniciar.bat"
(
    echo @echo off
    echo title Sistema de Controle de Horas
    echo cd /d "%INSTALL_DIR%"
    echo start "" javaw -jar "%JAR_PATH%"
) > "%LAUNCHER%"

echo [OK]   Script de inicializacao criado.

:: ── 5. Cria atalho na area de trabalho ───────────────────────
echo [INFO] Criando atalho na area de trabalho...

set "SHORTCUT=%USERPROFILE%\Desktop\Controle de Horas.lnk"
set "START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Controle de Horas.lnk"

powershell -Command "& { $ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut('%SHORTCUT%'); $s.TargetPath = 'javaw'; $s.Arguments = '-jar \"%JAR_PATH%\"'; $s.WorkingDirectory = '%INSTALL_DIR%'; $s.Description = 'Sistema de Controle de Horas e Ponto'; $s.Save() }"

powershell -Command "& { $ws = New-Object -ComObject WScript.Shell; $s = $ws.CreateShortcut('%START_MENU%'); $s.TargetPath = 'javaw'; $s.Arguments = '-jar \"%JAR_PATH%\"'; $s.WorkingDirectory = '%INSTALL_DIR%'; $s.Description = 'Sistema de Controle de Horas e Ponto'; $s.Save() }"

echo [OK]   Atalho criado na area de trabalho e no menu Iniciar.

:: ── 6. Registra no Painel de Controle (Adicionar/Remover) ────
echo [INFO] Registrando no Painel de Controle...

set "UNINSTALL_KEY=HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\ControleHoras"
set "UNINSTALL_BAT=%INSTALL_DIR%\desinstalar.bat"

(
    echo @echo off
    echo title Desinstalador - Controle de Horas
    echo echo.
    echo echo Desinstalando Sistema de Controle de Horas...
    echo set /p "confirm=Tem certeza? [s/N]: "
    echo if /i not "!confirm!"=="s" exit /b 0
    echo del "%USERPROFILE%\Desktop\Controle de Horas.lnk" 2^>nul
    echo del "%START_MENU%" 2^>nul
    echo reg delete "%UNINSTALL_KEY%" /f 2^>nul
    echo rmdir /s /q "%INSTALL_DIR%"
    echo echo.
    echo echo Sistema desinstalado com sucesso!
    echo pause
) > "%UNINSTALL_BAT%"

reg add "%UNINSTALL_KEY%" /v "DisplayName"     /t REG_SZ /d "Sistema de Controle de Horas" /f >nul
reg add "%UNINSTALL_KEY%" /v "DisplayVersion"  /t REG_SZ /d "%APP_VERSION%" /f >nul
reg add "%UNINSTALL_KEY%" /v "Publisher"       /t REG_SZ /d "github.com/%GITHUB_REPO%" /f >nul
reg add "%UNINSTALL_KEY%" /v "UninstallString" /t REG_SZ /d "%UNINSTALL_BAT%" /f >nul
reg add "%UNINSTALL_KEY%" /v "InstallLocation" /t REG_SZ /d "%INSTALL_DIR%" /f >nul
reg add "%UNINSTALL_KEY%" /v "NoModify"        /t REG_DWORD /d 1 /f >nul
reg add "%UNINSTALL_KEY%" /v "NoRepair"        /t REG_DWORD /d 1 /f >nul

echo [OK]   Registrado no Painel de Controle.

:: ── 7. Conclusao ─────────────────────────────────────────────
echo.
echo =============================================
echo    Instalacao concluida com sucesso!
echo =============================================
echo.
echo   Como abrir o sistema:
echo   Opcao 1: Clique no icone "Controle de Horas" na area de trabalho
echo   Opcao 2: Procure "Controle de Horas" no menu Iniciar
echo.
echo   Para desinstalar:
echo   Va em Configuracoes ^> Aplicativos ^> Controle de Horas ^> Desinstalar
echo.

set /p "abrir=Deseja abrir o sistema agora? [s/N]: "
if /i "%abrir%"=="s" (
    echo [INFO] Abrindo o sistema...
    start "" javaw -jar "%JAR_PATH%"
    echo [OK]   Sistema iniciado!
)

echo.
pause