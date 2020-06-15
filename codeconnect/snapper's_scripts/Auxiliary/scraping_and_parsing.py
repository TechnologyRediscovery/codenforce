import requests


def get_county_property_assessment(parcel_id):
    """
    Grabs the raw HTML of a property from the county's website

    Arguments:
        parcel_id: str

    Returns:
        str:
            The raw HTML from the county's property assessment
    """
    COUNTY_REAL_ESTATE_URL = (
        "http://www2.county.allegheny.pa.us/RealEstate/GeneralInfo.aspx?"
    )
    search_parameters = {
        "ParcelID": parcel_id,
        "SearchType": 3,
        "SearchParcel": parcel_id,
    }
    try:
        response = requests.get(
            COUNTY_REAL_ESTATE_URL, params=search_parameters, timeout=5
        )
        print("Scraping data from county: " + parcel_id)
    except requests.exceptions.Timeout:
        # TODO: log_error(). Perhaps logger should reside in its own auxiliary module?
        raise requests.exceptions.Timeout
    return response.text