select parcelidcnty, array_agg(parcelkey) from parcel group by parcelidcnty, deactivatedts
having count(*) > 1 and deactivatedts IS NULL;

select name, array_agg(streetid) from mailingstreet group by name, deactivatedts
having count(*) > 1 and mailingstreet.deactivatedts IS NULL;

