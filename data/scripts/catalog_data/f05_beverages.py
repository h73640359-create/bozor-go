"""Ichimliklar va suv (70 ta mahsulot)."""

CATEGORY_ID = "beverages-water"
CATEGORY_NAME = "Ichimliklar va suv"
ICON = "drinks"

RAW = """Suv (ichimlik, gazsiz) 1,5 litr|water|Suv Manbai|l|1.5|5900
Suv (ichimlik, gazsiz) 0,5 litr|water|Suv Manbai|ml|500|3500
Suv (mineral, gazli) 1 litr|water|Zarafshon Suv|l|1|7900
Suv (mineral, shifobaxsh) 0,5 litr|water|Zarafshon Suv|ml|500|6500
Suv (bola suvi, 1 litr)|water|Buloq Suv|l|1|9900
Suv (gazlangan, stol) 1 litr|water|Zarafshon Suv|l|1|6900
Suv (kislorodli) 0,5 litr|water|Buloq Suv|ml|500|8500
Suv (tomizg'ichdan, 5 litr)|water|Suv Manbai|l|5|12900
Suv (limonli, uy qurilishi) 1 litr|water|Meva Ta'mi|l|1|11900
Suv (muzlatib sovutilgan, 0,33 l)|water|Buloq Suv|ml|330|4900
Sharbat (olma, tiniq) 1 litr|juice|Meva Ta'mi|l|1|17900
Sharbat (apelsin, tiniq) 1 litr|juice|Meva Ta'mi|l|1|21500
Sharbat (uzum) 1 litr|juice|Sharbat Vodiy|l|1|19900
Sharbat (anor) 1 litr|juice|Sharbat Vodiy|l|1|27500
Sharbat (pomidor) 1 litr|juice|Sharbat Vodiy|l|1|15500
Sharbat (olcha) 1 litr|juice|Sharbat Vodiy|l|1|22500
Sharbat (shaftoli, nektar) 1 litr|juice|Meva Ta'mi|l|1|19500
Sharbat (ko'p mevali, multivitamin) 1 litr|juice|Meva Ta'mi|l|1|20900
Sharbat (sabzi, chaqaloqlar uchun) 200 ml|juice|Buloq Suv|ml|200|12900
Sharbat (qulupnay, nektar) 1 litr|juice|Meva Ta'mi|l|1|19900
Sharbat (mandarin) 1 litr|juice|Sharbat Vodiy|l|1|23500
Sharbat (olma, 0,2 litr)|juice|Meva Ta'mi|ml|200|6500
Sharbat (o'rik, nektar) 1 litr|juice|Sharbat Vodiy|l|1|18900
Sharbat (kivi va olma) 0,33 litr|juice|Buloq Suv|ml|330|9500
Kola (klassik) 1,5 litr|carbonated|Sarbon|l|1.5|14900
Kola (shakarsiz, layt) 1,5 litr|carbonated|Sarbon|l|1.5|15900
Limonad (limonli) 1,5 litr|carbonated|Sarbon|l|1.5|12900
Limonad (apelsinli, Fanta uslubi) 1 litr|carbonated|Sarbon|l|1|11900
Tarxun (estragonli) 1 litr|carbonated|Sarbon|l|1|11500
Sodali suv (gazli, 0,5 litr)|carbonated|Zarafshon Suv|ml|500|4500
Tonik (xininli) 0,33 litr|carbonated|Sarbon|ml|330|8900
Kvas (non ichimligi) 1 litr|carbonated|Sarbon|l|1|12500
Moxito (alkogolsiz, 0,5 litr)|carbonated|Meva Ta'mi|ml|500|13900
Gazlangan ichimlik (mevali, 2 litr)|carbonated|Sarbon|l|2|15900
Qora choy (barg, 250 g)|tea|Choy Bog'i|g|250|49000
Yashil choy (barg, 200 g)|tea|Anhor Choy|g|200|59000
Yashil choy (paketli, 25 dona)|tea|Anhor Choy|qadoq|1|21500
Qora choy (Erl Grey, 100 g)|tea|Choy Bog'i|g|100|39000
Qora choy (limonli, 100 g)|tea|Choy Bog'i|g|100|35000
Yashil choy (jasminli) 150 g|tea|Anhor Choy|g|150|49000
Yashil choy (yaponcha, sencha) 100 g|tea|Anhor Choy|g|100|79000
Oolong (sutli) 150 g|tea|Anhor Choy|g|150|69000
Ko'k choy (o'tli, parhez) 100 g|tea|Choy Bog'i|g|100|32000
Choy (zanjabil va limon) 100 g|tea|Choy Bog'i|g|100|34000
Choy (na'matak, qizil) 100 g|tea|Anhor Choy|g|100|29000
Choy (yaxna, shakarsiz) 0,5 litr|tea|Meva Ta'mi|ml|500|11900
Choy (qora, granulalangan) 250 g|tea|Choy Bog'i|g|250|39000
Qahva (maydalangan, arabika) 250 g|coffee|Qahva Navosi|g|250|89000
Qahva (don, arabika) 500 g|coffee|Qahva Navosi|g|500|159000
Qahva (maydalangan, robusta) 250 g|coffee|Qahva Navosi|g|250|59000
Qahva (eruvchan, sublimatsiyalangan) 100 g|coffee|Qahva Saroyi|g|100|79000
Qahva (eruvchan, granulalangan) 150 g|coffee|Qahva Saroyi|g|150|69000
Qahva (3-in-1, paketli 20 dona)|coffee|Qahva Saroyi|qadoq|1|39000
Qahva (kapsula, 10 dona)|coffee|Qahva Navosi|qadoq|1|119000
Qahva (turkcha, juda mayda) 200 g|coffee|Qahva Saroyi|g|200|69000
Qahva (kofeinsiz) 200 g|coffee|Qahva Navosi|g|200|79000
Qahva (latte, tayyor 0,25 l)|coffee|Qahva Saroyi|ml|250|19900
Qahva (muzli, cold brew) 0,25 litr|coffee|Qahva Saroyi|ml|250|24500
Qahva (yashil don, qovurilmagan) 250 g|coffee|Qahva Navosi|g|250|59000
Qahva (qo'shilma, espresso aralashmasi) 1 kg|coffee|Qahva Navosi|kg|1|239000
Energetik ichimlik (klassik) 0,25 litr|energy-sports|Kuch-Quvvat|ml|250|19900
Energetik ichimlik (shakarsiz) 0,25 litr|energy-sports|Kuch-Quvvat|ml|250|21500
Izotonik (sport ichimligi) 0,5 litr|energy-sports|Kuch-Quvvat|ml|500|17900
Aminokislotali ichimlik (BCAA) 0,33 litr|energy-sports|Kuch-Quvvat|ml|330|29000
Proteinli sut kokteyli 0,33 litr|energy-sports|Kuch-Quvvat|ml|330|29000
Sutli kokteyl (qulupnay) 0,3 litr|dairy-drinks|Nurli Tong|ml|300|13500
Sutli kokteyl (shokoladli) 0,3 litr|dairy-drinks|Nurli Tong|ml|300|13900
Kokteyl (bananli, probiotik) 0,3 litr|dairy-drinks|Nurli Tong|ml|300|14500
Ayran (gazlangan, 0,5 litr)|dairy-drinks|Sutli Vodiy|ml|500|10900
Qimiz (biya suti) 0,5 litr|dairy-drinks|Qishloq Suti|ml|500|29000
"""
