import logging
import inspect
import sys

# Important Note! Do not create classes in this module that are not Errors, as validate_exceptions uses introspection

error_logger = logging.getLogger(__name__)
error_logger.setLevel(logging.DEBUG)

error_logger.debug('Error logger initialized')


# Todo: Consider abstract base class implementation
class MalformedDataError(IndexError):
    """ Base class for errors resulting from Malformed Data """
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
        self.type = 'address'
        self.error_code = 1
        self.subtype = None


# Todo: Implement behavior using metaclasses to ensure attributes exist
class MalformedStateError(MalformedGenericAddressError):
    """ Thrown when the script can't parse State information. """

    def __init__(self):
        super().__init__()
        self.error_code = 2
        self.subtype = 'state'


class MalformedZipcodeError(MalformedGenericAddressError):
    """ Thrown when the script can't parse zipcode information. """

    def __init__(self):
        super().__init__()
        self.error_code = 3
        self.subtype = 'zipcode'


class MalformedLotAndBlockError(MalformedGenericAddressError):
    """ Thrown when the script can't parse lot and block information. """

    def __init__(self):
        super().__init__()
        self.error_code = 4
        self.subtype = 'lot and block'


# Todo: Find out what can break to throw this error. Currently not implemented
class MalformedStreetError(MalformedGenericAddressError):
    """ Thrown when the script can't parse street information. """

    def __init__(self):
        super().__init__()
        self.error_code = 5
        self.subtype = 'street'


# ------------------------------
# Owner Errors
# ------------------------------

class MalformedOwnerError(MalformedDataError):
    """ Thrown when scraped person (owner) HTML doesn't fit expected format. """

    def __init__(self):
        self.type = 'owner'
        self.error_code = 6
        self.subtype = None


# ------------------------------
# Code Validation
# ------------------------------
# Note: Code introspection is a haphazard approach to code validation.
# I would use metaclasses if I understood them well enough.
# Unfortunately, this code is likely to stick around. Although it's bad practice, the code validation is effective.

# Creates a dictionary where keys are the error code and values are the exception class
custom_exceptions = inspect.getmembers(sys.modules[__name__], inspect.isclass)

# It is important we import defaultdict after saving our custom_exceptions.
# Otherwise, it will erroneously be added to the list of exceptions
from collections import defaultdict

errorcode_lookup = defaultdict(list)
for exception in custom_exceptions:
    exception_name = exception[0]
    exception_class = exception[1]
    # Error codes are added during instance initialization.
    # Thus, we create an instance of each exception to check its errorcode
    try:
        raise exception_class()
    except Exception as e:
        errorcode_lookup[e.error_code].append(exception_name)

# Validates that no two errorcodes are the same
for key in errorcode_lookup:
    err_name_list = errorcode_lookup[key]
    if len(err_name_list) == 0:
        print(
            'ERROR: {} does not have a corresponding error code'.format(err_name_list[0])
        )
        raise ValueError
    elif len(err_name_list) > 1:
        print(
            'ERROR: error code {} is shared by the following exceptions: {}'.format(key, err_name_list)
        )
        raise ValueError
