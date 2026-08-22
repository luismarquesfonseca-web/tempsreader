BNF Weather - GitHub Actions build

This is a tiny native Android app that reads:
https://www.bnf.pt/weather.json

It refreshes once per minute and displays four sensor readings.

NO Android Studio is required.

BUILD WITH GITHUB:
1. Create a new GitHub repository (for example: bnf-weather).
2. Upload the CONTENTS of this folder to the repository root.
   Make sure .github/workflows/build-apk.yml is included.
3. Commit to the main branch.
4. Open the repository's Actions tab.
5. Open "Build Android APK".
6. Wait for the run to finish.
7. Open the completed run and find "Artifacts".
8. Download "BNF-Weather-debug".
9. Extract it and install app-debug.apk on the phone.

The workflow uses a GitHub-hosted runner with Java/Gradle and stores the
generated APK as a workflow artifact. No local Android development tools
are required.

JSON expected at the URL:
{
  "updated": 1724288700,
  "sensors": [
    {"slot":0,"name":"Home","temperature":23.4,"humidity":48,"battery":91},
    {"slot":1,"name":"Outside","temperature":21.1,"humidity":67,"battery":100},
    {"slot":2,"name":"Server Room","temperature":29.8,"humidity":38,"battery":82},
    {"slot":3,"name":"Mining Room","temperature":34.2,"humidity":31,"battery":76}
  ]
}

Version 2 changes: null readings are displayed as --, and the header shows the server's last-update timestamp.
