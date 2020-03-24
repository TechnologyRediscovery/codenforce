# Coding norms
A hodgepodge of notes made during development that probably should be sorted into domain-specific pages but live here for review by developers.

#### BOb Coordinators and the Search Coordinator
Coordinators should be asked for default Query objects, and that coordinator then sends the appropriate Enum of the QueryType to the initQuery variant in the SearchCoordinator who creates a Query object and populates it with appropriate SearchParam objects

#### Searching permissions
searchForXXX methods on Integrators should not require any user-level info for authentication purposes. User-specific search settings should be configured as needed down in the SearchParams objects. The runQuery method family requires a credential and a query, and will be responsible for checking permissions and logging all query runs

#### auditXXX methods
auditXXX methods are responsible for checking the state of objects after configuration or before key events, like insertions. Their return types should all be void and instead communicate problems by throwing a logically typed Exception subclass containing a useful error message that will eventually get passed all the way back to the user.

#### Coordinator versus Integrator method names
To differentiate between direct database operations that represent the base level function and Coordinator logic controls, the following norms of naming can help make roles clear
Coordinators createFoo &gt; Integrators updateFoo
Coordinators editFoo &gt; Integrators updateFoo
Coordinators deleteFoo &gt; Integrators inactivateFoo
Coordinators nukeFoo &gt; Integrators deleteFoo