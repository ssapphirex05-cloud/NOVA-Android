# NOVA Android Native 2.0

Ветка native-2.0 начинает полноценный нативный Android-клиент NOVA.

Рабочая WebView-версия 1.1.x остаётся в модуле app и в ветке main. Новый клиент находится в отдельном модуле nativeapp, поэтому старую сборку мы не ломаем.

Первая alpha уже работает без WebView:
- Kotlin + Jetpack Compose.
- Тот же сервер NOVA и те же аккаунты.
- Вход и регистрация.
- Список чатов и непрочитанные.
- Нативный экран диалога.
- Отправка текстовых сообщений.
- Онлайн, typing, delivered/read.
- Серверный polling.
- FCM token registration и native notification service.
- Safe area Android 15.
- Цвета и геометрия перенесены из styles-v3888.css.

Точные токены веб-NOVA:
- bg #080C12
- bg2 #0A1019
- panel #0F1722
- accent #3390EC
- accent2 #53B6FF
- mobile chat header 66dp
- conversation row 78dp
- list avatar 58dp
- chat avatar 42dp
- bubble radius 17dp, tail 5dp
- composer radius 19dp
- outgoing bubble #338FDC to #2878BD

Сборка:
gradle :nativeapp:assembleDebug

APK:
nativeapp/build/outputs/apk/debug/nativeapp-debug.apk

Для FCM положи google-services.json в nativeapp локально или через CI. Этот файл не коммитится.
