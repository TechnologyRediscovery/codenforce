import re
from collections import namedtuple
import bs4
from _constants import OWNER, TAXINFO
from _constants import SPAN

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
        # Todo: Log error HERE
        return cleaned_content
    return cleaned_content


TaxStatus = namedtuple(
    "tax_status",
    ["year", "paidstatus", "tax", "penalty", "interest", "total", "date_paid", "blank"],
)


def parse_tax_from_soup(soup):
    """
    :param soup:
    :return:
    """
    table = _extract_elementlist_from_soup(
        soup, element_id=TAXINFO, element=SPAN, remove_tags=False
    )
    row = table[0].contents[1]  # The most recent year's data
    try:
        return TaxStatus(*[clean_text(x.text) for x in row.contents])
    except TypeError:  # When taxes are unpaid:
        # Set date_paid and blank set to "" and eventually None. Todo: Make more pythonic?
        rows = [
            row.contents[0].text, row.contents[1].text, row.contents[2].text,
            row.contents[3].text, row.contents[4].text, row.contents[5].text,
            "", ""
        ]
        return TaxStatus(*[clean_text(x) for x in rows])

# Function currently unused
def clean_text(text):
    text = strip_whitespace(text)
    text = strip_dollarsign(text)
    text = remove_commas_from_numerics(text)
    if text == "":
        text = None
    return text


def strip_whitespace(text):
    return re.sub(" {2,}", " ", text).strip()
def strip_dollarsign(text):
    return re.sub(r"\$( )*", "", text)
def remove_commas_from_numerics(text):
    if re.fullmatch(r"[\d,\.]+", text):
        return re.sub(r"[,\.]", "", text)
    return text


class OwnerName:
    __slots__ = ["raw", "clean", "first", "last", "multientity", "compositelname"]

    def __init__(self, parid=None):
        self.multientity = None
    def __str__(self):
        return self.clean
    def __repr__(self):
        return f"{self.__class__.__name__}<{self.clean}>"



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




