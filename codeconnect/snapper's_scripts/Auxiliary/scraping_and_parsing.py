import re
import requests
import bs4

from Exceptions._exceptions import MalformedOwnerError


def get_county_property_assessment(parcel_id):
    """
    Grabs the raw HTML of a property from the county's website

    Arguments:
        parcel_id: str

    Returns:
        str:
            The raw HTML from the county's property assessment
    """
    COUNTY_REAL_ESTATE_URL = (
        "http://www2.county.allegheny.pa.us/RealEstate/GeneralInfo.aspx?"
    )
    search_parameters = {
        "ParcelID": parcel_id,
        "SearchType": 3,
        "SearchParcel": parcel_id,
    }
    try:
        response = requests.get(
            COUNTY_REAL_ESTATE_URL, params=search_parameters, timeout=5
        )
        print("Scraping data from county: " + parcel_id)
    except requests.exceptions.Timeout:
        # TODO: log_error(). Perhaps logger should reside in its own auxiliary module?
        raise requests.exceptions.Timeout
    return response.text


def extract_owner_name(property_html):
    """
    Arguments:
        property_html: str
            The raw HTML scraped from the county website

    Returns:
        str:
            The owner's full name
    """
    OWNER_NAME_SPAN_ID = "BasicInfo1_lblOwner"
    soup = bs4.BeautifulSoup(property_html, "lxml")
    owner_full_name = soup.find("span", id=OWNER_NAME_SPAN_ID).text
    # Remove extra spaces from owner's name
    try:
        owner_name = re.sub(r"\s+", " ", owner_full_name.strip())
    except AttributeError:
        raise MalformedOwnerError
    print("owner name: " + owner_name)
    return owner_full_name








