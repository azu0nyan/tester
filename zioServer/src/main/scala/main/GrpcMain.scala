package main
import scalapb.zio_grpc.{ServerMain, ServiceList}

object GrpcMain extends ServerMain {
  //todo add PostgresJsbcLayer
  def services = ServiceList.add(UserService)

  // Default port is 9000
  override def port: Int = 8980
}
