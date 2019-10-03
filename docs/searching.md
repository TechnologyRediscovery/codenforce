# Search utilities
The core idea of searching the database: every listing of database objects (CECase, Person, Property, etc.) is built using a SQL query that's assembled based on the configuration of a passed in `SearchParams` subclass, such as `SearchParamsCEActionRequests`.

`SearchParams` objects store various search paramater values that are injected into `SELECT` statments and switches that turn each one on and off.

To use the search utility:
1. Call the appropriate factory method on the `SearchCoordinator` for the default `SearchParams` subclass you want, such as `getDefaultSearchParamsCEActionRequests()`. This will usually happen by a backing bean or perhaps even the domain coordinator.
2. Customize the default configuration of the `SearchParams` object you get back.
3. Usually this will be used on page load to build a starting list for display
3. Wire the backing bean properties to the various parameters on your search object so the user can tinker with them
4. Use the `SearchParams` object to shuttle revisions on the initial query to the integrator.