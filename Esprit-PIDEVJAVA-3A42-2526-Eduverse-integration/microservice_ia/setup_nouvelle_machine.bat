@echo off
echo ============================================
echo   EduVerse - Installation IA (nouvelle machine)
echo ============================================
echo.
echo Ce script installe tout ce qu'il faut pour le microservice IA.
echo Pre-requis : Python installe (https://www.python.org/downloads/)
echo.
pause

cd /d "%~dp0"
set PYTHONIOENCODING=utf-8

:: Verifier Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Python n'est pas installe.
    echo 1. Allez sur https://www.python.org/downloads/
    echo 2. Installez Python en cochant "Add Python to PATH"
    echo 3. Relancez ce script
    pause
    exit /b
)

echo [1/4] Creation de l'environnement virtuel...
python -m venv venv

echo [2/4] Mise a jour pip...
venv\Scripts\python.exe -m pip install --upgrade pip setuptools wheel >nul 2>&1

echo [3/4] Installation des dependances Python (5-10 min)...
venv\Scripts\python.exe -m pip install --no-build-isolation -r requirements.txt

:: Verifier ffmpeg
echo [4/4] Verification de ffmpeg...
if exist "ffmpeg.exe" (
    echo [OK] ffmpeg.exe trouve.
) else (
    echo [ATTENTION] ffmpeg.exe manquant !
    echo Telechargez-le depuis https://www.gyan.dev/ffmpeg/builds/
    echo Choisissez "ffmpeg-release-essentials.zip"
    echo Extrayez et copiez ffmpeg.exe dans ce dossier :
    echo   %~dp0
)

echo.
echo ============================================
echo   Installation terminee !
echo   Pour lancer : double-cliquez sur lancer_ia.bat
echo ============================================
pause
