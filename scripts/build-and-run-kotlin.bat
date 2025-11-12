@echo off
cd /d "C:\path\to\your\kotlin\project"
REM Build the project (ensure Gradle is in your PATH)
gradlew.bat build

REM Run the app (adjust the path to your JAR if needed)
java -jar build\libs\your-app.jar