import re
import requests
import bs4
from Exceptions._exceptions import MalformedGenericAddressError, MalformedZipcodeError


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


def soupify_html(raw_html):
    return bs4.BeautifulSoup(raw_html, "lxml")


def extract_info_from_soup(soup, span_id):
    """
    Arguments:
        soup: bs4.element.NavigableString
            Note: bs4.element.Tag are accepted and filtered out
        span_id: str

    Returns:
        str or list[str]
    """
    # Although most keys work fine, addresses in particular return something like
    # ['1267\xa0BRINTON  RD', <br/>, 'PITTSBURGH,\xa0PA\xa015221'] which needs to be escaped
    content = soup.find("span", id=span_id).contents
    cleaned_content = []
    for tag in content:
        if isinstance(tag, bs4.element.Tag):    # Example: <br/> when evaluating the address
            continue
        cleaned_tag = remove_whitespace(tag)
        cleaned_content.append(cleaned_tag)

    if len(cleaned_content) == 0:
        return None
    elif len(cleaned_content) == 1:
        return cleaned_content[0]
    return cleaned_content


def remove_whitespace(str):
    return re.sub(r"\s+", " ", str.strip())


def extract_address_parts(full_address):
    # NOTE: Not compatible with mailing addresses
    address_map = {}
    # make sure we have all the parts of the address
    if len(full_address) != 2:
        raise MalformedGenericAddressError
    address_map['street'] = full_address[0]
    address_map['citystatezip'] = full_address[1]
    # print(full_address)

    # on the city, state, zip line, grab until the comma before the state
    exp = re.compile("[^,]*")
    address_map["city"] = exp.search(full_address[1]).group(0)
    # print("City:", address_map["city"])
    # Hardcoded
    address_map['state'] = 'PA'
    # Zipcode are the last 5 digits.
    address_map['zip'] = full_address[1][-5:]
    try:
        int(address_map['zip'])
    except ValueError:
        raise MalformedZipcodeError
    return address_map







