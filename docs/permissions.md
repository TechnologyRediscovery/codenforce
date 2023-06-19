# Permissions

## Overview
UserAuthorized objects are injected with a Credential object which is basically a collection of boolean flags mapped to each possible user rank and one extra flag for the UserAuthorized's code officer status. A permissions checkpoint method, all of which live in an appropriate Coordinator class, asks the UserAuthorized for this Credential and the user's current Municipality's Governing MuniProfile. MuniProfile objects primarily specify rank and code officer requirements applicable to various business operations. Using these two objects, a permissions checkpoint method then makes a yes/no determination for the given business operation based on the outcome of one or more flag evaluations.

## Two-stage permissions implementation
Users not permitted to perform an action should be prevented from doing so in two stages:
1. Encountering missing or disabled UI components that initiate and/or finalize permissions restricted operations
2. Subsystem coordinator methods governing permissions restricted operations will obtain verification of permission prior to writing to the database and throw an `AuthorizationException` for failed permissions screens.

## Structure of permissions logic and users of those determinations
Every permissions restricted action is governed by logic that lives ONLY in its subsystem coordinator method whose signature follows this pattern:

`public boolean permissionsCheckpointFooBar(UserAuthorized ua, ...other objects...)` 

Backing beans maintain their own boolean switches for turning on and off UI controls for each permissions restricted action based on the boolean `permissionsCheckpoint` method determination which they'll ask for during the bean initialization `initBean` method and pass the outcome through the faces page for use in `rendered=` or `disabled=` component property determinations.

When a permission restricted action is actually undertaken, the governing subsystem coordinator method will AGAIN ask the appropriate `permissionsCheckpoint` method to verify the legitimacy of the request. 

### Responding to a `false` checkpoint determination
In the event that the relevant `permissionsCheckpoint` method returns `false`, the backing beans will simply pass that `false` out to the faces pages which should either not render or disable components like buttons and links that both initiate and/or finalize permissions restricted operations.

Coordinator methods that implement the business rules of permission-restricted operations call the relevant `permissionsCheckpointFooBar` methods and all false return values trigger an `AuthorizationException` to be thrown. 

The calling backing bean catches the `AuthorizationException` and at minimum dispatches a `SEVERITY_ERROR` growl flag out to the user. The backing beans should also revert out of any active edit modes to discourage continued attempts. 

### Separation of concerns reminder
No permissions checking occurs in subsystem integration methods since the relevant business objects with permissions data aren't even passed in as parameters. 

## TODOs
TODO: Log any AuthorizationException throws in ideally a user-facing log for review.

## Permissions rules by subsystem

### Field Inspections
MuniProfile can specify:
* Is manager rank required to edit a checklist?
* Is code officer permissions required to conduct an inspection?
* Is code officer permissions required to finalize an inspection?
* Is manager rank required to finalize an inspection?

Finalization logic also applies to removing finalization. 
Code violations permissions are applied during transfer of failed inspection items to a CE case
Deactivation of field inspections is governed by the permission floor for conducting inspections.

Dispatches are governed by the checkpoint permissionsCheckpointAddUpdateFieldInspectionMetadata

### Occupancy permits
Users must have rank of muni staff or better to draft permits. `MuniProfile` can require code officer or manager roles for formal issuance of the permit (and generation of its sequence ID). Issuance check is also applied for permit nullification. The issuance check is also applied during permit finalize override failed audit.

## Structure of a permissions checkpoint method
0. Deny if any input is null
1. Allow system admins first and get them out of the logic chain
2. Deny non muni staff and lower (public users)
3. Check muni profile for ceo or manager rank required flags, and if triggered, check the user's rank and deny if floor of the muniprofile is not met 
4. Allow muni staff through with a final `return true;`

## System admin only operations

Only users with system administrator rank can undertake the following permissions-restricted operations:
* Adjust permissions settings for users
* Edit cross-muni code sources (source metadata and any of its ordinances) such as the IPMC family of code and PA State-level carbon monoxide statutes
* Event category management
* Deactivate:
	 * Ordinances (shared)
	 * Code Books (keep system wide display (perhaps add a show all munis check box)). Yes, this means that non sys admin users cannot deac their own muni or personally created code books
	 * Events they didn’t add themselves / on cases where they aren’t period manager
	 * Photos and Documents  they didn’t add themselves / on  cases where they aren’t period manager
	 * Permit Files they didn’t open
	 * Code Enforcement Case they didn’t open
	 * Persons (and any details about that person)
	 * Ord cats and sub-cats: Any editing user can ADD a cat/subcat but only sys admins can edit/deac
