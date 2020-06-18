import inspect
import sys

# Important Note! Do not create classes in this module that are not Errors, as validate_exceptions uses introspection


# Todo: Discuss refactoring this into a scraping error?
class MalformedDataError(IndexError):
    """
    Base class for errors resulting from Malformed Data
    Error likely raised during scraping
    """

    def __init__(self):
        self.error_code = 0


# ------------------------------
# Address Errors
# ------------------------------


class MalformedGenericAddressError(MalformedDataError):
    """
    Thrown when scraped address HTML doesn't fit expected format and the error can't be further specified.
    Further subclassed by more specific Address Errors.
    """

    def __init__(self):
        self.type = "address"
        self.error_code = 1
        self.subtype = None


class MalformedStateError(MalformedGenericAddressError):
    """ Thrown when the script can't parse State information. """

    def __init__(self):
        super().__init__()
        self.error_code = 2
        self.subtype = "state"


class MalformedZipcodeError(MalformedGenericAddressError):
    """ Thrown when the script can't parse zipcode information. """

    def __init__(self):
        super().__init__()
        self.error_code = 3
        self.subtype = "zipcode"


class MalformedLotAndBlockError(MalformedGenericAddressError):
    """ Thrown when the script can't parse lot and block information. """

    def __init__(self):
        super().__init__()
        self.error_code = 4
        self.subtype = "lot and block"


# Todo: Find out what can break to throw this error. Currently not implemented
class MalformedStreetError(MalformedGenericAddressError):
    """ Thrown when the script can't parse street information. """

    def __init__(self):
        super().__init__()
        self.error_code = 5
        self.subtype = "street"


# ------------------------------
# Owner Errors
# ------------------------------


class MalformedOwnerError(MalformedDataError):
    """ Thrown when scraped person (owner) HTML doesn't fit expected format. """

    def __init__(self):
        self.type = "owner"
        self.error_code = 6
        self.subtype = None


# ------------------------------
# Code Validation
# ------------------------------
# Note: Code introspection is a haphazard approach to code validation.
# However, the code works, and is heavily commented so the more "clever" approaches can be understood
# Feel free to rewrite the code using metaclasses (the 'proper' way to do code validation) if it bothers you too much 😛

# The following chunk of codes creates the dictionary errorcode_lookup where
#   The key is an error code
#   The value is a list of exception names that share the error code
# It is not in a function so that inspect.getmembers is in the same scope as the error classes

# custom_exceptions is a list of tuples where
#   The zeroth index of the tuple is the name of the error code
#   The first index of the tuple is the exception class
custom_exceptions = inspect.getmembers(sys.modules[__name__], inspect.isclass)
# It is important we import defaultdict after saving our custom_exceptions.
# Otherwise, it will erroneously be added to the list of exceptions
from collections import defaultdict

errorcode_lookup = defaultdict(list)

# This loop creates the dictionary.
# For each exception in the list, an exception object is created and its error_code is read.
# The error_code is stored as the key in the dict errorcode_lookup, and the exception name is stored as the value.
for exception in custom_exceptions:
    exception_name = exception[0]
    exception_class = exception[1]
    errorcode_lookup[exception_class().error_code].append(exception_name)


# Validates that no two errorcodes are the same
collision_flag = False
for key in errorcode_lookup:
    err_name_list = errorcode_lookup[key]
    if len(err_name_list) > 1:
        print(
            "ERROR: error code {} is shared by the following exceptions: {}".format(
                key, err_name_list
            )
        )
        collision_flag = True
if collision_flag:
    raise ValueError

# TODO: Create script to automatically add each errorcode into db
