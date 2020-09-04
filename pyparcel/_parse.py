import re
from dataclasses import dataclass

import bs4
from common import OWNER, ADDRESS, MUNICIPALITY, TAXINFO, SPAN
from common import TaxStatus
from typing import List, Any, NamedTuple


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


# Todo: The replace_taxstatus really messes this up. Is there a way to use a single function?
def replace_html_content(new_str, soup, id):
    tag = soup.find(id=id)
    tag.string = new_str
    return soup


def parse_tax_from_soup(soup: bs4.BeautifulSoup, clean=True) -> TaxStatus:
    """
    """
    table = _extract_elementlist_from_soup(
        soup, element_id=TAXINFO, element=SPAN, remove_tags=False
    )
    row = table[0].contents[1]  # The most recent year's data
    data = row.contents

    # Todo: Document Intellej bug claiming TaxStatus received an unexpected argument.
    if clean:
        return TaxStatus(*[clean_text(x.text) for x in data])
    return TaxStatus(*[x.text for x in data])


def validate_county_municode_against_portal(html) -> List[str]:
    """
    Args:
        html: Allegheny County Real Estate Portal html

    Returns:
        The municipality's municode and name if the html points to a real page.
            Example: ['843\xa0North Braddock  ']
        An invalid page returns an empty list
    """
    soup = soupify_html(html)
    # Makes the assumption that a page without an owner is invalid.
    return parse_municipality_from_soup(soup)


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
    # Only remove commas if the text is a number
    # (It will not
    if re.fullmatch(r"[\d,\.]+", text):
        return re.sub(r"[,\.]", "", text)
    return text


def remove_hyphnes(text):
    return re.sub("-", "", text)


def parse_owners_from_soup(soup: bs4.BeautifulSoup,) -> List[str]:
    return _extract_elementlist_from_soup(
        soup, element_id=OWNER, element=SPAN, remove_tags=True
    )


def parse_municipality_from_soup(soup: bs4.BeautifulSoup,) -> List[str]:
    return _extract_elementlist_from_soup(
        soup, element_id=MUNICIPALITY, element=SPAN, remove_tags=True
    )


class Municipality(NamedTuple):
    municode: int
    name: str

    @classmethod
    def from_raw(cls, raw_muni: List[str]):
        """
        Factory method for creating Municipalities from the raw text on Allegheny County Real Estate Portal's site.

            >>> muni = Municipality.from_raw(['843\xa0North Braddock  '])
            Municipality(municode='843', name='North Braddock')
        """
        # Makes the assumption that there will only ever be one muni
        return Municipality(*clean_text(raw_muni[0]).split("\xa0"))


class OwnerName:
    __slots__ = ["raw", "clean", "first", "last", "multientity", "compositelname"]

    def __init__(self, parid=None):
        self.multientity = None

    def __str__(self):
        return self.clean

    def __repr__(self):
        return f"{self.__class__.__name__}<{self.clean}>"

    @classmethod
    def from_soup(cls, soup: bs4.BeautifulSoup):
        """ Factory method for creating OwnerNames from a soup.
        """
        o = OwnerName()
        o.raw = parse_owners_from_soup(soup)
        o.clean = (
            o.clean_raw_name()
        )  # Method side effect: May change flag o.multientity

        # The Java side hasn't updated their code to match the cleanname, and instead concatenates fname and lname.
        # Todo: deprecate the composite last name flag
        o.first = ""
        o.last = o.clean
        o.compositelname = True
        return o

    def clean_raw_name(self) -> str:
        if len(self.raw) > 1:
            self.multientity = True
            cleaned_names = []
            for name in self.raw:
                cleaned_names.append(strip_whitespace(name))
            return ", ".join(cleaned_names)
        return strip_whitespace(self.raw[0])
