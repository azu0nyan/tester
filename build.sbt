ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

ThisBuild / cancelable := true

ThisBuild / connectInput := true

val scalaVer = "3.3.0"
//val scalaVer = "2.13.10"


val zioVersion = "2.0.13"
val basicZioDependencies = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-logging" % "2.1.13",
)

val quillDependencies = Seq(
  "io.getquill" %% "quill-jdbc-zio" % "4.6.0.1",
  "org.postgresql" % "postgresql" % "42.3.1"
)

val zioTestDependencies = Seq(
  "dev.zio" %% "zio-test"          % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
  "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
)

val doobieDependencies = Seq(
  "io.github.gaelrenoux" %% "tranzactio" % "4.2.0",
  "org.tpolecat" %% "doobie-core"      % "1.0.0-RC4",
  "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC4",

)

val grpcVersion = "1.50.1"
val grpcJvmDependencies = Seq(
  "io.grpc" % "grpc-netty" % grpcVersion
)

val loginRegisterHashing = Seq(
  libraryDependencies += "com.pauldijou" %% "jwt-core" % "5.0.0",
  libraryDependencies += "com.outr" %% "hasher" % "1.2.2",
)


//lazy val zioDockerRunner = RootProject(file("zioDockerRunner"))
/*lazy val jvmToJsApi = (project in file("jvmToJsApi"))
  .settings(
    scalaVersion := scalaVer,
    name := "jvmToJsApi",
  )*/
//lazy val dbCodeGen = project in file("dbCodeGen")
/*lazy val dbGenerated = (project in file("dbGenerated"))
  .settings(
    scalaVersion := scalaVer,
    name := "dbGenerated",
  )*/
//lazy val legacyOts = ProjectRef(file("legacy_ots"), "fooJVM")

/*
lazy val dbFromMongoMigration = (project in file("dbFromMongoMigration"))
  .settings(
    scalaVersion := "2.13.10",
    name := "dbFromMongoMigration",
    libraryDependencies ++= quillDependencies,
    scalacOptions += "-Ytasty-reader"
//    scalacOptions += "-Xignore-scala2-macros"
  ).dependsOn(legacyOts, dbGenerated)
*/
/**Protobuff gRPC api */
lazy val protos = crossProject(JSPlatform, JVMPlatform)
  .in(file("protos"))
  .settings(
    scalaVersion := scalaVer,
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb",
      scalapb.zio_grpc.ZioCodeGenerator -> (Compile / sourceManaged).value / "scalapb"
    ),
    Compile / PB.protoSources := Seq(
      (ThisBuild / baseDirectory).value / "protos" / "src" / "main" / "protobuf"
    ),
    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )
  )

val zioServer = (project in file("zioServer"))
  .dependsOn(/**jvmToJsApi,*/ /*zioDockerRunner,*/ protos.jvm /**, dbGenerated*/)
  .settings(
    version := "0.0.1",
    scalaVersion := scalaVer,
    name := "zioServer",
    fork := true,
    libraryDependencies ++= basicZioDependencies,
    libraryDependencies ++= zioTestDependencies,
    libraryDependencies ++= quillDependencies,
    libraryDependencies ++= doobieDependencies,
    libraryDependencies ++= grpcJvmDependencies,
    resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
