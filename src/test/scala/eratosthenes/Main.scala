package eratosthenes

object Main {

  protected var N: Long = 700000
  protected var M: Int = 2000
  protected var debug = false

  def main(args: Array[String]): Unit = {
    val np  = new NumberProducer(N)
    val pf = new PrimeFilter(1,2,M)
    np.filterActor(pf)

  }
}
