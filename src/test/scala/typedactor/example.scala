package typedactor

import abs.api.cwi._


class Example2 extends SugaredActor with TypedActor[Example2] {
  def doubler(a: Int) = messageHandler{ absMethod{
    a * 2
  }}
}

class Example3 extends SugaredActor with TypedActor[Example3] {
  def tripler(a: Int) = messageHandler{ absMethod{
    a * 3
  }}
}


class User extends SugaredActor with TypedActor[User] {
    def call() = messageHandler{ absVoidMethod {
      val a2 = new Example2
      val a3 = new Example3
      import a2._
      import a3._

      a2 ! doubler(2)
      a3 ! tripler(2)
//      a2 ! tripler(2)  // must fail to compile
    }
  }
}
