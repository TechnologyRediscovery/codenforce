### What is this directory?
The "mocks" directory is just a storage container for .pickle objects used for mocking.
The mock objects are BeautifulSoup instances instantiated by data scraped from [Allegheny County's website](http://www2.alleghenycounty.us/RealEstate/).

#### A note to maintainers
From the [Pickle documentation](https://docs.python.org/3/library/pickle.html): *It is possible to construct malicious pickle data which will execute arbitrary code during unpickling. Never unpickle data that could have come from an untrusted source, or that could have been tampered with.*

#### Learn more about mocks
* [PyTest mocks in 2 minutes (video)](https://www.youtube.com/watch?v=ClAdw7ZJf5E&list=PLJsmaNFr5mNqSeuNepT3IaMrgzRMm9lQR&index=5)
