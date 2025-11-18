@echo off
cd /d "C:\Users\alves\IdeaProjects\abacus-integration"

set /p ROUTELLM_API_KEY="Enter API Key: "

echo Building the project...
call gradlew.bat build

echo Launching application in new window...
start cmd /k "cd /d C:\Users\alves\IdeaProjects\abacus-integration && java -jar build\libs\abacus-integration-0.0.1.jar"