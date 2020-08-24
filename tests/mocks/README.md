### What is this directory?
The "mocks" directory is a storage container for mocks, fakes, stubs, and similar files and objects used for unittesting.

The pickles `paid`, `unpaid`, `balancedue`, and `none` are BeautifulSoup instances instantiated by data scraped from [Allegheny County's website](http://www2.alleghenycounty.us/RealEstate/).

`real_estate_portal.html` and `record.json` are a pair. 
Both files have been (poorly) mocked using utils.anonymize_html_and_record in order to ensure the parcel id / owner does not collide with anything in the database.

#### A note to maintainers
From the [Pickle documentation](https://docs.python.org/3/library/pickle.html): *It is possible to construct malicious pickle data which will execute arbitrary code during unpickling. Never unpickle data that could have come from an untrusted source, or that could have been tampered with.*
