"""
Miscellaneous code to be used in the creation of this package. Should not appear in production.
"""
# Todo: Create git hook that fails if any function in utils.py appears in the other files.
import pickle
import sys
from os import path
from _constants import MEDIUM_DASHES
import itertools
import bs4


from _constants import SPAN, PARCEL_ID, OWNER, TAXINFO
import _scrape as scrape
import _parse
from _parse import TaxStatus
from typing import Dict, NamedTuple, Any, Optional, Tuple, List

HERE = path.abspath(path.dirname(__file__))
MOCKS = path.join(HERE, "..", "tests", "mocks", "")  # The mock folder

THE_PICKLER_COUNT = 0


# Todo: Discuss merits of permanent calls to the pickler left in source code with @click.option(--pickle/--nopickle)
def pickler(obj, filename, path_to_file=MOCKS, incr=True):
    """
    A quick and dirty way to pickle objects. Useful for creating mocks.

    Don't forget to delete your call to the pickler after you are done!

    How to use:
        Given the following source code, assuming we want to pickle foo
            >>> class Foo: pass
            >>> for i in range(0,3):
            >>>     foo = Foo()
        Modify the source code!
            >>> from utils import pickler
            >>> class Foo: pass
            >>> for i in range(0,3):
            >>>     foo = Foo()
            >>>     pickler(foo, "foo")
        Enjoy your new serialized objects, "0_foo.pickle", "1_foo.pickle", and 2_foo.pickle".
        You can find them in codenforce/tests/mocks


        Given the following source code, assuming we want to pickle foo, bar, and baz
            >>> class Foo: pass
            >>> class Bar: pass
            >>> for i in range(0,3):
            >>>     foo = Foo()
        Modify the source code!
            >>> from utils import pickler
            >>> class Foo: pass
            >>> class Bar: pass
            >>> class Baz: pass
            >>> for i in range(0,3):
            >>>     foo = Foo(); bar = Bar(); baz = Baz()
            >>>     pickler(foo, "foo", incr=False)
            >>>     pickler(bar, "bar", incr=False)
            >>>     pickler(baz, "baz", incr=True)

        """
    global THE_PICKLER_COUNT
    initial_recursion = sys.getrecursionlimit()
    try:
        sys.setrecursionlimit(100000)
        with open(
            path.join(
                path_to_file, str(THE_PICKLER_COUNT) + "_" + filename + ".pickle"
            ),
            "wb",
        ) as f:
            pickle.dump(obj, f)
        if incr:
            THE_PICKLER_COUNT += 1
    finally:
        sys.setrecursionlimit(initial_recursion)


########################################################################################
# from _utils import pickler
# pickler(owner_name, "own", incr=False)
# pickler(soup, "soup", incr=False)
# pickler(imap, "prop_imap", incr=False)
# pickler(cecase_map, "cecase_imap", incr=False)
# pickler(owner_map, "owner_imap", incr=False)
# pickler(propextern_map, "propext_imap", incr=True)
########################################################################################


def replace_parid(new_parid, soup, r) -> Tuple[bs4.BeautifulSoup, Dict]:
    """
    """
    r["PARID"] = new_parid
    new_soup = _parse.replace_html_content(soup, PARCEL_ID, new_parid)
    # # TODO: REPLACE LOT AND BLOCK AS WELL
    return new_soup, r


def replace_name(new_name, soup) -> bs4.BeautifulSoup:
    """
    """
    return _parse.replace_html_content(soup, OWNER, new_name)


def replace_taxstatus(taxes, soup) -> bs4.BeautifulSoup:
    """
    """
    # Setup: We want taxes to be a list of at least 4 TaxStatuses or Nones, but we accept any number, including a single TaxStatus instance
    if isinstance(taxes, TaxStatus):
        taxes = [taxes, None, None, None]
    while len(taxes) < 4:
        taxes.append(None)

    table = soup.find(SPAN, id=TAXINFO)
    # sadly, replace_html_content is not robust enough to handle tables, nor is it able to handle the tuples
    # Todo: Make code make sense. LEARN HOW BS4 WORKS. Should I do a find_all("td") or something?
    # Todo: Make replace_html_content more robust
    # Todo: Refactor this mess of a function into parts (in _parse)
    rows = table.contents[0]  # The 4 most recent years of data
    if (
        len(rows) != 5
    ):  # Todo: Important test: Make sure next year after tax season there aren't suddenly 6 rows
        #   As far as I can tell, if a person has unpaid taxes,
        #       the header row and first row of unpaid taxes will be the table.
        #       The following row will have yellow highlighting, so we can seek it out
        #       These rows merge some columns with the text "See Below and Contact Jordan Tax Service at 412-835-5243"
        # Todo: Find out the Pythonic way to use BS4. There has to be a way to scrape the next row, even though it doesn't have the same number of columns
        unpaid_row1 = soup.find(bgcolor="#CCCCCC")
        unpaid_row2 = unpaid_row1.next_sibling
        unpaid_row3 = unpaid_row2.next_sibling
        rows.extend((unpaid_row1, unpaid_row2, unpaid_row3))
        assert len(rows) == 5
    # The first row is the header column (year, paid status, etc), which we do not need to replace.
    data = rows.contents[1:]

    # None is not an iterable. We replace any Nones in taxes with an iterable version to avoid exceptions while looping.
    for i, tax_status in enumerate(taxes):
        if tax_status is not None:
            continue
        taxes[i] = itertools.repeat(None,)

    # Replaces the old html content with the new content
    for row, tax_status in zip(data, taxes):
        for tag, field in zip(row, tax_status):

            if not isinstance(tax_status, TaxStatus):
                continue

            try:
                tag.string = field
            except TypeError as E:  # NoneType takes no arguments
                tag.contents.append(field)

    return soup


def anonymize_html_and_record(
    r: Dict[str, Any],
    new_parid: Optional[str] = None,
    new_name: Optional[str] = None,
    new_taxstatus: Optional[TaxStatus or List[TaxStatus,]] = None,
) -> Tuple[str, Dict[str, Any]]:
    """
    anonymize_html_and_record creates a custom Allegheny County Real Estate html based upon a real page.

    Args:
        r: A real record the anonymized output is based upon.
            The dictionary represents single property returned by the WPRDC property assessment API.
            https://data.wprdc.org/dataset/property-assessments/resource/518b583f-7cc8-4f60-94d0-174cc98310dc
        new_parid:
        new_name:
        new_taxstatus:

    Returns:
        A tuple containing the mocked HTML and record
    """
    orig_parid = r["PARID"]
    html = scrape.county_property_assessment(orig_parid)
    soup = _parse.soupify_html(html)
    if new_parid:
        soup, r = replace_parid(new_parid, soup, r)
    if new_name:
        soup = replace_name(new_name, soup)
    if new_taxstatus:
        soup = replace_taxstatus(new_taxstatus, soup)
    mocked_html = soup.text
    return mocked_html, r


if __name__ == "__main__":
    pass