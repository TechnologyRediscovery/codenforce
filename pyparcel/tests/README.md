# PyParcel tests
## Style
### Test Classes
Some test classes may only contain a single test class.

This is intentional:
  It allows for more test classes to be added under the umbrella of an outer class without refactoring

### Mocks and Fixtures:
The term "mock" is used as a shorthand for similar testing constructs, including mocks, fakes, spies, etc.

Mocks and Patches that are not reused are created at the function level so their returned values are clear.

On the other hand, TaxStatuses are generally created as fixtures. Their name gives enough understanding of their value.
This is not a hard and fast rule. 