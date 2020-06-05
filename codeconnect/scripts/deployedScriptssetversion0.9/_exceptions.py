import logging
error_logger = logging.getLogger(__name__)
error_logger.setLevel(logging.DEBUG)

error_logger.debug('Error logger initialized')

# Todo: Consider abstract base class implementation
class MalformedDataError(IndexError):
    """ Base class for errors resulting from Malformed Data """
    pass


class MalformedAddressError(MalformedDataError):
    """ Thrown when scraped address HTML doesn't fit expected format"""
    def __init__(self):
        self.type = 'address'


class MalformedOwnerError(MalformedDataError):
    """ Thrown when scraped address HTML doesn't fit expected format"""

    def __init__(self):
        self.type = 'owner'