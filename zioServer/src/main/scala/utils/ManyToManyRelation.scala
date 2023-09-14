package utils

import zio.*
import zio.concurrent.ConcurrentMap

//todo redo it on STM
case class ManyToManyRelation[X, Y](
                                     xToY: ConcurrentMap[X, Set[Y]],
                                     yToX: ConcurrentMap[Y, Set[X]],
                                   ) {
  def addXtoY(x: X, y: Y): UIO[Unit] = for {
    _ <- xToY.compute(x, {
      case (_, Some(setY)) => Some(setY + y)
      case (_, None) => Some(Set(y))
    })
    _ <- yToX.compute(y, {
      case (_, Some(setX)) => Some(setX + x)
      case (_, None) => Some(Set(x))
    })
  } yield ()

  def addManyXtoY(xs: Set[X], y: Y): UIO[Unit] = for {
    _ <- ZIO.foreach(xs)(x => xToY.compute(x, {
      case (_, Some(setY)) => Some(setY + y)
      case (_, None) => Some(Set(y))
    }))
    _ <- yToX.compute(y, {
      case (_, Some(setX)) => Some(setX | xs)
      case (_, None) => Some(xs)
    })
  } yield ()

  def addXtoManyY(x: X, ys: Set[Y]): UIO[Unit] = for {
    _ <- xToY.compute(x, {
      case (_, Some(setY)) => Some(setY | ys)
      case (_, None) => Some(ys)
    })
    _ <- ZIO.foreach(ys)(y => yToX.compute(y, {
      case (_, Some(setX)) => Some(setX + x)
      case (_, None) => Some(Set(x))
    }))
  } yield ()

  def addManyXToManyY(xs: Set[X], ys: Set[Y]) = for {
    _ <- ZIO.foreach(xs)(x => xToY.compute(x, {
      case (_, Some(setY)) => Some(setY + y)
      case (_, None) => Some(Set(y))
    }))
    _ <- ZIO.foreach(ys)(y => yToX.compute(y, {
      case (_, Some(setX)) => Some(setX + x)
      case (_, None) => Some(Set(x))
    }))
  } yield ()

  def getX(x: X): UIO[Set[Y]] = xToY.get(x).map(_.getOrElse(Set()))

  def getY(y: Y): UIO[Set[x]] = yToX.get(y).map(_.getOrElse(Set()))

  def getXs: UIO[List[X]] = xToY.toList.map(_._1)

  def getYs: UIO[List[Y]] = yToX.toList.map(_._1)
}


object ManyToManyRelation {
  def live[X, Y]: UIO[ManyToManyRelation[X, Y]] =
    for {
      xy <- ConcurrentMap.make[X, Y]()
      yx <- ConcurrentMap.make[Y, X]()
    } yield ManyToManyRelation(xy, yx)


  def layer[X, Y]: ULayer[ManyToManyRelation[X, Y]] = ZLayer.fromZIO(live)
}
