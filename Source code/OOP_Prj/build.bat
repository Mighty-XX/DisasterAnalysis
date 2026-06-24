@echo off
cd src
powershell -Command "Get-ChildItem -Recurse -Filter *.java | ForEach-Object { '\"' + $_.FullName.Replace('\', '\\') + '\"' } | Out-File -Encoding ascii sources.txt"
javac -d ..\bin -cp "..\lib\*" @sources.txt
del sources.txt
echo Build complete.
