### What is this directory?
The "pickles" directory is just a storage container for .pickle objects used for mocking objects.
The mock objects are BeautifulSoup instances instantiated by data scraped from [Allegheny County's website](http://www2.alleghenycounty.us/RealEstate/).

#### A note to maintainers
From the [Pickle documentation](https://docs.python.org/3/library/pickle.html): *It is possible to construct malicious pickle data which will execute arbitrary code during unpickling. Never unpickle data that could have come from an untrusted source, or that could have been tampered with.*
