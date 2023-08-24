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
//  "dev.zio" %% "zio-logging-slf4j2-bridge" % "2.1.14"

)
val zioSchema = Seq(
  "dev.zio" %% "zio-schema" % "0.4.13",
  "dev.zio" %% "zio-schema-derivation" % "0.4.13"
)

val zioTestDependencies = Seq(
  "dev.zio" %% "zio-test"          % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
  "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
)

val doobieDependencies = Seq(
  "org.postgresql" % "postgresql" % "42.3.1",
  "io.github.gaelrenoux" %% "tranzactio" % "4.2.0",
  "org.tpolecat" %% "doobie-core"      % "1.0.0-RC2",
  "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC2",

)
val jwtDependencies = Seq(
  "com.github.jwt-scala" %% "jwt-core" % "9.4.3"
)

val embeddedPG = Seq(
  "io.zonky.test"  % "embedded-postgres" %  "2.0.4" % Test,
  "ch.qos.logback" % "logback-classic" % "1.4.11" % Test
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


lazy val jvmToJsApi = RootProject(file("jvmToJsApi"))
lazy val legacyExtensionBridge = ProjectRef(file("legacy_extensions_bridge"), "fooJVM")



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
  .dependsOn(jvmToJsApi, /*zioDockerRunner,*/ protos.jvm, legacyExtensionBridge)
  .settings(
    version := "0.0.2",
    scalaVersion := scalaVer,
    name := "zioServer",
    fork := true,
    libraryDependencies ++= basicZioDependencies,
    libraryDependencies ++= zioSchema,
    libraryDependencies ++= zioTestDependencies,
    libraryDependencies ++= embeddedPG,
    libraryDependencies ++= doobieDependencies,
    libraryDependencies ++= grpcJvmDependencies,
    libraryDependencies ++= jwtDependencies,
    resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
//    envVars in Test ++= Map("LC_ALL" -> "en_US.UTF-8" , "LC_CTYPE" -> "en_US.UTF-8")
    //LC_ALL=en_US.UTF-8;LC_CTYPE=en_US.UTF-8
  )
