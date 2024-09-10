package utils

import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*

object SQL {
  val fs = Fragments

  def whereAndOpt(frs: Option[Fragment]*): Fragment =
    whereAnd(frs.flatten: _ *)

  def whereAnd(frs: Fragment*): Fragment =
    frs match
      case frs if frs.isEmpty => fr"WHERE TRUE"
      case frs => fs.whereAnd(frs.head, frs.tail: _ *)

}
