# Как получить APK

## Android Studio

Открой проект, дождись синхронизации и выбери:

`Build -> Build Bundle(s) / APK(s) -> Build APK(s)`

Для установки на телефон достаточно debug APK. Для Google Play или публичного релиза создаётся подписанная release-сборка через `Build -> Generate Signed Bundle / APK`.

## GitHub Actions

В проект добавлен workflow `.github/workflows/android-build.yml`. Если положить проект в GitHub и запустить workflow `Build NOVA Android`, GitHub соберёт debug APK и положит его в Artifacts.
