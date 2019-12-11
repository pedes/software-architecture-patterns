# software-architecture-patterns

In this repo, there's an example on how to implement a fault tolerant architecture, leveraging the Microprofile specification about
fault-tolerance.

## Overview 

Fault Tolerance provides developers with the following strategies for dealing with failure:

* Timeout: Define a maximum duration for execution
* Retry: Attempt execution again if it fails
* Bulkhead: Limit concurrent execution so that failures in that area can't overload the whole system
* CircuitBreaker: Automatically fail fast when execution repeatedly fails
* Fallback: Provide an alternative solution when execution fails


## How to Run
Use maven:
```mvn compile quarkus:dev```

By default application will run on port 8080.

Get all the coffee's data, and check the @Retry operation, try lowering the treshold from 4 to 2/1.
```GET http://localhost:8080/coffee``` 

Will use the @Fallback to get the recommendations from a different method.
```GET http://localhost:8080/coffee/2/recommendations```


References:
* https://quarkus.io/
* https://github.com/eclipse/microprofile-fault-tolerance
* https://quarkus.io/guides/microprofile-fault-tolerance
* https://www.tomitribe.com/blog/tomee-a-tutorial-on-microprofile-fault-tolerance/
* https://www.tomitribe.com/blog/microprofile-fault-tolerance-take-2/
* https://www.tomitribe.com/blog/microprofile-fault-tolerance-annotations/
* https://www.oreilly.com/library/view/patterns-for-fault/9780470319796/ch04.html