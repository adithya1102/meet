@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
cd /d %~dp0
"gradle-9.3.1\gradle-9.3.1\bin\gradle.bat" -g .gradle_user_home --no-daemon clean assembleDebug %*
