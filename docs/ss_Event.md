# Subsystem V Events

&lt; [home](index.md)

## Definitions
An event represents a happening of any kind pertaining to a number of business objects including:

* Code Enforcement Case `CECase`
* Occupancy Periods `OccPeriod`
* Property (Events on properties are stored in special code enforcement cases called property info cases)

## Components
Events are stored in the `event` table in the DB and the `EventCnF` object in Java Land. They are composite objects and contain a number of children, the most important is their `EventCategory` child, which itself is categorized using the `EventType` enum.

## Processing
The `EventCorodinator` is responsible for routing all event-related actions, including creating and editing events. As a new event is processed the `EventCoordinator` will give other applicable coordinators the chance to examine the event being created and tweak its fields or values, add an additional event to the list of events to be committed. 

