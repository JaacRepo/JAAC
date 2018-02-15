package NQueens.nqueenscoop

import abs.api.cwi._


object MainCoop extends LocalActor {

    def main(args: Array[String]): Unit = {
        // Init section: nqueens
        val numWorkers: Int = 4
        val priorities: Int = 14
        val size: Int = 14
        val threshold: Int = 5
        val solutions: List[Int] = List(1, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712, 365596, 2279184, 14772512, 95815104, 666090624)
        val solutionsLimit: Int = solutions(size - 1)

        val master = new Master(numWorkers, priorities, solutionsLimit, threshold, size)
        master.send(() => master.init)
    }
}
