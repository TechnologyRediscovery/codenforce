SELECT permitid FROM occpermit
INNER JOIN occperiod ON (occperiod.periodid = occpermit.occperiod_periodid)
INNER JOIN parcelunit ON (occperiod.parcelunit_unitid = parcelunit.unitid)
INNER JOIN parcel ON (parcelunit.parcel_parcelkey = parcel.parcelkey)
INNER JOIN municipality ON (parcel.muni_municode = municipality.municode)
WHERE municipality.municode = 999;