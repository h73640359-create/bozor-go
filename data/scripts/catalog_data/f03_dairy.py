"""Sut mahsulotlari va tuxum (70 ta mahsulot)."""

CATEGORY_ID = "dairy-eggs"
CATEGORY_NAME = "Sut mahsulotlari va tuxum"
ICON = "dairy"

RAW = """Sut 2,5% 1 litr|milk|Sog'lom Sut|l|1|12900
Sut 3,2% 1 litr|milk|Sog'lom Sut|l|1|13900
Sut 1,5% (parhez) 1 litr|milk|Oq Oltin|l|1|12500
Sut 6% (qaymoqli) 1 litr|milk|Qishloq Suti|l|1|15900
Sut pasterizatsiyalangan 0,5 litr|milk|Oq Oltin|ml|500|7900
Sut sterilizatsiyalangan 3,2% 1 litr|milk|Sutli Vodiy|l|1|13500
Sut (uy sharoiti, qishloq) 1 litr|milk|Qishloq Suti|l|1|16000
Sut (muzlatib quritilgan) 500 g|milk|Sutli Vodiy|g|500|39000
Sut (laktosasiz) 1 litr|milk|Oila Suti|l|1|24500
Sut (qulupnay ta'mli) 200 ml|milk|Nurli Tong|ml|200|6900
Sut (shokoladli) 200 ml|milk|Nurli Tong|ml|200|6900
Sut (banan ta'mli) 200 ml|milk|Nurli Tong|ml|200|7200
Sut kukuni 400 g|milk|Sutli Vodiy|g|400|59000
Quyultirilgan sut 380 g|milk|Sarvino|g|380|32000
Qatiq 2,5% 500 g|yogurt-sour|Sog'lom Sut|g|500|12900
Qatiq (qishloq, 6%) 400 g|yogurt-sour|Qishloq Suti|g|400|14500
Qatiq (mevali) 150 g|yogurt-sour|Nurli Tong|g|150|6900
Yogurt (tabiiy, 3%) 400 g|yogurt-sour|Sog'lom Sut|g|400|16500
Yogurt (ichimlik, shaftoli) 250 ml|yogurt-sour|Nurli Tong|ml|250|9900
Yogurt (ichimlik, qulupnay) 250 ml|yogurt-sour|Nurli Tong|ml|250|9900
Yogurt (yunoncha, 10%) 250 g|yogurt-sour|Oila Suti|g|250|24500
Smetana 15% 400 g|yogurt-sour|Sog'lom Sut|g|400|17900
Smetana 20% 500 g|yogurt-sour|Qishloq Suti|g|500|22500
Smetana 25% (qishloq) 500 g|yogurt-sour|Qishloq Suti|g|500|26500
Kefir 2,5% 1 litr|yogurt-sour|Sog'lom Sut|l|1|12900
Kefir 1% 0,5 litr|yogurt-sour|Oq Oltin|ml|500|7900
Ryajenka 4% 500 ml|yogurt-sour|Sutli Vodiy|ml|500|12500
Ayron (tuzli) 500 ml|yogurt-sour|Oila Suti|ml|500|9900
Kurt (quritilgan qatiq) 200 g|yogurt-sour|Qishloq Suti|g|200|29000
Suzma 400 g|yogurt-sour|Qishloq Suti|g|400|19500
Pishloq (Rossiya) 1 kg|cheese|Sarvino|kg|1|119000
Pishloq (Gollandiya) 1 kg|cheese|Sarvino|kg|1|129000
Pishloq (Mozzarella) 250 g|cheese|Oila Suti|g|250|39000
Pishloq (Parmesan) 200 g|cheese|Oila Suti|g|200|89000
Pishloq (qaymoqli, yumshoq) 180 g|cheese|Nurli Tong|g|180|35900
Pishloq (echki sutidan) 300 g|cheese|Qishloq Suti|g|300|69000
Pishloq (dudlangan, kolbasa) 500 g|cheese|Sarvino|g|500|69000
Pishloq (suvluğ, oq) 500 g|cheese|Sog'lom Sut|g|500|49000
Pishloq (Bryndza) 400 g|cheese|Qishloq Suti|g|400|59000
Pishloq (qattiq, 45%) 300 g|cheese|Sarvino|g|300|55000
Pishloq (plastinka, sendvich) 200 g|cheese|Nurli Tong|g|200|27900
Tvorog 5% 250 g|cheese|Sog'lom Sut|g|250|16900
Tvorog 9% (donador) 250 g|cheese|Qishloq Suti|g|250|19500
Tvorog (mayizli) 150 g|cheese|Nurli Tong|g|150|12900
Tvorog massasi (shirin) 200 g|cheese|Nurli Tong|g|200|13500
Sariyog' 82% 200 g|butter-oil|Sarvino|g|200|42000
Sariyog' 72% (qishloq) 500 g|butter-oil|Qishloq Suti|g|500|95000
Sariyog' (tuzsiz) 180 g|butter-oil|Sarvino|g|180|39000
Sariyog' (shokoladli pastasi) 200 g|butter-oil|Nurli Tong|g|200|29000
Margarin (pishiriq uchun) 400 g|butter-oil|Oila Suti|g|400|19000
Margarin (stol) 200 g|butter-oil|Oila Suti|g|200|11900
Yog' (eritilgan, ghi) 500 g|butter-oil|Qishloq Suti|g|500|89000
Yog' (o'simlik aralashmasi) 450 g|butter-oil|Oila Suti|g|450|21000
Qaymoq 20% 300 g|cream|Sog'lom Sut|g|300|24500
Qaymoq 30% (ko'pirtirish uchun) 250 ml|cream|Oila Suti|ml|250|29900
Qaymoq (qishloq, qalin) 400 g|cream|Qishloq Suti|g|400|39000
Qaymoq (suyuq, kofe uchun) 200 ml|cream|Nurli Tong|ml|200|16900
Qaymoq (o'simlik, qahva uchun) 250 g|cream|Nurli Tong|g|250|19000
Tuxum (tovuq, C1, 10 dona)|eggs|Parvanda|dona|10|18500
Tuxum (tovuq, C0 yirik, 10 dona)|eggs|Parvanda|dona|10|21900
Tuxum (tanlangan, 6 dona)|eggs|Ferma Fresh|dona|6|12900
Tuxum (qishloq, 10 dona)|eggs|Qishloq Suti|dona|10|26500
Tuxum (bedana, 20 dona)|eggs|Ferma Fresh|dona|20|19000
Tuxum (oqsil boyitilgan, 10 dona)|eggs|Ferma Fresh|dona|10|24500
Muzqaymoq (plombir, stakancha) 100 g|ice-cream|Shirin|g|100|14900
Muzqaymoq (vafli shoxcha) 80 g|ice-cream|Shirin|g|80|13500
Muzqaymoq (briket, shokoladli) 450 g|ice-cream|Shirin|g|450|49000
Muzqaymoq (mevali muz) 90 ml|ice-cream|Shirin|ml|90|9900
Muzqaymoq (eskimo, shokoladli) 75 g|ice-cream|Shirin|g|75|16900
Muzqaymoq (oilaviy, 1 litr)|ice-cream|Shirin|l|1|89000
"""
