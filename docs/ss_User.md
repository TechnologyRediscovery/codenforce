# Subsystem I Users, Authorization, and Credentials

&lt;- [home](index.md)

## Components
User
UserAuthorized
AuthPeriod
GoverningAuthPeriod
Credential


## Design principles (rought, subsystem-wide)
* When possible pass around Credential objects as the authorization vehicle, not a whole UserAuthorized. This is the most basic required object for determining who is requesting something, in what muni, and with what permissions. If additional info is needed, such as the Person name of the User requesting something, ask the coordinator for the relevant BOb.


