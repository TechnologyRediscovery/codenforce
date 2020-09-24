import requests
from common import GENERALINFO, BUILDING, TAX, SALES, IMAGES, COMPS, APPEAL, MAP


def county_property_assessment(
    parcel_id: str, full_response=False
) -> str or requests.Response:
    """
    Args:
        parcel_id:
        full_response:
            Defaults False.
            When false, this function returns the scraped text a parcel's Allegheny County Real Estate Portal.
            If full_response is truthy, it instead returns the full requests.Reponse object.
    """
    COUNTY_REAL_ESTATE_URL = "http://www2.county.allegheny.pa.us/RealEstate/"
    URL_ENDING = ".aspx?"
    search_parameters = {
        "ParcelID": parcel_id,
        "SearchType": 3,
        "SearchParcel": parcel_id,
    }
    response = requests.get(
        (COUNTY_REAL_ESTATE_URL + TAX + URL_ENDING),
        params=search_parameters,
        timeout=5,
    )
    if not full_response:
        return response.text
    return response


# # There was a point that we planned to scrape multiple pages. Thus, we set up threading.
# # However, we realized that the tax page had all the data we need
# # Thus, the threading doesn't actually do anything since we are on a single thread,
# # and the code is needlessly cluttered and complicated
# def county_property_assessments(parcel_id, pages):
#     results = {}
#     threads = []
#     for key in pages:
#         if key not in [GENERALINFO, BUILDING, TAX, SALES, IMAGES, COMPS, APPEAL, MAP]:
#             raise KeyError(
#                 "Allegheny County's website does not support the given search term"
#             )
#         t = threading.Thread(
#             target=_scrape_county_property_assessment, args=(parcel_id, key, results)
#         )
#         t.start()
#         threads.append(t)
#     for t in threads:
#         t.join()
#     return results
