import logging
error_logger = logging.getLogger(__name__)
error_logger.setLevel(logging.DEBUG)

error_logger.debug('Error logger initialized')

# Todo: Consider abstract base class implementation
class MalformedDataError(IndexError):
    """ Base class for errors resulting from Malformed Data """
    pass

# ------------------------------
# Address Errors
# ------------------------------


class MalformedAddressError(MalformedDataError):
    """ Thrown when scraped address HTML doesn't fit expected format """
    def __init__(self):
        self.type = 'address'
        self.subtype = None


class MalformedStateError(MalformedAddressError):
    """ Thrown when the program can't parse State information """
    def __init__(self):
        self.subtype = 'state'


class MalformedZipcodeError(MalformedAddressError):
    """ Thrown when the program can't parse zipcode information """
    def __init__(self):
        self.subtype = 'zipcode'


class MalformedLotAndBlockError(MalformedAddressError):
    """ Thrown when the program can't parse lot and block information """
    def __init__(self):
        self.subtype = 'lot and block'


# ------------------------------
# Owner Errors
# ------------------------------




class MalformedOwnerError(MalformedDataError):
    """ Thrown when scraped address HTML doesn't fit expected format"""

    def __init__(self):
        self.type = 'owner'
        self.subtype = None