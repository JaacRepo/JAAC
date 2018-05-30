package eratosthenes

object Main {

  protected var N: Long = 100000
  protected var M: Int = 1000
  protected var debug = false

  def main(args: Array[String]): Unit = {
    val np  = new NumberProducer(N)
    val pf = new PrimeFilter(1,2,M)
    np.filterActor(pf)

  }
}
