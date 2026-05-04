@echo off
echo ============================================
echo   EduVerse - Microservice Interview IA
echo ============================================
echo.

cd /d "%~dp0"

:: Ajouter ffmpeg au PATH (il est dans ce dossier)
set PATH=%~dp0;%PATH%
set PYTHONIOENCODING=utf-8

:: Verifier Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Python n'est pas installe ou pas dans le PATH.
    echo Installez Python depuis https://www.python.org/downloads/
    pause
    exit /b
)

:: Creer le venv s'il n'existe pas
if not exist "venv" (
    echo [1/3] Creation de l'environnement virtuel...
    python -m venv venv
    echo [2/3] Installation des dependances (ca peut prendre quelques minutes)...
    venv\Scripts\python.exe -m pip install --upgrade pip setuptools wheel >nul 2>&1
    venv\Scripts\python.exe -m pip install --no-build-isolation -r requirements.txt
    echo [3/3] Installation terminee !
) else (
    echo [OK] Environnement virtuel trouve.
)

:: Verifier ffmpeg
ffmpeg -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] ffmpeg.exe introuvable dans ce dossier.
    echo Telechargez ffmpeg et placez ffmpeg.exe ici : %~dp0
    pause
    exit /b
)

echo.
echo [OK] Demarrage du microservice sur http://localhost:8001 ...
echo [INFO] Chargement de Whisper (1-2 min la premiere fois)...
echo [INFO] Appuyez sur Ctrl+C pour arreter.
echo.

venv\Scripts\python.exe -m uvicorn main:app --host 0.0.0.0 --port 8001
pause
