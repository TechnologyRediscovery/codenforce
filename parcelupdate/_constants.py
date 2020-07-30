# TODO: Rename file. Or maybe move Tally to a new file. I don't want to because we already have too many files.
# Allegheny County Property Assessment tabs.
GENERALINFO = "GeneralInfo"
BUILDING = "Building"
TAX = "Tax"
SALES = "Sales"
IMAGES = "Images"
COMPS = "Comps"
APPEAL = "Appeal"
MAP = "Map"

DEFAULT_PROP_UNIT = -1

# Allegheny County Property Assessment span ids
OWNER = "BasicInfo1_lblOwner"
TAXINFO = "lblTaxInfo"

# HTML Elements
SPAN = "span"
TR = "tr"



# Database attributes
BOT_NAME = "sylvia"
BOT_ID = "99"
COG_DB = "cogdb"
BASE_DB = "cogdb"
TEST_DB = "testdb"

# Output directories
PARCEL_ID_LISTS = "parcelidlists"

# Formatting
DASHES = "-" * 88
MEDIUM_DASHES = "-" * 58
SHORT_DASHES = "-" * 29
SPACE = " "

class _Tally:
    def __init__(self):
        self.total = 0
        self.inserted = 0
        self.updated = 0
        self.muni_count = 0

Tally = _Tally()
