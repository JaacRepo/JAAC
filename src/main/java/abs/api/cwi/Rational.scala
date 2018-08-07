package abs.api.cwi

import java.util.Objects

class Rational(n: BigInt, d: BigInt) extends Ordered[Rational] {
  require( d != 0 )

  private val g = Rational.gcd(n.abs, d.abs)
  val numer: BigInt = n/g * d.signum
  val denom: BigInt = d.abs/g

  def this(n: Int) = this(n, 1)
  def this(rational: Rational)= this(rational.numer,rational.denom);

  override def toString ="" + numer + (if (denom == 1) "" else ("/"+denom))

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
  def toInt = (numer/denom).toInt
  def *(that: Rational): Rational = new Rational( this.numer * that.numer, this.denom * that.denom )
  def *(that: Int): Rational = new Rational( this.numer * that, this.denom )

  def /(that: Rational): Rational = this * that.inverse
  def /(that: Int): Rational = this * new Rational(1,that)

  def inverse = new Rational( denom, numer )

//  def compare(that: Rational) = this.numer * that.denom - that.numer * this.denom
//  def compare(that: Int) = this.numer - that * this.denom

  def equals(that: Rational) = {
    (this.numer==0 && that.numer==0)|| (this.numer == that.numer && this.denom == that.denom)
  }

  def equals(that: Int) = {
    (this.numer == that && this.denom == 1) || (this.numer == 0 && that == 0)
  }

  override def equals(obj: scala.Any): Boolean = {
    if(obj.isInstanceOf[Int]){
      return equals(obj.asInstanceOf[Int])
    }
    if(obj.isInstanceOf[Rational]){
      return equals(obj.asInstanceOf[Rational])
    }
    return super.equals(obj)
  }

  override def compare(that: Rational): Int = {
    val c = this.numer*that.denom-that.numer*this.denom
    if(c.isValidInt)
      c.toInt
    else if (c<0)
      Int.MinValue
    else
      Int.MaxValue
  }
}

object Rational {
  implicit def intToRational(x: Int) = new Rational(x)
  private def gcd(a: BigInt, b: BigInt) : BigInt = if (b == 0) a else gcd(b, a % b)

  def apply(numer: Int, denom: Int) = new Rational(numer, denom)
  def apply(numer: Int) = new Rational(numer)

  def main(args: Array[String]): Unit = {
    val r = new Rational(0);
    val t = new Rational(0,1);
    val z = new Rational(0,300);
    val u = new Rational(0,1);
    println(Objects.equals(r,0));
    println(Objects.equals(z,t));
    println(Objects.equals(z,u));
    println(Objects.equals(u,t));
    println(r.equals(0));
    println(z.equals(t));
    println(t.equals(r));
    println(z.equals(r));


  }
}
