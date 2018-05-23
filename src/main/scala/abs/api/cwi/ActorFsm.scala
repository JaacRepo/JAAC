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
}
