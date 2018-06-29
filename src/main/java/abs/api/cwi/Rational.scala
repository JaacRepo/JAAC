package abs.api.cwi
class Rational(n: Int, d: Int) extends Ordered[Rational] {
  require( d != 0 )

  private val g = Rational.gcd(n.abs, d.abs)
  val numer: Int = n/g * d.signum
  val denom: Int = d.abs/g

  def this(n: Int) = this(n, 1)
  def this(rational: Rational)= this(rational.numer,rational.denom);

  override def toString = numer + (if (denom == 1) "" else ("/"+denom))

  def getNumer() = numer
  def getDenom() = denom

  // default methods
  def +(that: Rational): Rational = new Rational( numer * that.denom + that.numer * denom, denom * that.denom )
  def +(that: Int): Rational = new Rational( numer + (that * denom), denom )
  def -(that: Rational): Rational = this + (-that)
  def -(that: Int): Rational = this + (-that)

  def unary_- = new Rational( -numer, denom )
  def abs = new Rational( numer.abs, denom )
  def signum = new Rational( numer.signum )
  def toFloat = (numer.toFloat/denom.toFloat)
  def toInt = (numer/denom)
  def *(that: Rational): Rational = new Rational( this.numer * that.numer, this.denom * that.denom )
  def *(that: Int): Rational = new Rational( this.numer * that, this.denom )

  def /(that: Rational): Rational = this * that.inverse
  def /(that: Int): Rational = this * new Rational(1,that)

  def inverse = new Rational( denom, numer )

  def compare(that: Rational) = this.numer * that.denom - that.numer * this.denom
  def compare(that: Int) = this.numer - that * this.denom

  def equals(that: Rational) = this.numer == that.numer && this.denom == that.denom
  def equals(that: Int) = this.numer == that && this.denom == 1

}

object Rational {
  implicit def intToRational(x: Int) = new Rational(x)
  private def gcd(a: Int, b: Int) : Int = if (b == 0) a else gcd(b, a % b)

  def apply(numer: Int, denom: Int) = new Rational(numer, denom)
  def apply(numer: Int) = new Rational(numer)
}
