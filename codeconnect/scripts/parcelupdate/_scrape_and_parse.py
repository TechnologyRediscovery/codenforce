import re
import threading
from collections import namedtuple

import requests
import bs4

from _constants import GENERALINFO, BUILDING, TAX, SALES, IMAGES, COMPS, APPEAL, MAP
from _constants import OWNER, TAXINFO
from _constants import SPAN


##########################################################################################
# Scraping Tools
##########################################################################################


def _scrape_county_property_assessment(parcel_id: str, page: str, output: dict):
    """
    Modifies a dictionary with raw html scraped from Allegheny County's property assessment database.

    Args:
        parcel_id:
        page:
        output:

    Returns:
        None    # Because it modifies the dictionary "output" in place.
    """
    COUNTY_REAL_ESTATE_URL = "http://www2.county.allegheny.pa.us/RealEstate/"
    URL_ENDING = ".aspx?"
    search_parameters = {
        "ParcelID": parcel_id,
        "SearchType": 3,
        "SearchParcel": parcel_id,
    }
    response = requests.get(
        (COUNTY_REAL_ESTATE_URL + page + URL_ENDING),
        params=search_parameters,
        timeout=5,
    )
    output[
        page
    ] = (
        response.text
    )  # As we are modifying the input in place, we do not need a return statement


# There was a point that we planned to scrape multiple pages. Thus, we set up threading.
# However, we realized that the tax page had all the data we need
# Thus, the threading doesn't actually do anything since we are on a single thread.
def scrape_county_property_assessments(parcel_id, pages):
    results = {}
    threads = []
    for key in pages:
        if key not in [GENERALINFO, BUILDING, TAX, SALES, IMAGES, COMPS, APPEAL, MAP]:
            raise KeyError(
                "Allegheny County's website does not support the given search term"
            )
        t = threading.Thread(
            target=_scrape_county_property_assessment, args=(parcel_id, key, results)
        )
        t.start()
        threads.append(t)
    for t in threads:
        t.join()
    return results


##########################################################################################
# Parsing Tools
##########################################################################################


def soupify_html(raw_html):
    return bs4.BeautifulSoup(raw_html, "html.parser")


def _extract_elementlist_from_soup(soup, element_id, element=SPAN, remove_tags=True):
    """
    Arguments:
        soup: bs4.element.NavigableString
            Note: bs4.element.Tag are accepted and filtered out
        element_id: str

    Returns:
        list[str,]
    # Todo: Refactor into function it is called from?
    """
    # Although most keys work fine, addresses in particular return something like
    # ['1267\xa0BRINTON  RD', <br/>, 'PITTSBURGH,\xa0PA\xa015221'] which needs to be escaped
    content = soup.find(element, id=element_id).contents
    if remove_tags != True:
        return content

    cleaned_content = []
    for tag in content:
        if isinstance(
            tag, bs4.element.Tag
        ):  # Example: <br/> when evaluating the address
            continue
        cleaned_content.append(tag)

    if len(cleaned_content) != 0:
        # Todo: Log error here
        return cleaned_content
    return cleaned_content


TaxStatus = namedtuple(
    "tax_status",
    ["year", "status", "tax", "penalty", "interest", "total", "date_paid", "blank"],
)


def parse_tax_from_soup(soup):
    table = _extract_elementlist_from_soup(
        soup, element_id=TAXINFO, element=SPAN, remove_tags=False
    )
    row = table[0].contents[1]  # The most recent year's data
    try:
        return TaxStatus(*[x.text for x in row.contents])
    except TypeError:  # When taxes are unpaid:
        return TaxStatus(
            year=row.contents[0].text,
            status=row.contents[1].text,
            tax=row.contents[2].text,
            penalty=row.contents[3].text,
            interest=row.contents[4].text,
            total=row.contents[5].text,
            date_paid=None,
            blank="",
        )


def strip_whitespace(text):
    return re.sub(" {2,}", " ", text).strip()


##########################################################################################
# Combination Functions (OwnerName)
##########################################################################################


class OwnerName:
    __slots__ = ["raw", "clean", "first", "last", "multientity", "compositelname"]

    def __init__(self, parid=None):
        self.multientity = None

    @classmethod
    def get_Owner_from_soup(cls, soup: str):
        """ Factory method for creating OwnerNames from parcel ids. """
        o = OwnerName()
        o.raw = o._parse_owners_from_soup(soup)
        o.clean = (
            o._clean_raw_name()
        )  # Method side effect: May change flag o.multientity

        # The Java side hasn't updated their code to match the cleanname, and instead concatenates fname and lname.
        # Todo: deprecate the composite last name flag
        o.first = ""
        o.last = o.clean
        o.compositelname = True
        return o

    @staticmethod
    def _parse_owners_from_soup(soup):
        return _extract_elementlist_from_soup(
            soup, element_id=OWNER, element=SPAN, remove_tags=True
        )

    def _clean_raw_name(self) -> str:
        if len(self.raw) > 1:
            self.multientity = True
            cleaned_names = []
            for name in self.raw:
                cleaned_names.append(strip_whitespace(name))
            return ", ".join(cleaned_names)
        return strip_whitespace(self.raw[0])
