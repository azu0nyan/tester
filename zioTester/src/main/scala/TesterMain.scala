import main.AppBootstrap.BootstrapedApp
import main.{AppBootstrap, HttpServer}
import tester.srv.controller.{Application, DataPackOps, PrivateApp}
import tester.srv.controller.impl.ApplicationImpl
import zio.*
import zio.logging.{ConsoleLoggerConfig, LogFilter, LogFormat}
import zioDockerRunner.testRunner.ConcurrentRunner
import zioDockerRunner.testRunner.ConcurrentRunner.ConcurrentRunnerConfig

import java.time.format.DateTimeFormatter


object TesterMain extends ZIOAppDefault {

  import zio.logging.consoleLogger
  import zio.logging.LogFormat._


  val logFormat = {
    val | = text("|")
    timestamp(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")).fixed(12) + | +
      level.fixed(3).highlight + | + fiberId.fixed(15) + | + text(" ") + line + | + cause.highlight
  }
  val logger = consoleLogger(ConsoleLoggerConfig(logFormat, LogFilter.logLevel(LogLevel.Trace)))
  override val bootstrap = Runtime.removeDefaultLoggers >>> logger


  val conf = ConcurrentRunnerConfig(fibersMax = 6, containerName = "cont:0.1")
  val runner = ConcurrentRunner.layer(Seq(conf), 16)



  def checkLogs = for {
    _ <- ZIO.logTrace("Checking log level TRACE")
    _ <- ZIO.logDebug("Checking log level DEBUG")
    _ <- ZIO.logInfo("Checking log level INFO")
    _ <- ZIO.logWarning("Checking log level WARNING")
    _ <- ZIO.logError("Checking log level ERROR")
    _ <- ZIO.logFatal("Checking log level FATAL")
  } yield ()



  override def run = (for {
    _ <- checkLogs
    cr <- ZIO.service[ConcurrentRunner]
    sr <- ZIO.service[BootstrapedApp]
    (pApp, secApp) = sr
    _ <- pApp.initCaches
    _ <- pApp.loadProblemTemplatesFromDb
    _ <- pApp.loadCourseTemplatesFromDb
    _ <- registetPacks(pApp.asInstanceOf[ApplicationImpl], cr)
    _ <- HttpServer.startServer(8080).provideSomeLayer(ZLayer.succeed(secApp))
  } yield ())
    .provideSomeLayer(AppBootstrap.layer)
    .provideSomeLayer(runner)

  def registetPacks(app: ApplicationImpl, cr: ConcurrentRunner) =
    for {
      _ <- DataPackOps.registerInApp(app, cr)(
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
