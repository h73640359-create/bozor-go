# APK yig'ish va o'rnatish

## Talablar

- JDK 17 (Temurin tavsiya etiladi)
- Android SDK (compileSdk 34, build-tools), `ANDROID_HOME` sozlangan
- Internet (Gradle va bog'liqliklarni yuklash uchun)
- Ixtiyoriy: PostgreSQL + Node.js 18+ (backend uchun)

## 1. local.properties

```bash
cp local.properties.example local.properties
```

Muhim maydonlar:

| Kalit | Ma'nosi |
|---|---|
| `sdk.dir` | Android SDK yo'li (masalan `/home/user/Android/Sdk`) |
| `API_BASE_URL` | Server manzili. **Bo'sh bo'lsa** — ilova to'liq oflayn (lokal baza, 1000 ta mahsulot) |
| `KEYSTORE_FILE` va h.k. | Faqat release imzolash uchun (ixtiyoriy) |

Misol (emulyatordan serverga chiqish):

```
sdk.dir=/home/user/Android/Sdk
API_BASE_URL=http://10.0.2.2:3000/api/
```

## 2. Debug APK

```bash
./gradlew assembleDebug
# Natija: app/build/outputs/apk/debug/app-debug.apk
```

O'rnatish:

```bash
./gradlew installDebug                      # ulangan qurilma/emulyator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 3. Testlar

```bash
./gradlew testDebugUnitTest                 # unit testlar (>=1000 mahsulot tekshiruvi)
python3 data/scripts/validate_catalog.py --with-images
cd server && npm test
```

## 4. Release APK (imzolash)

Variant A — `local.properties` orqali:

```
KEYSTORE_FILE=/home/user/bozorgo.jks
KEYSTORE_PASSWORD=*****
KEY_ALIAS=bozorgo
KEY_PASSWORD=*****
```

Variant B — muhit o'zgaruvchilari (CI uchun qulay):

```bash
export BOZORGO_KEYSTORE_FILE=/home/user/bozorgo.jks
export BOZORGO_KEYSTORE_PASSWORD=*****
export BOZORGO_KEY_ALIAS=bozorgo
export BOZORGO_KEY_PASSWORD=*****
./gradlew assembleRelease
```

Kalit yaratish (bir marta):

```bash
keytool -genkeypair -v -keystore bozorgo.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias bozorgo
```

Agar keystore ko'rsatilmasa, `assembleRelease` imzolanmagan APK yig'adi (o'rnatish uchun yaroqsiz).

## 5. CI

GitHub Actions (`.github/workflows/android.yml`) har `push`/`pull_request` da:
katalog validatorini, unit testlarni va `assembleDebug` ni bajaradi, so'ng
`app/build/outputs/apk/debug/app-debug.apk` faylini artefakt sifatida yuklaydi.
Batafsil: [CI.md](CI.md).

## 6. Muammolar

| Muammo | Yechim |
|---|---|
| `SDK location not found` | `local.properties` ichida `sdk.dir` ni to'g'rilang |
| Ilova ishga tushganda uzoq kutyapti | Birinchi ishga tushirishda 1000 ta mahsulot bazaga yoziladi (splash shu vaqt turadi) |
| Serverga ulanmayapti | `API_BASE_URL` to'g'ri va `http://` bilan tugaganligini tekshiring; telefon uchun kompyuterning LAN IP si kerak |
| `cleartext traffic not permitted` | `app/src/main/res/xml/network_security_config.xml` da o'z domeningizni ruxsat qiling |
