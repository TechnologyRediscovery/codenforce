import threading
import requests
from _constants import GENERALINFO, BUILDING, TAX, SALES, IMAGES, COMPS, APPEAL, MAP





def _scrape_county_property_assessment(parcel_id: str, page: str, output: dict):
    """
    Modifies a dictionary with raw html scraped from Allegheny County's property assessment database.

    Args:
        parcel_id:
        page:
        output:

    Returns:
        None    # Because it modifies the dictionary "output" in place.
    """
    COUNTY_REAL_ESTATE_URL = "http://www2.county.allegheny.pa.us/RealEstate/"
    URL_ENDING = ".aspx?"
    search_parameters = {
        "ParcelID": parcel_id,
        "SearchType": 3,
        "SearchParcel": parcel_id,
    }
    response = requests.get(
        (COUNTY_REAL_ESTATE_URL + page + URL_ENDING),
        params=search_parameters,
        timeout=5,
    )
    output[
        page
    ] = (
        response.text
    )  # As we are modifying the input in place, we do not need a return statement


# There was a point that we planned to scrape multiple pages. Thus, we set up threading.
# However, we realized that the tax page had all the data we need
# Thus, the threading doesn't actually do anything since we are on a single thread,
# and the code is needlessly cluttered and complicated
def county_property_assessments(parcel_id, pages):
    results = {}
    threads = []
    for key in pages:
        if key not in [GENERALINFO, BUILDING, TAX, SALES, IMAGES, COMPS, APPEAL, MAP]:
            raise KeyError(
                "Allegheny County's website does not support the given search term"
            )
        t = threading.Thread(
            target=_scrape_county_property_assessment, args=(parcel_id, key, results)
        )
        t.start()
        threads.append(t)
    for t in threads:
        t.join()
    return results
