ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

val zioVersion = "2.0.13"
val zioDependencies = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-logging" % "2.1.13",
)
val zioTestDependencies = Seq(
  "dev.zio" %% "zio-test"          % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
  "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
)


lazy val zioDockerRunner = RootProject(file("../zioDockerRunner"))

lazy val jvmToJsApi = (project in file("jvmToJsApi"))
  .settings(
    scalaVersion := "3.2.2",
    name := "jvmToJsApi",
  )

val dbModel = project in file("dbModel")

lazy val root = (project in file("zioRpcServer"))
  .settings(
    scalaVersion := "3.2.2",
    name := "zioRpcServer",
    libraryDependencies ++= zioDependencies,
    libraryDependencies ++= zioTestDependencies,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  ).dependsOn(jvmToJsApi, zioDockerRunner)

