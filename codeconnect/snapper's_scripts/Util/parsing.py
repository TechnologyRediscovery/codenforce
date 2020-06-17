import re
import bs4
from Exceptions._exceptions import MalformedGenericAddressError, MalformedZipcodeError

SPAN = "span"


def _soupify_html(raw_html):
    return bs4.BeautifulSoup(raw_html, "lxml")

def _create_insertmap(html, element_dict, element=SPAN, clean=True):
    # Todo: This function deserves a stellar docstring.
    imap = {}
    soup = _soupify_html(html)
    for key in element_dict:
        # Todo: Figure out how kwargs work and pass along clean using unpacking
        imap[key] = _extract_info_from_soup(soup, element_dict[key], element, clean)
    return imap


def create_general_insertmap(html):
    html_elements = {
        "ownername": "BasicInfo1_lblOwner",
        "fulladdress": "BasicInfo1_lblAddress",
        "ownermailing": "lblChangeMail",
        "saleprice": "lblSalePrice",
        "saledate": "lblSaleDate",
        "assessedlandvalue": "lblCountyLand",
        "assessedbuildingvalue": "lblCountyBuild",
        "assessmentyear": "LCounty",
        "usecode": "lblUse",
    }
    imap = _create_insertmap(html, html_elements)
    # TODO: IMPORTANT: Add Try/Except MalformedDataError
    address_map = _extract_address_parts(imap['fulladdress'])
    imap.update(address_map)
    for key in [
        "saleprice", "saledate", "assessedlandvalue", "assessedbuildingvalue",
    ]:
        imap[key] = imap[key].lstrip('$')
    imap["saleyear"] = imap["saledate"][-4:]
    imap["assessmentyear"] = imap["assessmentyear"][:4]
    return imap


def create_building_insertmap(html):
    html_element = {
        "yearbuilt": "lblResYearBuilt",
        "livingarea": "lblResLiveArea",
        "condition": "lblResCondition"
    }
    imap = _create_insertmap(html, html_element)
    imap["livingarea"] = imap["livingarea"].rstrip(" SqFt")
    return imap


def create_tax_insertmap(html):
    html_elements = {
        "taxtable": "lblTaxInfo"
    }
    tax_table = _create_insertmap(html, html_elements, clean=False)
    imap = {}
    # Very Non-Pythonic. Is there a better way to do this?
    table_span = tax_table["taxtable"][0]
    data_cells = table_span.find_all("td")
    imap['taxstatusyear'] = remove_whitespace(data_cells[7].text)
    imap['taxstatus'] = data_cells[8].text
    return imap


def _extract_info_from_soup(soup, element_id, element=SPAN, clean=True):
    """
    Arguments:
        soup: bs4.element.NavigableString
            Note: bs4.element.Tag are accepted and filtered out
        element_id: str

    Returns:
        str or list[str]
    """
    # Although most keys work fine, addresses in particular return something like
    # ['1267\xa0BRINTON  RD', <br/>, 'PITTSBURGH,\xa0PA\xa015221'] which needs to be escaped
    content = soup.find(element, id=element_id).contents
    if clean != True:
        return content

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


def _extract_address_parts(full_address):
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
