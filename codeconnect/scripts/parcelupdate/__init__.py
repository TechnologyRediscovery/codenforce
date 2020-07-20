__all__ = [
    "_constants",
    "_db_conn",
    "_scrape_and_parse",
    "_events",
    "_fetch",
    "parcelupdate",
    "_insert",
    "_property"
]

#   parcelupdate uses semantic versioning (https://semver.org/)
#   Please note that the versioning will not be kept up to date until version 1.0.0
VERSION = (0, 0, 1)
__version__ = '.'.join(map(str, VERSION))
