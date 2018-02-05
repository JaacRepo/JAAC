package NQueens.common


object Functions {

  def copyBoard(board: Array[Int], depth: Int, i: Int) = {
    val b: Array[Int] = new Array[Int](depth + 1)
    System.arraycopy(board, 0, b, 0, depth)
    b(depth) = i
    b
  }

  def boardValid(a: Array[Int], n: Int): Boolean = {
    for (i <- 0 to n-1) {
      var p = a(i)
      for (j <- (i + 1) to n-1) {
        var q = a(j)
        if (q == p || q == p - (j - i) || q == p + (j - i)) {
          return false
        }
      }
    }
    true
  }

}
