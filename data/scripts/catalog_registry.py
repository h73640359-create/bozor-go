"""Kategoriya va kichik kategoriyalar registri.

Har bir kichik kategoriya uchun:
  * O'zbekcha nomi
  * Ikonka kaliti (rasm generatori shu kalit bo'yicha chizadi)
  * Asosiy rang (rasm fon palitrasi shu rangdan hosil qilinadi)
  * Tavsif shabloni uchun matn parchasi
"""

# kichik_kategoriya: (nomi, ikonka, tavsif parchasi)
SUBCATEGORIES = {
    # --- Meva va sabzavotlar ---
    "fresh-fruits": ("Yangi mevalar", "apple", "Yangi terilgan, tabiiy pishgan mevalar."),
    "fresh-vegetables": ("Sabzavotlar", "carrot", "Kundalik ovqatlar uchun yangi sabzavotlar."),
    "greens": ("Ko'katlar va ziravor o'tlar", "leaf", "Xushbo'y va vitaminli yashil ko'katlar."),
    "dried-fruits-nuts": ("Quritilgan mevalar va yong'oqlar", "nuts", "Tabiiy quritilgan mevalar va yong'oqlar."),
    # --- Go'sht, tovuq va baliq ---
    "beef": ("Mol go'shti", "steak", "Sovutilgan, sifatli mol go'shti."),
    "mutton": ("Qo'y go'shti", "steak", "Halol, yog'li va yumshoq qo'y go'shti."),
    "poultry": ("Tovuq, kurka va boshqa parranda", "drumstick", "Ferma parrandasi go'shti, halol so'yilgan."),
    "sausages": ("Kolbasa va go'sht mahsulotlari", "sausage", "Tayyor go'sht mahsulotlari va kolbasalar."),
    "fish": ("Baliq va dengiz mahsulotlari", "fish", "Yangi va muzlatilgan baliq mahsulotlari."),
    # --- Sut mahsulotlari ---
    "milk": ("Sut", "milk-carton", "Tabiiy sigir suti, turli yog'lilikda."),
    "yogurt-sour": ("Qatiq, yogurt va kefir", "yogurt-cup", "Sut kislotasi mahsulotlari, foydali bakteriyalar bilan."),
    "cheese": ("Pishloq va tvorog", "cheese", "Turli navdagi pishloq va tvorog mahsulotlari."),
    "butter-oil": ("Sariyog' va yog'lar", "butter", "Tabiiy sariyog' va o'simlik yog' aralashmalari."),
    "cream": ("Qaymoq", "yogurt-cup", "Qalin va suyuq qaymoq, turli yog'lilikda."),
    "eggs": ("Tuxum", "egg", "Yangi tovuq va bedana tuxumi."),
    "ice-cream": ("Muzqaymoq", "ice-cream", "Sutli va mevali muzqaymoqlar."),
    # --- Non va shirinliklar ---
    "bread": ("Non va non mahsulotlari", "bread", "Tandir va zavod nonlari, har kuni yangi."),
    "pastry": ("Pishiriqlar va somsa", "samsa", "Go'shtli, sabzavotli va shirin pishiriqlar."),
    "cakes": ("Tortlar", "cake", "Bayram tortlari, buyurtma asosida tayyorlanadi."),
    "chocolate-candy": ("Shokolad va konfetlar", "chocolate", "Sutli, achchiq va to'ldirmali shokoladlar."),
    "cookies": ("Pechenye va vafli", "cookie", "Choyga qo'shib iste'mol qilish uchun qandolat."),
    "halva-sweets": ("Xolva va sharqona shirinliklar", "halva", "An'anaviy sharq shirinliklari."),
    # --- Ichimliklar ---
    "water": ("Suv", "water-bottle", "Toza ichimlik va mineral suvlar."),
    "juice": ("Sharbat va nektarlar", "juice-box", "Tabiiy meva sharbatlari va nektarlar."),
    "carbonated": ("Gazlangan ichimliklar", "soda-bottle", "Salqinlashtiruvchi gazlangan ichimliklar."),
    "tea": ("Choy", "cup-hot", "Qora, yashil va o'tli choylar."),
    "coffee": ("Qahva", "cup-hot", "Don, maydalangan va eruvchan qahva."),
    "energy-sports": ("Energetik va sport ichimliklari", "can", "Mashg'ulotdan keyin kuch to'plash uchun."),
    "dairy-drinks": ("Sutli ichimliklar", "milk-carton", "Sut asosidagi tayyor ichimliklar."),
    # --- Oziq-ovqat ---
    "rice": ("Guruch", "sack", "Osh va kundalik taomlar uchun guruch."),
    "flour-pasta": ("Un va makaron mahsulotlari", "sack", "Oliy nav un va makaron mahsulotlari."),
    "oil": ("O'simlik yog'i", "oil-bottle", "Tozalangan va xushbo'y o'simlik yog'lari."),
    "sugar-salt": ("Shakar va tuz", "sack", "Kundalik oshxona uchun zarur mahsulotlar."),
    "cereals": ("Yormalar", "box", "Nonushta va garnir uchun yormalar."),
    "canned": ("Konservalar", "can", "Uzoq saqlanadigan konserva mahsulotlari."),
    "sauces": ("Sous va ketchup", "oil-bottle", "Ta'm beruvchi souslar va ketchuplar."),
    "spices": ("Ziravorlar", "spice-jar", "Oshxonangiz uchun xushbo'y ziravorlar."),
    "legumes": ("Dukkaklilar", "sack", "Oqsilga boy dukkakli mahsulotlar."),
    # --- Uy-ro'zg'or ---
    "laundry": ("Kir yuvish vositalari", "box", "Kukun, gel va kapsulalar, barcha matolar uchun."),
    "dishwashing": ("Idish yuvish vositalari", "oil-bottle", "Yog'ni tez erituvchi idish yuvish vositalari."),
    "surface-cleaners": ("Yuza tozalash vositalari", "spray-bottle", "Pol, oyna va hammom yuzalari uchun."),
    "paper": ("Qog'oz mahsulotlari", "paper-roll", "Qog'oz sochiq, salfetka va hojatxona qog'ozi."),
    "trash-bags": ("Axlat qoplari va qutilari", "trash-bag", "Mustahkam axlat qoplari va qutilar."),
    "home-care": ("Uy parvarishi anjomlari", "mop", "Tozalash anjomlari va saqlash yechimlari."),
    "pest-control": ("Hasharotlarga qarshi vositalar", "spray-bottle", "Chivin, chumoli va boshqa hasharotlarga qarshi."),
    "air-freshener": ("Havo yangilagichlar", "air-freshener", "Uy va ofis uchun xushbo'y havo."),
    # --- Gigiyena va kosmetika ---
    "oral-care": ("Og'iz bo'shlig'i parvarishi", "toothbrush", "Tish pastasi, cho'tka va chayqash vositalari."),
    "hair-care": ("Soch parvarishi", "shampoo-bottle", "Shampun, balzam va soch maskalari."),
    "body-care": ("Tana parvarishi", "shampoo-bottle", "Dush geli, sovun va tana kremlari."),
    "skin-care": ("Yuz parvarishi", "cream-jar", "Krem, zardob va tozalash vositalari."),
    "shaving": ("Soqol olish", "razor", "Soqol olish vositalari va stanoklar."),
    "hygiene": ("Shaxsiy gigiyena", "soap-bar", "Kundalik gigiyena va tibbiy buyumlar."),
    "deodorants": ("Dezodorantlar", "deodorant", "Uzoq muddat himoya qiluvchi dezodorantlar."),
    # --- Bolalar ---
    "diapers": ("Tagliklar", "box", "Yumshoq va nafas oluvchi tagliklar."),
    "baby-food": ("Bolalar ovqati", "baby-bottle", "Yoshga mos aralashmalar va pyurelar."),
    "baby-care": ("Bolalar parvarishi", "shampoo-bottle", "Bola terisi uchun maxsus vositalar."),
    "baby-bath": ("Bolalar gigiyenasi", "tub", "Cho'milish anjomlari va aksessuarlari."),
    "toys": ("O'yinchoqlar", "teddy", "Rivojlantiruvchi va qiziqarli o'yinchoqlar."),
    # --- Kiyim va poyabzal ---
    "mens-clothing": ("Erkaklar kiyimi", "tshirt", "Kundalik va klassik erkaklar kiyimi."),
    "womens-clothing": ("Ayollar kiyimi", "dress", "Zamonaviy ayollar kiyimlari."),
    "kids-clothing": ("Bolalar kiyimi", "tshirt", "Bolalar uchun qulay kiyimlar."),
    "footwear": ("Poyabzal", "sneaker", "Krossovka, tufli va mavsumiy poyabzal."),
    "underwear-socks": ("Ichki kiyim va paypoq", "socks", "Paxtali ichki kiyim va paypoqlar."),
    "accessories": ("Aksessuarlar", "cap", "Sumka, kepka va boshqa aksessuarlar."),
    # --- Elektronika ---
    "phones": ("Telefonlar va planshetlar", "smartphone", "Smartfonlar, planshetlar va tugmali telefonlar."),
    "phone-accessories": ("Telefon aksessuarlari", "phone-case", "G'ilof, zaryadlovchi va kabellar."),
    "audio": ("Audio texnika", "headphones", "Quloqchinlar va portativ kolonkalar."),
    "power": ("Quvvatlovchi qurilmalar", "power-bank", "Quvvat banklari va tarmoq uskunalari."),
    "small-appliances": ("Kichik maishiy texnika", "kettle", "Oshxona va uy uchun elektr jihozlar."),
    "computer-accessories": ("Kompyuter aksessuarlari", "mouse", "Sichqoncha, klaviatura va kamera."),
    # --- Oshxona ---
    "cookware": ("Idish-tovoq", "pot", "Qozon, tovoq va pishirish idishlari."),
    "kitchen-tools": ("Oshxona anjomlari", "knife", "Pichoq, taxta va kichik anjomlar."),
    "tableware": ("Dasturxon va idishlar", "plate", "Likopcha, piyola va to'plamlar."),
    "storage": ("Saqlash va tartib", "container", "Konteynerlar va saqlash yechimlari."),
    "home-textile": ("Uy to'qimachiligi", "blanket", "Ko'rpa-to'shak, sochiq va yostiqlar."),
    # --- Sport ---
    "fitness": ("Fitnes va trenajorlar", "dumbbell", "Uy sharoitida mashq qilish uchun anjomlar."),
    "outdoor-sport": ("Sport turlari", "ball", "To'p, raketka va suzish anjomlari."),
    "bicycles": ("Velosipedlar", "bicycle", "Tog', shahar va bolalar velosipedlari."),
    "camping": ("Lager va sayohat", "tent", "Chodir, uyqu xaltasi va lager anjomlari."),
    "sport-accessories": ("Sport aksessuarlari", "backpack", "Sumka, shisha va himoya vositalari."),
    # --- Uy hayvonlari ---
    "cat": ("Mushuklar uchun", "pet-bowl", "Mushuklar uchun ozuqa va anjomlar."),
    "dog": ("Itlar uchun", "pet-bowl", "Itlar uchun ozuqa va parvarish vositalari."),
    "birds-other": ("Qushlar va boshqa hayvonlar", "pet-bowl", "Qush, baliq va kemiruvchilar uchun."),
    # --- Avto va boshqa ---
    "car-care": ("Avtomobil parvarishi", "oil-bottle", "Avtokosmetika va texnik suyuqliklar."),
    "car-accessories": ("Avtomobil aksessuarlari", "car-freshener", "Salon uchun aksessuar va jihozlar."),
    "stationery-other": ("Kanselyariya va boshqa mahsulotlar", "pen", "Yozuv va ofis buyumlari."),
}

# Kategoriya: (nomi, asosiy rang HEX, ikonka)
CATEGORY_COLORS = {
    "fruits-vegetables": "#4CAF50",
    "meat-poultry-fish": "#E4572E",
    "dairy-eggs": "#4A90D9",
    "bakery-sweets": "#C97B2E",
    "beverages-water": "#1E88E5",
    "grocery-staples": "#8D6E63",
    "household-cleaning": "#009688",
    "personal-care": "#7E57C2",
    "baby-products": "#F48FB1",
    "clothing-shoes": "#5C6BC0",
    "phones-electronics": "#3949AB",
    "kitchen-homeware": "#D84315",
    "sport-outdoor": "#00897B",
    "pet-supplies": "#8BC34A",
    "auto-other": "#546E7A",
}

# O'lchov birliklari (unit -> ko'rsatish uchun qisqa nom)
UNIT_LABELS = {
    "kg": "kg",
    "g": "g",
    "l": "l",
    "ml": "ml",
    "dona": "dona",
    "qadoq": "qadoq",
    "juft": "juft",
    "bog": "bog'",
}
