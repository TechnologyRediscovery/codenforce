# Subsystem  XV Workflow

&lt;- [home](index.md)

## Overview
The workflow subsystem comprises tools for encoding, managing, and reporting customized sequences of tasks through which code enforcement cases and occupancy periods/inspections are ushered. The core objects of the workflow system are the `WorkflowCoordinator` and the backing bean `WorkflowConfigBB` for configuration of `EventRules` and `Proposals`.

## Mechanisms
Workflow management is facilitated by examination of Event objects attached to a code enforcement case or an occupancy period. The examination of existing `EventCnF` objects is directed by a `EventRuleAbstract` subclass which specifies a required or forbidden event attribute--either by the higher-level `EventType` or more specific `EventCategory`.

The addition or removal of `EventCnF` objects on its container code enforcement case or occupancy period is made easy for the user by `Proposal` objects which contain one or more `Choice` objects. One subclass of `Choice` can add or remove an `EventCnF` and different subclass can direct the addition or removal of an `EventRule` from its parent object. (The third type of `Choice` subclass is a mere page redirection String).






