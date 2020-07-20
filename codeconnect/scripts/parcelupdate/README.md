# Parcel Update
###### A command line interface for keeping Turtle Creek COG's CodeNForce's data up to date.
## Using the code
### Quickstart
Assuming you are at the root of the CodeNForce folder structure:
~~~
$ pipenv install
$ pipenv shell
$ cd codeconnect\scripts\parcelupdate
$ python parcelupdate.py
~~~
This updates all parcels using the latest data from the WPRDC. For more options, run `python parcelupdate.py -h`.

*"[Pipenv](https://pipenv.pypa.io/en/latest/) automatically creates and manages a virtualenv for your projects, as well as adds/removes packages from your Pipfile as you install/uninstall packages. It also generates the ever-important Pipfile.lock, which is used to produce deterministic builds."*


## Maintaining the code
### How to add a new Event Category
* Add the new event category to the database. See example sql below.
~~~{caption="Example insert sql"}  
INSERT INTO public.eventcategory(
         categoryid, categorytype, title, description, notifymonitors,
         hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
         hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
         active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
 VALUES (?, 'PropertyInfoCase'::eventtype, '?', 'Documents ?', ?,
         TRUE, NULL, 0, 0,
         NULL, NULL, 1,
         TRUE, 7, 3, 7);
~~~
* Create a class corresponding to the event category in events.py. Make sure it inherits from the base class event. Make sure its name matches up exactly to the name in the database.
* Add a corresponding flag to the __init\__ method of the class events.Property. It should default to False.
* Add a check for your flag in the function events.parcel_changed. The check should take the following form:
~~~
if old[i] != new[i]:
       flags.yourflag = Flag("your flag", old[i], new[i])
~~~

* Add the test for the flag in the function update_muni in _update_muni.py.
~~~
if flags.taxstatus:
    event = events.DifferentTaxStatus(
       parid, prop_id, flags.your_flag, db_cursor
    )
    event.write_to_db(db_cursor)
~~~
