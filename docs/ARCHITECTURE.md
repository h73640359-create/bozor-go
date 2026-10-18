# BozorGo ilovasi arxitekturasi

## Texnologiyalar

| Qatlam | Texnologiya |
|---|---|
| UI | Jetpack Compose, Material Design 3, Navigation Compose |
| State | MVVM (`ViewModel` + `StateFlow`), bir tomonlama ma'lumotlar oqimi |
| DI | Hilt (`@HiltViewModel`, `@Singleton` provayderlar) |
| Ma'lumotlar | Room (oflayn baza), DataStore (sozlamalar), Retrofit + OkHttp + Gson (tarmoq) |
| Asinxronlik | Kotlin Coroutines + Flow |
| Rasmlar | Coil (xotira + disk kesh, 150 MB gacha) |
| Til | Kotlin 1.9.24, JDK 17, AGP 8.5.2, minSdk 24, targetSdk 34 |

## Paketlar

```
uz.bozorgo
├── core/
│   ├── common/      Resource<T> (Loading/Success/Error), Constants
│   ├── util/        MoneyFormatter, TextNormalizer, PhoneValidator, NetworkObserver, FileSizeFormatter
│   └── di/          AppModule, NetworkModule, DatabaseModule, RepositoryModule, Qualifiers
├── domain/
│   ├── model/       Product, Category, CartItem, CartSummary, Order, User, Address, ProductFilter, SortOption
│   ├── repository/  7 ta interfeys (Product, Catalog, Cart, Favorite, Order, User, Address)
│   └── usecase/     CartCalculator (savat hisobi)
├── data/
│   ├── local/db/    AppDatabase, 5 ta entity, 5 ta DAO, ProductQueryBuilder
│   ├── local/datastore/  UserPreferences (til, mavzu, profil, qidiruv tarixi)
│   ├── remote/      ApiService (Retrofit), ApiConfig, DTO
│   ├── repository/  7 ta implementatsiya (avval lokal baza, keyin server)
│   ├── seed/        SeedImporter (assets/seed/*.json)
│   └── mapper/      entity ↔ domain, DTO ↔ domain
└── ui/
    ├── theme/       ranglar, tipografiya, BozorGoTheme
    ├── components/  ProductCard, ProductGrid, EmptyState, ErrorState, QuantityStepper …
    ├── navigation/  Screen (marshrutlar), BozorGoBottomBar, BozorGoApp (NavHost)
    └── screens/     home, catalog, search, product, cart, checkout, orders,
                     profile, auth, address, favorites, info, settings
```

## Ma'lumotlar oqimi

```
UI (Composable)
  │  hodisa (foydalanuvchi bosdi)
  ▼
ViewModel (StateFlow<UiState>)
  │  suspend fun / Flow
  ▼
Repository interfeysi (domain)
  │
  ▼
RepositoryImpl (data)
  ├── Room (asosiy manba — oflayn ishlaydi)
  └── Retrofit/ApiService (faqat API_BASE_URL sozlangan bo'lsa)
```

- Har bir ekran `XxxRoute`(Hilt) → `XxxScreen`(sofa composable) juftligidan iborat:
  Route ViewModel'ni bog'laydi, Screen faqat holatni chizadi (oson testlash uchun).
- `Resource<T>` bilan xatoliklar (`isNetworkError` belgisi bilan) UI ga yetkaziladi.
- `combine()` Kotlin'da ko'pi bilan 5 ta oqim qabul qiladi — shuning uchun keng holatlar
  zanjir bilan yig'iladi: `combine(a..e){}.combine(f){}.combine(g){}`.

## Oflayn rejim (muhim)

`ApiConfig.isConfigured == false` (ya'ni `API_BASE_URL` bo'sh) bo'lsa:

- katalog `assets/seed/catalog.json` dan Room bazasiga yoziladi (`SeedImporter`, 1000 ta mahsulot);
- barcha o'qishlar faqat lokal bazadan;
- buyurtmalar qurilmada saqlanadi va `syncPendingOrders()` orqali internet paydo bo'lganda yuboriladi;
- hech bir joyda "server bilan bog'landi" degan yolg'on holat ko'rsatilmaydi (`checkout_offline_hint`).

Server sozlangan bo'lsa, har bir repository avval tarmoqqa murojaat qiladi, xatolik bo'lsa
lokal bazaga tushadi va `Resource.Error(isNetworkError = true)` qaytaradi.

## Savat, chegirma va yetkazib berish hisobi

`CartCalculator` (domain/usecase) — yagona manba, server (`server/src/pricing.js`) bilan bir xil qoidalar:

- `pickup` → 0 so'm;
- `express` → 25 000 so'm;
- `standard` → 15 000 so'm, lekin 100 000 so'mdan yuqori savatda bepul;
- savat bo'sh bo'lsa → 0.

Savat kaliti: `productId + "|" + selectedOption` — bir mahsulotning turli variantlari alohida qator.

## Xavfsizlik va halollik

- Hech qanday API kaliti, parol yoki to'lov tokeni ilova ichida saqlanmaydi.
- Karta ma'lumotlari ilovaga kiritilmaydi; to'lov demo rejimda ekani matnda ko'rsatiladi.
- Server narx/chegirma/ombor/summani qayta hisoblaydi (`server/src/routes/orders.js`).
- Takroriy buyurtma `idempotencyKey` (lokal UUID) bilan bloklanadi.
