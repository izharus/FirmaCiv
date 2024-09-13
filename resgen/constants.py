from typing import NamedTuple

TFC_WOODS = ["acacia",
             "ash",
             "aspen",
             "birch",
             "blackwood",
             "chestnut",
             "douglas_fir",
             "hickory",
             "kapok",
             "mangrove",
             "maple",
             "oak",
             "palm",
             "pine",
             "rosewood",
             "sequoia",
             "spruce",
             "sycamore",
             "white_cedar",
             "willow"]

ROOF_MATERIALS = ["acacia",
             "ash",
             "aspen",
             "birch",
             "blackwood",
             "chestnut",
             "douglas_fir",
             "hickory",
             "kapok",
             "mangrove",
             "maple",
             "oak",
             "palm",
             "pine",
             "rosewood",
             "sequoia",
             "spruce",
             "sycamore",
             "white_cedar",
             "willow"]


class Metal(NamedTuple):
    """
    A type representing a TFC Metal.
    Copied from TFC data gen
    """
    tier: int
    types: set[str]  # One of 'part', 'tool', 'armor', 'utility'
    heat_capacity_base: float  # Do not access directly, use one of specific or ingot heat capacity.
    melt_temperature: float
    melt_metal: str | None

    def specific_heat_capacity(self) -> float: return round(300 / self.heat_capacity_base) / 100_000

    def ingot_heat_capacity(self) -> float: return 1 / self.heat_capacity_base


METALS: dict[str, Metal] = {
    'bismuth': Metal(1, {'part'}, 0.14, 270, None),
    'bismuth_bronze': Metal(2, {'part', 'tool', 'armor', 'utility'}, 0.35, 985, None),
    'black_bronze': Metal(2, {'part', 'tool', 'armor', 'utility'}, 0.35, 1070, None),
    'bronze': Metal(2, {'part', 'tool', 'armor', 'utility'}, 0.35, 950, None),
    'brass': Metal(2, {'part'}, 0.35, 930, None),
    'copper': Metal(1, {'part', 'tool', 'armor', 'utility'}, 0.35, 1080, None),
    'gold': Metal(1, {'part'}, 0.6, 1060, None),
    'nickel': Metal(1, {'part'}, 0.48, 1453, None),
    'rose_gold': Metal(1, {'part'}, 0.35, 960, None),
    'silver': Metal(1, {'part'}, 0.48, 961, None),
    'tin': Metal(1, {'part'}, 0.14, 230, None),
    'zinc': Metal(1, {'part'}, 0.21, 420, None),
    'sterling_silver': Metal(1, {'part'}, 0.35, 950, None),
    'wrought_iron': Metal(3, {'part', 'tool', 'armor', 'utility'}, 0.35, 1535, 'cast_iron'),
    'cast_iron': Metal(1, {'part'}, 0.35, 1535, None),
    'pig_iron': Metal(3, set(), 0.35, 1535, None),
    'steel': Metal(4, {'part', 'tool', 'armor', 'utility'}, 0.35, 1540, None),
    'black_steel': Metal(5, {'part', 'tool', 'armor', 'utility'}, 0.35, 1485, None),
    'blue_steel': Metal(6, {'part', 'tool', 'armor', 'utility'}, 0.35, 1540, None),
    'red_steel': Metal(6, {'part', 'tool', 'armor', 'utility'}, 0.35, 1540, None),
    'weak_steel': Metal(4, set(), 0.35, 1540, None),
    'weak_blue_steel': Metal(5, set(), 0.35, 1540, None),
    'weak_red_steel': Metal(5, set(), 0.35, 1540, None),
    'high_carbon_steel': Metal(3, set(), 0.35, 1540, 'pig_iron'),
    'high_carbon_black_steel': Metal(4, set(), 0.35, 1540, 'weak_steel'),
    'high_carbon_blue_steel': Metal(5, set(), 0.35, 1540, 'weak_blue_steel'),
    'high_carbon_red_steel': Metal(5, set(), 0.35, 1540, 'weak_red_steel'),
    'unknown': Metal(0, set(), 0.5, 400, None)
}
"""
List of TFC metals.
Copied from TFC data gen
"""

FLORAE_WOODS: dict[str, str] = {"african_padauk": "African Padauk",
                                "alder": "Alder",
                                "angelim": "Angelim",
                                "argyle_eucalyptus": "Argyle Eucalyptus",
                                "bald_cypress": "Bald Cypress",
                                # TODO fill out the empty strings to be translation key version of the wood name
                                "baobab": "",
                                "beech": "",
                                "black_walnut": "",
                                "black_willow": "",
                                "brazilwood": "",
                                "butternut": "",
                                "buxus": "",
                                "cocobolo": "",
                                "common_oak": "",
                                "cypress": "",
                                "ebony": "",
                                "fever": "",
                                "ghaf": "",
                                "ginkgo": "",
                                "greenheart": "",
                                "hawthorn": "",
                                "hazel": "",
                                "hemlock": "",
                                "holly": "",
                                "hornbeam": "",
                                "iroko": "",
                                "ironwood": "",
                                "jabuticabeira": "",
                                "joshua": "",
                                "juniper": "",
                                "kauri": "",
                                "larch": "",
                                "laurel": "",
                                "limba": "",
                                "locust": "",
                                "logwood": "",
                                "maclura": "",
                                "mahoe": "",
                                "mahogany": "",
                                "marblewood": "",
                                "medlar": "",
                                "messmate": "",
                                "mountain_ash": "",
                                "mulberry": "",
                                "nordmann_fir": "",
                                "norway_spruce": "",
                                "pear": "",
                                "pink_cherry_blossom": "",
                                "pink_ipe": "",
                                "pink_ivory": "",
                                "poplar": "",
                                "purple_ipe": "",
                                "purple_jacaranda": "",
                                "purpleheart": "",
                                "quince": "",
                                "rainbow_eucalyptus": "",
                                "red_cedar": "",
                                "red_cypress": "",
                                "red_elm": "",
                                "red_mangrove": "",
                                "redwood": "",
                                "rowan": "",
                                "rubber_fig": "",
                                "sloe": "",
                                "snow_gum_eucalyptus": "",
                                "sorb": "",
                                "sweetgum": "",
                                "syzygium": "",
                                "teak": "",
                                "walnut": "",
                                "wenge": "",
                                "white_cherry_blossom": "",
                                "white_elm": "",
                                "white_ipe": "",
                                "white_jacaranda": "",
                                "white_mangrove": "",
                                "whitebeam": "",
                                "yellow_ipe": "",
                                "yellow_jacaranda": "",
                                "yellow_meranti": "",
                                "yew": "",
                                "zebrawood": "",
                                "persimmon": "",
                                "bamboo": ""}


def langify(s: str) -> str:
    """
    Takes a string like dark_oak and converts it to Dark Oak.
    """
    return ' '.join([word.capitalize() for word in s.split('_')])


DEFAULT_LANG = {
    # Entities
    **{f"entity.firmaciv.rowboat.{wood}": f"{langify(wood)} Rowboat" for wood in TFC_WOODS},
    **{f"entity.firmaciv.sloop.{wood}": f"{langify(wood)} Sloop" for wood in TFC_WOODS},
    **{f"entity.firmaciv.sloop_construction.{wood}": f"{langify(wood)} Sloop" for wood in TFC_WOODS},
    **{f"entity.firmaciv.dugout_canoe.{wood}": f"{langify(wood)} Dugout Canoe" for wood in TFC_WOODS},

    "block.firmaciv.thatch_roofing_stairs": "Angled Thatch",
    "block.firmaciv.thatch_roofing_slab": "Thatch Slab",
    "block.firmaciv.thatch_roofing": "Angled Thatch",

    "entity.firmaciv.kayak": "Kayak",
    "entity.firmaciv.cannon": "Cannon",

    "entity.firmaciv.compartment_tfc_chest": "Chest Compartment",
    "entity.firmaciv.compartment_tfc_barrel": "Barrel Compartment",
    "entity.firmaciv.compartment_large_vessel": "Large Vessel",

    # Jade
    "config.jade.plugin_firmaciv.barrel": "Barrel Compartment",
    "config.jade.plugin_firmaciv.tfc_chest": "TFC Chest Compartment",

    "itemGroup.firmaciv": "Firma: Civilization",
    "creativetab.firmaciv_tab": "Firma: Civilization",
    "creativetab.watercraft_tab": "Watercraft",
    "creativetab.navigation_tab": "Navigation",

    "tfc.recipe.barrel.firmaciv.barrel.large_waterproof_hide_olive": "Large Waterproof Hide",
    "tfc.recipe.barrel.firmaciv.barrel.large_waterproof_hide_tallow": "Large Waterproof Hide",

    "copy_latitude": "Copy Latitude to Clipboard",
    "copy_longitude": "Copy Longitude to Clipboard",
    "copy_latlon": "Copy Latitude and Longitude to Clipboard",
    "copy_latlonalt": "Copy Latitude, Longitude, and Altitude to Clipboard",
    "copy_altitude": "Copy Altitude to Clipboard",

    "latitude": "Latitude",
    "longitude": "Longitude",
    "altitude": "Altitude",
    "degrees": "Degrees",
    "sea_level": "Sea Level",
    "north": "North",
    "south": "South",
    "east": "East",
    "west": "West",
    "above": "Above",
    "below": "Below",
    "meters": "Meters",

    "press_button": "Press",
    "eject_passengers": "to eject",
    "restless_passenger": "This passenger is restless.",

    "firmaciv.advancements.kayak_paddle.title": "Double Trouble",
    "firmaciv.advancements.kayak_paddle.description": "Craft a Kayak Paddle",
    "firmaciv.advancements.canoe.title": "Burnout Paradise",
    "firmaciv.advancements.canoe.description": "Attempt to light a canoe hull",
    "firmaciv.advancements.oar.title": "Oaring my Paddleboat",
    "firmaciv.advancements.oar.description": "Craft an Oar",
    "firmaciv.advancements.kayak.title": "I'll carry you home tonight",
    "firmaciv.advancements.kayak.description": "Craft a Kayak",
    "firmaciv.advancements.nav_clock.title": "Get your time from the Admiral",
    "firmaciv.advancements.nav_clock.description": "Craft a Navigator's Timepiece",
    "firmaciv.advancements.sextant.title": "The Stars Will Aid",
    "firmaciv.advancements.sextant.description": "Craft a Sextant",
    "firmaciv.advancements.barometer.title": "Surf and/or Turf",
    "firmaciv.advancements.barometer.description": "Craft a Barometer",
    "firmaciv.advancements.oarlock.title": "The Montlake Cut",
    "firmaciv.advancements.oarlock.description": "Smith an Oarlock",

    # JEI
    # This should be under the TFC namespace, but they haven't released this
    # functionality yet, so we should supply it ourselves
    "firmaciv.jei.transfer.error.barrel_sealed": "Barrel is currently sealed"
}
