"""
Home for Magic Strings and classes that are used across different files.
    If a class uses functions that lay beyond its defined methods,
    the class is unlikely to belong here.

    For example, the class OwnerName resides in parse.py
    because it calls the function _parse.parse_owners_from_soup.

    Likewise, EventDetails resides in events.py
    because its usage is strongly tied to events.
"""

# Note: Some Magic Strings are currently unused, but kept anyways for potential future use

# Allegheny County Property Assessment tabs.
from collections import namedtuple
from typing import NamedTuple

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
ADDRESS = "BasicInfo1_lblAddress"
MUNICIPALITY = "BasicInfo1_lblMuni"
PARCEL_ID_LoB = "BasicInfo1_lblParcelID"
MORTGAGE = "lblMortgage"
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

TAX_NOTICE = """Important Notice

The data viewed on this page is for informational purposes only and should not be considered a true and final certified account summary for property closings. Payments posted to the current tax year file may be removed at any time during that year pending proof of erroneous payment. Furthermore, payments found to be insufficient may be removed within 45 days of posting. The actual proof of payment of all real estate taxes belongs to the owner of record.

A four (4) year tax certification bearing the official seal of the Allegheny County Treasurer's Office that verifies payment can be obtained from the Treasurer's Office (412-350-4100). A $25.00 fee is required for each property certification requested.

In 1997 and 1998, Allegheny County sold certain real estate tax liens to GLS Capital, Inc. For information, contact GLS Capital, Inc. at (412) 672-7200.

**PLEASE BE ADVISED that Allegheny County has appointed Jordan Tax Service, Inc. to collect delinquent/liened Allegheny County real estate taxes which have not been sold to GLS. Pursuant to County Ordinance 02-04-OR, a collection commission of ten percent (10%) plus postage and other collection charges, expenses, and fees are recoverable as part of the taxes collected: (1) for tax years 2004 and prior; and (2) for tax years 2005 and after if not paid-in-full by December 31 of the year the taxes first became due and payable.

For payment amount or information concerning delinquent/liened Allegheny County real estate taxes, please contact 
Jordan Tax Service, Inc. at (412) 835-5243. """


# --------------------------------------------------------------------------------------
# Classes
# --------------------------------------------------------------------------------------
#   Although they are stuck in parse.py for now, I would love to move
#       OwnerName and Municipality to this file.


class _Tally:
    # Perhaps Tally should be replaced with collections.Counter?
    def __init__(self):
        self.total = 0
        self.inserted = 0
        self.updated = 0
        self.muni_count = 0
        self.diff_count = 0


Tally = _Tally()


# Todo: Is there a clean way to write this with Typing?
TaxStatus = namedtuple(
    "tax_status",
    fields := [
        "year",
        "paidstatus",
        "tax",
        "penalty",
        "interest",
        "total",
        "date_paid",
        "blank",
    ],
    defaults=(None,) * len(fields),
)
