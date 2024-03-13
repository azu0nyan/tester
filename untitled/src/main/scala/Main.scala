import math._
import scala.util._
import scala.io.StdIn._

/**
 * Score points by scanning valuable fish faster than your opponent.
 * */
case class V2(x: Int, y: Int) {
  def +(that: V2) = V2(x + that.x, y + that.y)
  def -(that: V2) = V2(x - that.x, y - that.y)
  def *(that: V2) = V2(x * that.x, y * that.y)
  def /(that: V2) = V2(x / that.x, y / that.y)
  def length: Int = sqrt(x * x + y * y).toInt
  def dist(that: V2) = (that - this).length
}


case class GameState(
                      tick: Int,
                      myScore: Int,
                      foeScore: Int,
                      myCreatures: Seq[Int],
                      foeCreatures: Seq[Int],
                      myDrones: Seq[Drone],
                      foeDrones: Seq[Drone],
                      visibleCreatures: Seq[Creature],
                      radarBips: Seq[RadarBip],
                      scans: Seq[DroneScan],
                    ) {
  val creatures = Player.creatureIds.take(Player.creatureCount).toSeq
  val creaturesOnMap = radarBips.map(_.creatureId).toSet
  val scansInDrone = scans.filter(d => myDrones.exists(_.id == d.droneId)).map(_.creatureId)
  val unscanned = (creaturesOnMap &~ (myCreatures.toSet | scansInDrone.toSet)) & radarBips.map(_.creatureId).toSet


  val creaturesMap = visibleCreatures.map(c => (c.id, c)).toMap

  def creature(id: Int): Creature =
   creaturesMap(id)

  def pointsIfScan(id: Int): Int =
    Player.TYPE_POINTS(Player.types(id))

  def bestUnscanned: Option[Int] = unscanned.maxByOption(pointsIfScan)

  def closestVisibleUnscanned(pos:V2): Option[Creature] =
    (unscanned & visibleCreatures.map(_.id).toSet)
      .filter(c => creature(c).pos.dist(pos) < Player.AUTO_SCAN_RADIUS)
      .minByOption[Int](c => creature(c).pos.dist(pos))
      .map(creature)
}

case class Creature(id: Int, pos: V2, vel: V2) {
  def color: Int = Player.colors(id)
  def type_ : Int = Player.types(id)
}

case class DroneScan(droneId: Int, creatureId: Int)

case class RadarBip(droneId: Int, creatureId: Int, dir: V2)

case class Drone(id: Int, pos: V2, emergency: Int, battery: Int)

object Player {
  var start = System.currentTimeMillis()

  val MAP_MAX = 10000
  val SPEED = 600
  val SINK_SPEED = 300
  val AUTO_SCAN_RADIUS = 800
  val SCAN_RADIUS = 2000
  val ALARM_RADIUS = 1400
  val ALERTED_SPEED = 400
  val SCAN_BATTERY_DRAIN = 5
  val BATTERY_CAPACITY = 30
  val BATTERY_RECHARGE_RATE = 1
  val TYPE_POINTS = IndexedSeq(1, 2, 3)
  val ALL_ONE_COLOR = 3
  val ALL_TYPES = 4
  val MAX_CREATURES = 16

  val colors = Array.ofDim[Int](64)
  val types = Array.ofDim[Int](64)
  var creatureCount: Int = _
  var creatureIds: Array[Int] = _
  var tick = 0
  val log = true

  def main(args: Array[String]): Unit = {
    creatureCount = readLine.toInt
    creatureIds = Array.ofDim[Int](creatureCount)
    for (i <- 0 until creatureCount) {
      val Array(creatureId, color, _type) = (readLine split " ").filter(_ != "").map(_.toInt)
      creatureIds(i) = creatureId
      colors(creatureId) = color
      types(creatureId) = _type
    }
    if (log) {
      val end = System.currentTimeMillis() - start
      Console.err.println(s"startup ms: $end")
    }
    tick += 1

    // game loop
    while (true) {
      start = System.currentTimeMillis()
      val myScore = readLine.toInt
      val foeScore = readLine.toInt

      val myScanCount = readLine.toInt
      val myCreatures =
        for (i <- 0 until myScanCount) yield
          readLine.toInt

      val foeScanCount = readLine.toInt
      val foeCreatures =
        for (i <- 0 until foeScanCount) yield
          readLine.toInt

      def readDrone(): Drone = {
        val Array(droneId, droneX, droneY, emergency, battery) = (readLine split " ").filter(_ != "").map(_.toInt)
        Drone(droneId, V2(droneX, droneY), emergency, battery)
      }

      val myDroneCount = readLine.toInt
      val myDrones = for (i <- 0 until myDroneCount) yield readDrone()

      val foeDroneCount = readLine.toInt
      val foeDrones = for (i <- 0 until foeDroneCount) yield readDrone()


      //Next line: droneScanCount for the amount of scans currently within a drone.
      //Next droneScanCount lines: droneId and creatureId describing which drone contains a scan of which fish.
      val droneScanCount = readLine.toInt
      val scans = for (i <- 0 until droneScanCount) yield {
        val Array(droneId, creatureId) = (readLine split " ").filter(_ != "").map(_.toInt)
        DroneScan(droneId, creatureId)
      }
      val visibleCreatureCount = readLine.toInt
      val visibleCreatures = for (i <- 0 until visibleCreatureCount) yield {
        val Array(creatureId, creatureX, creatureY, creatureVx, creatureVy) = (readLine split " ").filter(_ != "").map(_.toInt)

        Creature(creatureId, V2(creatureX, creatureY), V2(creatureVx, creatureVy))
      }

      def radarDir(radar: String): V2 =
        radar match {
          case "TL" => V2(-1, -1)
          case "TR" => V2(1, -1)
          case "BL" => V2(-1, 1)
          case "BR" => V2(1, 1)
        }

      val radarBlipCount = readLine.toInt
      val radarBips = for (i <- 0 until radarBlipCount) yield {
        val Array(_droneId, _creatureId, radar) = readLine split " "
        val droneId = _droneId.toInt
        val creatureId = _creatureId.toInt
        val dir = radarDir(radar)
        Console.err.println(RadarBip(droneId, creatureId, dir))
        RadarBip(droneId, creatureId, dir)
      }


      val state = GameState(
        tick,
        myScore,
        foeScore,
        myCreatures,
        foeCreatures,
        myDrones,
        foeDrones,
        visibleCreatures,
        radarBips,
        scans,
      )
      tick(state)
      if (log) {
        Console.err.println(s"creatures: ${state.creatures.mkString(", ")}")
        Console.err.println(s"unscanned: ${state.unscanned.mkString(", ")}")
        Console.err.println(s"my       : ${state.myCreatures.mkString(", ")}")
        val end = System.currentTimeMillis() - start
        Console.err.println(s"${tick} ms: $end")
      }
      tick += 1
    }
  }

  def tick(state: GameState): Unit = {
    for (drone <- state.myDrones) {
      println(think(drone, state))
    }
  }

  def think(d: Drone, state: GameState): String = {
    state.bestUnscanned match {
      case Some(unscannedId) =>
        if (log) Console.err.println(s"best unscanned: $unscannedId")
        state.closestVisibleUnscanned(d.pos) match {
          case Some(creature) =>
            s"MOVE ${creature.pos.x} ${creature.pos.y} 0"
          case None =>
            state.radarBips.find(b => b.droneId == d.id && b.creatureId == unscannedId) match {
              case Some(bip) =>
                val dest = d.pos + bip.dir * V2(SPEED, SPEED)
                s"MOVE ${dest.x} ${dest.y} 0"
              case None =>
                s"MOVE ${d.pos.x} ${d.pos.y - SPEED} 0"
            }
        }
      case None => s"MOVE ${d.pos.x} ${d.pos.y - SPEED} 0"
    }
  }
}
