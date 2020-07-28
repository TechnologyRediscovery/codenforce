"""
Miscellaneous code to be used in the creation of this package. Should not appear in production.
"""
# Todo: Create git hook that fails if any function in _utils.py appears in the other files.
import pickle
import sys
from os import path

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
    with open(path.join(path_to_file, str(THE_PICKLER_COUNT) + "_" + filename + ".pickle"), "wb") as f:
        sys.setrecursionlimit(100000)
        pickle.dump(obj, f)
    if incr:
        THE_PICKLER_COUNT += 1
    sys.setrecursionlimit(initial_recursion)
