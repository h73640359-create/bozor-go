# GitHub Actions (`.github/workflows/android.yml`)

## Ish oqimlari

1. **build** — har bir `push`/`pull_request`/qo'lda ishga tushirishda:
   - `python3 data/scripts/validate_catalog.py --with-images` (katalog: 1000 ta mahsulot);
   - `./gradlew testDebugUnitTest` (unit testlar);
   - `./gradlew assembleDebug`;
   - artefakt: `app/build/outputs/apk/debug/app-debug.apk` (30 kun saqlanadi);
   - test muvaffaqiyatsiz bo'lsa — `app/build/reports/tests` ham yuklanadi.
2. **release** — faqat `v*` tegi va `vars.ENABLE_RELEASE_BUILD == 'true'` bo'lganda:
   - imzolangan `app-release.apk` + `mapping.txt`.

APK havolasi: **Actions → eng oxirgi muvaffaqiyatli ish → Artifacts → `app-debug`**
(artefakt 30 kun saqlanadi; debug paket nomi `uz.bozorgo.debug`).

Qo'shimcha: yig'ish muvaffaqiyatsiz bo'lsa, ish Gradle chiqishidagi xatolik
qatorlarini `::error::` annotatsiya sifatida chiqaradi va `unit-tests.log` /
`assemble-debug.log` fayllarini artefaktga qo'shadi — Actions jurnali
to'liq ko'rinmasa ham xatolik sababini ko'rish mumkin.

## Kerakli sirlari (Settings → Secrets and variables → Actions)

| Sir | Majburiy | Izoh |
|---|---|---|
| `API_BASE_URL` | yo'q | Bo'sh bo'lsa — oflayn APK yig'iladi |
| `KEYSTORE_BASE64` | faqat release | `base64 -w0 bozorgo.jks` chiqishi |
| `KEYSTORE_PASSWORD` | faqat release | keystore paroli |
| `KEY_ALIAS` | faqat release | kalit nomi |
| `KEY_PASSWORD` | faqat release | kalit paroli |

Release ish oqimini yoqish uchun repozitoriyga o'zgaruvchi qo'shing:
**Settings → Secrets and variables → Actions → Variables → `ENABLE_RELEASE_BUILD = true`**.

## Tekshiruvlar (local)

```bash
act -j build          # opsiyaviy: mahalliy ishga tushirish (act o'rnatilgan bo'lsa)
python3 data/scripts/validate_catalog.py --with-images
```

## Nima uchun CI muhim?

Loyihada JDK/Android SDK bo'lmagan muhitda ham APK aynan GitHub Actions'da yig'iladi va
haqiqiy artefakt sifatida saqlanadi — ya'ni "APK yig'ildi" degan gap har doim
`./gradlew assembleDebug` muvaffaqiyatli tugaganini bildiradi.
