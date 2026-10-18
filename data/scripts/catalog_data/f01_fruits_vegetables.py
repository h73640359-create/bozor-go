"""Meva va sabzavotlar (100 ta mahsulot).

Har bir qator:  nom|kichik_kategoriya|brend|o'lchov_birligi|miqdor|narx(UZS)

Barcha ma'lumotlar DEMO ma'lumotlari bo'lib, haqiqiy do'kon narxlari emas.
Brend nomlari to'qima (fictional) va faqat namuna uchun yaratilgan.
"""

CATEGORY_ID = "fruits-vegetables"
CATEGORY_NAME = "Meva va sabzavotlar"
ICON = "produce"

# nom|sub|brand|unit|unitValue|price
RAW = """Olma Golden 1 kg|fresh-fruits|Bog'bon|kg|1|16900
Olma Qizil Delishes 1 kg|fresh-fruits|Bog'bon|kg|1|17900
Olma Renet Simirenko 1 kg|fresh-fruits|Farg'ona Bog'i|kg|1|15900
Olma Fuji 1 kg|fresh-fruits|Yashil Vodiy|kg|1|23900
Armud Konferensiya 1 kg|fresh-fruits|Farg'ona Bog'i|kg|1|24900
Armud Lesnaya Krasavitsa 1 kg|fresh-fruits|Bog'bon|kg|1|22900
Armud Nashi (osiyo nav) 1 kg|fresh-fruits|Samarqand Mevasi|kg|1|27900
Banan (Ekvador) 1 kg|fresh-fruits|Agro Fresh|kg|1|24500
Banan Lady Finger 1 kg|fresh-fruits|Agro Fresh|kg|1|31500
Banan pishgan (ekspress) 1 kg|fresh-fruits|Agro Fresh|kg|1|26900
Apelsin qizil (qonli) 1 kg|fresh-fruits|Samarqand Mevasi|kg|1|29900
Mandarin (Turkiya) 1 kg|fresh-fruits|Bog' Rizqi|kg|1|23500
Mandarin Klementin 1 kg|fresh-fruits|Bog' Rizqi|kg|1|31500
Limon (Turkiya) 1 kg|fresh-fruits|Hosildor|kg|1|18500
Limon yirik navli 1 kg|fresh-fruits|Hosildor|kg|1|21500
Laym 500 g|fresh-fruits|Hosildor|g|500|16900
Greyfurt 1 kg|fresh-fruits|Samarqand Mevasi|kg|1|18900
Uzum Husayni (oq) 1 kg|fresh-fruits|Farg'ona Bog'i|kg|1|19900
Uzum Qora Kishmish 1 kg|fresh-fruits|Farg'ona Bog'i|kg|1|24900
Uzum Toifi (pushti) 1 kg|fresh-fruits|Farg'ona Bog'i|kg|1|27500
Tarvuz Chillaki 1 kg|fresh-fruits|Hosildor|kg|1|4900
Tarvuz ertaki (mayda) 1 kg|fresh-fruits|Hosildor|kg|1|6500
Qovun Gurvak 1 kg|fresh-fruits|Hosildor|kg|1|12900
Shaftoli 1 kg|fresh-fruits|Bog'bon|kg|1|26500
Nektarin 1 kg|fresh-fruits|Bog'bon|kg|1|28900
O'rik 1 kg|fresh-fruits|Bog' Rizqi|kg|1|22500
Gilos 1 kg|fresh-fruits|Farg'ona Bog'i|kg|1|39500
Olcha 1 kg|fresh-fruits|Farg'ona Bog'i|kg|1|34500
Qulupnay 500 g|fresh-fruits|Yashil Vodiy|g|500|39000
Qulupnay issiqxona 1 kg|fresh-fruits|Yashil Vodiy|kg|1|69000
Smorodina qora 250 g|fresh-fruits|Yashil Vodiy|g|250|27000
Bektoshi uzumi 500 g|fresh-fruits|Bog' Rizqi|g|500|29000
Anor 1 kg|fresh-fruits|Samarqand Mevasi|kg|1|32500
Xurmo (kaki) 1 kg|fresh-fruits|Samarqand Mevasi|kg|1|27500
Ananas 1 dona|fresh-fruits|Agro Fresh|dona|1|59000
Mango 1 kg|fresh-fruits|Agro Fresh|kg|1|89000
Kivi 1 kg|fresh-fruits|Yashil Vodiy|kg|1|49000
Kokos yong'og'i 1 dona|fresh-fruits|Agro Fresh|dona|1|45000
Anjir 500 g|fresh-fruits|Bog' Rizqi|g|500|42000
Feijoa 500 g|fresh-fruits|Bog' Rizqi|g|500|31000
Olxo'ri sariq 1 kg|fresh-fruits|Bog'bon|kg|1|19500
Olxo'ri qora (vengrka) 1 kg|fresh-fruits|Bog'bon|kg|1|21500
Tut (oq) 500 g|fresh-fruits|Hosildor|g|500|18500
Kartoshka qizil 1 kg|fresh-vegetables|Hosildor|kg|1|7900
Piyoz bosh (sariq) 1 kg|fresh-vegetables|Hosildor|kg|1|5900
Piyoz qizil (salat) 1 kg|fresh-vegetables|Hosildor|kg|1|9900
Piyoz ko'k (boshli) 1 kg|fresh-vegetables|Bog'bon|kg|1|8900
Sabzi (qizil) 1 kg|fresh-vegetables|Hosildor|kg|1|6400
Sabzi chaqaloq (baby) 500 g|fresh-vegetables|Yashil Vodiy|g|500|8900
Sabzi sariq (yem-xashak) 1 kg|fresh-vegetables|Hosildor|kg|1|4900
Bodring (dala) 1 kg|fresh-vegetables|Yashil Vodiy|kg|1|10900
Bodring kornishon 500 g|fresh-vegetables|Yashil Vodiy|g|500|9500
Pomidor (issiqxona) 1 kg|fresh-vegetables|Yashil Vodiy|kg|1|16900
Pomidor (dala, go'shtli) 1 kg|fresh-vegetables|Hosildor|kg|1|14900
Pomidor cherry 500 g|fresh-vegetables|Yashil Vodiy|g|500|19500
Pomidor pushti (yirik) 1 kg|fresh-vegetables|Hosildor|kg|1|18900
Bolg'or qalampiri (qizil) 1 kg|fresh-vegetables|Yashil Vodiy|kg|1|24500
Bolg'or qalampiri (yashil) 1 kg|fresh-vegetables|Yashil Vodiy|kg|1|21500
Achchiq qalampir (chili) 250 g|fresh-vegetables|Bog'bon|g|250|9900
Baqlajon 1 kg|fresh-vegetables|Hosildor|kg|1|13900
Karam oq (bosh) 1 kg|fresh-vegetables|Hosildor|kg|1|5200
Karam qizil 1 kg|fresh-vegetables|Hosildor|kg|1|7900
Gulkaram 1 kg|fresh-vegetables|Yashil Vodiy|kg|1|12900
Qovoq (kabachok) 1 kg|fresh-vegetables|Hosildor|kg|1|8900
Qovoq patisson 1 kg|fresh-vegetables|Bog'bon|kg|1|10900
Qovoq (osh) 1 kg|fresh-vegetables|Hosildor|kg|1|6900
Lavlagi 1 kg|fresh-vegetables|Hosildor|kg|1|6400
Turp (qizil) 500 g|fresh-vegetables|Bog'bon|g|500|7900
Turp (oq, daikon) 1 kg|fresh-vegetables|Bog'bon|kg|1|11900
Sholg'om 1 kg|fresh-vegetables|Hosildor|kg|1|5900
Sarimsoq (tozalangan) 250 g|fresh-vegetables|Bog' Rizqi|g|250|19000
Makkajo'xori (shirin) 1 dona|fresh-vegetables|Bog'bon|dona|1|6500
Makkajo'xori (yosh) 1 kg|fresh-vegetables|Bog'bon|kg|1|12900
Bryussel karami 500 g|fresh-vegetables|Yashil Vodiy|g|500|18500
Qushqo'nmas (sparja) 250 g|fresh-vegetables|Yashil Vodiy|g|250|34000
Bodomcha (baklajan kichik) 500 g|fresh-vegetables|Bog'bon|g|500|9500
Petrushka (bog') 1 bog'|greens|Bog'bon|bog|1|4500
Kashnich (koriandr) 1 bog'|greens|Bog'bon|bog|1|4500
Rayhon (yashil) 1 bog'|greens|Yashil Vodiy|bog|1|5500
Rayhon (binafsha) 100 g|greens|Yashil Vodiy|g|100|8900
Yalpiz 100 g|greens|Bog'bon|g|100|7900
Salat bargi (ayzberg) 1 dona|greens|Yashil Vodiy|dona|1|11900
Salat bargi (romano) 1 dona|greens|Yashil Vodiy|dona|1|13900
Ko'k piyoz 1 bog'|greens|Bog'bon|bog|1|5000
Zira ko'kati (javz) 1 bog'|greens|Bog' Rizqi|bog|1|4900
Qizilcha bargi (mangold) 250 g|greens|Yashil Vodiy|g|250|11500
Maydanoz ildizi 500 g|greens|Bog'bon|g|500|9900
Yong'oq (tozalangan) 500 g|dried-fruits-nuts|Farg'ona Bog'i|g|500|89000
Yong'oq (po'stloqli) 1 kg|dried-fruits-nuts|Farg'ona Bog'i|kg|1|79000
Bodom (tozalangan) 500 g|dried-fruits-nuts|Bog' Rizqi|g|500|92000
Kungaboqar urug'i (qovurilgan) 500 g|dried-fruits-nuts|Hosildor|g|500|19000
Qovoq urug'i (tozalangan) 250 g|dried-fruits-nuts|Hosildor|g|250|28000
Kunjut 250 g|dried-fruits-nuts|Bog' Rizqi|g|250|21000
Kishmish (oq, sabza) 500 g|dried-fruits-nuts|Samarqand Mevasi|g|500|34000
Kishmish (qora, shigani) 500 g|dried-fruits-nuts|Samarqand Mevasi|g|500|38000
Qora o'rik (quritilgan) 500 g|dried-fruits-nuts|Samarqand Mevasi|g|500|42000
Quritilgan o'rik 500 g|dried-fruits-nuts|Samarqand Mevasi|g|500|39000
Xurmo (quritilgan) 500 g|dried-fruits-nuts|Bog' Rizqi|g|500|45000
Pista (tuzlangan) 250 g|dried-fruits-nuts|Farg'ona Bog'i|g|250|69000
Yeryong'oq (qovurilgan) 500 g|dried-fruits-nuts|Hosildor|g|500|24000
"""
