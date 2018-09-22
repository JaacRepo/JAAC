#JAAC

Java implementation of Actors with coroutines. Extended to Scala (ASCOOP) for typechecking method calls.

# Pitfall

Make sure that methods in actors are all using `messageHandler` (or if it is a FSM `stateHandler`).
This means also that there should be no static `main` method in an actor.


# Futures

## Akka comparison
There is no need for an execution context because the execution context is always the containing actor.

Functional operators like `map` and `flatMap` take functions, which means these functions 
do not return a future. On the contrary the continuations passed to `onSuccess` need to return a future.