
val scalaVer = "3.2.0"

val zioVersion = "2.0.13"
val basicZioDependencies = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-logging" % "2.1.13",
)

val quillDependencies = Seq(
  "io.getquill" %% "quill-jdbc-zio" % "4.6.0",
  "org.postgresql" % "postgresql" % "42.3.1"
)

val zioTestDependencies = Seq(
  "dev.zio" %% "zio-test"          % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
  "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
)


lazy val zioDockerRunner = RootProject(file("zioDockerRunner"))

lazy val jvmToJsApi = (project in file("jvmToJsApi"))
  .settings(
    scalaVersion := scalaVer,
    name := "jvmToJsApi",
  )

val dbCodeGen = project in file("dbCodeGen")

lazy val dbGenerated = (project in file("dbGenerated"))
  .settings(
    scalaVersion := scalaVer,
    name := "dbGenerated",
  )

val legacyOts = ProjectRef(file("legacy_ots"), "fooJVM")
val dbFromMongoMigration = (project in file("dbFromMongoMigration"))
  .settings(
    scalaVersion := "2.13.10",
    name := "dbFromMongoMigration",
    libraryDependencies ++= quillDependencies,
    scalacOptions += "-Ytasty-reader"
//    scalacOptions += "-Xignore-scala2-macros"
  ).dependsOn(legacyOts, dbGenerated)

val zioServer = (project in file("zioServer"))
  .settings(
    scalaVersion := scalaVer,
    name := "zioServer",
    libraryDependencies ++= basicZioDependencies,
    libraryDependencies ++= zioTestDependencies,
    libraryDependencies ++= quillDependencies,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  ).dependsOn(jvmToJsApi, zioDockerRunner, dbGenerated)

