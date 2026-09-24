# NOVA Android v1

Первый Android-клиент для NOVA Messenger. Это нативная Android-оболочка над текущим HTTPS-интерфейсом NOVA: сайт и PHP/API остаются на сервере, а APK отвечает за Android WebView, микрофон, выбор файлов, загрузки, полноэкранное видео и системную кнопку «Назад».

## Текущая конфигурация

- Package: `com.nova.messenger`
- App name: `NOVA`
- Version: `1.0.0`
- Min Android: 8.0 (API 26)
- Target/Compile SDK: 35
- Java: 17
- NOVA URL: `https://9yy8ob5y94yx.mjtest.ru/`

Адрес сайта хранится в `app/build.gradle` в `BuildConfig.NOVA_URL`. Если домен NOVA изменится, достаточно поменять только эту строку и пересобрать APK.

## Уже подключено

- сохранение авторизации через постоянные cookies/localStorage WebView;
- JavaScript и DOM Storage;
- микрофон через Android `RECORD_AUDIO` + WebRTC permission bridge;
- камера как подготовленная WebView permission;
- системный Android file picker для `<input type=file>`;
- загрузка файлов через Android Download Manager с cookies текущей сессии;
- внешние ссылки открываются системным браузером, NOVA остаётся внутри приложения;
- полноэкранное воспроизведение видео;
- кнопка Android «Назад» сначала работает по истории NOVA;
- тёмные status/navigation bars;
- splash screen и иконка из текущего оригинального NOVA artwork;
- только HTTPS (`usesCleartextTraffic=false`).

## Что сознательно не включено в v1

Нативный background push (FCM) пока не подключён. Web Push из браузерной версии нельзя считать надёжным background push внутри Android WebView. Для v2 лучше добавить отдельный FCM-транспорт и серверную регистрацию Android device token. Чаты, API и данные при этом менять с нуля не придётся.

## Сборка в Android Studio

1. Установить Android Studio и Android SDK Platform 35.
2. Открыть папку `NOVA_Android_v1` как проект.
3. Дождаться Gradle Sync.
4. `Build -> Build Bundle(s) / APK(s) -> Build APK(s)`.
5. Debug APK появится в `app/build/outputs/apk/debug/`.

Для релизной публикации нужен собственный signing keystore. Его специально нет в этом архиве.

## Безопасность

В проект не включаются `nova-store.php`, `nova-secret.php`, VAPID private key, серверная база и пользовательские uploads. Android-клиент подключается к уже работающему HTTPS-серверу NOVA.
