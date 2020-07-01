# Subsystem XIII Public information

&lt;- [home](index.md)

## Overview
The "public" means a user on the site who has not authenticated with a Username and password. Individuals who have involvement in a Code enforcement case or are a party in any way to a occupancy period should be able to
1. Access basic information about The status of their case/period
2. Retrieve a human name with a phone number to contact with questions
3. Add notes, messages, updates, or photographs to applicable items (A CE Case, an inspeection) 

## Subcomponents
1. Code enforcement action request status lookup: For folks who have complained about an issue by filing an online request for CE action, they can use a 6-digit control code to see their action request and perhaps any associated CE cases

## Access a InfoBundle through a PACC
A PACC is a public access control code that allows for retrieval of information about any number of codeNforce objects. This is a stand-in for a full logon; as long as that PACC is enabled, it can retrieve info that is packaged inside of a `PublicInfoBundle` subclass

## Design goals (unfinished components)
1. Track each use of each PACC as an event on an associated object; e.g. when somebody views the status of a code enforcement case against their neighbor, the internal user should see an EventCnF appear on that case's internal event history

## PublicInfoBundles as a wrapper class
In our newest approach, we are going to use PublicInfoBundles as a wrapper class that contains a filtered BOb that can be viewed or edited by a public user!

![Diagram explaining the ](img/PublicInfoBundleEcosystem.png "Open this image in a new tab if it's too small to read!")


The wrapper class also contains bundles that might be useful to the user viewing their public data. For instance, a PublicInfoBundleFeeAssigned has a list of PublicInfoBundlePayment objects. When converting a FeeAssigned object to a PublicInfoBundleFeeAssigned, each Payment in the list of Payments on the FeeAssigned is individually converted and added to the list of PublicInfoBundlePayments on the PublicInfoBundleFeeAssigned. The original Payment list is then cleared.

When exporting the PublicInfoBundleFeeAssigned back into a FeeAssigned object, the reverse is done: The bundled FeeAssigned object is checked for changes, and any fields that were not changed or were cleared are filled in using a copy of the entry currently in the database. Then, the each PublicInfoBundlePayment in the PublicInfoBundlePayment list is individually exported, and this exported list of Payment objects is set on the FeeAssigned object.

One thing to keep in mind is that many export methods return the data heavy form of an object in order to preserve all the various lists the public user might have changed. Since these methods are designed export individual objects to check for changes, this shouldn't be an issue, but exporting object en masse could lead to lapses in performance.