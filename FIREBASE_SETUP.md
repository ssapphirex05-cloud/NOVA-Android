# NOVA Android 1.1.0 — Firebase Cloud Messaging setup

Native Android push is already implemented in the source. Two Firebase files are needed for real delivery.

## 1) Android client
1. Open https://console.firebase.google.com and create/select a Firebase project.
2. Add an Android app with package name: `com.nova.messenger`.
3. Download `google-services.json`.
4. Put it at `app/google-services.json` in this repository and commit it.
   Firebase documents this file as containing project/app identifiers rather than a private server credential.
5. GitHub Actions will rebuild the APK automatically.

## 2) NOVA server
1. In Firebase / Google Cloud create a service-account private key for the same project.
2. Download the JSON key.
3. On the NOVA web hosting upload it as:
   `nova_messenger_v1/data/nova-firebase-service-account.json`
4. Never commit this service-account JSON to GitHub and never put it in a public ZIP.
5. Upload the NOVA 3.8.88 server build so `api.php` can register Android FCM tokens and send HTTP v1 messages.

## 3) Test
- Install the rebuilt APK.
- Log in to NOVA.
- Enable notifications in NOVA settings and grant Android notification permission.
- Send a message from another account while the app is in background.
- Tapping the notification should open the matching chat.
