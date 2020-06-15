# Subsystem III Properties

&lt;- [home](index.md)

## Rental
"Rentalness" is manifest in two ways in codeNforce. The first is called rental intent and includes a start and stop date, along with the user who marked it last and a set of notes. 

This is useful because clients want to be able to differentiate between units that sombody--like the code officer or borough staff--knows is or will become a rental unit and the units for which a full occupancy certification process has been undertaken resulting in a formal certificate of occupancy.

Wiklins, for example, wants to be able to charge a rental fee to all rental units, regardless of their tenant status, once per year. They'd use the "rentalintentstart" and "rentalintentstop" dates to pull a list of currently "rentalintent" units on which to assess a fee. 

Another use of the rental intent tracking is to locate properties whose owners/managers are not following proper procedure. If a tenant is occupying a rental unit flagged with rental intent, but an occupancy permitting process has not been completed for this unit yet, a query could find these noncompliant units.

## Rental registry
A borough's rental registry, therefore, could also be managed with these two classifications.

## The unit -1
Each property must have at least one PropertyUnit because occupancy periods can only be attached to property units. We use the -1 marker because it's extremely unlikely for an owner to officially give a unit a negative unit identifier.

## Property Unit update rules
1. When a property's unit list is updated, if the user is requesting only two units, and one of them is still the unit -1, the operation will result in ONLY an update to the original unit entry with the data submitted for the second unit. Be sure to note this to the user.
2. A user cannot delete all units on a property, and such an action will result in the system asking the user what to name the single unit--perhaps previously unit -1.



