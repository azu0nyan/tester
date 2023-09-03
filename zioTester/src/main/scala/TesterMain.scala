import main.{AppBootstrap, HttpServer}
import tester.srv.controller.{Application, DataPackOps}
import tester.srv.controller.impl.ApplicationImpl
import zio.*

object TesterMain extends ZIOAppDefault {

  override def run = (for {
    appI <- ZIO.service[Application]
    _ <- appI.loadProblemTemplatesFromDb
    _ <- appI.loadCourseTemplatesFromDb
    _ <- registetPacks(appI.asInstanceOf[ApplicationImpl])
    _ <- HttpServer.startServer
  } yield ()).provideLayer(AppBootstrap.layer)

  def registetPacks(app: ApplicationImpl) =
    for {
      _ <- DataPackOps.registerInApp(app)(
        courses.javaCourse.data,
        courses.datastructures.data,
        courses.algos.data,
        courses.graphics3d.data,
        courses.simpleProblems.data,
        courses.unorderedProblems.data,
        myCourses.g6_20_21.data,
        myCourses.g7_20_21.data,
        myCourses.g7i_20_21.data,
        myCourses.g8_20_21.data,
        myCourses.g8i_20_21.data,
        myCourses.g9_20_21.data,
        myCourses.g9i_20_21.data,
        myCourses.g11_20_21.data,
        Projects.data,
      )
    } yield ()


}
