#!/usr/bin/env python3
"""Har bir kichik bo'lim uchun rasm tavsifi (sun'iy intellekt bilan generatsiya uchun).

Tuzilishi:
    PHOTO_PROMPTS[subcategoryId] = [tavsif, ...]   # 1-3 ta variant

Eng ko'p mahsulotli kichik bo'limlar bir nechta variant oladi — bitta rasm
o'nlab mahsulotda takrorlanishi mumkin, lekin yuzlab emas.

Uslub: bozor (marketpleys) katalogi uchun — oq fonli studiya, mahsulot markazda,
yumshoq soya. Qadoqli mahsulotlarda brend va matn bo'lmasligi shart
(mualliflik huquqi va tartibsiz ko'rinish oldini olish uchun).
"""

# Barcha rasmlarga qo'shiladigan umumiy uslub qo'shimchasi.
STYLE_SUFFIX = (
    "professional e-commerce catalog product photo, single product group centered in frame, "
    "soft even studio lighting, clean pure white seamless background, subtle natural soft shadow, "
    "razor-sharp focus, high detail, realistic colours, square composition, eye-level slightly angled view. "
    "Absolutely no text, no letters, no numbers, no labels, no watermarks, no logos, no brands, "
    "no people, no hands, no props, no cutlery, no plates with patterns"
)

# Qadoqli / jihozli mahsulotlar uchun qo'shimcha shart.
PLAIN_PACKAGING = ", plain unbranded packaging with no printed text"

PHOTO_PROMPTS: dict[str, list[str]] = {
    # --- Meva, sabzavot, ko'katlar ---
    "fresh-fruits": [
        "a pile of fresh glossy red apples with one apple cut in half showing white flesh",
        "a colourful arrangement of fresh seasonal fruits: bananas, oranges and red grapes",
        "a pile of fresh citrus fruits: oranges, lemons and mandarins, one orange halved",
    ],
    "fresh-vegetables": [
        "a pile of fresh potatoes and yellow onions with clean dry skins",
        "a colourful arrangement of fresh vegetables: red tomatoes, cucumbers, carrots and bell peppers",
    ],
    "greens": [
        "fresh green herb bunches: parsley, cilantro and dill tied with a string",
    ],
    "dried-fruits-nuts": [
        "a bowl of shelled walnuts and almonds with dried apricots and raisins",
    ],
    # --- Go'sht, baliq ---
    "beef": [
        "fresh raw boneless beef steak cuts with marbling on a plain white plate",
        "fresh raw beef ribs and stew cubes on a plain white plate",
    ],
    "mutton": [
        "fresh raw lamb ribs and a leg cut with white fat on a plain white plate",
    ],
    "poultry": [
        "a fresh raw whole chicken on a plain white plate",
        "fresh raw chicken drumsticks and thighs on a plain white plate",
    ],
    "sausages": [
        "a selection of sliced sausages: boiled bologna-style sausage and smoked salami",
    ],
    "fish": [
        "a fresh whole carp fish on a plain white plate with crushed ice",
    ],
    "canned": [
        "three plain unbranded metal food cans stacked with open lids",
    ],
    # --- Sut, tuxum ---
    "milk": [
        "a plain unbranded milk carton and a bottle of fresh milk",
    ],
    "yogurt-sour": [
        "two plain unbranded white cups of yogurt and kefir with foil lids",
        "a glass jar of thick white smetana (sour cream) with a wooden spoon",
    ],
    "cream": [
        "a small bowl of fresh white cream (smetana) with a spoon",
    ],
    "cheese": [
        "a block of hard yellow cheese with cut slices and a wedge",
        "a bowl of fresh white cottage cheese (tvorog) with a spoon",
    ],
    "butter-oil": [
        "a block of butter with a knife and a slice of bread beside it",
    ],
    "eggs": [
        "a carton of fresh white chicken eggs, six eggs visible",
    ],
    "dairy-drinks": [
        "two plain unbranded bottles of strawberry and chocolate milkshake",
    ],
    # --- Non, shirinliklar ---
    "bread": [
        "a round golden Central Asian tandoor flatbread with stamped pattern",
        "freshly baked bread: a sliced white loaf and a whole brown loaf",
    ],
    "pastry": [
        "golden baked flaky savory samsa pastries",
        "golden baked pumpkin-filled samsa pastries with sesame seeds",
    ],
    "cakes": [
        "a chocolate cream cake with a slice cut on a plain white plate",
    ],
    "cookies": [
        "a pile of round butter cookies and wafer biscuits",
    ],
    "chocolate-candy": [
        "a milk chocolate bar with broken squares and cocoa beans",
        "an assortment of assorted chocolate candies and truffles arranged in rows",
    ],
    "halva-sweets": [
        "a piece of halva with nuts and sesame on a plain white plate",
    ],
    "ice-cream": [
        "a cup of vanilla ice cream with a wafer cone beside it",
    ],
    # --- Ichimliklar ---
    "water": [
        "a clear plastic bottle of still drinking water with condensation droplets",
    ],
    "carbonated": [
        "a plastic bottle of cola and a glass of cola with ice cubes",
    ],
    "juice": [
        "a glass bottle of clear apple juice with fresh red apples beside it",
    ],
    "energy-sports": [
        "a slim plain unbranded can of energy drink with condensation droplets",
    ],
    "tea": [
        "a small bowl of loose black tea leaves with a few green tea leaves",
    ],
    "coffee": [
        "a pile of roasted coffee beans with a small heap of ground coffee",
    ],
    # --- Oziq-ovqat bazasi ---
    "flour-pasta": [
        "a linen sack of white wheat flour with a wooden scoop and spilled flour",
        "an assortment of dry pasta: spaghetti, penne and fusilli in small piles",
    ],
    "rice": [
        "a pile of long-grain white rice with a wooden scoop",
    ],
    "cereals": [
        "small bowls of buckwheat groats, rice and oat flakes",
    ],
    "legumes": [
        "small bowls of white beans, red beans and chickpeas",
    ],
    "sugar-salt": [
        "small bowls of white granulated sugar and fine salt with a wooden spoon",
    ],
    "oil": [
        "a clear glass bottle of golden sunflower oil with sunflower seeds",
    ],
    "spices": [
        "small bowls of ground spices: cumin, paprika, black pepper and coriander",
    ],
    "sauces": [
        "a bottle of tomato ketchup and a bottle of chili sauce, plain unbranded",
    ],
    # --- Tozalash, uy-ro'zg'or ---
    "laundry": [
        "a plain unbranded box of laundry detergent powder with colourful granules spilling",
        "a plain unbranded bottle of liquid laundry detergent with a measuring cap",
    ],
    "surface-cleaners": [
        "a plain unbranded spray bottle of all-purpose surface cleaner with a cloth",
        "a plain unbranded bottle of floor cleaning liquid",
    ],
    "dishwashing": [
        "a plain unbranded bottle of dishwashing liquid with a yellow sponge",
    ],
    "paper": [
        "a stack of folded paper towels and a roll of paper towel",
    ],
    "trash-bags": [
        "a roll of black garbage bags, plain unbranded packaging",
    ],
    "home-care": [
        "a microfiber mop with a telescopic metal handle",
    ],
    "pest-control": [
        "an insect spray can and a tube of gel bait, plain unbranded",
    ],
    "air-freshener": [
        "an automatic air freshener spray dispenser, plain unbranded white device",
    ],
    # --- Gigiyena, kosmetika ---
    "hair-care": [
        "a plain unbranded pastel shampoo bottle with pump and a conditioner bottle",
        "a plain unbranded hair mask jar and a shampoo tube",
    ],
    "body-care": [
        "a plain unbranded shower gel bottle and a body lotion bottle",
    ],
    "skin-care": [
        "a plain unbranded white face cream jar and a serum dropper bottle",
    ],
    "oral-care": [
        "a plain unbranded tube of toothpaste and a toothbrush",
    ],
    "hygiene": [
        "a pack of cotton pads and a pack of cotton buds, plain unbranded",
    ],
    "deodorants": [
        "a plain unbranded roll-on deodorant and a spray deodorant",
    ],
    "shaving": [
        "a plain unbranded shaving foam can and a safety razor",
    ],
    # --- Bolalar ---
    "baby-care": [
        "a plain unbranded tube of baby cream and a jar of baby ointment",
    ],
    "baby-food": [
        "a plain unbranded jar of baby food puree and a tin of infant formula",
    ],
    "diapers": [
        "a stack of plain unbranded white disposable baby diapers",
    ],
    "baby-bath": [
        "a plain white plastic baby bathtub",
    ],
    "toys": [
        "colourful plastic building blocks stacked into a small tower",
    ],
    # --- Kiyim, poyabzal ---
    "womens-clothing": [
        "a folded women's summer floral dress, neatly laid flat",
        "an elegant women's long evening dress on an invisible mannequin",
    ],
    "mens-clothing": [
        "a folded plain white cotton men's t-shirt, neatly laid flat",
        "a folded men's blue polo shirt, neatly laid flat",
    ],
    "kids-clothing": [
        "a folded child's red cotton t-shirt, neatly laid flat",
    ],
    "footwear": [
        "a pair of white sneakers, side view",
    ],
    "underwear-socks": [
        "a folded stack of cotton socks in different colours",
    ],
    "accessories": [
        "a plain grey city backpack with shoulder straps",
    ],
    # --- Elektronika ---
    "phones": [
        "a modern black smartphone lying flat with a blank dark screen and a second phone showing its back",
    ],
    "phone-accessories": [
        "a black silicone smartphone case, plain, isolated",
        "a plain white wireless earbuds charging case with two earbuds beside it",
    ],
    "audio": [
        "plain white wireless earbuds with their charging case",
    ],
    "power": [
        "a plain black portable power bank with USB ports and a cable",
    ],
    "computer-accessories": [
        "a plain black wireless computer mouse and a compact keyboard",
    ],
    "small-appliances": [
        "a plain stainless steel electric kettle with a handle",
    ],
    # --- Uy, oshxona ---
    "cookware": [
        "a stack of stainless steel cooking pots with glass lids",
    ],
    "kitchen-tools": [
        "a set of kitchen knives with steel blades and black handles",
    ],
    "tableware": [
        "a stack of plain white ceramic plates",
    ],
    "storage": [
        "a set of clear plastic food storage containers with coloured lids",
    ],
    "home-textile": [
        "a folded bedding set with pillowcases in plain pastel fabric",
    ],
    # --- Sport, dam olish ---
    "fitness": [
        "a pair of adjustable dumbbells with weight plates",
    ],
    "outdoor-sport": [
        "a classic black and white football (soccer ball)",
    ],
    "sport-accessories": [
        "a plain sports gym bag and a water bottle",
    ],
    "bicycles": [
        "a black and green mountain bicycle, side view",
    ],
    "camping": [
        "a small green camping tent pitched with a sleeping bag beside it",
    ],
    # --- Uy hayvonlari ---
    "cat": [
        "a bowl of dry cat food kibble with a few pieces spilled",
    ],
    "dog": [
        "a bowl of dry dog food kibble with a few pieces spilled",
    ],
    "birds-other": [
        "a bowl of mixed bird seed with seeds spilled around it",
    ],
    # --- Avto, boshqa ---
    "car-care": [
        "a plain unbranded bottle of car shampoo and a bottle of liquid car wax",
    ],
    "car-accessories": [
        "a plain hanging car air freshener and a black rubber car floor mat",
    ],
    "stationery-other": [
        "a set of blue ballpoint pens and coloured pencils, plain unbranded",
    ],
}


def prompt_for(subcategory_id: str, variant: int = 0) -> str:
    """Kichik bo'lim va variant indeksi bo'yicha to'liq promt."""
    variants = PHOTO_PROMPTS.get(subcategory_id) or ["a generic grocery product"]
    text = variants[variant % len(variants)]
    return f"{text}{PLAIN_PACKAGING}, {STYLE_SUFFIX}"


if __name__ == "__main__":
    total = sum(len(v) for v in PHOTO_PROMPTS.values())
    print(f"Kichik bo'limlar: {len(PHOTO_PROMPTS)} ta, jami rasm: {total} ta")
