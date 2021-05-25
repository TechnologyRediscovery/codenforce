# PyParcel
###### A command line interface for Turtle Creek COG's CodeNForce's database to maintain current data.
## Using the code
### Quick Start
After setting up a local copy of `cogdb` for testing, run ```python pyparcel.py``` to update your database with the latest data from the WPRDC!

## Maintaining the code
### Setting Up a Development Environment

Assuming you are at the root of the CodeNForce folder structure and have [Pipenv](https://pipenv.pypa.io/en/latest/) installed:
~~~
$ pipenv install --dev
$ pipenv shell
$ pre-commit install
~~~
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
* **If the event is classified as a [Parcel Change](#parcel-change):**
  * Add a check for your flag in the function events.query_propertyexternaldata_for_changes_and_write_events. The check should take the following form:
    ~~~
    if old[i] != new[i]:
        details.unpack(old[i], new[i])
        YourNewEvent(details).write_to_db()
    ~~~
   
  * Add your event to the list parcel_changed_events in tests/test_pyparcel.py
    ~~~
    # A manually maintained list of Parcel Changed Event categories
    parcel_changed_events = [
        PCE(YourEvent, "Example starting data" "Example new data",
        ...
    ~~~
    The parametrization takes care of running the tests.


<h4 id=parcel-change>What constitutes a Parcel Change?</h4>
A regular, month to month change.
Examples include DifferentTaxStatus and DifferentOwner, but do not include events such as NewParcelid and ParcelNotInWprdcData.

### Mocking Event Initialization (For Testing)
Sometimes, your new Event contains setup code that causes the automatic testing of your event to fail.
Creating a custom mocked patch is easy: add your patch to `TestsRequiringADatabaseConnection.TestEventCategories.patches` using the provided format.
```
patches = [
    ...,
    PatchMaker(
        production_class=pyparcel.events.YourNewEvent,
        method="method_to_mock",
        return_value="return_value"
    )
]
```
The method `setup_mocks` then automatically adds your patch to a stack of context managers.
