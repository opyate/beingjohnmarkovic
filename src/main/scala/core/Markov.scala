package core

object Markov {

  /*  
   * Given n, and a list of words of size n+m for m > 0, produce a list of tuples of the form (List(0, 1, ... , n-1), n).
   *
   * Example: groups(3, List("foo", "bar", "baz", "quux", "garply"))
   * Yields:  List(
   *            (List("foo", "bar"), "baz"),
   *            (List("bar", "baz"), "quux"),
   *            (List("baz", "quux"), "garply"))
   */
  def groups[T](n: Int, words: Array[T]) = { 
    val leaders = for (i <- List.range(0, words.length - n + 1)) yield words.slice(i, i + n - 1)
    val completions = words drop (n - 1)
    leaders zip completions
  }

}