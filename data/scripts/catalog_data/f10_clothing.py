"""Kiyim-kechak va poyabzal (80 ta mahsulot).

Bu kategoriya uchun o'lcham variantlari (options) avtomatik qo'shiladi.
"""

CATEGORY_ID = "clothing-shoes"
CATEGORY_NAME = "Kiyim-kechak va poyabzal"
ICON = "fashion"

RAW = """Erkaklar futbolkasi (paxta, oq)|mens-clothing|Zamon|dona|1|129000
Erkaklar futbolkasi (polo, ko'k)|mens-clothing|Zamon|dona|1|189000
Erkaklar ko'ylagi (klassik, oq)|mens-clothing|Uslub|dona|1|289000
Erkaklar ko'ylagi (yozgi, zig'ir)|mens-clothing|Uslub|dona|1|259000
Erkaklar jinsi (klassik, ko'k)|mens-clothing|Zamon|dona|1|389000
Erkaklar jinsi (tor, qora)|mens-clothing|Zamon|dona|1|359000
Erkaklar shimi (chino, bej)|mens-clothing|Uslub|dona|1|299000
Erkaklar shimi (sport, qora)|mens-clothing|Sport Style|dona|1|229000
Erkaklar shortisi (plyaj, havorang)|mens-clothing|Sport Style|dona|1|169000
Erkaklar sviteri (jun, kulrang)|mens-clothing|Uslub|dona|1|399000
Erkaklar xudisi (paxta, kulrang)|mens-clothing|Zamon|dona|1|329000
Erkaklar kurtkasi (shamoldan himoya)|mens-clothing|Kiyim Saroyi|dona|1|549000
Erkaklar pal'tosi (kuzgi, yomg'irlik)|mens-clothing|Kiyim Saroyi|dona|1|689000
Erkaklar kostyumi (klassik, 2 qism)|mens-clothing|Uslub|qadoq|1|1290000
Erkaklar galstugi (ipak, ko'k)|mens-clothing|Uslub|dona|1|129000
Erkaklar kamari (teri, qora)|mens-clothing|Kiyim Saroyi|dona|1|189000
Erkaklar pijamasi (2 qism)|mens-clothing|Zamon|qadoq|1|259000
Erkaklar shapkasi (trikotaj, qishki)|mens-clothing|Kiyim Saroyi|dona|1|99000
Ayollar ko'ylagi (yozgi, gulli)|womens-clothing|Moda Vodiy|dona|1|349000
Ayollar ko'ylagi (kechki, uzun)|womens-clothing|Moda Vodiy|dona|1|589000
Ayollar bluzkasi (ipak, oq)|womens-clothing|Moda Vodiy|dona|1|269000
Ayollar futbolkasi (paxta, pushti)|womens-clothing|Yoshlar|dona|1|139000
Ayollar jinsi (yuqori bel, ko'k)|womens-clothing|Moda Vodiy|dona|1|399000
Ayollar shimi (keng, qora)|womens-clothing|Moda Vodiy|dona|1|299000
Ayollar yubkasi (midi, qora)|womens-clothing|Uslub|dona|1|279000
Ayollar yubkasi (qisqa, katakli)|womens-clothing|Yoshlar|dona|1|219000
Ayollar sviteri (katta trikotaj)|womens-clothing|Moda Vodiy|dona|1|429000
Ayollar kardigani (uzun)|womens-clothing|Uslub|dona|1|359000
Ayollar kurtkasi (kuzgi, bej)|womens-clothing|Kiyim Saroyi|dona|1|629000
Ayollar pal'tosi (qishki, qora)|womens-clothing|Kiyim Saroyi|dona|1|899000
Ayollar xalati (uy uchun, paxta)|womens-clothing|Zamon|dona|1|199000
Ayollar platye-sarafani (yozgi)|womens-clothing|Yoshlar|dona|1|279000
Ayollar sport kostyumi (2 qism)|womens-clothing|Sport Style|qadoq|1|459000
Ayollar ro'moli (ipak, gulli)|womens-clothing|Moda Vodiy|dona|1|159000
Ayollar sharfi (jun, qishki)|womens-clothing|Kiyim Saroyi|dona|1|129000
Ayollar qo'ltiq sumkasi (teri)|womens-clothing|Moda Vodiy|dona|1|549000
Ayollar tunikasi (erkin)|womens-clothing|Yoshlar|dona|1|189000
Bolalar futbolkasi (qizil, 5-6 yosh)|kids-clothing|Bolajon|dona|1|89000
Bolalar futbolkasi (ko'k, 7-8 yosh)|kids-clothing|Bolajon|dona|1|89000
Bolalar jinsi (yosh o'smirlar)|kids-clothing|Bolajon|dona|1|189000
Bolalar ko'ylagi (bayram, oq)|kids-clothing|Moda Vodiy|dona|1|229000
Bolalar ko'ylagi (qizlar, gulli)|kids-clothing|Moda Vodiy|dona|1|249000
Bolalar kombinezoni (chaqaloqlar)|kids-clothing|Bolajon|dona|1|159000
Bolalar kurtkasi (kuzgi, suv o'tkazmaydigan)|kids-clothing|Kiyim Saroyi|dona|1|349000
Bolalar pijamasi (paxta)|kids-clothing|Bolajon|qadoq|1|149000
Bolalar paypoqlari (5 juft)|kids-clothing|Bolajon|qadoq|1|59000
Bolalar shapkasi va sharfi to'plami|kids-clothing|Kiyim Saroyi|qadoq|1|129000
Maktab formasi (o'g'il bolalar)|kids-clothing|Uslub|qadoq|1|389000
Erkaklar krossovkasi (yugurish uchun)|footwear|Sport Style|juft|1|489000
Erkaklar krossovkasi (kundalik, oq)|footwear|Sport Style|juft|1|419000
Erkaklar tuflisi (klassik, qora)|footwear|Uslub|juft|1|589000
Erkaklar botinki (qishki, teri)|footwear|Kiyim Saroyi|juft|1|789000
Erkaklar shippagi (uy, yumshoq)|footwear|Zamon|juft|1|129000
Ayollar krossovkasi (yengil, oq)|footwear|Sport Style|juft|1|459000
Ayollar tuflisi (poshnali, bej)|footwear|Moda Vodiy|juft|1|529000
Ayollar botinkasi (qishki, qora)|footwear|Moda Vodiy|juft|1|749000
Ayollar sandali (yozgi)|footwear|Moda Vodiy|juft|1|329000
Ayollar baletkasi (yumshoq)|footwear|Uslub|juft|1|279000
Bolalar krossovkasi (chiroqli)|footwear|Bolajon|juft|1|259000
Bolalar sandali (yozgi)|footwear|Bolajon|juft|1|169000
Sport shippagi (dush uchun)|footwear|Sport Style|juft|1|89000
Rezinovaya etik (yomg'irlik)|footwear|Kiyim Saroyi|juft|1|219000
Erkaklar paypoqlari (5 juft, paxta)|underwear-socks|Zamon|qadoq|1|79000
Erkaklar paypoqlari (sport, 3 juft)|underwear-socks|Sport Style|qadoq|1|59000
Erkaklar trusisi (3 dona, paxta)|underwear-socks|Zamon|qadoq|1|99000
Erkaklar maykasi (3 dona)|underwear-socks|Zamon|qadoq|1|109000
Ayollar paypoqlari (kapron, 3 juft)|underwear-socks|Moda Vodiy|qadoq|1|45000
Ayollar paypoqlari (paxta, 5 juft)|underwear-socks|Uslub|qadoq|1|59000
Ayollar ichki kiyimi (to'plam)|underwear-socks|Moda Vodiy|qadoq|1|189000
Ayollar uy kiyimi (pijama)|underwear-socks|Zamon|qadoq|1|229000
Bolalar paypoqlari (5 juft, rangli)|underwear-socks|Bolajon|qadoq|1|55000
Sport paypoqlari (3 juft, oq)|underwear-socks|Sport Style|qadoq|1|49000
Termo ichki kiyim (qishki)|underwear-socks|Sport Style|qadoq|1|259000
Bolalar ichki kiyimi (3 dona)|underwear-socks|Bolajon|qadoq|1|89000
Ryukzak (shahar, 25 litr)|accessories|Kiyim Saroyi|dona|1|329000
Sumka (sport, 40 litr)|accessories|Sport Style|dona|1|279000
Kepka (beysbolka, qora)|accessories|Zamon|dona|1|99000
Kamar (to'qilgan, jigarrang)|accessories|Uslub|dona|1|119000
Quyosh ko'zoynagi (UV400)|accessories|Moda Vodiy|dona|1|149000
Qo'lqop (qishki, jun)|accessories|Kiyim Saroyi|juft|1|129000
"""
