package abs.api.cwi


trait ActorFsm extends TypedActor {
  type TState

  def initState: TState

  private var currentState: TState = initState

  def stateHandler[V](fn: TState => (TState, ABSFuture[V])): ABSFuture[V] = messageHandler {
      val (newState, output) = fn(currentState)
      currentState = newState
      output
  }

  def goto[V](next: TState): FsmHelper[V] = {
    new FsmHelper(next)
  }

  class FsmHelper[V] (val next: TState) {
    def andReturn(retVal: ABSFuture[V]): (TState, ABSFuture[V]) = {
      (next, retVal)
    }
  }
}
