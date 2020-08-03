"""
Miscellaneous code to be used in the creation of this package. Should not appear in production.
"""
# Todo: Create git hook that fails if any function in _utils.py appears in the other files.
import pickle
import sys
from os import path
import re
from _parse import TaxStatus

HERE = path.abspath(path.dirname(__file__))
MOCKS = path.join(HERE, "..", "tests", "mocks", "") # The mock folder

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
            >>> from _utils import pickler
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
            >>> from _utils import pickler
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
        with open(path.join(path_to_file, str(THE_PICKLER_COUNT) + "_" + filename + ".pickle"), "wb") as f:
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


# def _find_parid(html):
#     parid_finder = re.compile(r"(ParcelID=)([\d\w]*)")
#     return parid_finder.search(html).group(2)
#
#
# def _replace_parid(old_parid, new_parid, html):
#     return re.sub(old_parid, new_parid, html)
#     # lot_and_block_finder = re.compile(r'(BasicInfo1_lblParcelID" class="Data">)((\w*)-)*\w*')
#     # lot_and_block = lot_and_block_finder.search(html).group(2)
#     # # old-parid -> lotandblock
#
#
# def anonymize(html: str, new_parid: str) -> str:
#     # Todo: Replace lot and block version BasicInfo1_lblParcelID, 0083-K-00020-0000-00
#     old_parid = _find_parid(html)
#     html = _replace_parid(old_parid, new_parid, html)
#     return html

def anonymize2(record, new_parid, new_name, new_taxstatus):
    record = change_record_parid()
    owner_name = mock_owner_name()
    tax_status = mock_tax_status()
    mock_validate()


