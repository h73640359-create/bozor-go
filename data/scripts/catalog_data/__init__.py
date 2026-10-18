"""Katalog xom ma'lumotlari modullari.

Tartib muhim: mahsulot ID lari shu tartibda beriladi.
"""

from . import (
    f01_fruits_vegetables,
    f02_meat,
    f03_dairy,
    f04_bakery,
    f05_beverages,
    f06_grocery,
    f07_cleaning,
    f08_personal_care,
    f09_baby,
    f10_clothing,
    f11_electronics,
    f12_kitchen,
    f13_sport,
    f14_pets,
    f15_auto,
)

MODULES = [
    f01_fruits_vegetables,
    f02_meat,
    f03_dairy,
    f04_bakery,
    f05_beverages,
    f06_grocery,
    f07_cleaning,
    f08_personal_care,
    f09_baby,
    f10_clothing,
    f11_electronics,
    f12_kitchen,
    f13_sport,
    f14_pets,
    f15_auto,
]

__all__ = ["MODULES"]
