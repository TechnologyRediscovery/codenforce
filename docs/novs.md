# Subsystem briefing: Notices of violation (NOVs)
A notice of violation is a letter sent to a property owner and/or tenants notifying them of one or more code violations that have been assigned to a code enforcement case attached to their property. Issuance of this letter opens the compliance timeframes of each violation attached to the case.

## Notice of violation (NOV) life cycle
Notices and events associated with their processing lie at the heart of many case management decisions. As such, their creation, processing, and monitoring are all directed by COGConnect through a set of user controls whose availability is adjusted dynamically based on the current status of the `NoticeOfViolation` and its hosting `CECase`. 

The typical workflow for NOVs proceeds as follows:

11. Code Enforcement Officer (CEO) initiates the creation of a new NOV by selecting a set of `CodeViolations` (which do not have a compliance date) to declare in the letter.
11. CEO chooses the `Person` to which she wishes to send the NOV
11. CEO chooses template text blocks to create the body of the notice text (before and after the violation list). CEO also configures violation printing options (such as whether to include full text, human friendly text, and any photos of violation)
11. When the NOV is in ship shape, CEO then locks and queues the NOV, which triggers the following system actions:

    * Shift the case's phase to `CasePhase.NoticeDelivery`
    * Create a ghost person out of the selected recipient, and address the NOV to the ghost, meaning the contact     info for that particular Person at that particular time becomes read only.    
    * Declare the notice text and associated violations read only (and disable     the NOV edit button)    
    * Allow printing of the NOV (i.e. loading the in a plain HTML page minus nav bars)    
    * Attach an `Event` documenting the locking and queuing of the NOV
    * On the lock and queue event, request an event category for printing and mailing notice

11. Any user with MuniStaff+ permissions for the NOV's muni actually prints and mails the NOV and indicates mailing complete by clicking the appropriate command button in the NOV's listing on Cases. This triggers the following system actions:

    * Shift the case's phase to `CasePhase.InitialComplianceTimeframe`
    * Attach an `Event` documenting the mailing of the notice

11. For NOVs which are successfully delivered to the addressee, the NOV life cycle ends here. IF a NOV is returned, any user with MuniStaff+ permission for the NOV's muni can declare the notice as returned. This triggers the following system actions:

    * Attach an `Event` documenting the returned notice
    * On the returned notice event, request an event category for reviewing notice recipients address
    * Attach a note to the recipient ghost `Person`'s reference `Person` object, noting an undeliverable address