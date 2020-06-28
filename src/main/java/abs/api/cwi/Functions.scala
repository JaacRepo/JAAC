package abs.api.cwi

import abs.api.realtime.TimedActorSystem

import scala.io.StdIn._
import scala.util.Random;

object Functions {

  val r = new Random(System.currentTimeMillis());
  val modelStart = System.currentTimeMillis();

  def random(below: Int): Int = {
    r.nextInt(below);
  }

  def float( a : Rational): Float= {
    a.toFloat
  }

  def rat( f : Float): Rational= {
    val d = 100000000
    val n = (f*d).toInt
    new Rational(n,d)
  }

  def floor( f : Float): Int= {
    math.floor(f).toInt;
  }

  def ceil( f : Float): Int= {
    math.ceil(f).toInt;
  }


  def sqrt( x : Float): Float= {
    math.sqrt(x).toFloat;
  }

  def log( x : Float): Float= {
    math.log(x).toFloat;
  }

  def exp( x : Float): Float= {
    math.exp(x).toFloat
  }

  def truncate(a: Rational): Int = {
    return a.toInt;
  }

  def numerator(a: Rational): Int = {
    a.numer.toInt
  }

  def denominator(a: Rational): Int = {
    a.denom.toInt
  }

  def substr( str : String,  start : Int,  length : Int): String= {
    str.substring(start, start+length);
  }

  def strlen( str : String): Int= {
    str.length
  }

  def readln(): String={
    readLine();
  }

  def  toString[A](actor :A):String ={
    if(actor==null)
      "null"
    else
      actor.toString()
  }

  //TODO: println, print

  def currentms(): Rational= {
    TimedActorSystem.now();
  }

  def lowlevelDeadline(): Rational = {
    return -1;
  }

  def ms_since_model_start(): Int= {
    return (System.currentTimeMillis()-modelStart).toInt
  }


}
