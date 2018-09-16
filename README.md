#JAAC

Java implementation of Actors with coroutines. Extended to Scala (ASCOOP) for typechecking method calls.

# Futures

## Akka comparison
There is no need for an execution context because the execution context is always the containing actor.

Functional operators like `map` and `flatMap` take functions, which means these functions 
do not return a future. On the contrary the continuations passed to `onSuccess` need to return a future.