# Todo: Move functions from parcelupdate into create
from _constants import BOT_ID
from _constants import SPACE


def cecase_imap(prop_id, unit_id):
    imap = {}
    imap["cecasepubliccc"] = 111111
    imap["property_propertyid"] = prop_id
    imap["propertyunit_unitid"] = unit_id
    imap["login_userid"] = BOT_ID
    imap["casename"] = "Property Info Case"
    imap["notes"] = "Autogenerated."
    imap["paccenabled"] = False
    imap["allowuplinkaccess"] = None
    imap["propertyinfocase"] = True
    imap["personinfocase_personid"] = None
    imap["bobsource_sourceid"] = None
    imap["active"] = True
    return imap


def propertyexternaldata_imap(prop_id, name, r, taxstatus_id):
    imap = {}
    imap["property_propertyid"] = prop_id
    imap["ownername"] = name
    imap["address_street"] = SPACE.join((r["PROPERTYHOUSENUM"], r["PROPERTYADDRESS"]))
    imap["address_city"] = r["PROPERTYCITY"]
    imap["address_state"] = "PA"
    imap["address_zip"] = r["PROPERTYZIP"]
    imap["address_citystatezip"] = SPACE.join(
        (imap["address_city"], imap["address_state"], imap["address_zip"])
    )
    imap["saleprice"] = r["SALEPRICE"]
    imap["saledate"] = r["SALEDATE"]  # Todo: Add column to databse
    try:
        imap["saleyear"] = r["SALEDATE"][-4:]
    except TypeError:
        imap["saleyear"] = None
    imap["assessedlandvalue"] = r["COUNTYLAND"]
    imap["assessedbuildingvalue"] = r["COUNTYBUILDING"]
    imap["assessmentyear"] = r[
        "TAXYEAR"
    ]  # BIG TODO: Scrape assessment year from county
    imap["usecode"] = r["USECODE"]
    imap["livingarea"] = r["FINISHEDLIVINGAREA"]
    imap["condition"] = r["CONDITION"]  # Todo: Condition to condition desc table
    imap["notes"] = SPACE.join(("Scraped by bot", BOT_ID))
    imap["taxstatus_taxstatusid"] = taxstatus_id
    return imap


def property_insertmap(r):
    """
    Arguments:
        r: dict
            The dictionized JSON record for a parcel id provided by the WPRDC
    Returns:
        dict
    """
    imap = {}
    imap["municipality_municode"] = r["MUNICODE"]
    imap["parid"] = r["PARID"]
    # imap["lotandblock"] = extract_lotandblock_from_parid(imap["PARID"])
    imap["usegroup"] = r[
        "USEDESC"
    ]  # ? I THINK this is what we want? Example, MUNICIPAL GOVERMENT.
    imap["constructiontype"] = None  # ?
    imap["countycode"] = None  # ? 02
    imap["notes"] = "Data from the WPRDC API"

    imap["address"] = SPACE.join((r["PROPERTYHOUSENUM"], r["PROPERTYADDRESS"]))
    imap["address_extra"] = r["PROPERTYFRACTION"]  # TODO: Add column
    imap["addr_city"] = r["PROPERTYCITY"]
    imap["addr_state"] = r["PROPERTYSTATE"]
    imap["addr_zip"] = r["PROPERTYZIP"]
    imap["ownercode"] = r["OWNERDESC"]
    imap["propclass"] = r["CLASS"]
    imap["lastupdatedby"] = BOT_ID
    imap["locationdescription"] = None
    imap["bobsource"] = None
    imap["lotandblock"] = ""
    return imap


def owner_imap(name, r):
    imap = {}
    imap["muni_municode"] = r["MUNICODE"]

    imap["jobtitle"] = None
    imap["phonecell"] = None
    imap["phonehome"] = None
    imap["phonework"] = None
    imap["email"] = None
    # TODO: Change our database to match theirs

    imap["mailing1"] = r["CHANGENOTICEADDRESS1"]
    imap["mailing2"] = r["CHANGENOTICEADDRESS2"]
    imap["mailing3"] = r["CHANGENOTICEADDRESS3"]
    imap["mailing4"] = r["CHANGENOTICEADDRESS4"]

    # Todo: Deprecate
    imap["address_street"] = r["CHANGENOTICEADDRESS1"]
    imap["address_city"] = r["CHANGENOTICEADDRESS3"].rstrip(" PA")
    imap["address_state"] = "PA"
    imap["address_zip"] = r["CHANGENOTICEADDRESS4"]
    imap[
        "notes"
    ] = "In case of confusion, check automated record entry with raw text from the county database."
    imap["expirydate"] = None
    imap["isactive"] = True
    imap["isunder18"] = None
    imap["humanverifiedby"] = None
    imap["rawname"] = name.raw
    imap["cleanname"] = name.clean
    imap["fname"] = name.first
    imap["lname"] = name.last
    imap["multientity"] = name.multientity
    imap["compositelname"] = name.compositelname
    return imap
